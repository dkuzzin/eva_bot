package ru.eva.server.event;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.eva.server.event.model.Event;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>{
    List<Event> findByOwnerMaxUserIdOrderByStartsAtAsc(Long ownerMaxUserId);
}
