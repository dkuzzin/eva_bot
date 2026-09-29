package ru.eva.server.event;

import org.springframework.stereotype.Service;
import ru.eva.exception.EventNotFoundException;
import ru.eva.exception.InvalidEventArgumentsException;
import ru.eva.server.event.dto.CreateEventRequest;
import ru.eva.server.event.dto.EventResponse;
import ru.eva.server.event.dto.FormFieldRequest;
import ru.eva.server.event.model.Event;
import ru.eva.server.event.model.EventStatus;
import ru.eva.server.event.model.FormField;
import org.springframework.transaction.annotation.Transactional;
import ru.eva.exception.EventAccessDeniedException;
import ru.eva.exception.EventAlreadyCancelledException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EventService {
    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository){
        this.eventRepository = eventRepository;
    }

    private void validateBusinessRules(CreateEventRequest request){
        if (request.endsAt() != null && !request.endsAt().isAfter(request.startsAt())){
            throw new InvalidEventArgumentsException(
                    "INVALID_EVENT_TIME_RANGE",
                    "endsAt must be after startsAt"
            );
        }
    }
    public EventResponse create(CreateEventRequest request, Long maxUserId){
        validateBusinessRules(request);

        OffsetDateTime now = OffsetDateTime.now();
        Event event = new Event(
                maxUserId,
                request.title(),
                request.description(),
                request.startsAt(),
                request.endsAt(),
                request.location(),
                request.capacity(),
                EventStatus.OPEN,
                now,
                now
        );
        for (int i = 0; i < request.formFields().size(); i++){
            FormFieldRequest fieldRequest = request.formFields().get(i);
            event.addFormField(fieldRequest.label(), i);
        }
        Event savedEvent = eventRepository.save(event);
        return eventToResponse(savedEvent);
    }

    public EventResponse get(Long id){
        Event event = eventRepository.findById(id).orElseThrow(()-> new EventNotFoundException(id));
        return eventToResponse(event);
    }

    private EventResponse eventToResponse(Event event){
        List<EventResponse.FormField> formFields = new ArrayList<>();

        for (FormField field : event.getFormFields()) {
            EventResponse.FormField responseField = new EventResponse.FormField(
                    field.getId(),
                    field.getLabel(),
                    field.getPosition());
            formFields.add(responseField);
        }

        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getStartsAt(),
                event.getEndsAt(),
                event.getLocation(),
                event.getCapacity(),
                event.getStatus(),
                formFields
        );
    }

    public List<EventResponse> getMyEvents(Long maxUserId) {
        List<Event> events = eventRepository.findByOwnerMaxUserIdOrderByStartsAtAsc(maxUserId);
        List<EventResponse> responses = new ArrayList<>();

        for (Event event : events){
            responses.add(eventToResponse(event));
        }
        return responses;
    }

    @Transactional
    public EventResponse cancel(Long eventId, Long maxUserId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (!event.getOwnerMaxUserId().equals(maxUserId)) {
            throw new EventAccessDeniedException(eventId);
        }

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new EventAlreadyCancelledException(eventId);
        }

        event.cancel(OffsetDateTime.now());

        return eventToResponse(event);
    }
}
