package ru.eva.server.event;

import jakarta.persistence.*;

@Entity
@Table(name = "form_fields")
public class FormField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private Integer position;

    protected FormField() {
    }

    public FormField(Event event, String label, Integer position) {
        this.event = event;
        this.label = label;
        this.position = position;
    }

    public Long getId() {
        return id;
    }

    public Event getEvent() {
        return event;
    }

    public String getLabel() {
        return label;
    }

    public Integer getPosition() {
        return position;
    }
}