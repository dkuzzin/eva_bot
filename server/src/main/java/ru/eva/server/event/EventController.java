package ru.eva.server.event;

import org.springframework.web.bind.annotation.*;
import ru.eva.server.event.dto.CreateEventRequest;
import ru.eva.server.event.dto.EventResponse;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService service;
    public EventController(EventService service){
        this.service = service;
    }

    @PostMapping
    public EventResponse create(@RequestBody CreateEventRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    public EventResponse getEvent(@PathVariable Long id){
        return service.get(id);
    }
}
