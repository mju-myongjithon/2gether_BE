package com.twogether.backend.chat.repository;

import com.twogether.backend.chat.domain.MessageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface MessageAttachmentRepository
        extends JpaRepository<MessageAttachment, Long> {

    /*
     * 단일 메시지의 첨부 목록 (정렬 순).
     */
    List<MessageAttachment> findAllByMessageIdOrderBySortOrderAsc(
            Long messageId
    );

    /*
     * 여러 메시지의 첨부를 한 번에 조회(이력 조회 N+1 방지). 메시지·정렬 순.
     */
    List<MessageAttachment> findAllByMessageIdInOrderByMessageIdAscSortOrderAsc(
            Collection<Long> messageIds
    );
}
