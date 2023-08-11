package capstone.timetable.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class TimetableRepository {

    @PersistenceContext
    private EntityManager em;



}
