package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.model.Event;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

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
    return "index";
  }
}
