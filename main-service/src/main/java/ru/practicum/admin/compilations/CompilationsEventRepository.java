package ru.practicum.admin.compilations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.admin.compilations.model.CompilationEvent;

import java.util.List;

@Repository
public interface CompilationsEventRepository extends JpaRepository<CompilationEvent, Long> {

    List<CompilationEvent> findAllByCompilationId(Long id);

    void deleteAllByCompilationId(Long id);
}
