package com.example.eventsphere.service.impl;

import com.example.eventsphere.dto.EventDto;
import com.example.eventsphere.dto.mapper.EventMapper;
import com.example.eventsphere.exception.DataAlreadyExistsException;
import com.example.eventsphere.exception.ResourceNotFoundException;
import com.example.eventsphere.model.Event;
import com.example.eventsphere.model.User;
import com.example.eventsphere.repository.EventRepository;
import com.example.eventsphere.repository.UserRepository;
import com.example.eventsphere.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventServiceImpl implements EventService {

    Logger logger = LoggerFactory.getLogger(EventServiceImpl.class);

    private EventRepository eventRepository;
    private UserRepository userRepository;

    public EventServiceImpl(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }
    @Override
    public void createEvent(EventDto eventDto) {

        Event event = EventMapper.mapToEvent(eventDto, new Event());

        Optional<Event> optionalEvent = eventRepository.findByEventNameAndEventDateAndEventLocation(
                eventDto.getEventName(), eventDto.getEventDate(), eventDto.getEventLocation());
        if (optionalEvent.isPresent()) {
            throw new DataAlreadyExistsException("Event already exists with given infos %s : %s : %s"
                    .formatted(eventDto.getEventName(), eventDto.getEventDate(), eventDto.getEventLocation()));
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "email", email));
        event.setUser(user);

        Event savedEvent = eventRepository.save(event);
    }

    @Override
    public Event fetchEvent(int eventId) {
        Event event = eventRepository.findById(eventId);

        return event;
    }

    @Override
    public List<Event> fetchAllSortedDesc() {
        return eventRepository.findAll(Sort.by("eventName").descending());
    }

    @Override
    public List<User> fetchParticipantsByEventId(int eventId) {
        return eventRepository.findParticipantsByEventId(eventId);
    }

    @Override
    public void updateEvent(Event event) {
        eventRepository.save(event);
    }

    @Override
    public void deleteEvent(int eventId) {
        Event event = eventRepository.findById(eventId);

        eventRepository.delete(event);
    }

    @Override
    public List<Event> findEventsByUserId(int userId) {
        return eventRepository.findEventsByUserId(userId);
    }


    @Override
    public List<Event> findEventsByOrganizerId(int userId) {
        return eventRepository.findEventsByOrganizerUserId(userId);
    }

    @Override
    public List<Object[]> findEventsWithParticipantsCountByUserId(int userId) {
        return eventRepository.findEventsWithParticipantsCountByUserId(userId);
    }

    @Override
    public List<Event> findEventsByUserIdSortedByEventDate(int userId) {
        return eventRepository.findEventsByEventIdSortedByEventDate(userId);
    }
}