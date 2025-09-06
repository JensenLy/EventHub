package au.edu.rmit.sept.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.repository.CategoryRepository;
import au.edu.rmit.sept.webapp.model.EventCategory;
import java.util.List;

@Controller
public class EventController {
    private final EventService eventService;
    private final CategoryRepository categoryRepository;

    public EventController(EventService Service, CategoryRepository categoryRepository)
    {
      this.eventService = Service;
      this.categoryRepository = categoryRepository;
    }
    
  @GetMapping("/eventPage")
    public String mainpage(Model model) {
      List<EventCategory> categories = categoryRepository.findAll();
      model.addAttribute("categories", categories);
      model.addAttribute("event", new Event());
      return "eventPage";
    }

  @PostMapping("/eventForm")
    public String submitEvent(@ModelAttribute("event") Event event, @RequestParam(name = "categoryIds", required = false) List<Long> categoryIds, Model model) {
    if (categoryIds == null) categoryIds = java.util.List.of();
      // server-side cap (mirrors the JS)
    if (categoryIds.size() > 3) {
      model.addAttribute("confirmation", "You can select up to 3 categories only.");
  } else if (!eventService.isValidDateTime(event)) {
      model.addAttribute("confirmation", "Enter a valid date!");
  } else {
      // fetch names for duplicate check
      List<String> categoryNames = categoryRepository.findNamesByIds(categoryIds);

      boolean exists = eventService.eventExist(
          event.getCreatedByUserId(),
          event.getName(),
          categoryNames,             
          event.getLocation()
      );

      if (!exists) {
          eventService.saveEventWithCategories(event, categoryIds); 
          model.addAttribute("confirmation", "Event created successfully!");
      } else {
          model.addAttribute("confirmation", "Event already exists!");
      }
  }

  // re-populate form
  model.addAttribute("event", new Event());
  model.addAttribute("categories", categoryRepository.findAll());
  return "eventPage";
}




}
