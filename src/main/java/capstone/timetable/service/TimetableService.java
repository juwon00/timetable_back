package capstone.timetable.service;

import capstone.timetable.model.CreateTimetable;
import capstone.timetable.repository.TimetableRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TimetableService {

    private final TimetableRepository timetableRepository;

    public List<CreateTimetable> generateTimetable(String major, int grade, int semester, int majorCredit, int culturalCredit,
                                                   int freeSelectCredit, List<String> hopeSubject, List<String> noTime) throws JsonProcessingException {

        // 1. 전공, 학기에 맞는 모든 과목 가져오기
        List<CreateTimetable> allMajorSubject = timetableRepository.findAllSubject(major, semester);

        // 2. 안되는 시간 처리하기
        List<CreateTimetable> removeNoTimeSubject = noTimeProcess(allMajorSubject, noTime);

        // 3. 원하는 과목 먼저 넣기
        List<CreateTimetable[][]> matchingSubjects = hopeSubjectProcess(hopeSubject, removeNoTimeSubject);

        for (CreateTimetable[][] timetable : matchingSubjects) {
            for (int hour = 0; hour < 13; hour++) {
                for (int day = 0; day < 7; day++) {
                    if (timetable[hour][day] != null) {
                        System.out.printf("%-30s", timetable[hour][day].getSubject());
                    } else {
                        System.out.printf("%-30s", " - ");
                    }
                }
                System.out.println();
            }
            System.out.println();
            System.out.println();
            System.out.println();
        }

        // 4. 전공 - 교양 - 자선 순으로 시간표 만들기


        return removeNoTimeSubject;
    }




    /**
     *  2. 안되는 시간 처리하기
     */
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



    /**
     * 3. 원하는 과목 먼저 넣기
     */
    public List<CreateTimetable[][]> hopeSubjectProcess(List<String> hopeSubject, List<CreateTimetable> removeNoTimeSubject) throws JsonProcessingException {


        List<CreateTimetable> matchingSubjects = findMatchingSubjects(hopeSubject, removeNoTimeSubject);

        List<List<CreateTimetable>> combinations = generateCombinations(matchingSubjects);
        combinations.sort((list1, list2) -> Integer.compare(list2.size(), list1.size()));

        List<CreateTimetable[][]> timetableList = generateTimetableList(combinations);

        return timetableList;
    }

    private List<CreateTimetable> findMatchingSubjects(List<String> hopeSubject, List<CreateTimetable> removeNoTimeSubject) {
        List<CreateTimetable> matchingSubjects = new ArrayList<>();

        for (CreateTimetable createTimetable : removeNoTimeSubject) {
            String subject = createTimetable.getSubject();
            String professor = createTimetable.getProfessor();

            for (String hopeSubjectList : hopeSubject) {
                String[] hopeSubjectPart = hopeSubjectList.split("/");

                String hopeSubjectName = hopeSubjectPart[0];
                String hopeProfessorName = hopeSubjectPart[1];

                if (subject.equals(hopeSubjectName) && professor.equals(hopeProfessorName)) {
                    matchingSubjects.add(createTimetable);
                    break;
                }
            }
        }
        return matchingSubjects;
    }


    public static List<List<CreateTimetable>> generateCombinations(List<CreateTimetable> matchingSubjects) {
        List<List<CreateTimetable>> combinations = new ArrayList<>();

        generateCombination(matchingSubjects, 0, new ArrayList<>(), combinations);

        return combinations;
    }

    private static void generateCombination(List<CreateTimetable> matchingSubjects, int currentIndex,
                                            List<CreateTimetable> currentCombination,
                                            List<List<CreateTimetable>> combinations) {
        if (currentIndex == matchingSubjects.size()) {
            combinations.add(new ArrayList<>(currentCombination));
            return;
        }

        CreateTimetable currentSubject = matchingSubjects.get(currentIndex);

        // Check if the current subject conflicts with any subject in the current combination
        boolean hasConflict = false;
        for (CreateTimetable subjectInCombination : currentCombination) {
            if (subjectInCombination.getSubject().equals(currentSubject.getSubject())) {
                hasConflict = true;
                break;
            }
        }

        // If no conflict, add the current subject to the combination
        if (!hasConflict) {
            currentCombination.add(currentSubject);
            generateCombination(matchingSubjects, currentIndex + 1, currentCombination, combinations);
            currentCombination.remove(currentCombination.size() - 1);
        }

        // Move to the next subject
        generateCombination(matchingSubjects, currentIndex + 1, currentCombination, combinations);
    }


    private int getDayIndex(String day) {
        String[] daysOfWeek = {"월", "화", "수", "목", "금", "토", "일"};
        for (int i = 0; i < daysOfWeek.length; i++) {
            if (day.equals(daysOfWeek[i])) {
                return i;
            }
        }
        return -1; // Not found
    }

    public List<CreateTimetable[][]> generateTimetableList(List<List<CreateTimetable>> combinations) {
        List<CreateTimetable[][]> timetableList = new ArrayList<>();

        for (List<CreateTimetable> combination : combinations) {
            CreateTimetable[][] timetable = new CreateTimetable[13][7];

            for (CreateTimetable timetableEntry : combination) {
                String classTime = timetableEntry.getClassTime();
                String[] classTimeParts = classTime.split(",");

                for (String part : classTimeParts) {
                    String[] partDivide = part.split("/");
                    String day = partDivide[0];

                    int dayIdx = getDayIndex(day);
                    int startHour = Integer.parseInt(partDivide[1].split("-")[0]);
                    int endHour = Integer.parseInt(partDivide[1].split("-")[1]);

                    for (int i = startHour - 1; i < endHour; i++) {
                        timetable[i][dayIdx] = timetableEntry;
                    }
                }
            }
//            for (CreateTimetable[] createTimetables : timetable) {
//                System.out.println(Arrays.toString(createTimetables));
//            }
//            System.out.println();
            timetableList.add(timetable);
        }

        return timetableList;
    }




}
