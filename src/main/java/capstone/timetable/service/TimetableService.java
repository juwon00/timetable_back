package capstone.timetable.service;

import capstone.timetable.model.CreateTimetable;
import capstone.timetable.repository.TimetableRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TimetableService {

    private final TimetableRepository timetableRepository;

    public List<CreateTimetable> generateTimetable(String major, int grade, int semester, int majorCredit, int culturalCredit,
                                                   int freeSelectCredit, List<String> hopeSubject, List<String> noTime) throws JsonProcessingException {

        // 1. 전공, 학기에 맞는 모든 과목 가져오기
        List<CreateTimetable> allSubject = timetableRepository.findAllSubject(major, semester);

        // 2. 안되는 시간 처리하기
        List<CreateTimetable> removeNoTimeSubject = noTimeProcess(allSubject, noTime);

        // 3. 원하는 과목 먼저 넣기

        // 4. 전공 - 교양 - 자선 순으로 시간표 만들기


        return removeNoTimeSubject;
    }


    public List<CreateTimetable> noTimeProcess(List<CreateTimetable> createTimetables, List<String> noTime) {
        List<CreateTimetable> timetablesToRemove = new ArrayList<>();

        for (CreateTimetable timetable : createTimetables) {
            String classTime = timetable.getClassTime();
            for (String noTimeSlot : noTime) {
                if (isTimeSlotOverlap(classTime, noTimeSlot)) {
                    timetablesToRemove.add(timetable);
                    break;
                }
            }
        }
        createTimetables.removeAll(timetablesToRemove);

        return createTimetables;
    }

    private boolean isTimeSlotOverlap(String classTime, String noTimeSlot) {

        String[] classTimeParts = classTime.split(",");
        boolean overlap = false;

        for (String part : classTimeParts) {
            String[] timeSlotParts = part.split("/");
            String[] noTimeParts = noTimeSlot.split("/");
            String classDay = timeSlotParts[0];
            String noTimeDay = noTimeParts[0];

            if (timeSlotParts[1].length() >= 2 && noTimeParts[1].length() >= 2) {
                int classTimeStart = Integer.parseInt(timeSlotParts[1].split("-")[0]);
                int classTimeEnd = Integer.parseInt(timeSlotParts[1].split("-")[1]);

                int noTimeStart = Integer.parseInt(noTimeParts[1].split("-")[0]);
                int noTimeEnd = Integer.parseInt(noTimeParts[1].split("-")[1]);

                if (classDay.equals(noTimeDay) && ((classTimeStart <= noTimeStart && noTimeStart <= classTimeEnd) ||
                                                    (classTimeStart <= noTimeEnd && noTimeEnd <= classTimeEnd) ||
                                                    (classTimeStart >= noTimeStart && classTimeEnd <= noTimeEnd))) {
                    overlap = true;
                    break;
                }
            }
            else if (timeSlotParts[1].length() >= 2 && noTimeParts[1].length() == 1) {
                int classTimeStart = Integer.parseInt(timeSlotParts[1].split("-")[0]);
                int classTimeEnd = Integer.parseInt(timeSlotParts[1].split("-")[1]);

                int noTimeHour = Integer.parseInt(noTimeParts[1]);

                if (classDay.equals(noTimeDay) && (classTimeStart <= noTimeHour && noTimeHour <= classTimeEnd)) {
                    overlap = true;
                    break;
                }
            }
            else if (timeSlotParts[1].length() == 1 && noTimeParts[1].length() >= 2) {
                int classTimeHour = Integer.parseInt(timeSlotParts[1]);

                int noTimeStart = Integer.parseInt(noTimeParts[1].split("-")[0]);
                int noTimeEnd = Integer.parseInt(noTimeParts[1].split("-")[1]);

                if (classDay.equals(noTimeDay) && (noTimeStart <= classTimeHour && classTimeHour <= noTimeEnd)) {
                    overlap = true;
                    break;
                }
            }
            else if (timeSlotParts[1].length() == 1 && noTimeParts[1].length() == 1) {
                int classTimeHour = Integer.parseInt(timeSlotParts[1]);

                int noTimeHour = Integer.parseInt(noTimeParts[1]);

                if (classDay.equals(noTimeDay) && classTimeHour == noTimeHour) {
                    overlap = true;
                    break;
                }
            }
        }

        return overlap;
    }




}
