package com.buzzlink.repository;

import com.buzzlink.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {

    /**
     * Find channel by name
     */
    Optional<Channel> findByName(String name);

    /**
     * Find all channels in a workspace
     */
    @Query("SELECT c FROM Channel c WHERE c.workspace.id = :workspaceId")
    List<Channel> findByWorkspaceId(@Param("workspaceId") Long workspaceId);

    /**
     * Find all channels by workspace entity
     */
    @Query("SELECT c FROM Channel c WHERE c.workspace = :workspace")
    List<Channel> findByWorkspace(@Param("workspace") com.buzzlink.entity.Workspace workspace);

    /**
     * Find channel by name within a workspace
     */
    @Query("SELECT c FROM Channel c WHERE c.name = :name AND c.workspace.id = :workspaceId")
    Optional<Channel> findByNameAndWorkspaceId(@Param("name") String name, @Param("workspaceId") Long workspaceId);
}
