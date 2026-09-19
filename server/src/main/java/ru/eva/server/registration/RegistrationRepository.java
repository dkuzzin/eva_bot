package ru.eva.server.registration;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.eva.server.registration.model.Registration;

public interface RegistrationRepository
        extends JpaRepository<Registration, Long> {
}