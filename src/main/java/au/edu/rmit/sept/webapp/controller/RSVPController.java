package au.edu.rmit.sept.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import au.edu.rmit.sept.webapp.service.RSVPService;

@Controller
@RequestMapping("/rsvp")
public class RSVPController {

    private final RSVPService rsvpService;

    public RSVPController(RSVPService rsvpService) {
        this.rsvpService = rsvpService;
    }

    @PostMapping("/{userId}/event/{eventId}")
    public String rsvp(@PathVariable Long userId, @PathVariable Long eventId) {
        try {
            rsvpService.toggleRSVP(userId, eventId);
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            return "redirect:/";
        }
    }
}

