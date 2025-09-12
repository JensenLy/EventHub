package au.edu.rmit.sept.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.EventCategory;
import au.edu.rmit.sept.webapp.repository.RsvpRepository;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.EventService;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Integration tests for MainPageController:
 * - Runs with Spring MVC (MockMvc).
 * - Services/repos are mocked via @MockBean for speed and determinism.
 */
@SpringBootTest(classes = au.edu.rmit.sept.webapp.controller.MainPageController.class)
@AutoConfigureMockMvc
public class MainPageControllerIntegrationTest {
  
  @Autowired
  private MockMvc mvc;

  @MockBean private EventService eventService;
  @MockBean private RsvpRepository rsvpRepository;
  @MockBean private CategoryService categoryService;

  // ---- helpers ----
  private static Event event(long id, String name) {
    Event e = new Event();
    e.setEventId(id);
    e.setName(name);
    return e;
  }

  private static EventCategory category(long id, String name) {
      return new EventCategory(id, name);
  }

  /**
     * GET "/" with no category filter:
     * - RSVP map built from upcoming events.
     * - Displayed events are the upcoming list.
     * - Categories included, selectedCategoryId = null.
     */
    @Test
    void rendersIndex_withUpcomingEvents_andRsvpMap() throws Exception {
        var upcoming = List.of(event(1L, "Tech Talk"), event(2L, "Career Fair"));
        when(eventService.getUpcomingEvents()).thenReturn(upcoming);
        when(categoryService.getAllCategories()).thenReturn(List.of(category(10L, "Tech"), category(20L, "Networking")));
        when(rsvpRepository.checkUserAlreadyRsvped(5L, 1L)).thenReturn(true);
        when(rsvpRepository.checkUserAlreadyRsvped(5L, 2L)).thenReturn(false);

        MvcResult res = mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("events", "rsvpStatusMap", "categories", "currentUserId"))
                .andExpect(model().attribute("selectedCategoryId", (Long) null))
                .andReturn();

        @SuppressWarnings("unchecked")
        Map<Long, Boolean> rsvp = (Map<Long, Boolean>) res.getModelAndView().getModel().get("rsvpStatusMap");
        assertThat(rsvp).containsEntry(1L, true).containsEntry(2L, false);

        @SuppressWarnings("unchecked")
        List<EventCategory> cats = (List<EventCategory>) res.getModelAndView().getModel().get("categories");
        assertThat(cats)
                .extracting(EventCategory::getCategoryId, EventCategory::getName)
                .containsExactly(
                        tuple(10L, "Tech"),
                        tuple(20L, "Networking")
                );

        verify(eventService, times(2)).getUpcomingEvents(); // RSVP build + display
        verify(eventService, never()).filterEventsByCategory(anyLong());
        verify(categoryService).getAllCategories();
        verify(rsvpRepository).checkUserAlreadyRsvped(5L, 1L);
        verify(rsvpRepository).checkUserAlreadyRsvped(5L, 2L);
        verifyNoMoreInteractions(eventService, categoryService, rsvpRepository);
    }


}
