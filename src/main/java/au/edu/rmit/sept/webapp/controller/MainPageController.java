package au.edu.rmit.sept.webapp.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.EventCategory;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.RsvpRepository;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.UserService;

@Controller
public class MainPageController {
  private final EventService eventService;
  private final RsvpRepository rsvpRepository;
  private  final CategoryService categoryService;

  private final UserService userService;
  
  public MainPageController(EventService eventService, RsvpRepository rsvpRepository, CategoryService categoryService
  
  , UserService userService) {
    this.eventService = eventService;
    this.rsvpRepository = rsvpRepository;
    this.categoryService = categoryService;

    this.userService = userService;
  }

  @GetMapping("/")
  public String mainpage(@RequestParam(name = "categoryId", required = false) Long categoryId, Model model 
  
  ,Principal principal) {
    List<Event> events = eventService.getUpcomingEvents();
    
    Long currentUserId = null;
        if (principal != null) {
      String email = principal.getName(); // This gets the username (email in our case)
      User currentUser = userService.getUserByEmail(email);
      if (currentUser != null) {
        currentUserId = currentUser.getUserId();
      }
    }

        // Fallback to default user if no user is logged in or user not found
    if (currentUserId == null) {
      currentUserId = 5L; 
    }


    
    // Map to hold RSVP status for each event
    Map<Long, Boolean> rsvpStatusMap = new HashMap<>();
    for (Event event : events) {
        boolean hasRsvped = rsvpRepository.checkUserAlreadyRsvped(currentUserId, event.getEventId());
        rsvpStatusMap.put(event.getEventId(), hasRsvped);
    }

    if (categoryId != null) {
        events = eventService.filterEventsByCategory(categoryId);
    } else {
        events = eventService.getUpcomingEvents();
    }

    List<EventCategory> categories = categoryService.getAllCategories();
    model.addAttribute("events", events);
    model.addAttribute("currentUserId", currentUserId);
    model.addAttribute("rsvpStatusMap", rsvpStatusMap);

    model.addAttribute("categories", categories);
    model.addAttribute("selectedCategoryId", categoryId);

    return "index";
  }
}
