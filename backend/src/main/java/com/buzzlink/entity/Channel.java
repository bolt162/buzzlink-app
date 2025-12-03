package com.buzzlink.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Channel entity representing a chat channel.
 * Channels are like Slack channels - users can join and send messages.
 */
@Entity
@Table(name = "channels", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "workspace_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Channel name (e.g., "general", "random", "engineering")
     * Must be unique within a workspace
     */
    @Column(nullable = false)
    private String name;

    /**
     * Channel description (optional)
     */
    private String description;

    /**
     * Workspace this channel belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
