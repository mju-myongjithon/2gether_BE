package com.twogether.backend.gatheringcomment.domain;

import com.twogether.backend.gathering.domain.Gathering;
import com.twogether.backend.user.domain.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "gathering_comments")
public class GatheringComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id", nullable = false)
    private Gathering gathering;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private GatheringComment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GatheringComment> children = new ArrayList<>();

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @Column(name = "is_secret", nullable = false)
    private boolean isSecret;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected GatheringComment() {
    }

    public GatheringComment(
            Gathering gathering,
            User user,
            GatheringComment parent,
            String content,
            boolean isSecret
    ) {
        this.gathering = gathering;
        this.user = user;
        this.parent = parent;
        this.content = content;
        this.isSecret = isSecret;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Gathering getGathering() {
        return gathering;
    }

    public User getUser() {
        return user;
    }

    public GatheringComment getParent() {
        return parent;
    }

    public List<GatheringComment> getChildren() {
        return children;
    }

    public String getContent() {
        return content;
    }

    public boolean isSecret() {
        return isSecret;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
