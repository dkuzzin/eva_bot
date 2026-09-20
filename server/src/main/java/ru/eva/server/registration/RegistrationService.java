package ru.eva.server.registration;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import ru.eva.exception.EventNotFoundException;
import ru.eva.exception.RegistrationNotAllowedException;
import ru.eva.server.event.EventRepository;
import ru.eva.server.event.model.Event;
import ru.eva.server.event.model.EventStatus;
import ru.eva.server.event.model.FormField;
import ru.eva.server.registration.dto.RegistrationRequest;
import ru.eva.server.registration.dto.RegistrationResponse;
import ru.eva.server.registration.model.Registration;
import ru.eva.server.registration.model.RegistrationAnswer;
import ru.eva.server.registration.repository.RegistrationAnswerRepository;
import ru.eva.server.registration.repository.RegistrationRepository;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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

        Long maxUserId = 1L;
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
            throw new RegistrationNotAllowedException("Wrong number of answers");
        }

        for (RegistrationRequest.Answer answer : request.answers()) {
            boolean fieldExists = false;

            if (!answeredFields.add(answer.fieldId())) {
                throw new RegistrationNotAllowedException("Duplicate answer for form field");
            }

            for (FormField field : event.getFormFields()) {
                if (field.getId().equals(answer.fieldId())) {
                    fieldExists = true;
                    break;
                }
            }

            if (!fieldExists) {
                throw new RegistrationNotAllowedException("Form field does not belong to this event");
            }
        }
    }

    private void validateRegistration(Event event, Long maxUserId){
        if (event.getStatus() != EventStatus.OPEN){
            throw new RegistrationNotAllowedException("Event status must be OPEN");
        }

        Optional<Registration> existingRegistration =
                registrationRepository.findByEventIdAndMaxUserId(event.getId(), maxUserId);

        if (existingRegistration.isPresent()) {
            throw new RegistrationNotAllowedException("User is already registered");
        }
        //TODO fix maxuser id and add validation
        if (event.getCapacity() != null) {
            long registeredCount =
                    registrationRepository.countByEventId(event.getId());

            if (registeredCount >= event.getCapacity()) {
                throw new RegistrationNotAllowedException("Event capacity is full");
            }
        }
    }
}
