package uz.javachi.autonline.aopConfig;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    // 1️⃣ Pointcut — qayerda ishlashini aniqlaymiz
//    @Pointcut("execution(* uz.javachi.autonline.service..*(..))")
    @Pointcut("@annotation(uz.javachi.autonline.customAnnotations.Loggable)")
    public void serviceMethods() {
    }

    // 2️⃣ Before advice — metod chaqirilishidan oldin ishlaydi
    @Before("serviceMethods()")
    public void logBefore(JoinPoint joinPoint) {
        log.info("➡️ {} metod chaqirildi. Parametrlar: {}",
                joinPoint.getSignature().toShortString(),
                Arrays.toString(joinPoint.getArgs()));
    }

    // 3️⃣ AfterReturning — metod muvaffaqiyatli tugagach
    @AfterReturning(pointcut = "serviceMethods()", returning = "result")
    public void logAfter(JoinPoint joinPoint, Object result) {
        log.info("✅ {} tugadi. Natija: {}",
                joinPoint.getSignature().toShortString(), result);
    }

    // 4️⃣ AfterThrowing — xatolik bo‘lganda
    @AfterThrowing(pointcut = "serviceMethods()", throwing = "error")
    public void logException(JoinPoint joinPoint, Throwable error) {
        log.error("❌ {} xato bilan tugadi: {}",
                joinPoint.getSignature().toShortString(), error.getMessage());
    }

}
