package com.buzzlink.repository;

import com.buzzlink.entity.Message;
import com.buzzlink.entity.Reaction;
import com.buzzlink.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {

    /**
     * Find all reactions for a message
     */
    List<Reaction> findByMessage(Message message);

    /**
     * Find a specific user's reaction to a message
     */
    Optional<Reaction> findByMessageAndUser(Message message, User user);

    /**
     * Count reactions for a message (for thumbs-up counter)
     */
    long countByMessage(Message message);

    /**
     * Delete a user's reaction to a message (toggle off)
     */
    void deleteByMessageAndUser(Message message, User user);
}
