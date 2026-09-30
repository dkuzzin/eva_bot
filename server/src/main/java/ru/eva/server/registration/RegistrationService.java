package ru.eva.server.registration;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import ru.eva.exception.*;
import ru.eva.server.event.EventRepository;
import ru.eva.server.event.model.Event;
import ru.eva.server.event.model.EventStatus;
import ru.eva.server.event.model.FormField;
import ru.eva.server.registration.dto.EventRegistrationsResponse;
import ru.eva.server.registration.dto.RegistrationRequest;
import ru.eva.server.registration.dto.RegistrationResponse;
import ru.eva.server.registration.model.Registration;
import ru.eva.server.registration.model.RegistrationAnswer;
import ru.eva.server.registration.repository.RegistrationAnswerRepository;
import ru.eva.server.registration.repository.RegistrationRepository;
import ru.eva.server.registration.dto.MyRegistrationResponse;

import java.time.OffsetDateTime;
import java.util.*;

@Service
public class RegistrationService {
    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final RegistrationAnswerRepository registrationAnswerRepository;

    public RegistrationService(
            RegistrationRepository registrationRepository,
            RegistrationAnswerRepository registrationAnswerRepository,
            EventRepository eventRepository
    ) {
        this.registrationRepository = registrationRepository;
        this.registrationAnswerRepository = registrationAnswerRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public RegistrationResponse registration(Long eventId, RegistrationRequest request, Long maxUserId){
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        validateRegistration(event, maxUserId);
        validateAnswers(event, request);
        Registration registration = new Registration(
                eventId,
                maxUserId,
                OffsetDateTime.now()
        );
        Registration savedReg = registrationRepository.save(registration);

        for (RegistrationRequest.Answer answer :request.answers()){
            RegistrationAnswer registrationAnswer = new RegistrationAnswer(
                    savedReg.getId(),
                    answer.fieldId(),
                    answer.value()
            );
            registrationAnswerRepository.save(registrationAnswer);
        }
        return new RegistrationResponse(
                savedReg.getId(),
                savedReg.getEventId(),
                savedReg.getRegisteredAt()
        );
    }

    private void validateAnswers(Event event, RegistrationRequest request) {
        Set<Long> answeredFields = new HashSet<>();
        if (request.answers().size() != event.getFormFields().size()) {
            throw new InvalidRegistrationException(
                    "INVALID_REGISTRATION_FORM",
                    "Wrong number of answers"
            );
        }

        for (RegistrationRequest.Answer answer : request.answers()) {
            boolean fieldExists = false;

            if (!answeredFields.add(answer.fieldId())) {
                throw new InvalidRegistrationException(
                        "DUPLICATE_FORM_FIELD",
                        "Duplicate answer for form field"
                );
            }

            for (FormField field : event.getFormFields()) {
                if (field.getId().equals(answer.fieldId())) {
                    fieldExists = true;
                    break;
                }
            }

            if (!fieldExists) {
                throw new InvalidRegistrationException(
                        "FORM_FIELD_DOESNT_EXIST",
                        "Form field does not belong to this event");
            }
        }
    }

    private void validateRegistration(Event event, Long maxUserId){
        if (event.getStatus() != EventStatus.OPEN){
            throw new RegistrationNotAllowedException(
                    "EVENT_NOT_OPEN",
                    "Event status must be OPEN"
            );
        }

        Optional<Registration> existingRegistration =
                registrationRepository.findByEventIdAndMaxUserId(event.getId(), maxUserId);

        if (existingRegistration.isPresent()) {
            throw new RegistrationNotAllowedException(
                    "ALREADY_REGISTERED",
                    "User is already registered"
            );
        }

        if (event.getCapacity() != null) {
            long registeredCount =
                    registrationRepository.countByEventId(event.getId());

            if (registeredCount >= event.getCapacity()) {
                throw new RegistrationNotAllowedException(
                        "EVENT_CAPACITY_FULL",
                        "Event capacity is full"
                );
            }
        }
    }

    @Transactional
    public void cancelRegistration(Long eventId, Long maxUserId) {
        Registration registration =
                registrationRepository.findByEventIdAndMaxUserId(eventId, maxUserId)
                        .orElseThrow(RegistrationNotFoundException::new);
        registrationRepository.delete(registration);
    }

    public RegistrationResponse getRegistration(Long eventId, Long maxUserId) {

        Registration registration = registrationRepository.findByEventIdAndMaxUserId(eventId, maxUserId)
                .orElseThrow(() -> new RegistrationNotFoundException());

        return new RegistrationResponse(
                registration.getId(),
                registration.getEventId(),
                registration.getRegisteredAt()
        );
    }


    public EventRegistrationsResponse getEventRegistrations(Long eventId, Long maxUserId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        validateEventOwner(event, maxUserId);

        List<Registration> registrations =
                registrationRepository.findByEventIdOrderByRegisteredAtAsc(eventId);

        List<Long> registrationIds = new ArrayList<>();

        for (Registration registration : registrations) {
            registrationIds.add(registration.getId());
        }

        List<RegistrationAnswer> answers = new ArrayList<>();

        if (!registrationIds.isEmpty()) {
            answers = registrationAnswerRepository.findByRegistrationIdIn(registrationIds);
        }

        List<EventRegistrationsResponse.FormField> formFieldResponses =
                new ArrayList<>();

        for (FormField field : event.getFormFields()) {
            formFieldResponses.add(
                    new EventRegistrationsResponse.FormField(
                            field.getId(),
                            field.getLabel(),
                            field.getPosition()
                    )
            );
        }

        List<EventRegistrationsResponse.Registration> registrationResponses =
                new ArrayList<>();

        for (Registration registration : registrations) {

            List<EventRegistrationsResponse.Answer> answerResponses =
                    new ArrayList<>();

            for (RegistrationAnswer answer : answers) {
                if (answer.getRegistrationId().equals(registration.getId())) {
                    answerResponses.add(
                            new EventRegistrationsResponse.Answer(
                                    answer.getFormFieldId(),
                                    answer.getValue()
                            )
                    );
                }
            }

            registrationResponses.add(
                    new EventRegistrationsResponse.Registration(
                            registration.getId(),
                            registration.getRegisteredAt(),
                            answerResponses
                    )
            );
        }

        return new EventRegistrationsResponse(
                event.getId(),
                formFieldResponses,
                registrationResponses
        );
    }
    private void validateEventOwner(Event event, Long maxUserId){
        if (!event.getOwnerMaxUserId().equals(maxUserId)){
            throw new EventAccessDeniedException(event.getId());
        }
    }

    public String exportEventRegistrations(Long eventId, Long maxUserId) {
        EventRegistrationsResponse response = getEventRegistrations(eventId, maxUserId);

        StringBuilder csv = new StringBuilder();

        csv.append('\uFEFF');
        csv.append("registrationId;registeredAt");

        for (EventRegistrationsResponse.FormField field : response.formFields()) {
            csv.append(";").append(escapeCsv(field.label()));
        }

        csv.append("\n");

        for (EventRegistrationsResponse.Registration registration : response.registrations()) {
            csv.append(escapeCsv(registration.id().toString()));
            csv.append(";");
            csv.append(escapeCsv(registration.registeredAt().toString()));

            for (EventRegistrationsResponse.FormField field : response.formFields()) {
                String value = "";

                for (EventRegistrationsResponse.Answer answer : registration.answers()) {
                    if (answer.fieldId().equals(field.id())) {
                        value = answer.value();
                        break;
                    }
                }

                csv.append(";").append(escapeCsv(value));
            }

            csv.append("\n");
        }

        return csv.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    public List<MyRegistrationResponse> getMyRegistrations(Long maxUserId) {
        List<Registration> registrations =
                registrationRepository.findByMaxUserId(maxUserId);

        List<Long> eventIds = new ArrayList<>();

        for (Registration registration : registrations) {
            eventIds.add(registration.getEventId());
        }

        List<Event> events = eventRepository.findAllById(eventIds);

        Map<Long, Event> eventsById = new HashMap<>();
        for (Event event : events) {
            eventsById.put(event.getId(), event);
        }

        List<MyRegistrationResponse> responses = new ArrayList<>();
        for (Registration registration : registrations) {
            Event event = eventsById.get(registration.getEventId());

            MyRegistrationResponse.Event eventResponse =
                    new MyRegistrationResponse.Event(
                            event.getId(),
                            event.getTitle(),
                            event.getStartsAt(),
                            event.getEndsAt(),
                            event.getLocation(),
                            event.getStatus()
                    );

            responses.add(
                    new MyRegistrationResponse(
                            registration.getId(),
                            registration.getRegisteredAt(),
                            eventResponse
                    )
            );
        }

        responses.sort(
                Comparator.comparing(response -> response.event().startsAt())
        );

        return responses;
    }
}
