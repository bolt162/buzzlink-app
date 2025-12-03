package com.buzzlink;

import com.buzzlink.entity.Channel;
import com.buzzlink.entity.User;
import com.buzzlink.entity.Workspace;
import com.buzzlink.entity.UserWorkspaceMember;
import com.buzzlink.repository.ChannelRepository;
import com.buzzlink.repository.UserRepository;
import com.buzzlink.repository.WorkspaceRepository;
import com.buzzlink.repository.UserWorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Main Spring Boot application class for BuzzLink
 */
@SpringBootApplication
@RequiredArgsConstructor
public class BuzzLinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(BuzzLinkApplication.class, args);
    }

    /**
     * Initialize sample data on startup (for demo purposes)
     * Creates default workspace, channels, and sample users
     */
    @Bean
    public CommandLineRunner initData(
            ChannelRepository channelRepository,
            UserRepository userRepository,
            WorkspaceRepository workspaceRepository,
            UserWorkspaceMemberRepository memberRepository) {
        return args -> {
            // Create default workspace if it doesn't exist
            Workspace defaultWorkspace;
            if (workspaceRepository.findBySlug("default").isEmpty()) {
                defaultWorkspace = new Workspace("Default Workspace", "default", "The default workspace for BuzzLink");
                defaultWorkspace = workspaceRepository.save(defaultWorkspace);
                System.out.println("✓ Created default workspace");
            } else {
                defaultWorkspace = workspaceRepository.findBySlug("default").get();
            }

            // Create default channels if they don't exist
            if (channelRepository.findByNameAndWorkspaceId("general", defaultWorkspace.getId()).isEmpty()) {
                Channel general = new Channel();
                general.setName("general");
                general.setDescription("General discussion");
                general.setWorkspace(defaultWorkspace);
                channelRepository.save(general);
            }

            if (channelRepository.findByNameAndWorkspaceId("random", defaultWorkspace.getId()).isEmpty()) {
                Channel random = new Channel();
                random.setName("random");
                random.setDescription("Random conversations");
                random.setWorkspace(defaultWorkspace);
                channelRepository.save(random);
            }

            if (channelRepository.findByNameAndWorkspaceId("engineering", defaultWorkspace.getId()).isEmpty()) {
                Channel engineering = new Channel();
                engineering.setName("engineering");
                engineering.setDescription("Engineering team channel");
                engineering.setWorkspace(defaultWorkspace);
                channelRepository.save(engineering);
            }

            System.out.println("✓ BuzzLink backend started successfully!");
            System.out.println("✓ Default workspace initialized: " + defaultWorkspace.getName());
            System.out.println("✓ Default channels initialized: general, random, engineering");
            System.out.println("✓ H2 Console (dev mode): http://localhost:8080/h2-console");
            System.out.println("✓ Prometheus metrics: http://localhost:8080/actuator/prometheus");
            System.out.println();
            System.out.println("📝 New Features:");
            System.out.println("  - Workspaces: GET /api/workspaces?clerkId={clerkId}");
            System.out.println("  - Direct Messages: GET /api/direct-messages/conversations?clerkId={clerkId}");
            System.out.println("  - Channels by Workspace: GET /api/channels?workspaceId={workspaceId}");
        };
    }
}
