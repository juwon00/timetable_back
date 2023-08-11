package capstone.timetable.controller;

import capstone.timetable.DTO.CreateTimetable;
import capstone.timetable.service.TimetableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/timetable")
public class TimetableController {

    private final TimetableService timetableService;

    @GetMapping("make-up")
    public ResponseEntity<List<CreateTimetable>> makeUp(
            @RequestParam("major") String major,
            @RequestParam("grade") int grade,
            @RequestParam("semester") int semester,
            @RequestParam("majorCredit") int majorCredit,
            @RequestParam("culturalCredit") int culturalCredit,
            @RequestParam("freeSelectCredit") int freeSelectCredit,
            @RequestParam("hopeSubject") String hopeSubject,
            @RequestParam("noTime") String noTime) {

        List<CreateTimetable> timetableResults = timetableService.generateTimetable(
                major, grade, semester, majorCredit, culturalCredit,
                freeSelectCredit, hopeSubject, noTime
        );

        return ResponseEntity.ok(timetableResults);
    }

}
