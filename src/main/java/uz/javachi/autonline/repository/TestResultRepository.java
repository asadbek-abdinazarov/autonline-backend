package uz.javachi.autonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.javachi.autonline.model.TestResult;

public interface TestResultRepository extends JpaRepository<TestResult, Long> {
}