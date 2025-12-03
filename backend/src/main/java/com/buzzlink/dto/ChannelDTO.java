package com.buzzlink.dto;

import com.buzzlink.entity.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Channel information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChannelDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;

    /**
     * Convert Channel entity to DTO
     */
    public static ChannelDTO fromEntity(Channel channel) {
        return new ChannelDTO(
            channel.getId(),
            channel.getName(),
            channel.getDescription(),
            channel.getCreatedAt()
        );
    }
}
