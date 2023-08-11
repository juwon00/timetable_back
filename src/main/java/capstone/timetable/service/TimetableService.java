package capstone.timetable.service;

import capstone.timetable.DTO.CreateTimetable;
import capstone.timetable.repository.TimetableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TimetableService {

    private final TimetableRepository timetableRepository;

    public List<CreateTimetable> generateTimetable(String major, int grade, int semester, int majorCredit, int culturalCredit,
                                                   int freeSelectCredit, String hopeSubject, String noTime) {

        // 1. 전공, 학기에 맞는 모든 과목 가져오기
        List<CreateTimetable> result = timetableRepository.findAllSubject(major, semester);

        // 2. 안되는 시간 처리하기

        // 3. 원하는 과목 먼저 넣기

        // 4. 전공 - 교양 - 자선 순으로 시간표 만들기


        return result;
    }
}
