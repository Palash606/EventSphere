package com.example.eventsphere.repository;

import com.example.eventsphere.model.Event;
import com.example.eventsphere.model.Notification;
import com.example.eventsphere.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    Notification findByNotificationContentAndUserAndEvent(String notificationContent, User user, Event event);

    @Query("SELECT n FROM Notification n JOIN n.user u WHERE u.userId = :userId")
    List<Notification> findNotificationsByUserId(@Param("userId") int userId);

    @Query("SELECT n FROM Notification n JOIN n.event e WHERE e.eventId = :eventId")
    List<Notification> findNotificationsByEventId(@Param("eventId") int eventId);

    @Query("SELECT e.eventId, COUNT(n.notificationId) " +
            "FROM Notification n " +
            "JOIN n.event e " +
            "JOIN e.participants p " +
            "WHERE p.userId = :userId " +
            "GROUP BY e.eventId")
    List<Object[]> findEventIdsWithUnreadNotificationCounts(@Param("userId") int userId);
}