package ru.eva.server.event;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}
