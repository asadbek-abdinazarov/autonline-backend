package uz.javachi.autonline.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.javachi.autonline.dto.request.TeacherRegisterStudentRequest;
import uz.javachi.autonline.dto.response.StudentsResponseToTeacherDTO;
import uz.javachi.autonline.model.User;
import uz.javachi.autonline.repository.RoleRepository;
import uz.javachi.autonline.repository.SubscriptionRepository;
import uz.javachi.autonline.repository.UserRepository;
import uz.javachi.autonline.utils.SecurityUtils;

import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public List<StudentsResponseToTeacherDTO> getTeacherAllStudent() {

        Integer currentUserId = SecurityUtils.getCurrentUserIdOrThrow();

        User teacher = userRepository.findTeacherWithStudents(currentUserId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));

        if (teacher.hasRole("TEACHER")) {
            throw new RuntimeException("Access denied, you are not a teacher!");
        }

        return teacher.getStudents()
                .stream()
                .map(User::studentToDtoForTeacher)
                .toList();
    }


    @Transactional
    public StudentsResponseToTeacherDTO createStudentToTeacher(TeacherRegisterStudentRequest dto) {

        Integer teacherId = SecurityUtils.getCurrentUserIdOrThrow();

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match!");
        }

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));

        if (teacher.hasRole("TEACHER")) {
            throw new RuntimeException("Access denied, you are not a teacher!");
        }

        User student = User.studentToUserForTeacher(dto);
        student.setPassword(passwordEncoder.encode(dto.getPassword()));
        student.setIsActive(true);
        student.setRoles(new HashSet<>());

        roleRepository.findByName("USER").ifPresent(student::addRole);
        roleRepository.findByName("STUDENT").ifPresent(student::addRole);

        String sub = teacher.getSubscription().getName();
        switch (sub) {
            case "BASIC" -> subscriptionRepository.findByName("STUDENT_BASIC").ifPresent(student::setSubscription);
            case "PRO" -> subscriptionRepository.findByName("STUDENT_PRO").ifPresent(student::setSubscription);
            case "FULL" -> subscriptionRepository.findByName("STUDENT_FULL").ifPresent(student::setSubscription);
            default -> throw new RuntimeException("Invalid subscription mapping");
        }

        userRepository.save(student);

        teacher.getStudents().add(student);

        return User.studentToDtoForTeacher(student);
    }

    @Transactional(readOnly = true)
    public StudentsResponseToTeacherDTO getStudentById(Integer id) {
        Integer currentUserId = SecurityUtils.getCurrentUserIdOrThrow();

        User teacher = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));

        if (teacher.hasRole("TEACHER")) {
            throw new RuntimeException("Access denied, you are not a teacher!");
        }

        User student = userRepository.findStudentOfTeacher(currentUserId, id)
                .orElseThrow(() -> new RuntimeException("Student not found or does not belong to you!"));

        return User.studentToDtoForTeacher(student);
    }

    @Transactional
    public StudentsResponseToTeacherDTO deleteStudentById(Integer id) {
        Integer currentUserId = SecurityUtils.getCurrentUserIdOrThrow();

        User teacher = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));

        if (teacher.hasRole("TEACHER")) {
            throw new RuntimeException("Access denied, you are not a teacher!");
        }

        User student = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Student not found!"));

        student.softDelete();
        userRepository.save(student);

        return User.studentToDtoForTeacher(student);
    }

}
