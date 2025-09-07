package au.edu.rmit.sept.webapp.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.service.EventService;

@Controller
public class MainPageController {
  private final EventService eventService;

  public MainPageController (EventService eventService) {
    this.eventService = eventService;
  }

  @GetMapping("/")
  public String mainpage(Model model) {
    List<Event> events = eventService.getUpcomingEvents();
    model.addAttribute("events", events);
    model.addAttribute("currentUserId", 5L);
    return "index";
  }
}
