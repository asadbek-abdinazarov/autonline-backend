package uz.javachi.autonline;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
import uz.javachi.autonline.config.LocaleCopyingTaskDecorator;

import java.util.TimeZone;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@Slf4j
@EnableAsync
@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@EnableAspectJAutoProxy
public class AutonlineApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutonlineApplication.class, args);
    }


    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tashkent"));
        TimeZone aDefault = TimeZone.getDefault();
        log.info("Using default timezone: {}", aDefault);
        log.info("Using default timezone: {}", aDefault.getDisplayName());
        log.info("✅Dastur ishga tushirildi...");
    }

    @PreDestroy
    public void destroy() {
        log.info("Dastur ishlashdan to'xtatildi...");
    }

    @Bean(name = "applicationTaskExecutor")
    public AsyncTaskExecutor taskExecutor(LocaleCopyingTaskDecorator decorator) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setTaskDecorator(decorator);
        executor.setThreadNamePrefix("AsyncExecutor-");
        executor.initialize();

        return new DelegatingSecurityContextAsyncTaskExecutor(executor);
    }
}
