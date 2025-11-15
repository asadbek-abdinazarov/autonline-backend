package uz.javachi.autonline.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.javachi.autonline.customAnnotations.CountView;
import uz.javachi.autonline.projection.LessonAnonsProjection;
import uz.javachi.autonline.dto.response.LessonResponseDTO;
import uz.javachi.autonline.service.LessonService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lesson")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<LessonAnonsProjection> getLessonsAnons() {
        return lessonService.getLessonsAnons();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @CountView
    public ResponseEntity<LessonResponseDTO> getByLessonId(@PathVariable(name = "id") Integer lessonId) {
        return ResponseEntity.ok(lessonService.getLesson(lessonId));
    }


}
