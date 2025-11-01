package uz.javachi.autonline.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.javachi.autonline.dto.res.LessonHistoryDTO;
import uz.javachi.autonline.service.LessonHistoryService;

@RestController
@RequestMapping("/api/v1/lesson-history")
@RequiredArgsConstructor
public class LessonHistoryController {
    private final LessonHistoryService lessonHistoryService;

    @GetMapping
    private ResponseEntity<?> getAllMyLessonHistory() {
        return lessonHistoryService.getAllMyLessonHistory();
    }

    @PostMapping("/add")
    private ResponseEntity<?> createLessonHistory(@RequestBody LessonHistoryDTO lessonHistoryDTO) {
        return lessonHistoryService.createLessonHistory(lessonHistoryDTO);
    }

}
