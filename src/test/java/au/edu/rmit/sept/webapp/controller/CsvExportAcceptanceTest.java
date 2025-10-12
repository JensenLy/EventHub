package au.edu.rmit.sept.webapp.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.service.CurrentUserService;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.RSVPService;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
    "spring.sql.init.mode=never"
})
public class CsvExportAcceptanceTest {
    
    @Autowired 
    private MockMvc mvc;

    @MockBean 
    private EventService eventService;
    
    @MockBean 
    private RSVPService rsvpService;
    
    @MockBean 
    private CurrentUserService currentUserService;

    @BeforeEach
    void setUp() {
        when(currentUserService.getCurrentUserId()).thenReturn(5L);
    }

    private static Event createEvent(long id, String name, long organiserId) {
        Event e = new Event();
        e.setEventId(id);
        e.setName(name);
        e.setCreatedByUserId(organiserId);
        e.setLocation("Test Location");
        e.setDateTime(LocalDateTime.now().plusDays(1));
        return e;
    }

    // ---- Test : CSV Export Returns Valid CSV Content ----
    @Test
    void csvExport_returnsValidCsvContent_withCorrectHeaders() throws Exception {
        long organiserId = 5L;
        long eventId = 10L;
        
        Event event = createEvent(eventId, "Tech Meetup", organiserId);
        when(eventService.findEventsByIdAndOrganiser(eventId, organiserId)).thenReturn(event);
        
        List<Map<String, Object>> attendees = List.of(
            Map.of("name", "Alice Smith", "email", "alice@example.com"),
            Map.of("name", "Bob Johnson", "email", "bob@example.com")
        );
        when(rsvpService.getAttendeesForCsvExport(eventId)).thenReturn(attendees);

        mvc.perform(get("/organiser/events/{eventId}/attendees/export", eventId))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/csv"))
            .andExpect(header().string("Content-Disposition", containsString("attachment")))
            .andExpect(header().string("Content-Disposition", containsString("Tech_Meetup_attendees.csv")))
            .andExpect(content().string(containsString("Name,Email")))
            .andExpect(content().string(containsString("Alice Smith,alice@example.com")))
            .andExpect(content().string(containsString("Bob Johnson,bob@example.com")));
    }


    // ---- Test : test event not owned by organiser ----
    @Test
    void csvExport_whenEventNotOwnedByOrganiser_returns404() throws Exception {
        long organiserId = 5L;
        long eventId = 13L;
        
        // Event doesn't belong to this organiser
        when(eventService.findEventsByIdAndOrganiser(eventId, organiserId)).thenReturn(null);

        mvc.perform(get("/organiser/events/{eventId}/attendees/export", eventId))
            .andExpect(status().isNotFound());
    }

}