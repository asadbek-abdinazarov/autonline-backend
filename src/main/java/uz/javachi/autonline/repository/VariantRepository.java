package uz.javachi.autonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.javachi.autonline.model.Question;
import uz.javachi.autonline.model.Variant;

import java.util.Optional;

@Repository
public interface VariantRepository extends JpaRepository<Variant, Integer> {
    Optional<Variant> findVariantByQuestion(Question question);
}
