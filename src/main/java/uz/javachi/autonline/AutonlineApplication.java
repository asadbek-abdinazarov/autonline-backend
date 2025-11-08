package uz.javachi.autonline;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@Slf4j
@SpringBootApplication
@EnableAspectJAutoProxy
//@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class AutonlineApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutonlineApplication.class, args);
    }


    @PostConstruct
    public void init() {
        log.info("✅Dastur ishga tushirildi...");
    }

    @PreDestroy
    public void destroy() {
        log.info("Dastur ishlashdan to'xtatildi...");
    }

}
