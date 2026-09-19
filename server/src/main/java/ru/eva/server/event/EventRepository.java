package ru.eva.server.event;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.eva.server.event.model.Event;

public interface EventRepository extends JpaRepository<Event, Long>{
}
