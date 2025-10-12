package au.edu.rmit.sept.webapp.controller;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.repository.RsvpRepository;
import au.edu.rmit.sept.webapp.service.CurrentUserService;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.RSVPService;

@Controller
@RequestMapping("/organiser")
public class OrganiserController {
  
  private final EventService eventService;
  private final RSVPService rsvpService;

  private final CurrentUserService currentUserService;

  public OrganiserController(EventService eventService, RSVPService rsvpService , CurrentUserService currentUserService) {
    this.eventService = eventService;
    this.rsvpService = rsvpService;
    this.currentUserService = currentUserService;
  }

/**
   * GET /organiser/dashboard
   *
   * Loads the organiser dashboard with the list of upcoming events that belong
   * to the currently logged-in organiser (determined via CurrentUserService).
   *
   * Model attributes:
   * - "events": List<Event> the organiser's upcoming events
   *
   * @param model Spring MVC model for passing data to the view
   * @return "organiserDashboard" Thymeleaf template
   */

  @GetMapping("/dashboard")
  public String dashboard (Model model) {
    Long organiserId = currentUserService.getCurrentUserId();

    List<Event> events = eventService.getEventsByOrganiser(organiserId);
    model.addAttribute("events", events);
    return "organiserDashboard";
  }

  /**
   * GET /organiser/events/{eventId}/rsvps
   *
   * Displays the RSVP list for a specific event, but only if the event
   * is owned by the currently logged-in organiser. If not found (or not owned),
   * shows an error on the dashboard and reloads the organiser's events.
   *
   * Model attributes on success:
   * - "event": Event details
   * - "attendees": List<RsvpRepository.AttendeeRow> RSVP rows for the event
   *
   * Model attributes on failure:
   * - "error": String message
   * - "events": List<Event> organiser's upcoming events (for dashboard)
   *
   * @param eventId the ID of the event to view RSVPs for
   * @param model   Spring MVC model for passing data to the view
   * @return "organiserRsvps" on success, "organiserDashboard" if not found/unauthorized
   */
  @GetMapping("/events/{eventId}/rsvps")
  public String eventRsvps(@PathVariable Long eventId, Model model) {
    Long organiserId = currentUserService.getCurrentUserId();

    
    Event event = eventService.findEventsByIdAndOrganiser(eventId, organiserId);
    if (event == null) {
      model.addAttribute("error", "Event not found or not hosted by you.");
      // Reload the dashboard list
      model.addAttribute("events", eventService.getEventsByOrganiser(organiserId));
      return "organiserDashboard";
    }

    List<RsvpRepository.AttendeeRow> attendees = rsvpService.getAllAttendeesForEvent(eventId);
    model.addAttribute("event", event);
    model.addAttribute("attendees", attendees);
    return "organiserRsvps";
  }

  @GetMapping("/events/{eventId}/attendees/export")
  public ResponseEntity<byte[]> exportAttendeesToCsv(@PathVariable Long eventId) {
    System.out.println("Export called for eventId: " + eventId);
    
    // Get current user
    long organiserId = currentUserService.getCurrentUserId();
    
    // Verify event belongs to this organiser
    Event event = eventService.findEventsByIdAndOrganiser(eventId, organiserId);
    if (event == null) {
        System.out.println("Event not found or not authorized");
        return ResponseEntity.notFound().build();
    }
    
    // Get attendees
    List<Map<String, Object>> attendees = rsvpService.getAttendeesForCsvExport(eventId);
    System.out.println("Found " + attendees.size() + " attendees");
    
    try {
        // Create CSV content
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8));
        
        // Write CSV header
        writer.println("Name,Email");
        
        // Write attendee data
        for (Map<String, Object> attendee : attendees) {
            String name = escapeCsv((String) attendee.get("name"));
            String email = escapeCsv((String) attendee.get("email"));
            writer.println(name + "," + email);
        }
        
        writer.flush();
        writer.close();
        
        // Prepare response
        byte[] csvBytes = baos.toByteArray();
        
        // Create sanitized filename from event name
        String filename = sanitizeFilename(event.getName()) + "_attendees.csv";
        System.out.println("Generating CSV file: " + filename);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        
        return ResponseEntity
            .ok()
            .headers(headers)
            .body(csvBytes);
            
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.internalServerError().build();
    }
  }

  
private String escapeCsv(String value) {
    if (value == null) {
        return "";
    }
    // If value contains comma, quote, or newline, wrap in quotes and escape quotes
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
}


// Helper method to create safe filename
private String sanitizeFilename(String filename) {
    if (filename == null) {
        return "event";
    }
    // Remove or replace invalid filename characters
    return filename.replaceAll("[^a-zA-Z0-9-_\\s]", "")
                   .replaceAll("\\s+", "_")
                   .substring(0, Math.min(filename.length(), 50));
}


}
