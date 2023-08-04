package capstone.timetable.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

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




//-- auto-generated definition
//        create table Timetable
//        (
//        timeTableIdx int auto_increment comment '인덱스 번호'
//        primary key,
//        major        varchar(1000) not null comment '전공',
//        grade        int           not null comment '학년',
//        semester     int           not null comment '학기',
//        majorRequire varchar(100)  null,
//        credit       int           null comment '학점',
//        subject      varchar(100)  null comment '과목명',
//        professor    varchar(100)  null comment '담당교수명',
//        classTime    varchar(100)  null comment '수업시간',
//        classRoom    varchar(100)  null comment '수업장소'
//        );
