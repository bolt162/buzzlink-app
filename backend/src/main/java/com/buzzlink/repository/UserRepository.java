package com.buzzlink.repository;

import com.buzzlink.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by Clerk ID (used for authentication mapping)
     */
    Optional<User> findByClerkId(String clerkId);

    /**
     * Check if a user exists by Clerk ID
     */
    boolean existsByClerkId(String clerkId);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Search users by display name or email (case-insensitive)
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.displayName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<User> searchUsers(@Param("query") String query);
}
