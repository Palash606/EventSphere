package com.example.eventsphere.controller;

import com.example.eventsphere.dto.EventDto;
import com.example.eventsphere.dto.NotificationDto;
import com.example.eventsphere.exception.ResourceNotFoundException;
import com.example.eventsphere.model.Event;
import com.example.eventsphere.model.Notification;
import com.example.eventsphere.model.User;
import com.example.eventsphere.service.EventService;
import com.example.eventsphere.service.NotificationService;
import com.example.eventsphere.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Optional;

@Controller
@AllArgsConstructor
@RequestMapping("admin")
public class AdminController {

    EventService eventService;
    NotificationService notificationService;
    UserService userService;

    /**
     * Displays the events for admin users.
     *
     * @param model          Spring MVC Model object.
     * @param authentication Authentication object providing user authentication
     *                       details.
     * @return ModelAndView object containing the events_secure.html page and
     *         related model data.
     * @throws ResourceNotFoundException Thrown if the specified user is not found.
     */

    @GetMapping("/displayEvents")
    public ModelAndView displayEvents(Model model, Authentication authentication) {

        List<Event> eventList = eventService.fetchAllSortedDesc();

        User user = userService.readUser(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", authentication.getName()));

        ModelAndView modelAndView = new ModelAndView("events_secure");

        modelAndView.addObject("eventList", eventList);
        modelAndView.addObject("eventDto", new EventDto());
        modelAndView.addObject("notificationDto", new NotificationDto());
        modelAndView.addObject("userId", user.getUserId());

        // modelAndView.addObject("isAdmin", userService.isAdmin());
        return modelAndView;
    }

    /**
     * Displays the notifications for admin users.
     *
     * @param model Spring MVC Model object.
     * @return ModelAndView object containing the notifications_secure.html page and
     *         related model data.
     */
    @GetMapping("/displayNotifications")
    public ModelAndView displayNotifications(Model model) {

        List<Notification> notificationList = notificationService.fetchAll();

        ModelAndView modelAndView = new ModelAndView("notifications_secure");

        modelAndView.addObject("notificationList", notificationList);
        modelAndView.addObject("notification", new Notification());

        return modelAndView;
    }

    /**
     * Creates a new event.
     *
     * @param model    Spring MVC Model object.
     * @param eventDto EventDto object containing the details of the event to be
     *                 created.
     * @param errors   Errors object containing validation errors.
     * @return ModelAndView object, redirects to the displayEvents page if there are
     *         no errors.
     */
    @PostMapping("/createEvent")
    public ModelAndView createEvent(Model model, @Valid @ModelAttribute("eventDto") EventDto eventDto,
                                    Errors errors) {

        ModelAndView modelAndView = new ModelAndView("events_secure");
        if (errors.hasErrors())
            return modelAndView;

        modelAndView.setViewName("redirect:/admin/displayEvents");

        eventService.createEvent(eventDto);

        return modelAndView;
    }

    /**
     * Displays the participants for a specific event, allowing the admin to manage
     * them.
     *
     * @param model   Spring MVC Model object.
     * @param id      ID of the event for which participants are to be managed.
     * @param session HttpSession object for storing the event information.
     * @param error   Optional error message to display.
     * @return ModelAndView object containing the events_participants.html page and
     *         related model data.
     */
    @GetMapping("/manageParticipants")
    public ModelAndView manageParticipants(Model model, @RequestParam int id, HttpSession session,
                                           @RequestParam(required = false) String error) {

        String errorMessage = null;

        ModelAndView modelAndView = new ModelAndView("events_participants");

        Event event = eventService.fetchEvent(id);
        List<User> participants = eventService.fetchParticipantsByEventId(id);

        event.setParticipants(participants);
        modelAndView.addObject("event", event);
        modelAndView.addObject("user", new User());
        session.setAttribute("event", event);

        if (error != null) {
            errorMessage = "Invalid Email!";
            modelAndView.addObject("errorMessage", errorMessage);
        }

        return modelAndView;
    }

    /**
     * Adds a participant to the specified event.
     *
     * @param model   Spring MVC Model object.
     * @param user    User object representing the participant to be added.
     * @param session HttpSession object containing the event information.
     * @return ModelAndView object, redirects to the manageParticipants page of the
     *         event.
     */
    @PostMapping("/addParticipantToEvent")
    public ModelAndView addParticipantToEvent(Model model, @ModelAttribute("user") User user,
                                              HttpSession session) {

        ModelAndView modelAndView = new ModelAndView();

        Event event = (Event) session.getAttribute("event");

        Optional<User> optionalUserEntity = userService.readUser(user.getEmail());
        if (!optionalUserEntity.isPresent()) {
            modelAndView.setViewName("redirect:/admin/manageParticipants?id=" + event.getEventId());
            return modelAndView;
        }

        User userEntity = optionalUserEntity.get();

        userEntity.getEvents().add(event);
        event.getParticipants().add(userEntity);
        userService.updateUser(userEntity);
        session.setAttribute("event", event);

        modelAndView.setViewName("redirect:/admin/manageParticipants?id=" + event.getEventId());

        return modelAndView;
    }

    /**
     * Deletes a participant from the specified event.
     *
     * @param model   Spring MVC Model object.
     * @param email   Email of the participant to be deleted.
     * @param session HttpSession object containing the event information.
     * @return ModelAndView object, redirects to the manageParticipants page of the
     *         event.
     */
    @GetMapping("/deleteParticipantsFromEvent")
    public ModelAndView deleteParticipantsFromEvent(Model model, @RequestParam String email,
                                                    HttpSession session) {

        Event event = (Event) session.getAttribute("event");

        ModelAndView modelAndView = new ModelAndView("redirect:/admin/manageParticipants?id=" + event.getEventId());

        Optional<User> optionalUser = userService.readUser(email);
        if (!optionalUser.isPresent()) {
            return modelAndView;
        }

        User userEntity = optionalUser.get();

        userEntity.getEvents().remove(event);
        event.getParticipants().remove(userEntity);
        eventService.updateEvent(event);
        session.setAttribute("event", event);

        return modelAndView;
    }

    /**
     * Deletes the specified event.
     *
     * @param eventId ID of the event to be deleted.
     * @return ModelAndView object, redirects to the displayEvents page.
     */
    @GetMapping("/deleteEvent")
    public ModelAndView deleteEvent(@RequestParam int eventId) {

        ModelAndView modelAndView = new ModelAndView("redirect:/admin/displayEvents");

        eventService.deleteEvent(eventId);

        return modelAndView;
    }

    /**
     * Creates a new notification.
     *
     * @param notificationDto NotificationDto object containing the details of the
     *                        notification to be created.
     * @param session         HttpSession object.
     * @param errors          Errors object containing validation errors.
     * @return ModelAndView object, redirects to the displayEvents page if there are
     *         no errors.
     */
    @PostMapping("/createNotification")
    public ModelAndView createNotification(@Valid @ModelAttribute("notificationDto") NotificationDto notificationDto,
                                           HttpSession session, Errors errors) {

        ModelAndView modelAndView = new ModelAndView("redirect:/admin/displayEvents");

        if (errors.hasErrors())
            return modelAndView;

        notificationService.createNotification(notificationDto);

        return modelAndView;
    }

    /**
     * Deletes the specified notification.
     *
     * @param notificationId ID of the notification to be deleted.
     * @return ModelAndView object, redirects to the displayNotifications page.
     */
    @GetMapping("/deleteNotification")
    public ModelAndView deleteNotification(@RequestParam int notificationId) {

        ModelAndView modelAndView = new ModelAndView("redirect:/admin/displayNotifications");

        notificationService.deleteNotifications(notificationId);

        return modelAndView;
    }
}