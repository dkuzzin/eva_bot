package ru.eva.server.event.model;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_max_user_id", nullable = false)
    private Long ownerMaxUserId;


    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;


    @Column(name = "starts_at", nullable = false)
    private OffsetDateTime startsAt;

    @Column(name = "ends_at")
    private OffsetDateTime endsAt;

    private String location;

    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;


    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(
            mappedBy = "event",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<FormField> formFields = new ArrayList<>();

    public Event(){

    }

    public Event(
            Long ownerMaxUserId,
            String title,
            String description,
            OffsetDateTime startsAt,
            OffsetDateTime endsAt,
            String location,
            Integer capacity,
            EventStatus status,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.ownerMaxUserId = ownerMaxUserId;
        this.title = title;
        this.description = description;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.location = location;
        this.capacity = capacity;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void addFormField(String label, int position) {
        FormField formField = new FormField(this, label, position);
        formFields.add(formField);
    }

    public Long getId() {
        return id;
    }

    public Long getOwnerMaxUserId() {
        return ownerMaxUserId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public OffsetDateTime getStartsAt() {
        return startsAt;
    }

    public OffsetDateTime getEndsAt() {
        return endsAt;
    }

    public String getLocation() {
        return location;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public EventStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<FormField> getFormFields() {
        return formFields;
    }
}