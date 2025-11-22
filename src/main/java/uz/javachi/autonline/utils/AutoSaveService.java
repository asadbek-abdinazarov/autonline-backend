//package uz.javachi.autonline.utils;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import uz.javachi.autonline.model.*;
//import uz.javachi.autonline.repository.LessonRepository;
//
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class AutoSaveService {
//    private final LessonRepository lessonRepository;
//
//    public void saveQuestion() {
//        for (int n = 3; n <= 42; n++) {
//            String url = "https://api.rulionline.uz/api/lesson/%s/question?random=null".formatted(n); // API URL
//            String accessToken = "5024754|B2rGQNj5CU6ejXpOlOjxrWVRiO8pwwrTxvbyCoWG35938e3b"; // access token
//
//            try (HttpClient client = HttpClient.newHttpClient()) {
//
//
//                HttpRequest request = HttpRequest.newBuilder()
//                        .uri(new URI(url))
//                        .GET()
//                        .header("Authorization", "Bearer " + accessToken) // token header
//                        .build();
//
//
//                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//                String responseBody = response.body();
//
//                ObjectMapper objectMapper = new ObjectMapper();
//                objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//
//                JsonNode rootNode = objectMapper.readTree(responseBody);
//
//                if (rootNode.isObject()) {
//                    JsonNode lessonNode = rootNode;
//                    String lessonName = lessonNode.has("lesson") ? lessonNode.get("lesson").asText() : null;
//                    JsonNode dataArray = lessonNode.get("data");
//
//                    Lesson lesson = new Lesson();
//                    LessonTranslation lessonTranslation = new LessonTranslation("oz", lessonName);
//                    lessonTranslation.setLesson(lesson);
//                    lesson.setTranslations(List.of(lessonTranslation));
//                    lesson.setViewsCount(0L);
//                    lesson.setQuestions(new ArrayList<>());
//
//                    if (dataArray != null && dataArray.isArray()) {
//                        for (JsonNode dataNode : dataArray) {
//                            // Photo va question ma'lumotlarini olish
//                            String photo = (dataNode.has("photo") && !dataNode.get("photo").isNull())
//                                    ? dataNode.get("photo").asText() : null;
//
//                            JsonNode questionNode = dataNode.get("question");
//                            String questionUz = getTextOrNull(questionNode, "uz");
//                            String questionOz = getTextOrNull(questionNode, "oz");
//                            String questionRu = getTextOrNull(questionNode, "ru");
//
//                            // Question yaratish
//                            Question question = new Question();
//                            question.setLesson(lesson);
//                            question.setPhoto(photo);
//                            question.setTranslations(new ArrayList<>());
//                            question.setVariants(new ArrayList<>());
//
//                            // Question translations
//                            addQuestionTranslation(question, "uz", questionUz);
//                            addQuestionTranslation(question, "oz", questionOz);
//                            addQuestionTranslation(question, "ru", questionRu);
//
//                            // Javoblarni olish
//                            JsonNode answersNode = dataNode.get("answers");
//                            int correctIndex = answersNode.get("status").asInt() - 1;
//                            JsonNode answerNode = answersNode.get("answer");
//
//                            List<String> answersUz = jsonArrayToList(answerNode.get("uz"));
//                            List<String> answersOz = jsonArrayToList(answerNode.get("oz"));
//                            List<String> answersRu = jsonArrayToList(answerNode.get("ru"));
//
//                            // Har bir javob varianti uchun Variant yaratish
//                            int maxSize = Math.max(answersUz.size(),
//                                    Math.max(answersOz.size(), answersRu.size()));
//
//                            for (int i = 0; i < maxSize; i++) {
//                                Variant variant = new Variant();
//                                variant.setQuestion(question);
//                                variant.setIsCorrect(i == correctIndex);
//                                variant.setTranslations(new ArrayList<>());
//
//                                // Har bir til uchun translation qo'shish
//                                if (i < answersUz.size()) {
//                                    addVariantTranslation(variant, "uz", answersUz.get(i));
//                                }
//                                if (i < answersOz.size()) {
//                                    addVariantTranslation(variant, "oz", answersOz.get(i));
//                                }
//                                if (i < answersRu.size()) {
//                                    addVariantTranslation(variant, "ru", answersRu.get(i));
//                                }
//
//                                question.getVariants().add(variant);
//                            }
//
//                            lesson.getQuestions().add(question);
//                        }
//                    }
//
//                    System.out.println("N: " + n + " " + lessonName + " saqlandi");
//
//                    lessonRepository.save(lesson);
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private static String getTextOrNull(JsonNode node, String field) {
//        return (node != null && node.has(field) && !node.get(field).isNull())
//                ? node.get(field).asText() : null;
//    }
//
//    private static List<String> jsonArrayToList(JsonNode arrayNode) {
//        List<String> list = new ArrayList<>();
//        if (arrayNode != null && arrayNode.isArray()) {
//            arrayNode.forEach(n -> list.add(n.asText()));
//        }
//        return list;
//    }
//
//    private static void addQuestionTranslation(Question question, String lang, String text) {
//        if (text != null) {
//            QuestionTranslation translation = QuestionTranslation.builder()
//                    .lang(lang)
//                    .questionText(text)
//                    .question(question)
//                    .build();
//            question.getTranslations().add(translation);
//        }
//    }
//
//    private static void addVariantTranslation(Variant variant, String lang, String text) {
//        if (text != null) {
//            VariantTranslation translation = VariantTranslation.builder()
//                    .lang(lang)
//                    .text(text)
//                    .variant(variant)
//                    .build();
//            variant.getTranslations().add(translation);
//        }
//    }
//}
