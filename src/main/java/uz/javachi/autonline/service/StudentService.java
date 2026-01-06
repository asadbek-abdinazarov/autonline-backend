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
import java.util.List;

import static uz.javachi.autonline.DefaultValues.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;
    private final PasswordEncoder passwordEncoder;
    private final UserBlockService userBlockService;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public Page<StudentsResponseToTeacherDTO> getTeacherAllStudent(int page, int size) {

        Integer currentUserId = SecurityUtils.getCurrentUserIdOrThrow();

        User teacher = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UsernameNotFoundException(messageService.get("user.not.found")));

        if (teacher.hasRole(ROLE_TEACHER)) {
            throw new RuntimeException(messageService.get("you.are.not.teacher"));
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
                throw new RuntimeException(messageService.get("password.not.match"));
            }

            User teacher = userRepository.findById(teacherId)
                    .orElseThrow(() -> new UsernameNotFoundException(messageService.get("user.not.found")));

            if (teacher.hasRole(ROLE_TEACHER)) {
                throw new RuntimeException(messageService.get("you.are.not.teacher"));
            }
            Subscription subscription = teacher.getSubscription();

            Integer studentLimit = subscription.getStudentLimit();
            List<User> students = teacher.getStudents();
            if (students.size() >= studentLimit) {
                throw new UserManyStudentsException(messageService.get("teachers.student.limit.exceeded", studentLimit));
            }

            User student = User.studentToUserForTeacher(dto);
            student.setPassword(passwordEncoder.encode(dto.getPassword()));
            student.setIsActive(true);
            student.setRoles(new HashSet<>());

            roleRepository.findByName(DEFAULT_USER_ROLE).ifPresent(student::addRole);
            roleRepository.findByName(ROLE_STUDENT).ifPresent(student::addRole);

            String sub = subscription.getName();
            switch (sub) {
                case BASIC_TEACHER ->
                        subscriptionRepository.findByName(STUDENT_BASIC).ifPresent(student::setSubscription);
                case RPO_TEACHER ->
                        subscriptionRepository.findByName(STUDENT_RPO).ifPresent(student::setSubscription);
                case FULL_TEACHER ->
                        subscriptionRepository.findByName(STUDENT_FULL).ifPresent(student::setSubscription);
                default -> throw new UserManyStudentsException(messageService.get("teacher.free.subscription"));
            }

            userRepository.save(student);

            students.add(student);

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
                .orElseThrow(() -> new UsernameNotFoundException(messageService.get("user.not.found")));

        if (teacher.hasRole(ROLE_TEACHER)) {
            throw new RuntimeException(messageService.get("you.are.not.teacher"));
        }

        User student = userRepository.findStudentOfTeacher(currentUserId, id)
                .orElseThrow(() -> new RuntimeException(messageService.get("student.not.found")));

        return User.studentToDtoForTeacher(student);
    }

    @Transactional
    public StudentsResponseToTeacherDTO deleteStudentById(Integer id) {
        Integer currentUserId = SecurityUtils.getCurrentUserIdOrThrow();

        User teacher = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UsernameNotFoundException(messageService.get("user.not.found")));

        if (teacher.hasRole(ROLE_TEACHER)) {
            throw new RuntimeException(messageService.get("you.are.not.teacher"));
        }

        User student = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(messageService.get("student.not.found")));
        userBlockService.blockUser(student.getUserId());
        student.softDelete();
        userRepository.save(student);

        return User.studentToDtoForTeacher(student);
    }

    @Transactional(readOnly = true)
    public Page<StudentsResponseToTeacherDTO> searchUserPaged(String value, int page, int size) {

        Integer teacherId = SecurityUtils.getCurrentUserIdOrThrow();
        value = value.trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by("full_name", "phone_number", "username").ascending());

        return userRepository.searchUserPageable(teacherId, value, pageable)
                .map(User::studentToDtoForTeacher);
    }

}
