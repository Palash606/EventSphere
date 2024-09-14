package com.example.eventsphere.dto.mapper;

import com.example.eventsphere.dto.NotificationDto;
import com.example.eventsphere.model.Notification;

public class NotificationMapper {
    public static NotificationDto mapTonotificationDto(Notification notification,
                                                       NotificationDto notificationDto) {
        notificationDto.setNotificationContent(notification.getNotificationContent());
        notificationDto.setUserId(notification.getUser().getUserId());
        notificationDto.setEventId(notification.getEvent().getEventId());
        return notificationDto;
    }

    public static Notification mapToNotification(NotificationDto notificationDto, Notification notification) {
        notification.setNotificationContent(notificationDto.getNotificationContent());
        return notification;
    }

}
