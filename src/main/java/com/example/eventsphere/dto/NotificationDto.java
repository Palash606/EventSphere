package com.example.eventsphere.dto;

import lombok.Data;

@Data
public class NotificationDto {
    private String notificationContent;
    private int userId;
    private int eventId;
}
