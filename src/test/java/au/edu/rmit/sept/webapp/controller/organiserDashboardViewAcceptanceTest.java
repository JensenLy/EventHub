package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.RSVP;
import au.edu.rmit.sept.webapp.repository.RsvpRepository;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.RSVPService;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc (addFilters = false)
@TestPropertySource(properties = {
    "spring.sql.init.mode=never"
})
public class organiserDashboardViewAcceptanceTest {
  @Autowired MockMvc mvc;

  @MockBean EventService eventService;
  @MockBean RSVPService rsvpService;

   // Helper to create events
    private static Event ev(long id, String name, String location, LocalDateTime dt, long organiserId) {
    Event e = new Event();
    e.setEventId(id);
    e.setName(name);
    e.setLocation(location);
    e.setDateTime(dt);
    e.setCreatedByUserId(organiserId);
    return e;
    }

  // Helper to create RSVPs
    private static RsvpRepository.AttendeeRow attendee(String name, String email, String status) {
        var row = Mockito.mock(RsvpRepository.AttendeeRow.class);
        when(row.getName()).thenReturn(name);
        when(row.getEmail()).thenReturn(email);
        when(row.getStatus()).thenReturn(status);
        return row;
    }

    // ---- Scenario 1: Dashboard Event List of the Organizer ----------
    @Test
    void dashboard_showsOrganiserEvents_sorted() throws Exception {
      // hard-coded organiserId
      long organiserId = 5L;
      var dt1 = LocalDateTime.now().plusDays(1).withSecond(0).withNano(0);
      var dt2 = LocalDateTime.now().plusDays(3).withSecond(0).withNano(0);

      var e1 = ev(1L, "AI Summit", "Campus A", dt1, organiserId);
      var e2 = ev(2L, "Tech Talk", "Hall 3",   dt2, organiserId);

      // Should return sorted list of the events created by this organiser
      when(eventService.getEventsByOrganiser(organiserId)).thenReturn(List.of(e1, e2));

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm", Locale.ENGLISH);

        mvc.perform(get("/organiser/dashboard"))
            .andExpect(status().isOk())
            .andExpect(view().name("organiserDashboard"))
            // Titles visible
            .andExpect(content().string(containsString("AI Summit")))
            .andExpect(content().string(containsString("Tech Talk")))
            .andExpect(content().string(containsString(formatter.format(dt1))))
            .andExpect(content().string(containsString("Campus A")))
            .andExpect(content().string(containsString(formatter.format(dt2))))
            .andExpect(content().string(containsString("Hall 3")));
    }
}
