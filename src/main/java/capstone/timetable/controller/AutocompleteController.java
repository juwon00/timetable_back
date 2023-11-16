package capstone.timetable.controller;

import capstone.timetable.domain.Timetable;
import capstone.timetable.repository.AutocompleteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/timetable")
public class AutocompleteController {

    @Autowired
    private AutocompleteRepository autocompleteRepository;


    @GetMapping("/autocomplete-major")
    public ResponseEntity<List<String>> getAutoCompleteMajor(@RequestParam("query") String query) {
        List<Timetable> results = autocompleteRepository.findByMajorContaining(query);
        List<String> majorList = results.stream()
                .map(Timetable::getMajor)
                .distinct()
                .collect(Collectors.toList());
        return ResponseEntity.ok(majorList);
    }

    @GetMapping("/autocomplete-subject")
    public ResponseEntity<List<String>> getAutocompleteSubject(@RequestParam("major") String major,
                                                               @RequestParam("semester") int semester,
                                                               @RequestParam("query") String query) {
        List<Timetable> results =
                autocompleteRepository.findByMajorAndSemesterAndSubjectContainingOrMajorAndSemesterAndProfessorContaining(
                major, semester, query, major, semester, query
        );
        List<String> subjectAndProfessorList = results.stream()
                .map(entry -> entry.getSubject() + "/" + entry.getProfessor())
                .distinct()
                .collect(Collectors.toList());
        return ResponseEntity.ok(subjectAndProfessorList);
    }
}
