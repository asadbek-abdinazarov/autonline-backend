package uz.javachi.autonline.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.javachi.autonline.dto.request.TeacherRegisterStudentRequest;
import uz.javachi.autonline.dto.response.StudentsResponseToTeacherDTO;
import uz.javachi.autonline.exceptions.UserManyStudentsException;
import uz.javachi.autonline.model.Subscription;
import uz.javachi.autonline.model.User;
import uz.javachi.autonline.repository.RoleRepository;
import uz.javachi.autonline.repository.SubscriptionRepository;
import uz.javachi.autonline.repository.UserRepository;
import uz.javachi.autonline.utils.SecurityUtils;

import java.util.HashSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserBlockService userBlockService;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public Page<StudentsResponseToTeacherDTO> getTeacherAllStudent(int page, int size) {

        Integer currentUserId = SecurityUtils.getCurrentUserIdOrThrow();

        User teacher = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));

        if (teacher.hasRole("TEACHER")) {
            throw new RuntimeException("Access denied, you are not a teacher!");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());

        return userRepository.findActiveStudentsByTeacher(currentUserId, pageable)
                .map(User::studentToDtoForTeacher);
    }


    @Transactional
    public StudentsResponseToTeacherDTO createStudentToTeacher(TeacherRegisterStudentRequest dto) throws UserManyStudentsException {

        try {
            Integer teacherId = SecurityUtils.getCurrentUserIdOrThrow();

            if (!dto.getPassword().equals(dto.getConfirmPassword())) {
                throw new RuntimeException("Passwords do not match!");
            }

            User teacher = userRepository.findById(teacherId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found!"));

            if (teacher.hasRole("TEACHER")) {
                throw new RuntimeException("Access denied, you are not a teacher!");
            }
            Subscription subscription = teacher.getSubscription();

            if (teacher.getStudents().size() > subscription.getStudentLimit()) {
                throw new UserManyStudentsException("You have many students already, please contact with admin!");
            }

            User student = User.studentToUserForTeacher(dto);
            student.setPassword(passwordEncoder.encode(dto.getPassword()));
            student.setIsActive(true);
            student.setRoles(new HashSet<>());

            roleRepository.findByName("USER").ifPresent(student::addRole);
            roleRepository.findByName("STUDENT").ifPresent(student::addRole);

            String sub = subscription.getName();
            switch (sub) {
                case "BASIC" -> subscriptionRepository.findByName("STUDENT_BASIC").ifPresent(student::setSubscription);
                case "PRO" -> subscriptionRepository.findByName("STUDENT_PRO").ifPresent(student::setSubscription);
                case "FULL" -> subscriptionRepository.findByName("STUDENT_FULL").ifPresent(student::setSubscription);
                default -> throw new RuntimeException("Invalid subscription mapping");
            }

            userRepository.save(student);

            teacher.getStudents().add(student);

            return User.studentToDtoForTeacher(student);
        } catch (UserManyStudentsException e) {
            log.error(e.getMessage());
            throw e;
        }
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
        userBlockService.blockUser(student.getUserId());
        student.softDelete();
        userRepository.save(student);

        return User.studentToDtoForTeacher(student);
    }

}
