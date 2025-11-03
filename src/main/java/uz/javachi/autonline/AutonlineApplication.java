package uz.javachi.autonline;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Slf4j
@SpringBootApplication
@EnableAspectJAutoProxy
public class AutonlineApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutonlineApplication.class, args);
    }


   /* @PostConstruct
    public void init() throws URISyntaxException {

        String bearerToken = "3695532|VsJKlWSvAJSHCIzrOH73LZMt0wWtOkvZ8nVRNMzlefb9d218";

        HttpClient client = HttpClient.newHttpClient();

        for (int i = 1; i <= 42; i++) {
            String apiUrl = "https://api.rulionline.uz/api/lesson/%s/question?random=null".formatted(i);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Authorization", "Bearer " + bearerToken)
                    .GET()
                    .build();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                ObjectMapper mapper = new ObjectMapper();

                LessonResponse lessonResponse = mapper.readValue(response.body(), LessonResponse.class);

                System.out.println("Lesson: " + lessonResponse.getLesson());
                Lesson lesson = new Lesson();
                lesson.setLessonName(lessonResponse.getLesson());

                List<Question> questions = new ArrayList<>();
                for (QuestionData q : lessonResponse.getData()) {
                    Question question = new Question();
                    question.setPhoto(q.getPhoto());

                    question.setLesson(lesson);

                    uz.javachi.autonline.dto.QuestionText question1 = q.getQuestion();
                    QuestionText questionText = new QuestionText();
                    questionText.setUz(question1.getUz());
                    questionText.setRu(question1.getRu());
                    questionText.setOz(question1.getOz());
                    question.setQuestionText(questionText);
                    questionText.setQuestion(question);

                    uz.javachi.autonline.dto.Answers answers1 = q.getAnswers();
                    if (answers1 != null) {
                        Answers answers = new Answers();
                        answers.setStatus(answers1.getStatus());
                        answers.setQuestion(question);

                        uz.javachi.autonline.dto.AnswerText answer = answers1.getAnswer();
                        if (answer != null) {
                            AnswerText answerText = new AnswerText();
                            answerText.setUz(answer.getUz());
                            answerText.setRu(answer.getRu());
                            answerText.setOz(answer.getOz());
                            answers.setAnswerText(answerText);
                            answerText.setAnswer(answers);
                        }

                        question.setAnswers(answers);
                    }

                    questions.add(question);
                }

                lesson.setQuestions(questions);

            String jsonLesson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(lesson);
            System.out.println(jsonLesson);
                lessonRepository.save(lesson);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }*/

    @PostConstruct
    public void init() {
        log.info("✅Dastur ishga tushirildi...");
    }

    @PreDestroy
    public void destroy() {
        log.info("Dastur ishlashdan to'xtatildi...");
    }

}
