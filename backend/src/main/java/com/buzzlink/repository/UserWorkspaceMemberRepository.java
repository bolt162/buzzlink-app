package com.buzzlink.repository;

import com.buzzlink.entity.UserWorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserWorkspaceMemberRepository extends JpaRepository<UserWorkspaceMember, Long> {

    @Query("SELECT uwm FROM UserWorkspaceMember uwm WHERE uwm.user.id = :userId")
    List<UserWorkspaceMember> findByUserId(@Param("userId") Long userId);

    @Query("SELECT uwm FROM UserWorkspaceMember uwm WHERE uwm.workspace.id = :workspaceId")
    List<UserWorkspaceMember> findByWorkspaceId(@Param("workspaceId") Long workspaceId);

    @Query("SELECT uwm FROM UserWorkspaceMember uwm WHERE uwm.user.id = :userId AND uwm.workspace.id = :workspaceId")
    Optional<UserWorkspaceMember> findByUserIdAndWorkspaceId(@Param("userId") Long userId, @Param("workspaceId") Long workspaceId);

    @Query("SELECT uwm FROM UserWorkspaceMember uwm WHERE uwm.user.clerkId = :clerkId")
    List<UserWorkspaceMember> findByUserClerkId(@Param("clerkId") String clerkId);
}
