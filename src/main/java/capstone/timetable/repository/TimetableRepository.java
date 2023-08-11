package capstone.timetable.repository;

import capstone.timetable.DTO.CreateTimetable;
import capstone.timetable.domain.Timetable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class TimetableRepository {

    @PersistenceContext
    private EntityManager em;

    public List<CreateTimetable> findAllSubject(String major, int semester) {

        List<CreateTimetable> createTimetableList = new ArrayList<>();

        List<Timetable> timetableList = em.createQuery("select t from Timetable t " +
                        "where t.major =:major and t.semester =:semester", Timetable.class)
                .setParameter("major", major)
                .setParameter("semester", semester)
                .getResultList();

        for (Timetable timetable : timetableList) {
            CreateTimetable createTimetable = new CreateTimetable();
            createTimetable.setSubject(timetable.getSubject());
            createTimetable.setClassRoom(timetable.getClassRoom());
            createTimetable.setClassTime(timetable.getClassTime());
            createTimetable.setGrade(timetable.getGrade());
            createTimetableList.add(createTimetable);
        }

        return createTimetableList;
    }
}

