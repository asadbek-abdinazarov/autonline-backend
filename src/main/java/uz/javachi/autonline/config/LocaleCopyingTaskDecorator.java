package uz.javachi.autonline.config;

import lombok.NonNull;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.task.TaskDecorator;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class LocaleCopyingTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(@NonNull Runnable runnable) {
        Locale locale = LocaleContextHolder.getLocale();
        return () -> {
            try {
                LocaleContextHolder.setLocale(locale);
                runnable.run();
            } finally {
                LocaleContextHolder.resetLocaleContext();
            }
        };
    }
}
