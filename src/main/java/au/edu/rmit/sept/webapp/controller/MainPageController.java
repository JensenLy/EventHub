package au.edu.rmit.sept.webapp.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.EventCategory;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.EventService;

@Controller
public class MainPageController {
  private final EventService eventService;
  private final CategoryService categoryService;

  public MainPageController (EventService eventService, CategoryService categoryService) {
    this.eventService = eventService;
    this.categoryService = categoryService;
  }

  @GetMapping("/")
  public String mainpage(@RequestParam(name = "categoryId", required = false) Long categoryId, Model model) {
    List<Event> events;
    if (categoryId != null) {
        events = eventService.filterEventsByCategory(categoryId);
    } else {
        events = eventService.getUpcomingEvents();
    }

    List<EventCategory> categories = categoryService.getAllCategories();
    model.addAttribute("events", events);
    model.addAttribute("categories", categories);
    model.addAttribute("selectedCategoryId", categoryId);
    model.addAttribute("currentUserId", 5L);
    return "index";
  }
}
