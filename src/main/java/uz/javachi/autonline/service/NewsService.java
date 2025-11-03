package uz.javachi.autonline.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uz.javachi.autonline.dto.response.NewsResponse;
import uz.javachi.autonline.exceptions.ResourceNotFoundException;
import uz.javachi.autonline.model.News;
import uz.javachi.autonline.repository.NewsRepository;

import java.util.List;
import java.util.Optional;

import static uz.javachi.autonline.model.News.toResponseList;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;


    public ResponseEntity<List<NewsResponse>> getAllActiveNews() {
        Optional<List<News>> newsByIsActive = newsRepository.findNewsByIsActive(true);

        if (newsByIsActive.isEmpty()) {
            throw new ResourceNotFoundException("No active news found!");
        }

        return ResponseEntity.ok(toResponseList(newsByIsActive.get()));
    }
}
