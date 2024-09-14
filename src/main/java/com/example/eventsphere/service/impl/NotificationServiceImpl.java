package com.example.eventsphere.service.impl;

import com.example.eventsphere.dto.NotificationDto;
import com.example.eventsphere.dto.mapper.NotificationMapper;
import com.example.eventsphere.exception.ResourceNotFoundException;
import com.example.eventsphere.model.Event;
import com.example.eventsphere.model.Notification;
import com.example.eventsphere.model.User;
import com.example.eventsphere.repository.EventRepository;
import com.example.eventsphere.repository.NotificationRepository;
import com.example.eventsphere.repository.UserRepository;
import com.example.eventsphere.service.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    NotificationRepository notificationRepository;
    UserRepository userRepository;
    EventRepository eventRepository;

    /**
     * Save Notification into DB with notificationDto entity
     *
     * @param notificationDto
     */
    @Override
    public void createNotification(NotificationDto notificationDto) {
        User user = userRepository.findById(notificationDto.getUserId()).orElseThrow(
                () -> new ResourceNotFoundException("User", "id",
                        String.valueOf(notificationDto.getUserId())));
        Event event = eventRepository.findById(notificationDto.getEventId());
        Notification notification = notificationRepository
                .findByNotificationContentAndUserAndEvent(notificationDto.getNotificationContent(),
                        user, event);
        // todo send comm
        if (notification == null)
            notification = NotificationMapper.mapToNotification(notificationDto, new Notification());
        notification.setRead(false);
        notification.setUser(user);
        notification.setEvent(event);
        Notification savedNotification = notificationRepository.save(notification);
    }

    /**
     * Find All Notifications by user's userId who created the notification
     *
     * @param userId
     * @return Notification List
     */
    @Override
    public List<Notification> fetchNotificationsByUserId(int userId) {
        List<Notification> notificationList = notificationRepository.findNotificationsByUserId(userId);
        return notificationList;
    }

    /**
     * Get All Notifications
     *
     * @return Notification List
     */
    @Override
    public List<Notification> fetchAll() {
        return notificationRepository.findAll();
    }

    /**
     * Save Notification into DB with notification entity
     *
     * @param notification
     */
    @Override
    public void createNotification(Notification notification) {
        notificationRepository.save(notification);
    }

    /**
     * Delete notification by notificationId
     *
     * @param notificationId
     */
    @Override
    public void deleteNotifications(int notificationId) {
        notificationRepository.delete(notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "notificationId",
                        String.valueOf(notificationId))));
    }

    /**
     * Find all notifications by event's eventId which is related to notification
     *
     * @param eventId
     * @return Notification List
     */
    @Override
    public List<Notification> fetchNotificationsByEventId(int eventId) {
        List<Notification> notificationList = notificationRepository.findNotificationsByEventId(eventId);
        return notificationList;
    }

    /**
     * Get a list which contains eventId - unread notification count for all
     * notifications
     *
     * @return List of eventId - unread notification count pair object
     */
    @Override
    public List<Object[]> findEventIdsWithUnreadNotificationCounts(int userId) {
        return notificationRepository.findEventIdsWithUnreadNotificationCounts(userId);
    }

}