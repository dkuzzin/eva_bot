package ru.eva.server.event;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.eva.server.event.dto.CreateEventRequest;
import ru.eva.server.event.dto.EventResponse;
import ru.eva.server.registration.dto.RegistrationResponse;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService service;
    public EventController(EventService service){
        this.service = service;
    }

    @PostMapping
    public EventResponse create(@Valid @RequestBody CreateEventRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    public EventResponse getEvent(@PathVariable Long id){
        return service.get(id);
    }

    @PostMapping("/{id}/registrations")
    public RegistrationResponse registration(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateEventRequest request)
    {
        return new RegistrationResponse();
    }
}
