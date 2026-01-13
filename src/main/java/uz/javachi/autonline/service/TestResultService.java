package uz.javachi.autonline.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.javachi.autonline.dto.request.TestFinishRequest;
import uz.javachi.autonline.model.TestResult;
import uz.javachi.autonline.repository.TestResultRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TestResultService {
    private final TestResultRepository testResultRepository;

    public TestResult startTest(Long userId, Long testTemplateId) {

        /*int attempt = testResultRepository
                .countByUserIdAndTestTemplateId(userId, testTemplateId) + 1;

        TestResult result = new TestResult();
        result.setUser(userRepository.getReferenceById(userId));
        result.setTestTemplate(testTemplateRepository.getReferenceById(testTemplateId));
        result.setStartedAt(LocalDateTime.now());
        result.setAttemptNumber(attempt);*/

//        return testResultRepository.save(result);
        return null;
    }

    public TestResult finishTest(TestFinishRequest req) {

       /* TestResult result = testResultRepository
                .findById(req.testResultId())
                .orElseThrow();

        result.setScore(req.score());
        result.setCorrectCount(req.correctCount());
        result.setWrongCount(req.wrongCount());
        result.setFinishedAt(LocalDateTime.now());

        int passScore = result.getTestTemplate().getPassScore();
        result.setStatus(
                req.score() >= passScore ? TestStatus.PASSED : TestStatus.FAILED
        );

        return testResultRepository.save(result);*/
        return null;
    }


}
