package uz.javachi.autonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.javachi.autonline.model.News;

import java.util.List;
import java.util.Optional;

public interface NewsRepository extends JpaRepository<News, Integer> {
    @Query(nativeQuery = true, value = """
                SELECT * FROM news n where n.is_active = :isActive order by news_created_at desc
            """)
    Optional<List<News>> findNewsByIsActive(Boolean isActive);
}