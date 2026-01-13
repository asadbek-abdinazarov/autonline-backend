package uz.javachi.autonline.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.javachi.autonline.dto.request.TestFinishRequest;
import uz.javachi.autonline.dto.request.TestStartRequest;
import uz.javachi.autonline.dto.response.FinishResponseDTO;
import uz.javachi.autonline.dto.response.StartedResponseDTO;
import uz.javachi.autonline.dto.response.TestTemplateResponseDTO;
import uz.javachi.autonline.enums.TestStatus;
import uz.javachi.autonline.exceptions.CustomException;
import uz.javachi.autonline.model.TestResult;
import uz.javachi.autonline.model.TestTemplate;
import uz.javachi.autonline.model.User;
import uz.javachi.autonline.repository.TestResultRepository;
import uz.javachi.autonline.repository.TestTemplateRepository;
import uz.javachi.autonline.repository.UserRepository;
import uz.javachi.autonline.utils.SecurityUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TestTemplateService {

    private final TestTemplateRepository testTemplateRepository;
    private final UserRepository userRepository;
    private final MessageService ms;
    private final TestResultRepository testResultRepository;


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

        TestResult result = TestResult.builder()
                .startedAt(LocalDateTime.now())
                .testTemplate(testTemplate)
                .user(user)
                .wrongCount(0)
                .correctCount(0)
                .attemptNumber(testTemplate.getTestTemplateId())
                .build();
        testResultRepository.saveAndFlush(result);

        List<TestResult> testResults = testTemplate.getTestResults();
        if (testResults.isEmpty()) {
            testTemplate.setTestResults(List.of(result));
        } else {
            testResults.add(result);
        }

        testTemplateRepository.saveAndFlush(testTemplate);

        return StartedResponseDTO.builder()
                .testResultId(result.getId())
                .testTemplateId(testTemplate.getTestTemplateId())
                .status("STARTED")
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
        return FinishResponseDTO.builder()
                .testResultId(result.getId())
                .correctCount(dto.correctCount())
                .wrongCount(dto.wrongCount())
                .score(dto.score())
                .startedAt(result.getStartedAt())
                .finishedAt(result.getFinishedAt())
                .build();
    }
}
