package uz.javachi.autonline.utils;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import uz.javachi.autonline.model.Question;
import uz.javachi.autonline.repository.QuestionRepository;
import uz.javachi.autonline.service.StorageService;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class ChangeBucketLocation {

    private final QuestionRepository questionRepository;
    private final StorageService storageService;
    private static final Logger log = LoggerFactory.getLogger(ChangeBucketLocation.class);
    private static final String BASE_URL = "https://api.rulionline.uz/storage/";
    private static final String UPLOAD_PATH = "images/qp";


    /**
     * Migrates all question photos from old storage to new storage.
     */
    public void migrateQuestionPhotos() {
        log.info("Starting photo migration process");

        List<Question> questions = questionRepository.findQuestionByPhotoIsNotNull();
        log.info("Found {} questions with photos to migrate", questions.size());

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (Question question : questions) {
            try {
                migrateQuestionPhoto(question);
                successCount.incrementAndGet();

                if (successCount.get() % 100 == 0) {
                    log.info("Progress: {} questions processed successfully", successCount.get());
                }
            } catch (Exception e) {
                failureCount.incrementAndGet();
                log.error("Failed to migrate photo for question ID: {}", question.getQuestionId(), e);
            }
        }

        log.info("Migration completed. Success: {}, Failures: {}", successCount.get(), failureCount.get());
    }

    /**
     * Migrates a single question's photo.
     * Transaction is managed at repository level via @Modifying + @Transactional
     */
    public void migrateQuestionPhoto(Question question) throws Exception {
        String photo = question.getPhoto();

        if (photo == null || photo.trim().isEmpty()) {
            throw new IllegalArgumentException("Photo path is null or empty");
        }

        String sourceUrl = BASE_URL + photo;
        log.debug("Downloading photo from: {}", sourceUrl);

        MultipartFile fileFromUrl = getFileFromUrl(sourceUrl);

        log.debug("Uploading photo for question ID: {}", question.getQuestionId());
        String uploadedPath = storageService.uploadFile(fileFromUrl, UPLOAD_PATH).get();

        int rowsUpdated = questionRepository.updatePhoto(uploadedPath, question.getQuestionId());

        if (rowsUpdated != 1) {
            throw new IllegalStateException(
                    String.format("Expected to update 1 row but updated %d rows for question ID: %d",
                            rowsUpdated, question.getQuestionId())
            );
        }

        log.debug("Successfully migrated photo for question ID: {}", question.getQuestionId());
    }


    public MultipartFile getFileFromUrl(String fileUrl) {
        URL url;
        try {
            url = new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        try (InputStream inputStream = url.openStream()) {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            String contentType = URLConnection.guessContentTypeFromName(fileName);
            if (contentType == null) contentType = "application/octet-stream";

            return new MockMultipartFile(
                    "file",
                    fileName,
                    contentType,
                    inputStream
            );
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}