package capstone.timetable.repository;

import capstone.timetable.domain.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutocompleteRepository extends JpaRepository<Timetable, Long> {
    List<Timetable> findBySubjectContainingOrProfessorContaining(String subjectQuery, String professorQuery);

    List<Timetable> findByMajorContaining(String majorQuery);
}

