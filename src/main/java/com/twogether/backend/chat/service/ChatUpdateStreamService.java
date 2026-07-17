package com.twogether.backend.chat.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 채팅방 목록 실시간 갱신용 SSE 연결 관리.
 *
 * 사용자별 SseEmitter 를 보관하고, 새 메시지 이벤트가 오면 방 참여자에게
 * room-update 이벤트를 push 한다. 방 하나만 구독하는 STOMP 와 달리
 * 열어두지 않은 방의 새 메시지도 목록에 즉시 반영할 수 있다.
 *
 * 단일 인스턴스 in-memory 레지스트리(브로커 없음). 연결이 끊기면
 * EventSource 가 자동 재연결하므로 유실 구간은 REST 폴링이 보정한다.
 */
@Service
public class ChatUpdateStreamService {

    private static final Logger log = LoggerFactory.getLogger(ChatUpdateStreamService.class);

    /** 프록시/서블릿 비동기 타임아웃보다 길게 잡고, 유휴 연결은 하트비트로 유지한다. */
    private static final long EMITTER_TIMEOUT_MS = 30L * 60 * 1000;

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emittersByUser =
            new ConcurrentHashMap<>();

    /**
     * 사용자의 SSE 구독을 등록한다. 같은 사용자가 여러 탭에서 구독해도 각각 유지된다.
     */
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);

        emittersByUser
                .computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>())
                .add(emitter);

        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onTimeout(() -> remove(userId, emitter));
        emitter.onError(e -> remove(userId, emitter));

        // 연결 확인용 초기 이벤트(프록시 버퍼링 방지 겸용)
        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException | IllegalStateException e) {
            remove(userId, emitter);
        }

        return emitter;
    }

    /**
     * 특정 사용자에게 room-update 이벤트를 발송한다. 끊긴 연결은 발송 시점에 정리한다.
     */
    public void sendRoomUpdate(Long userId, Object payload) {
        List<SseEmitter> emitters = emittersByUser.get(userId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("room-update")
                        .data(payload, MediaType.APPLICATION_JSON));
            } catch (IOException | IllegalStateException e) {
                log.debug("SSE room-update 발송 실패(연결 종료로 간주): userId={}", userId);
                remove(userId, emitter);
            }
        }
    }

    /**
     * 유휴 연결 유지를 위한 하트비트. 중간 프록시의 idle timeout 으로 인한 끊김을 줄인다.
     */
    @Scheduled(fixedRate = 25_000)
    public void heartbeat() {
        emittersByUser.forEach((userId, emitters) -> {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().comment("ping"));
                } catch (IOException | IllegalStateException e) {
                    remove(userId, emitter);
                }
            }
        });
    }

    private void remove(Long userId, SseEmitter emitter) {
        List<SseEmitter> emitters = emittersByUser.get(userId);
        if (emitters == null) {
            return;
        }
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emittersByUser.remove(userId, emitters);
        }
    }
}
