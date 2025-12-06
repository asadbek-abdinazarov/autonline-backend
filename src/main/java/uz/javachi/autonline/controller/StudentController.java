package uz.javachi.autonline.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.javachi.autonline.dto.request.TeacherRegisterStudentRequest;
import uz.javachi.autonline.dto.response.StudentsResponseToTeacherDTO;
import uz.javachi.autonline.exceptions.UserManyStudentsException;
import uz.javachi.autonline.service.StudentService;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {
    private final StudentService studentService;

    @GetMapping
    public ResponseEntity<Page<StudentsResponseToTeacherDTO>> getTeacherAllStudent(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(studentService.getTeacherAllStudent(page, size));
    }

    @GetMapping("/by-id/{id}")
    public ResponseEntity<StudentsResponseToTeacherDTO> getStudentById(@PathVariable Integer id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @PostMapping
    public ResponseEntity<StudentsResponseToTeacherDTO> createStudentToTeacher(@RequestBody TeacherRegisterStudentRequest dto) throws UserManyStudentsException {
        return ResponseEntity.ok(studentService.createStudentToTeacher(dto));
    }

    @DeleteMapping("/by-id/{id}")
    public ResponseEntity<StudentsResponseToTeacherDTO> deleteStudentById(@PathVariable Integer id) {
        return ResponseEntity.ok(studentService.deleteStudentById(id));
    }


    @GetMapping("/search")
    public ResponseEntity<Page<StudentsResponseToTeacherDTO>> searchUser(
            @RequestParam String value,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(studentService.searchUserPaged(value, page, size));
    }

}
