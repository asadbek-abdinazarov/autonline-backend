package uz.javachi.autonline.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.javachi.autonline.dto.request.TestFinishRequest;
import uz.javachi.autonline.dto.request.TestStartRequest;
import uz.javachi.autonline.dto.response.*;
import uz.javachi.autonline.enums.TestStatus;
import uz.javachi.autonline.exceptions.CustomException;
import uz.javachi.autonline.model.Question;
import uz.javachi.autonline.model.TestResult;
import uz.javachi.autonline.model.TestTemplate;
import uz.javachi.autonline.model.User;
import uz.javachi.autonline.repository.*;
import uz.javachi.autonline.utils.SecurityUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static uz.javachi.autonline.DefaultValues.TEMPLATE_LESSON_COUNT;
import static uz.javachi.autonline.DefaultValues.TEMPLATE_LESSON_ID;
import static uz.javachi.autonline.utils.Utils.getQuestionResponseDTOS;

@Service
@RequiredArgsConstructor
public class TestTemplateService {

    private final TestTemplateRepository testTemplateRepository;
    private final UserRepository userRepository;
    private final MessageService ms;
    private final TestResultRepository testResultRepository;
    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;
    private final LessonHistoryService lessonHistoryService;


    @Transactional(readOnly = true)
    public List<TestTemplateResponseDTO> getAllTestTemplates() {

        Integer userId = SecurityUtils.getCurrentUserIdOrThrow();

        List<Object[]> rows =
                testTemplateRepository.findAllTemplatesWithLastResult(userId);

        return rows.stream()
                .map(row -> {
                    TestTemplate tt = (TestTemplate) row[0];
                    TestResult tr = (TestResult) row[1];

                    return TestTemplateResponseDTO.builder()
                            .id(tt.getTestTemplateId())
                            .title(tt.getTitle())
                            .duration(tt.getDuration())
                            .maxScore(tt.getMaxScore())
                            .passScore(tt.getPassScore())
                            .createdAt(tt.getCreatedAt())
                            .testResultResponse(
                                    tr == null ? null : TestResult.toResponseDto(tr)
                            )
                            .build();
                })
                .toList();
    }


    @Transactional
    public StartedResponseDTO startTestTemplate(TestStartRequest dto) {
        String lang = LocaleContextHolder.getLocale().getLanguage();
        Integer currentUserIdOrThrow = SecurityUtils.getCurrentUserIdOrThrow();

        Optional<User> byId = userRepository.findById(currentUserIdOrThrow);
        if (byId.isEmpty()) {
            throw new CustomException(HttpStatus.NOT_FOUND.name(), new Throwable(ms.get("user.not.found")));
        }

        User user = byId.get();

        if (!user.getIsActive()) {
            throw new CustomException(HttpStatus.FORBIDDEN.name(), new Throwable(ms.get("user.is.blocked")));
        }

        Optional<TestTemplate> byIdTemplate = testTemplateRepository.findById(dto.testTemplateId());
        if (byIdTemplate.isEmpty()) {
            throw new CustomException(HttpStatus.NOT_FOUND.name(), new Throwable(ms.get("test.template.not.found")));
        }

        TestTemplate testTemplate = byIdTemplate.get();
        Integer testTemplateId = testTemplate.getTestTemplateId();

        Optional<TestResult> lastNotFinishedTestResult = testResultRepository.findLastAndNotFinishedAndByTemplateId(testTemplateId);

        TestResult result;
        result = lastNotFinishedTestResult.orElseGet(() -> TestResult.builder()
                .startedAt(LocalDateTime.now())
                .testTemplate(testTemplate)
                .user(user)
                .wrongCount(0)
                .percentage(0)
                .status(TestStatus.IN_PROCESS)
                .correctCount(0)
                .attemptNumber(testTemplateId)
                .build());

        testResultRepository.saveAndFlush(result);

        List<TestResult> testResults = testTemplate.getTestResults();
        if (testResults.isEmpty()) {
            testTemplate.setTestResults(List.of(result));
        } else {
            testResults.add(result);
        }

        testTemplateRepository.saveAndFlush(testTemplate);

        Object[] row = lessonRepository.getLessonWithTranslation(TEMPLATE_LESSON_ID, lang);
        Object[] rs = (Object[]) row[0];
        Integer lessonId = (Integer) rs[0];
        String icon = (String) rs[1];
        String name = (String) rs[2];
        String description = (String) rs[3];

        int pageSize = 20;
        int page = testTemplateId - 1;

        Page<Question> questionPage =
                questionRepository.findByInterval(PageRequest.of(page, pageSize));

        List<Question> questions = questionPage.getContent();

        List<QuestionResponseDTO> questionResponseDTOS = getQuestionResponseDTOS(questions, lang);


        return StartedResponseDTO.builder()
                .testResultId(result.getId())
                .testTemplateId(testTemplateId)
                .lessonId(lessonId)
                .icon(icon)
                .name(name)
                .description(description)
                .questions(questionResponseDTOS)
                .build();
    }

    public FinishResponseDTO finishTestTemplate(TestFinishRequest dto) {
        TestResult result = testResultRepository
                .findById(dto.testResultId())
                .orElseThrow();
        result.setScore(dto.score());
        result.setCorrectCount(dto.correctCount());
        result.setWrongCount(dto.wrongCount());
        result.setFinishedAt(LocalDateTime.now());

        int passScore = result.getTestTemplate().getPassScore();
        result.setStatus(
                dto.score() >= passScore ? TestStatus.PASSED : TestStatus.FAILED
        );
        testResultRepository.save(result);

        lessonHistoryService.createLessonHistory(
                LessonHistoryDTO.builder()
                        .lessonId(TEMPLATE_LESSON_ID)
                        .correctAnswersCount(result.getCorrectCount())
                        .notCorrectAnswersCount(result.getWrongCount())
                        .percentage(result.getPercentage())
                        .allQuestionsCount(TEMPLATE_LESSON_COUNT)
                        .build()
        );

        return FinishResponseDTO.builder()
                .testResultId(result.getId())
                .correctCount(result.getCorrectCount())
                .wrongCount(result.getWrongCount())
                .score(result.getScore())
                .percentage(result.getPercentage())
                .startedAt(result.getStartedAt())
                .finishedAt(result.getFinishedAt())
                .build();
    }

}
