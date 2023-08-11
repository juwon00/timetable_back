package capstone.timetable.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;

@Getter
@Entity
public class Timetable {

    @Id // primary key
    @GeneratedValue // 자동 생성 전략
    @Column(name = "timeTable_id")
    private Long id; // 인덱스 번호

    private String major; // 전공
    private int grade; // 학년
    private int semester; // 학기

    private String majorRequire; // 전필, 전선 등등

    private int credit; // 학점

    private String subject; // 과목명
    private String professor; // 담당교수명
    private String classTime; // 수업시간
    private String classRoom; // 수업장소

}
