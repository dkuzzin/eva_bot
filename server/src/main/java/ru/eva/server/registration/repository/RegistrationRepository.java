package ru.eva.server.registration.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import ru.eva.server.registration.model.Registration;

import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    Optional<Registration> findByEventIdAndMaxUserId(
            Long eventId,
            Long maxUserId
    );

    long countByEventId(Long eventId);
}