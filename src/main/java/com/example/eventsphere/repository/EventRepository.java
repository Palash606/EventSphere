package com.example.eventsphere.repository;

import com.example.eventsphere.model.Event;
import com.example.eventsphere.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
    Optional<Event> findByEventNameAndEventDateAndEventLocation(String eventName, LocalDate eventDate,
                                                                String eventLocation);

    @Query("SELECT e.participants FROM   Event e WHERE e.eventId = :eventId")
    List<User> findParticipantsByEventId(@Param("eventId") int eventId);

    @Query("SELECT e FROM Event e JOIN e.participants p WHERE p.userId = :userId")
    List<Event> findEventsByUserId(@Param("userId") int userId);

    @Query("SELECT e FROM Event e JOIN e.user u WHERE u.userId = :userId")
    List<Event> findEventsByOrganizerUserId(@Param("userId") int userId);

    @Query("SELECT e, COUNT(p.id) AS participants_count FROM Event e LEFT JOIN e.participants p JOIN e.user u WHERE u.userId = :userId GROUP BY e")
    List<Object[]> findEventsWithParticipantsCountByUserId(@Param("userId") int userId);

    @Query("SELECT e FROM Event e WHERE e.eventId = :eventId")
    Event findById(@Param("eventId") int eventId);

    @Query("SELECT e FROM Event e JOIN e.participants p WHERE p.userId = :userId ORDER BY e.eventDate")
    List<Event> findEventsByEventIdSortedByEventDate(@Param("userId") int userId);

}