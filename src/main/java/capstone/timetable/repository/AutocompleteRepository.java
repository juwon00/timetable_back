package capstone.timetable.repository;

import capstone.timetable.domain.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutocompleteRepository extends JpaRepository<Timetable, Long> {
    List<Timetable> findByMajorAndSemesterAndSubjectContainingOrMajorAndSemesterAndProfessorContaining(
            String major1, int semester1, String query1,
            String major2, int semester2, String query2
    );

    List<Timetable> findByMajorContaining(String majorQuery);
}

