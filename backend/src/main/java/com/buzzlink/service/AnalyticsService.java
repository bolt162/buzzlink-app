package com.buzzlink.service;

import com.buzzlink.entity.Message;
import com.buzzlink.entity.User;
import com.buzzlink.entity.Workspace;
import com.buzzlink.entity.Channel;
import com.buzzlink.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private ReactionRepository reactionRepository;

    @Autowired
    private UserWorkspaceMemberRepository memberRepository;

    public Map<String, Object> getKPIs() {
        Map<String, Object> kpis = new HashMap<>();

        long totalUsers = userRepository.count();
        long totalMessages = messageRepository.count();
        long totalWorkspaces = workspaceRepository.count();
        long totalChannels = channelRepository.count();
        long totalReactions = reactionRepository.count();
        long totalMembers = memberRepository.count();

        // Active users in last 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Message> recentMessages = messageRepository.findAll().stream()
                .filter(m -> m.getCreatedAt().isAfter(thirtyDaysAgo))
                .collect(Collectors.toList());

        long activeUsers = recentMessages.stream()
                .map(m -> m.getSender().getId())
                .distinct()
                .count();

        kpis.put("totalUsers", totalUsers);
        kpis.put("totalMessages", totalMessages);
        kpis.put("totalWorkspaces", totalWorkspaces);
        kpis.put("totalChannels", totalChannels);
        kpis.put("totalReactions", totalReactions);
        kpis.put("totalMembers", totalMembers);
        kpis.put("activeUsers", activeUsers);
        kpis.put("engagementRate", totalUsers > 0 ? (int) ((activeUsers * 100.0) / totalUsers) : 0);

        return kpis;
    }

    public Map<String, Object> getMessagesTimeline() {
        List<Message> allMessages = messageRepository.findAll();

        // Group messages by date
        Map<LocalDate, Long> messagesByDate = allMessages.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getCreatedAt().toLocalDate(),
                        Collectors.counting()));

        // Get last 90 days
        List<LocalDate> last90Days = new ArrayList<>();
        for (int i = 89; i >= 0; i--) {
            last90Days.add(LocalDate.now().minusDays(i));
        }

        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

        for (LocalDate date : last90Days) {
            labels.add(date.format(formatter));
            data.add(messagesByDate.getOrDefault(date, 0L));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("data", data);

        return result;
    }

    public Map<String, Object> getTopWorkspaces() {
        List<Workspace> workspaces = workspaceRepository.findAll();

        // Count messages per workspace
        Map<String, Long> workspaceMessageCounts = new HashMap<>();

        for (Workspace workspace : workspaces) {
            long messageCount = channelRepository.findByWorkspace(workspace).stream()
                    .mapToLong(channel -> messageRepository.countByChannel(channel))
                    .sum();
            workspaceMessageCounts.put(workspace.getName(), messageCount);
        }

        // Sort and get top 5
        List<Map.Entry<String, Long>> sortedWorkspaces = workspaceMessageCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

        List<String> labels = sortedWorkspaces.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<Long> data = sortedWorkspaces.stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("data", data);

        return result;
    }

    public Map<String, Object> getTopUsers() {
        List<User> users = userRepository.findAll();

        // Count messages per user
        Map<String, Long> userMessageCounts = new HashMap<>();

        for (User user : users) {
            long messageCount = messageRepository.countBySender(user);
            if (messageCount > 0) {
                userMessageCounts.put(user.getDisplayName(), messageCount);
            }
        }

        // Sort and get top 10
        List<Map.Entry<String, Long>> sortedUsers = userMessageCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());

        List<String> labels = sortedUsers.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<Long> data = sortedUsers.stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("data", data);

        return result;
    }

    public Map<String, Object> getMessagesByChannel() {
        List<Channel> channels = channelRepository.findAll();

        // Count messages per channel
        Map<String, Long> channelMessageCounts = new HashMap<>();

        for (Channel channel : channels) {
            long messageCount = messageRepository.countByChannel(channel);
            if (messageCount > 0) {
                channelMessageCounts.put(channel.getName(), messageCount);
            }
        }

        // Sort and get top 10
        List<Map.Entry<String, Long>> sortedChannels = channelMessageCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());

        List<String> labels = sortedChannels.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<Long> data = sortedChannels.stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("data", data);

        return result;
    }

    public Map<String, Object> getUserActivityDistribution() {
        List<User> users = userRepository.findAll();

        // Categorize users by message count
        int lowActivity = 0; // 0-10 messages
        int mediumActivity = 0; // 11-50 messages
        int highActivity = 0; // 51+ messages

        for (User user : users) {
            long messageCount = messageRepository.countBySender(user);
            if (messageCount <= 10) {
                lowActivity++;
            } else if (messageCount <= 50) {
                mediumActivity++;
            } else {
                highActivity++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", Arrays.asList("0-10 messages", "11-50 messages", "51+ messages"));
        result.put("data", Arrays.asList(lowActivity, mediumActivity, highActivity));

        return result;
    }
}
