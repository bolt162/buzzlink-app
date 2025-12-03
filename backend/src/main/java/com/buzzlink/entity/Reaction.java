package com.buzzlink.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Reaction entity for emoji reactions on messages.
 * For simplicity, we support only thumbs-up (👍) reactions.
 */
@Entity
@Table(name = "reactions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"message_id", "user_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Message being reacted to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    /**
     * User who reacted
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * For this demo, we only support THUMBS_UP
     * Could extend to support multiple emoji types
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReactionType type = ReactionType.THUMBS_UP;

    public enum ReactionType {
        THUMBS_UP
    }
}
