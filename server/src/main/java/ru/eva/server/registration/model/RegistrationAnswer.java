package ru.eva.server.registration.model;

import jakarta.persistence.*;

@Entity
@Table(name = "registrations_answer")
public class RegistrationAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "registration_id", nullable = false)
    private Long registrationId;

    @Column(name = "form_field_id", nullable = false)
    private Long formFieldId;

    @Column(name = "value", nullable = false)
    private String value;

    protected RegistrationAnswer() {
    }

    public RegistrationAnswer(
            Long registrationId,
            Long formFieldId,
            String value
    ) {
        this.registrationId = registrationId;
        this.formFieldId = formFieldId;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public Long getRegistrationId() {
        return registrationId;
    }

    public Long getFormFieldId() {
        return formFieldId;
    }

    public String getValue() {
        return value;
    }
}
