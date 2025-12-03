package uz.javachi.autonline.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.javachi.autonline.dto.request.TeacherRegisterStudentRequest;
import uz.javachi.autonline.dto.response.StudentsResponseToTeacherDTO;
import uz.javachi.autonline.service.StudentService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {
    private final StudentService studentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<List<StudentsResponseToTeacherDTO>> getTeacherAllStudent() {
        return ResponseEntity.ok(studentService.getTeacherAllStudent());
    }

    @GetMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<StudentsResponseToTeacherDTO> getStudentById(@PathVariable Integer id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<StudentsResponseToTeacherDTO> createStudentToTeacher(TeacherRegisterStudentRequest dto) {
        return ResponseEntity.ok(studentService.createStudentToTeacher(dto));
    }

    @DeleteMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<StudentsResponseToTeacherDTO> deleteStudentById(@PathVariable Integer id) {
        return ResponseEntity.ok(studentService.deleteStudentById(id));
    }


}
