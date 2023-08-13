package capstone.timetable.model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateTimetable {
    private String subject;
    private String classRoom;
    private String classTime;
    private int grade;
}
