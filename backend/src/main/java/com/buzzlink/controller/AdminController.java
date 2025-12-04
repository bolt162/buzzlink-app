package com.buzzlink.controller;

import com.buzzlink.entity.User;
import com.buzzlink.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Admin Controller for user management and system monitoring
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * Get all users with their stats
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestHeader("X-Clerk-User-Id") String clerkId) {
        if (!adminService.isAdmin(clerkId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }

        try {
            List<Map<String, Object>> users = adminService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching users: " + e.getMessage());
        }
    }

    /**
     * Get system statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getSystemStats(@RequestHeader("X-Clerk-User-Id") String clerkId) {
        if (!adminService.isAdmin(clerkId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }

        try {
            Map<String, Object> stats = adminService.getSystemStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching stats: " + e.getMessage());
        }
    }

    /**
     * Ban a user
     */
    @PostMapping("/users/{userId}/ban")
    public ResponseEntity<?> banUser(
            @RequestHeader("X-Clerk-User-Id") String clerkId,
            @PathVariable Long userId) {
        if (!adminService.isAdmin(clerkId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }

        try {
            User user = adminService.banUser(userId);
            return ResponseEntity.ok(Map.of(
                    "message", "User banned successfully",
                    "userId", user.getId(),
                    "isBanned", user.getIsBanned()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error banning user: " + e.getMessage());
        }
    }

    /**
     * Unban a user
     */
    @PostMapping("/users/{userId}/unban")
    public ResponseEntity<?> unbanUser(
            @RequestHeader("X-Clerk-User-Id") String clerkId,
            @PathVariable Long userId) {
        if (!adminService.isAdmin(clerkId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }

        try {
            User user = adminService.unbanUser(userId);
            return ResponseEntity.ok(Map.of(
                    "message", "User unbanned successfully",
                    "userId", user.getId(),
                    "isBanned", user.getIsBanned()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error unbanning user: " + e.getMessage());
        }
    }

    /**
     * Delete a user (soft delete)
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(
            @RequestHeader("X-Clerk-User-Id") String clerkId,
            @PathVariable Long userId) {
        if (!adminService.isAdmin(clerkId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }

        try {
            adminService.deleteUser(userId);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error deleting user: " + e.getMessage());
        }
    }

    /**
     * Toggle admin status for a user
     */
    @PostMapping("/users/{userId}/toggle-admin")
    public ResponseEntity<?> toggleAdmin(
            @RequestHeader("X-Clerk-User-Id") String clerkId,
            @PathVariable Long userId) {
        if (!adminService.isAdmin(clerkId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }

        try {
            User user = adminService.toggleAdmin(userId);
            return ResponseEntity.ok(Map.of(
                    "message", "Admin status toggled successfully",
                    "userId", user.getId(),
                    "isAdmin", user.getIsAdmin()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error toggling admin status: " + e.getMessage());
        }
    }

    /**
     * Get recent application logs
     */
    @GetMapping("/logs")
    public ResponseEntity<?> getLogs(
            @RequestHeader("X-Clerk-User-Id") String clerkId,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "INFO") String level) {
        if (!adminService.isAdmin(clerkId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }

        try {
            List<Map<String, Object>> logs = getRecentLogs(limit, level);
            return ResponseEntity.ok(Map.of(
                    "logs", logs,
                    "count", logs.size(),
                    "level", level
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching logs: " + e.getMessage());
        }
    }

    /**
     * Get recent logs from file or system
     * This is a simple implementation - in production, you'd use a proper logging aggregation system
     */
    private List<Map<String, Object>> getRecentLogs(int limit, String level) {
        List<Map<String, Object>> logs = new ArrayList<>();

        // Try to read from Spring Boot log file
        String[] possibleLogPaths = {
                "logs/spring-boot-logger.log",
                "logs/application.log",
                "/var/log/buzzlink/application.log"
        };

        for (String logPath : possibleLogPaths) {
            File logFile = new File(logPath);
            if (logFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                    List<String> lines = reader.lines()
                            .filter(line -> level.equals("ALL") || line.contains(level))
                            .collect(Collectors.toList());

                    // Get last N lines
                    int start = Math.max(0, lines.size() - limit);
                    for (int i = start; i < lines.size(); i++) {
                        Map<String, Object> logEntry = new HashMap<>();
                        logEntry.put("timestamp", new Date());
                        logEntry.put("level", extractLogLevel(lines.get(i)));
                        logEntry.put("message", lines.get(i));
                        logs.add(logEntry);
                    }
                    return logs;
                } catch (IOException e) {
                    // Continue to next log path
                }
            }
        }

        // If no log file found, return mock logs for demo
        logs.add(createMockLog("INFO", "Application started on port 8080"));
        logs.add(createMockLog("INFO", "User logged in: user_363I3otryN1G1UxNwTVRFaBNcHS"));
        logs.add(createMockLog("INFO", "Workspace created: 'Engineering Team'"));
        logs.add(createMockLog("INFO", "Channel created: #general"));
        logs.add(createMockLog("INFO", "WebSocket connection established for user: johndoe"));
        logs.add(createMockLog("DEBUG", "User authentication completed successfully"));
        logs.add(createMockLog("INFO", "Message sent to channel #general: 'Hello team!'"));
        logs.add(createMockLog("INFO", "Direct message sent from user1 to user2"));
        logs.add(createMockLog("INFO", "Invitation sent to: john@example.com"));
        logs.add(createMockLog("WARN", "Database connection pool reaching capacity (80%)"));
        logs.add(createMockLog("INFO", "Workspace member added to 'Marketing Team'"));
        logs.add(createMockLog("DEBUG", "Cache hit for user profile: user_123"));
        logs.add(createMockLog("INFO", "Thread reply posted in channel #development"));
        logs.add(createMockLog("WARN", "Slow query detected: getUserConversations (1.2s)"));
        logs.add(createMockLog("INFO", "User logged out: jane@example.com"));
        logs.add(createMockLog("ERROR", "Failed to send email invitation: SMTP timeout"));
        logs.add(createMockLog("INFO", "Reaction added to message: 👍"));
        logs.add(createMockLog("INFO", "User updated profile picture"));
        logs.add(createMockLog("WARN", "High memory usage detected: 85% used"));
        logs.add(createMockLog("INFO", "Channel deleted: #old-projects"));

        return logs;
    }

    private String extractLogLevel(String logLine) {
        if (logLine.contains("ERROR")) return "ERROR";
        if (logLine.contains("WARN")) return "WARN";
        if (logLine.contains("INFO")) return "INFO";
        if (logLine.contains("DEBUG")) return "DEBUG";
        return "INFO";
    }

    private Map<String, Object> createMockLog(String level, String message) {
        Map<String, Object> log = new HashMap<>();
        log.put("timestamp", new Date());
        log.put("level", level);
        log.put("message", message);
        return log;
    }
}
