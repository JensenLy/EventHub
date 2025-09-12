package au.edu.rmit.sept.webapp.controllers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.EventCategory;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.EventService;


@SpringBootTest
@AutoConfigureMockMvc
public class createEventTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    EventService eventService;

    @MockBean
    private CategoryService categoryService;

    @Test
    void ShowSuccessfulEventCreation() throws Exception {
        List<EventCategory> categories = List.of(
            new EventCategory(1L, "Social"),
            new EventCategory(2L, "Career")
        );

        List<String> categoryNames = categories.stream()
                                               .map(EventCategory::getName)
                                               .toList();

        List<Long> categoryIds = categories.stream()
                                          .map(EventCategory::getCategoryId)
                                          .toList();

                                          
        when(categoryService.getAllCategories()).thenReturn(categories);
        when(categoryService.findCategoryNamesByIds(eq(List.of(1L, 2L)))).thenReturn(categoryNames);

        Event event = new Event(
            1L, 
            "Test Event", 
            "For testing purposes", 
            5L, 
            LocalDateTime.of(2025, 9, 22, 12, 0, 0), 
            "Vic", 
            categoryNames, 
            100,
            BigDecimal.ZERO
        );

        when(eventService.isValidDateTime(argThat(e ->
            e.getName().equals("Test Event") &&
            e.getDesc().equals("For testing purposes") &&
            e.getCreatedByUserId().equals(5L) &&
            e.getLocation().equals("Vic")
        ))).thenReturn(true);

        when(eventService.eventExist(
                eq(5L),
                eq("Test Event"),
                eq(categoryNames),
                eq("Vic")
        )).thenReturn(false);

        when(eventService.saveEventWithCategories(
                argThat(e -> e.getName().equals("Test Event") && e.getLocation().equals("Vic")),
                eq(List.of(1L, 2L))
        )).thenReturn(event);

        // Simulate form submission
        mvc.perform(post("/eventForm")
                .param("name", "Test Event")
                .param("desc", "For testing purposes")
                .param("createdByUserId", "5")
                .param("location", "Vic")
                .param("capacity", "100")
                .param("price", "0")   
                .param("dateTime", "2025-09-22T12:00:00")
                .param("categoryIds", "1", "2") 
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(flash().attribute("successMessage", "Event created successfully!"));
    }
}

