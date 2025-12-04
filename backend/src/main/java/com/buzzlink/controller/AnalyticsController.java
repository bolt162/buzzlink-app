package com.buzzlink.controller;

import com.buzzlink.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/kpis")
    public ResponseEntity<Map<String, Object>> getKPIs() {
        return ResponseEntity.ok(analyticsService.getKPIs());
    }

    @GetMapping("/messages-timeline")
    public ResponseEntity<Map<String, Object>> getMessagesTimeline() {
        return ResponseEntity.ok(analyticsService.getMessagesTimeline());
    }

    @GetMapping("/top-workspaces")
    public ResponseEntity<Map<String, Object>> getTopWorkspaces() {
        return ResponseEntity.ok(analyticsService.getTopWorkspaces());
    }

    @GetMapping("/top-users")
    public ResponseEntity<Map<String, Object>> getTopUsers() {
        return ResponseEntity.ok(analyticsService.getTopUsers());
    }

    @GetMapping("/messages-by-channel")
    public ResponseEntity<Map<String, Object>> getMessagesByChannel() {
        return ResponseEntity.ok(analyticsService.getMessagesByChannel());
    }

    @GetMapping("/user-activity-distribution")
    public ResponseEntity<Map<String, Object>> getUserActivityDistribution() {
        return ResponseEntity.ok(analyticsService.getUserActivityDistribution());
    }
}
