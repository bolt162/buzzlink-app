package com.buzzlink.controller;

import com.buzzlink.dto.UserDTO;
import com.buzzlink.entity.User;
import com.buzzlink.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for user operations
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    private final UserService userService;

    /**
     * POST /api/users/sync - Create or update user from Clerk auth
     * Called by frontend after Clerk authentication
     */
    @PostMapping("/sync")
    public ResponseEntity<UserDTO> syncUser(@RequestBody SyncUserRequest request) {
        User user = userService.createOrUpdateUser(
            request.clerkId(),
            request.displayName(),
            request.email(),
            request.avatarUrl()
        );
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    /**
     * GET /api/users/me - Get current user info
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@RequestHeader("X-Clerk-User-Id") String clerkId) {
        return userService.findByClerkId(clerkId)
            .map(user -> ResponseEntity.ok(UserDTO.fromEntity(user)))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/users/me - Update current user profile
     */
    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateProfile(
        @RequestHeader("X-Clerk-User-Id") String clerkId,
        @RequestBody UpdateProfileRequest request
    ) {
        User user = userService.updateProfile(clerkId, request.displayName(), request.avatarUrl());
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    /**
     * POST /api/users/make-admin - Make a user admin (for demo purposes)
     * In production, this would require proper authorization
     */
    @PostMapping("/make-admin")
    public ResponseEntity<Void> makeAdmin(@RequestBody MakeAdminRequest request) {
        userService.setAdmin(request.clerkId(), request.isAdmin());
        return ResponseEntity.ok().build();
    }

    /**
     * GET /api/users - List all users (for debugging)
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserDTO> userDTOs = users.stream()
            .map(UserDTO::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    /**
     * GET /api/users/search - Search users by name or email
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String query) {
        System.out.println("Searching for users with query: " + query);
        List<User> users = userService.searchUsers(query);
        System.out.println("Found " + users.size() + " users");
        List<UserDTO> userDTOs = users.stream()
            .map(UserDTO::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    /**
     * Request DTOs
     */
    public record SyncUserRequest(String clerkId, String displayName, String email, String avatarUrl) {}
    public record UpdateProfileRequest(String displayName, String avatarUrl) {}
    public record MakeAdminRequest(String clerkId, boolean isAdmin) {}
}
