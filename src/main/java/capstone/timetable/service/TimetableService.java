package capstone.timetable.service;

import capstone.timetable.model.CreateTimetable;
import capstone.timetable.repository.TimetableRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TimetableService {

    private final TimetableRepository timetableRepository;

    public List<List<CreateTimetable>> generateTimetable(String major, int grade, int semester, int majorCredit, int culturalCredit,
                                                         int freeSelectCredit, List<String> hopeSubject, List<String> removeSubject,
                                                         List<String> noTime) throws JsonProcessingException {

        // 1. 전공, 학기에 맞는 모든 과목 가져오기
        List<CreateTimetable> allMajorSubject = timetableRepository.findAllSubject(major, semester);

        // 2. 안되는 시간 처리하기
        List<CreateTimetable> removeNoTimeSubject = noTimeProcess(allMajorSubject, noTime);

        // 2-1 제거할 과목 처리하기
        List<CreateTimetable> removeWantSubject = removeWantProcess(removeNoTimeSubject, removeSubject);

        // 3. 원하는 과목 먼저 넣기
        List<CreateTimetable[][]> matchingSubjects = hopeSubjectProcess(hopeSubject, removeWantSubject);

        // 위의 원하는 과목 중복없이 추출한 리스트
        List<List<CreateTimetable>> transformedMatchingSubjects = transformedMatchingSubjectsProcess(matchingSubjects);

        for (CreateTimetable[][] timetable : matchingSubjects) {
            for (int hour = 0; hour < 13; hour++) {
                for (int day = 0; day < 7; day++) {
                    if (timetable[hour][day] != null) {
                        System.out.printf("%-10s", timetable[hour][day].getSubject());
                    } else {
                        System.out.printf("%-10s", " - ");
                    }
                }
                System.out.println();
            }
            System.out.println();
            System.out.println();
            System.out.println();
        }


        // 4. 전공 - 교양 - 자선 순으로 시간표 만들기
        int listenMajorCredit = listenMajorCreditProcess(hopeSubject, allMajorSubject);
        List<List<List<CreateTimetable>>> majorInTimetable = majorSubjectProcess(matchingSubjects, majorCredit, removeWantSubject, listenMajorCredit);

        // 원하는 전공 + 임의로 선택된 전공
        List<List<CreateTimetable>> combinedTimetables = new ArrayList<>();
        for (List<CreateTimetable> matchingBlock : transformedMatchingSubjects) {
            for (List<List<CreateTimetable>> majorBlock : majorInTimetable) {
                for (List<CreateTimetable> majorIn : majorBlock) {
                    List<CreateTimetable> combinedBlock = new ArrayList<>(matchingBlock);
                    combinedBlock.addAll(majorIn);
                    combinedTimetables.add(combinedBlock);
                }

            }
        }
//        for (List<CreateTimetable> combinedBlock : combinedTimetables) {
//            System.out.println("group");
//            for (CreateTimetable x : combinedBlock) {
//                System.out.println(x.getSubject() + " " + x.getClassTime() + " " + x.getProfessor());
//            }
//            System.out.println();
//        }


        return combinedTimetables;
    }


    /**
     * 2. 안되는 시간 처리하기
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
            } else if (timeSlotParts[1].length() >= 2 && noTimeParts[1].length() == 1) {
                int classTimeStart = Integer.parseInt(timeSlotParts[1].split("-")[0]);
                int classTimeEnd = Integer.parseInt(timeSlotParts[1].split("-")[1]);

                int noTimeHour = Integer.parseInt(noTimeParts[1]);

                if (classDay.equals(noTimeDay) && (classTimeStart <= noTimeHour && noTimeHour <= classTimeEnd)) {
                    overlap = true;
                    break;
                }
            } else if (timeSlotParts[1].length() == 1 && noTimeParts[1].length() >= 2) {
                int classTimeHour = Integer.parseInt(timeSlotParts[1]);

                int noTimeStart = Integer.parseInt(noTimeParts[1].split("-")[0]);
                int noTimeEnd = Integer.parseInt(noTimeParts[1].split("-")[1]);

                if (classDay.equals(noTimeDay) && (noTimeStart <= classTimeHour && classTimeHour <= noTimeEnd)) {
                    overlap = true;
                    break;
                }
            } else if (timeSlotParts[1].length() == 1 && noTimeParts[1].length() == 1) {
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

    private List<CreateTimetable> removeWantProcess(List<CreateTimetable> removeNoTimeSubject, List<String> removeSubject) {

        List<CreateTimetable> removeWantSubject = new ArrayList<>(removeNoTimeSubject);

        List<CreateTimetable> removeSubjectList = new ArrayList<>();

        for (String removeSubjectEach : removeSubject) {

            // 과목명 + 교수명 입력했을 경우
            if (removeSubjectEach.contains("/")) {
                String removeSubjectName = removeSubjectEach.split("/")[0];
                String removeSubjectProfName = removeSubjectEach.split("/")[1];

                for (CreateTimetable timetable : removeNoTimeSubject) {
                    String timetableSubject = timetable.getSubject();
                    String timetableProf = timetable.getProfessor();
                    if (timetableSubject.equals(removeSubjectName) && timetableProf.equals(removeSubjectProfName)) {
                        removeSubjectList.add(timetable);
                    }
                }
            }
            // 과목명만 입력했을 경우
            else {
                for (CreateTimetable timetable : removeNoTimeSubject) {
                    String timetableSubject = timetable.getSubject();
                    if (timetableSubject.equals(removeSubjectEach)) {
                        removeSubjectList.add(timetable);
                    }
                }
            }

        }
        removeWantSubject.removeAll(removeSubjectList);


        return removeWantSubject;
    }


    /**
     * 3. 원하는 과목 먼저 넣기
     */
    public List<CreateTimetable[][]> hopeSubjectProcess(List<String> hopeSubject, List<CreateTimetable> removeNoTimeSubject) throws JsonProcessingException {

        // 남은 과목중 희망하는 과목 추출
        List<CreateTimetable> matchingSubjects = findMatchingSubjects(hopeSubject, removeNoTimeSubject);

        // 희망하는 과목을 조합을 이용해 나눔 (내림차순 정렬)
        List<List<CreateTimetable>> combinations = generateCombinations(matchingSubjects);
        combinations.sort((list1, list2) -> Integer.compare(list2.size(), list1.size()));

        // 중복시간을 제거후 짧은것도 제거
        List<List<CreateTimetable>> hopeTimetableList = removeShortAndOverlapSubject(combinations);

        // 2차원 배열로 변환
        List<CreateTimetable[][]> timetableList = generateTimetableList(hopeTimetableList);

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


    public List<List<CreateTimetable>> removeShortAndOverlapSubject(List<List<CreateTimetable>> combinations) {

        List<List<CreateTimetable>> combinationsRemove = new ArrayList<>();
        for (List<CreateTimetable> timetables : combinations) {
            for (int j = 0; j < timetables.size(); j++) {
                for (int k = j + 1; k < timetables.size(); k++) {
                    String[] secondClassTimeList = timetables.get(k).getClassTime().split(",");

                    for (String secondClassTime : secondClassTimeList) {
                        if (isTimeSlotOverlap(timetables.get(j).getClassTime(), secondClassTime)) {
                            combinationsRemove.add(timetables);
                        }
                    }
                }
            }
        }
        combinations.removeAll(combinationsRemove);


        List<List<CreateTimetable>> shortRemoveCreateTimetable = new ArrayList<>();
        for (List<CreateTimetable> createTimetables : combinations) {
            int i = combinations.get(0).size();
            if (createTimetables.size() != i) {
                shortRemoveCreateTimetable.add(createTimetables);
            }
        }
        combinations.removeAll(shortRemoveCreateTimetable);

        return combinations;
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

                    int startHour, endHour;
                    if (partDivide[1].contains("-")) {
                        startHour = Integer.parseInt(partDivide[1].split("-")[0]);
                        endHour = Integer.parseInt(partDivide[1].split("-")[1]);
                    } else {
                        startHour = Integer.parseInt(partDivide[1].split("-")[0]);
                        endHour = startHour;
                    }

                    for (int i = startHour - 1; i < endHour; i++) {
                        timetable[i][dayIdx] = timetableEntry;
                    }
                }
            }
            timetableList.add(timetable);
        }
        return timetableList;
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

    List<List<CreateTimetable>> transformedMatchingSubjectsProcess(List<CreateTimetable[][]> matchingSubjects) {
        List<List<CreateTimetable>> transformedMatchingSubjects = new ArrayList<>();
        for (CreateTimetable[][] timetable : matchingSubjects) {
            List<CreateTimetable> blockTimetable = new ArrayList<>();

            for (int hour = 0; hour < 13; hour++) {
                for (int day = 0; day < 7; day++) {
                    if (timetable[hour][day] != null && !blockTimetable.contains(timetable[hour][day])) {
                        blockTimetable.add(timetable[hour][day]);
                    }
                }
            }
            transformedMatchingSubjects.add(blockTimetable);
        }

        return transformedMatchingSubjects;
    }


    /**
     * 4. 전공 - 교양 - 자선 순으로 시간표 만들기
     */
    private int listenMajorCreditProcess(List<String> hopeSubject, List<CreateTimetable> allMajorSubject) {

        int credit = 0;
        for (String hope : hopeSubject) {
            String subject = hope.split("/")[0];
            String professor = hope.split("/")[1];
            for (CreateTimetable timetable : allMajorSubject) {
                if (timetable.getSubject().equals(subject) && timetable.getProfessor().equals(professor)) {
                    credit = credit + timetable.getCredit();
                    break;
                }
            }
        }
        return credit;
    }

    private List<List<List<CreateTimetable>>> majorSubjectProcess(List<CreateTimetable[][]> matchingSubjects,
                                                                  int majorCredit, List<CreateTimetable> removeNoTimeSubject,
                                                                  int listenMajorCredit) {

        List<CreateTimetable> newRemoveNoTimeSubject = new ArrayList<>(removeNoTimeSubject);

        // 전공 남은 학점
        int lastMajorCredit = majorCredit - listenMajorCredit;

        // 희망하는 과목 처리
        List<String> flatMatchingSubjects = new ArrayList<>(); // 겹치는 과목이름
        List<List<String>> flatMatchingClassTime = new ArrayList<>(); // 겹치는 시간

        for (CreateTimetable[][] timetableArray : matchingSubjects) {
            List<String> classTimes = new ArrayList<>();
            for (CreateTimetable[] timetableRow : timetableArray) {
                for (CreateTimetable timetable : timetableRow) {
                    if (timetable != null) {
                        flatMatchingSubjects.add(timetable.getSubject());
                        if (!classTimes.contains(timetable.getClassTime())) {
                            classTimes.add(timetable.getClassTime());
                        }
                    }
                }
            }
            flatMatchingClassTime.add(classTimes);
        }

        // 과목이름 겹치는 과목 처리
        List<CreateTimetable> removeOverlapSubject = new ArrayList<>();
        for (String subject : flatMatchingSubjects) {
            for (CreateTimetable timetable : newRemoveNoTimeSubject) {
                if (Objects.equals(timetable.getSubject(), subject)) {
                    removeOverlapSubject.add(timetable);
                }
            }
        }
        newRemoveNoTimeSubject.removeAll(removeOverlapSubject);

        // 시간 겹치는 과목 처리
        List<List<CreateTimetable>> algoTimetable = new ArrayList<>(); // 각 matchingSubjects 에 들어갈 수 있는 전공 과목들

        for (int i = 0; i < matchingSubjects.size(); i++) {
            algoTimetable.add(new ArrayList<>(newRemoveNoTimeSubject));
        }

        for (int i = 0; i < algoTimetable.size(); i++) {
            List<CreateTimetable> removeAlgo = new ArrayList<>();
            for (int j = 0; j < flatMatchingClassTime.get(i).size(); j++) {
                for (int k = 0; k < algoTimetable.get(i).size(); k++) {
                    String[] noTimeList = flatMatchingClassTime.get(i).get(j).split(",");
                    for (String noTime : noTimeList) {
                        if (isTimeSlotOverlap(algoTimetable.get(i).get(k).getClassTime(), noTime)) {
                            removeAlgo.add(algoTimetable.get(i).get(k));
                        }
                    }
                }
            }
            algoTimetable.get(i).removeAll(removeAlgo);
        }

        // 오래 걸리는 부분
        List<List<List<CreateTimetable>>> resultTimetable = new ArrayList<>();

        for (List<CreateTimetable> createTimetables : algoTimetable) {
            List<List<CreateTimetable>> combTimetable = generateCombinations(createTimetables, lastMajorCredit);

            List<List<CreateTimetable>> removeShortTimetable = filterLongestSublists(combTimetable);

            List<List<CreateTimetable>> nonOverlapTimetable = new ArrayList<>();

            for (List<CreateTimetable> timetableList : removeShortTimetable) {
                boolean overlaps = false;

                for (int i = 0; i < timetableList.size() - 1 && !overlaps; i++) {
                    String timeI = timetableList.get(i).getClassTime();

                    for (int j = i + 1; j < timetableList.size(); j++) {
                        String timeJ = timetableList.get(j).getClassTime();

                        if (timeJ.split(",").length >= 2) {
                            String[] splitList = timeJ.split(",");
                            for (String split : splitList) {
                                if (isTimeSlotOverlap(timeI, split)) {
                                    overlaps = true;
                                    break;
                                }
                            }
                        } else {
                            if (isTimeSlotOverlap(timeI, timeJ)) {
                                overlaps = true;
                                break;
                            }
                        }
                    }
                }
                if (!overlaps) {
                    nonOverlapTimetable.add(timetableList);
                }
            }
            resultTimetable.add(nonOverlapTimetable);
        }


        return resultTimetable;
    }


    public List<List<CreateTimetable>> generateCombinations(List<CreateTimetable> courses, int lastMajorCredit) {
        List<List<CreateTimetable>> result = new ArrayList<>();
        List<CreateTimetable> course = new ArrayList<>(courses);
        generateCombinationsRecursive(course, 0, lastMajorCredit, new ArrayList<>(), result);
        return result;
    }

    private void generateCombinationsRecursive(
            List<CreateTimetable> courses,
            int currentIndex,
            int remainingCredit,
            List<CreateTimetable> currentCombination,
            List<List<CreateTimetable>> result) {

        if (currentIndex == courses.size() || remainingCredit == 0) {
            result.add(new ArrayList<>(currentCombination));
            return;
        }

        // Include the current course
        CreateTimetable currentCourse = courses.get(currentIndex);
        int courseCredit = currentCourse.getCredit();

        if (!containsSubject(currentCombination, currentCourse.getSubject()) &&
                courseCredit <= remainingCredit) {
            currentCombination.add(currentCourse);
            generateCombinationsRecursive(courses, currentIndex + 1, remainingCredit - courseCredit, currentCombination, result);
            currentCombination.remove(currentCombination.size() - 1);
        }

        // Skip the current course
        generateCombinationsRecursive(courses, currentIndex + 1, remainingCredit, currentCombination, result);
    }

    private boolean containsSubject(List<CreateTimetable> combination, String subject) {
        for (CreateTimetable course : combination) {
            if (course.getSubject().equals(subject)) {
                return true;
            }
        }
        return false;
    }

    public List<List<CreateTimetable>> filterLongestSublists(List<List<CreateTimetable>> result) {
        int maxLength = 0;

        // Find the length of the longest sublist
        for (List<CreateTimetable> sublist : result) {
            maxLength = Math.max(maxLength, sublist.size());
        }

        // Remove sublists shorter than the longest one
        int finalMaxLength = maxLength;
        result.removeIf(sublist -> sublist.size() < finalMaxLength);

        return result;
    }


}
