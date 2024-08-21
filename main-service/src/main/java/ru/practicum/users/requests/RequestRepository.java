package ru.practicum.users.requests;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.admin.users.model.User;
import ru.practicum.users.events.model.Event;
import ru.practicum.users.requests.model.Request;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByUser(User user);

    List<Request> findAllByEvent(Event event);

    @Query("select r from Request as r " +
            "where r.user.id = ?1 and r.event.id = ?2")
    Optional<Request> findByRequesterAndEvent(Long requesterId, Long eventId);
}
