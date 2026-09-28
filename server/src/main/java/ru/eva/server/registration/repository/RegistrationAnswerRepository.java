package ru.eva.server.registration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.eva.server.registration.model.RegistrationAnswer;

import java.util.List;

public interface RegistrationAnswerRepository
        extends JpaRepository<RegistrationAnswer, Long> {
    List<RegistrationAnswer> findByRegistrationIdIn(List<Long> registrationIds);
}