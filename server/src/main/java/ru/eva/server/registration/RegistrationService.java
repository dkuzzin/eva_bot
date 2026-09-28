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
    public RegistrationResponse registration(Long eventId, RegistrationRequest request){
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        Long maxUserId = getCurrentMaxUserId();
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
    public void cancelRegistration(Long eventId) {
        Long maxUserId = getCurrentMaxUserId();

        Registration registration =
                registrationRepository.findByEventIdAndMaxUserId(eventId, maxUserId)
                        .orElseThrow(RegistrationNotFoundException::new);
        registrationRepository.delete(registration);
    }

    public RegistrationResponse getRegistration(Long eventId) {
        Long maxUserId = getCurrentMaxUserId();

        Registration registration = registrationRepository.findByEventIdAndMaxUserId(eventId, maxUserId)
                .orElseThrow(() -> new RegistrationNotFoundException());

        return new RegistrationResponse(
                registration.getId(),
                registration.getEventId(),
                registration.getRegisteredAt()
        );
    }


    public EventRegistrationsResponse getEventRegistrations(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        Long maxUserId = getCurrentMaxUserId();
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
    private Long getCurrentMaxUserId() {
        return 1L;
    }
    private void validateEventOwner(Event event, Long maxUserId){
        if (!event.getOwnerMaxUserId().equals(maxUserId)){
            throw new EventAccessDeniedException(event.getId());
        }
    }

}
