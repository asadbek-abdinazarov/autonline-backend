package uz.javachi.autonline.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.javachi.autonline.dto.request.TeacherRegisterStudentRequest;
import uz.javachi.autonline.dto.response.StudentsResponseToTeacherDTO;
import uz.javachi.autonline.exceptions.CustomException;
import uz.javachi.autonline.exceptions.UserManyStudentsException;
import uz.javachi.autonline.model.Subscription;
import uz.javachi.autonline.model.User;
import uz.javachi.autonline.repository.RoleRepository;
import uz.javachi.autonline.repository.SubscriptionRepository;
import uz.javachi.autonline.repository.UserRepository;
import uz.javachi.autonline.utils.SecurityUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import static uz.javachi.autonline.DefaultValues.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final MessageService ms;
    private final PasswordEncoder passwordEncoder;
    private final UserBlockService userBlockService;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public Page<StudentsResponseToTeacherDTO> getTeacherAllStudent(int page, int size) {

        Integer currentUserId = SecurityUtils.getCurrentUserIdOrThrow();

        User teacher = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UsernameNotFoundException(ms.get("user.not.found")));

        if (teacher.hasRole(ROLE_TEACHER)) {
            throw new CustomException(ms.get("you.are.not.teacher"), new Throwable(HttpStatus.BAD_REQUEST.name()));
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
                throw new CustomException(ms.get("password.not.match"), new Throwable(HttpStatus.BAD_REQUEST.name()));
            }

            if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
                throw new CustomException(ms.get("user.already.exists.with.username"), new Throwable(HttpStatus.BAD_REQUEST.name()));
            }

            if (userRepository.findByPhoneNumber(dto.getPhoneNumber()).isPresent()) {
                throw new CustomException(ms.get("user.already.exists.with.phone"), new Throwable(HttpStatus.BAD_REQUEST.name()));
            }

            User teacher = userRepository.findById(teacherId)
                    .orElseThrow(() -> new UsernameNotFoundException(ms.get("user.not.found")));

            if (teacher.hasRole(ROLE_TEACHER)) {
                throw new CustomException(ms.get("you.are.not.teacher"), new Throwable(HttpStatus.BAD_REQUEST.name()));
            }

            Subscription subscription = teacher.getSubscription();

            LocalDateTime nextPaymentDate = dto.getNextPaymentDate();
            if (nextPaymentDate.isBefore(LocalDateTime.now())) {
                throw new CustomException(ms.get("next.payment.date.invalid.before"), new Throwable(ms.get("next.payment.date.invalid")));
            } else if (nextPaymentDate.isAfter(LocalDateTime.now().plusDays(subscription.getActiveDays()))) {
                throw new CustomException(ms.get("next.payment.date.invalid.after"), new Throwable(ms.get("next.payment.date.invalid")));
            }

            Integer studentLimit = subscription.getStudentLimit();
            List<User> students = teacher.getStudents();
            if (students.size() >= studentLimit) {
                throw new UserManyStudentsException(ms.get("teachers.student.limit.exceeded", studentLimit));
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
                case PRO_TEACHER -> subscriptionRepository.findByName(STUDENT_RPO).ifPresent(student::setSubscription);
                case FULL_TEACHER ->
                        subscriptionRepository.findByName(STUDENT_FULL).ifPresent(student::setSubscription);
                default -> throw new UserManyStudentsException(ms.get("teacher.free.subscription"));
            }

            userRepository.save(student);

            students.add(student);

            return User.studentToDtoForTeacher(student);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public StudentsResponseToTeacherDTO getStudentById(Integer id) {
        Integer currentUserId = SecurityUtils.getCurrentUserIdOrThrow();

        User teacher = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UsernameNotFoundException(ms.get("user.not.found")));

        if (teacher.hasRole(ROLE_TEACHER)) {
            throw new CustomException(ms.get("you.are.not.teacher"), new Throwable(HttpStatus.BAD_REQUEST.name()));
        }

        User student = userRepository.findStudentOfTeacher(currentUserId, id)
                .orElseThrow(() -> new RuntimeException(ms.get("student.not.found")));

        return User.studentToDtoForTeacher(student);
    }

    @Transactional
    public StudentsResponseToTeacherDTO deleteStudentById(Integer id) {
        Integer currentUserId = SecurityUtils.getCurrentUserIdOrThrow();

        User teacher = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UsernameNotFoundException(ms.get("user.not.found")));

        if (teacher.hasRole(ROLE_TEACHER)) {
            throw new CustomException(ms.get("you.are.not.teacher"), new Throwable(HttpStatus.BAD_REQUEST.name()));
        }

        User student = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(ms.get("student.not.found")));
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
