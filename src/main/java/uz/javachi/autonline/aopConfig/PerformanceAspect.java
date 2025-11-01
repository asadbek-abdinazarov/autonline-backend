package uz.javachi.autonline.aopConfig;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class PerformanceAspect {

    @Around("@annotation(uz.javachi.autonline.customAnnotations.Loggable)")
    public Object measureExecutionTime(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = pjp.proceed(); // asosiy metodni bajaradi
        long end = System.currentTimeMillis();
        log.info("⏱️ {} ishlash vaqti: {} ms", pjp.getSignature().getName(), (end - start));
        return result;
    }
}
