package com.twogether.backend.notification.support;

import com.twogether.backend.notification.service.NotificationDispatchService;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 새 메시지 알림 디바운서(방·유저 단위 코얼레싱).
 *
 * 첫 메시지 수신 후 {@code WINDOW_SECONDS} 창 동안 들어온 같은 방·유저의 메시지를 묶어
 * 한 번만 발송한다(1건이면 미리보기, 여러 건이면 "새 메시지 N개").
 *
 * 인메모리 단일 인스턴스 기준. 다중 인스턴스로 확장 시 Redis 등으로 이전 필요(후속).
 */
@Component
public class MessageNotificationDebouncer {

    private static final Logger log = LoggerFactory.getLogger(MessageNotificationDebouncer.class);
    private static final long WINDOW_SECONDS = 5;

    private final NotificationDispatchService dispatchService;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public MessageNotificationDebouncer(NotificationDispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    public void record(
            Long roomId,
            Long recipientUserId,
            String preview
    ) {
        String key = roomId + ":" + recipientUserId;
        Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket(roomId, recipientUserId));
        bucket.lastPreview = preview;
        int count = bucket.count.incrementAndGet();
        if (count == 1) {
            scheduler.schedule(() -> flush(key), WINDOW_SECONDS, TimeUnit.SECONDS);
        }
    }

    private void flush(String key) {
        Bucket bucket = buckets.remove(key);
        if (bucket == null) {
            return;
        }
        int count = bucket.count.get();
        String text = count <= 1 ? bucket.lastPreview : "새 메시지 " + count + "개";
        try {
            dispatchService.notifyChatMessage(bucket.recipientUserId, bucket.roomId, text);
        } catch (Exception e) {
            log.warn("새 메시지 알림 발송 실패 room={} user={}: {}",
                    bucket.roomId, bucket.recipientUserId, e.getMessage());
        }
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }

    private static final class Bucket {
        private final Long roomId;
        private final Long recipientUserId;
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile String lastPreview;

        private Bucket(Long roomId, Long recipientUserId) {
            this.roomId = roomId;
            this.recipientUserId = recipientUserId;
        }
    }
}
