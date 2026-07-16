package com.twogether.backend.tag.domain;

import com.twogether.backend.user.domain.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "user_tags",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_tag",
                        columnNames = {
                                "user_id",
                                "tag_id"
                        }
                )
        }
)
public class UserTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tag_id",
            nullable = false
    )
    private Tag tag;

    protected UserTag() {
    }

    public UserTag(
            User user,
            Tag tag
    ) {
        this.user = user;
        this.tag = tag;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Tag getTag() {
        return tag;
    }
}