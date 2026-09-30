package ru.eva.server.event;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.eva.server.event.dto.CreateEventRequest;
import ru.eva.server.event.dto.EventResponse;
import ru.eva.server.registration.RegistrationService;
import ru.eva.server.registration.dto.EventRegistrationsResponse;
import ru.eva.server.registration.dto.RegistrationRequest;
import ru.eva.server.registration.dto.RegistrationResponse;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final RegistrationService registrationService;


    public EventController(EventService eventService, RegistrationService registrationService){
        this.eventService = eventService;
        this.registrationService = registrationService;
    }

    @PostMapping
    public EventResponse create(
            @Valid @RequestBody CreateEventRequest request,
            @RequestHeader("X-Max-User-Id") Long maxUserId
    ) {
        return eventService.create(request, maxUserId);
    }

    @GetMapping("/{id}")
    public EventResponse getEvent(@PathVariable Long id){
        return eventService.get(id);
    }

    @GetMapping
    public List<EventResponse> getMyEvents(@RequestHeader("X-Max-User-Id") Long maxUserId) {
        return eventService.getMyEvents(maxUserId);
    }

    @PostMapping("/{eventId}/registrations")
    public RegistrationResponse registration(
            @PathVariable Long eventId,
            @Valid @RequestBody RegistrationRequest request,
            @RequestHeader("X-Max-User-Id") Long maxUserId
    ) {
        return registrationService.registration(eventId, request, maxUserId);
    }


    @DeleteMapping("/{eventId}/registrations")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelRegistration(
            @PathVariable Long eventId,
            @RequestHeader("X-Max-User-Id") Long maxUserId
    ) {
        registrationService.cancelRegistration(eventId, maxUserId);
    }

    @GetMapping("/{eventId}/registrations/me")
    public RegistrationResponse getRegistration(
            @PathVariable Long eventId,
            @RequestHeader("X-Max-User-Id") Long maxUserId
    ) {
        return registrationService.getRegistration(eventId, maxUserId);
    }

    @GetMapping("/{eventId}/registrations")
    public EventRegistrationsResponse getEventRegistrations(
            @PathVariable Long eventId,
            @RequestHeader("X-Max-User-Id") Long maxUserId
    ) {
        return registrationService.getEventRegistrations(eventId, maxUserId);
    }

    @GetMapping(
            value = "/{eventId}/registrations/export",
            produces = "text/csv;charset=UTF-8"
    )
    public ResponseEntity<String> exportRegistrations(
            @PathVariable Long eventId,
            @RequestHeader("X-Max-User-Id") Long maxUserId
    ) {
        String csv = registrationService.exportEventRegistrations(eventId, maxUserId);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"event-" + eventId + "-registrations.csv\""
                ).body(csv);
    }

    @PatchMapping("/{eventId}/cancel")
    public EventResponse cancelEvent(
            @PathVariable Long eventId,
            @RequestHeader("X-Max-User-Id") Long maxUserId
    ) {
        return eventService.cancel(eventId, maxUserId);
    }
}
