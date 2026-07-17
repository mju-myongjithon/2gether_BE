package com.twogether.backend.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * 이미지/파일 첨부 메타. 다중 첨부·썸네일·용량 대응을 위해 message 와 분리.
 */
@Entity
@Table(
        name = "message_attachment",
        indexes = {
                @Index(name = "idx_message_attachment_message_id", columnList = "message_id")
        }
)
public class MessageAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "message_id",
            nullable = false
    )
    private Message message;

    @Column(
            name = "file_url",
            nullable = false,
            length = 300
    )
    private String fileUrl;

    @Column(
            name = "thumbnail_url",
            length = 300
    )
    private String thumbnailUrl;

    @Column(
            name = "content_type",
            length = 100
    )
    private String contentType;

    @Column(name = "size")
    private Integer size;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(
            name = "sort_order",
            nullable = false
    )
    private short sortOrder;

    protected MessageAttachment() {
    }

    public MessageAttachment(
            Message message,
            String fileUrl,
            String thumbnailUrl,
            String contentType,
            Integer size,
            Integer width,
            Integer height,
            short sortOrder
    ) {
        this.message = message;
        this.fileUrl = fileUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.contentType = contentType;
        this.size = size;
        this.width = width;
        this.height = height;
        this.sortOrder = sortOrder;
    }

    public Long getId() {
        return id;
    }

    public Message getMessage() {
        return message;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getContentType() {
        return contentType;
    }

    public Integer getSize() {
        return size;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public short getSortOrder() {
        return sortOrder;
    }
}
