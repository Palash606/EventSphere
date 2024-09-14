package com.example.eventsphere.controller;

import com.example.eventsphere.dto.NotificationDto;
import com.example.eventsphere.model.Event;
import com.example.eventsphere.model.Notification;
import com.example.eventsphere.model.User;
import com.example.eventsphere.service.EventService;
import com.example.eventsphere.service.NotificationService;
import com.example.eventsphere.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
public class UserController {

    UserService userService;
    NotificationService notificationService;
    EventService eventService;

    /**
     * Displays the events page with organized, enrolled, and available events for
     * the logged-in user.
     *
     * This function retrieves the user information from the session, fetches
     * organized, enrolled, and available events,
     * and prepares the data to be displayed on the events page.
     *
     * @param session HttpSession object containing the user information.
     * @return ModelAndView object containing the events.html page and related model
     *         data.
     */
    @GetMapping("/displayEvents")
    public ModelAndView displayEvents(HttpSession session) {
        User user = getUserFromSession(session);

        ModelAndView modelAndView = new ModelAndView("events.html");
        modelAndView.addObject("user", user);

        // Fetch organized, enrolled, and all events from the database
        List<Object[]> organizedEvents = eventService.findEventsWithParticipantsCountByUserId(user.getUserId());
        List<Event> enrolledEvents = eventService.findEventsByUserId(user.getUserId());
        List<Event> events = eventService.fetchAllSortedDesc();

        // Filter and prepare data for display
        List<Event> organizedEventList = organizedEvents.stream()
                .filter(event -> event.length > 0 && event[0] instanceof Event)
                .map(event -> (Event) event[0])
                .collect(Collectors.toList());
        events.removeAll(enrolledEvents);
        events.removeAll(organizedEventList);

        // Add data to the model
        modelAndView.addObject("organizedEvents", organizedEvents);
        modelAndView.addObject("enrolledEvents", enrolledEvents);
        modelAndView.addObject("events", events);
        modelAndView.addObject("event", new Event());
        modelAndView.addObject("notificationDto", new NotificationDto());

        return modelAndView;
    }

    /**
     * Displays the profile page for the logged-in user.
     *
     * This function retrieves the user information from the session and fetches the
     * ordered events for the user,
     * then prepares the data to be displayed on the profile page.
     *
     * @param session HttpSession object containing the user information.
     * @return ModelAndView object containing the profile.html page and related
     *         model data.
     */
    @GetMapping("/displayProfile")
    public ModelAndView displayProfile(HttpSession session) {
        User user = getUserFromSession(session);

        ModelAndView modelAndView = new ModelAndView("profile.html");

        modelAndView.addObject("user", user);

        // Fetch ordered events from the database
        List<Event> orderedEvents = eventService.findEventsByUserIdSortedByEventDate(user.getUserId());
        List<Object[]> eventIdsWithUnreadNotificationCounts = notificationService
                .findEventIdsWithUnreadNotificationCounts(user.getUserId());

        modelAndView.addObject("orderedEvents", orderedEvents);
        modelAndView.addObject("eventIdsWithUnreadNotificationCounts", eventIdsWithUnreadNotificationCounts);

        return modelAndView;
    }

    /**
     * Creates a new notification and redirects to the events page.
     *
     * This function handles the POST request for the /createNotification endpoint.
     * It creates a new notification based on the provided data and redirects to the
     * events page.
     *
     * @param model           Spring MVC Model object.
     * @param notificationDto NotificationDto object containing the notification
     *                        details.
     * @param session         HttpSession object.
     * @return ModelAndView object, redirects to the displayEvents page.
     */
    @PostMapping("/createNotification")
    public ModelAndView createNotification(Model model,
                                           @Valid @ModelAttribute("notificationDto") NotificationDto notificationDto, HttpSession session) {
        ModelAndView modelAndView = new ModelAndView("redirect:/displayEvents");

        notificationService.createNotification(notificationDto);

        return modelAndView;
    }

    /**
     * Updates the specified event and redirects to the events page.
     *
     * This function handles the POST request for the /updateEvent endpoint.
     * It validates the event data, updates the event if validation passes, and
     * redirects to the events page.
     *
     * @param event   Event object containing the updated event information.
     * @param session HttpSession object containing the user information.
     * @param errors  Errors object containing validation errors, if any.
     * @return ModelAndView object, redirects to the displayEvents page.
     */
    @PostMapping("/updateEvent")
    public ModelAndView updateEvent(@Valid @ModelAttribute("event") Event event,
                                    HttpSession session, Errors errors) {
        if (errors.hasErrors())
            return new ModelAndView("redirect:/displayEvents");

        User user = getUserFromSession(session);

        event.setUser(user);

        eventService.updateEvent(event);

        return new ModelAndView("redirect:/displayEvents");
    }

    @GetMapping("/joinEvent")
    public ModelAndView joinEvent(@RequestParam int eventId, HttpSession session) {

        User user = getUserFromSession(session);
        ModelAndView modelAndView = new ModelAndView("redirect:/displayEvents");
        Event event = eventService.fetchEvent(eventId);

        HandleUserEventInteraction(UserEventInteractionType.ADD, user, event);
        return modelAndView;
    }

    /**
     * Allows the authenticated user to leave the specified event and redirects to
     * the events page.
     *
     * This function handles the GET request for the /leaveEvent endpoint.
     * It retrieves the authenticated user information from the session, fetches the
     * specified event,
     * and removes the user from the event's participants list.
     *
     * @param eventId ID of the event to leave.
     * @param session HttpSession object containing user information.
     * @return ModelAndView object, redirects to the displayEvents page.
     */
    @GetMapping("/leaveEvent")
    public ModelAndView leaveEvent(@RequestParam int eventId, HttpSession session) {

        User user = getUserFromSession(session);
        ModelAndView modelAndView = new ModelAndView("redirect:/displayEvents");
        Event event = eventService.fetchEvent(eventId);

        HandleUserEventInteraction(UserEventInteractionType.REMOVE, user, event);
        return modelAndView;
    }

    @GetMapping("/getNotifications")
    public ModelAndView fetchNotificationsByEventId(@RequestParam int eventId, Model model) {
        ModelAndView modelAndView = new ModelAndView("redirect:/displayProfile");
        List<Notification> notifications = notificationService.fetchNotificationsByEventId(eventId);
        model.addAttribute("notifications", notifications);
        return modelAndView;
    }

    enum UserEventInteractionType {
        ADD,
        REMOVE
    }

    /**
     * Handles the interaction between user and event (addition or removal).
     *
     * This function takes the interaction type (add or remove), user, and event as
     * parameters,
     * updates their associations accordingly, and persists the changes to the
     * database.
     *
     * @param interactionType Type of interaction (ADD or REMOVE).
     * @param user            User object participating in the interaction.
     * @param event           Event object participating in the interaction.
     */
    public void HandleUserEventInteraction(UserEventInteractionType interactionType, User user, Event event) {

        // Get Lazy list values from each entity
        event.setParticipants(eventService.fetchParticipantsByEventId(event.getEventId()));
        user.setEvents(eventService.findEventsByUserId(user.getUserId()));

        if (interactionType == UserEventInteractionType.ADD && event.getUser().getUserId() == user.getUserId())
            return;

        // update objects
        switch (interactionType) {
            case ADD:
                user.getEvents().add(event);
                event.getParticipants().add(user);
                break;
            case REMOVE:
                user.getEvents().remove(event);
                event.getParticipants().remove(user);
                break;
            default:
                break;
        }
        // update database
        eventService.updateEvent(event);
    }

    private User getUserFromSession(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }

}