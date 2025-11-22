package uz.javachi.autonline.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.javachi.autonline.dto.response.LessonHistoryDTO;
import uz.javachi.autonline.dto.response.UserLessonStatisticResponseDTO;
import uz.javachi.autonline.service.LessonHistoryService;
import uz.javachi.autonline.service.StatisticService;

@RestController
@RequestMapping("/api/v1/lesson-history")
@RequiredArgsConstructor
public class LessonHistoryController {
    private final LessonHistoryService lessonHistoryService;
    private final StatisticService statisticService;

    @GetMapping
    public ResponseEntity<UserLessonStatisticResponseDTO> getUserLessonHistory() throws Exception {
        UserLessonStatisticResponseDTO userLessonStatisticResponseDTO = statisticService.getUserLessonHistory().get();
        return ResponseEntity.ok(userLessonStatisticResponseDTO);
    }

    @PostMapping("/add")
    private ResponseEntity<?> createLessonHistory(@RequestBody LessonHistoryDTO lessonHistoryDTO) {
        return lessonHistoryService.createLessonHistory(lessonHistoryDTO);
    }

}
