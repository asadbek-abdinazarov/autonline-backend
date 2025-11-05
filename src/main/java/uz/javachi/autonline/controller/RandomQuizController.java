package uz.javachi.autonline.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.javachi.autonline.dto.res.LessonResponseDTO;
import uz.javachi.autonline.service.LessonService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/random-quiz")
@RequiredArgsConstructor
public class RandomQuizController {

    private final LessonService lessonService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<LessonResponseDTO>> getRandomQuiz() {
        return lessonService.getRandomQuiz();
    }
}

