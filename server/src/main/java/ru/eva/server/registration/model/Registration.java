package ru.eva.server.registration.model;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "registrations")
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "max_user_id", nullable = false)
    private Long maxUserId;

    @Column(name = "registered_at", nullable = false)
    private OffsetDateTime registeredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status;

    protected Registration() {
    }

    public Registration(
            Long eventId,
            Long maxUserId,
            OffsetDateTime registeredAt,
            RegistrationStatus status
    ) {
        this.eventId = eventId;
        this.maxUserId = maxUserId;
        this.registeredAt = registeredAt;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Long getEventId() {
        return eventId;
    }

    public Long getMaxUserId() {
        return maxUserId;
    }

    public OffsetDateTime getRegisteredAt() {
        return registeredAt;
    }

    public RegistrationStatus getStatus() {
        return status;
    }
}
