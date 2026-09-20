package ru.eva.server.event;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.eva.server.event.dto.CreateEventRequest;
import ru.eva.server.event.dto.EventResponse;
import ru.eva.server.registration.RegistrationService;
import ru.eva.server.registration.dto.RegistrationRequest;
import ru.eva.server.registration.dto.RegistrationResponse;

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
    public EventResponse create(@Valid @RequestBody CreateEventRequest request) {
        return eventService.create(request);
    }

    @GetMapping("/{id}")
    public EventResponse getEvent(@PathVariable Long id){
        return eventService.get(id);
    }

    @PostMapping("/{eventId}/registrations")
    public RegistrationResponse registration(
            @PathVariable Long eventId,
            @Valid @RequestBody RegistrationRequest request)
    {
        return registrationService.registration(eventId, request);
    }
}
