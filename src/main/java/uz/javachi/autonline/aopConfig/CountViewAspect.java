package uz.javachi.autonline.aopConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import uz.javachi.autonline.customAnnotations.CountView;
import uz.javachi.autonline.service.LessonService;

import static uz.javachi.autonline.utils.SecurityUtils.getCurrentUserId;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class CountViewAspect {

    private final LessonService lessonService;

    @Around("@annotation(countView)")
    public Object countTopicView(ProceedingJoinPoint pjp, CountView countView) throws Throwable {
        log.info("📊 CountView AOP ishladi: {}", pjp.getSignature().toShortString());
        Object[] args = pjp.getArgs();

        Integer topicId = null;
        for (Object arg : args) {
            if (arg instanceof Integer) {
                topicId = (Integer) arg;
                break;
            }
        }

        Object result = pjp.proceed();

        if (topicId != null) {
//            lessonService.recordView(topicId, getCurrentUserId());
        }

        return result;
    }

}
