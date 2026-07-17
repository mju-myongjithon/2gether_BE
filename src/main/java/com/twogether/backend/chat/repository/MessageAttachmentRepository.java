package com.twogether.backend.chat.repository;

import com.twogether.backend.chat.domain.MessageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageAttachmentRepository
        extends JpaRepository<MessageAttachment, Long> {

    /*
     * 메시지의 첨부 목록 (정렬 순).
     */
    List<MessageAttachment> findAllByMessageIdOrderBySortOrderAsc(
            Long messageId
    );
}
