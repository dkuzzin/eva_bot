package ru.eva.server.registration;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.eva.server.registration.model.RegistrationAnswer;

public interface RegistrationAnswerRepository
        extends JpaRepository<RegistrationAnswer, Long> {
}