package uz.javachi.autonline.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.web.servlet.LocaleResolver;
import uz.javachi.autonline.enums.Lang;

import java.util.Locale;

public class CustomLocaleResolver implements LocaleResolver {

    @Override
    public Locale resolveLocale(@NonNull HttpServletRequest request) {

        String lang = request.getParameter("lang");
        if (lang != null && !lang.isEmpty()) {
            return checkLangIsValid(Locale.forLanguageTag(lang));
        }

        String headerLang = request.getHeader("Accept-Language");
        if (headerLang != null && !headerLang.isEmpty()) {
            return checkLangIsValid(Locale.forLanguageTag(headerLang));
        }

        return Locale.of("uz");
    }

    private Locale checkLangIsValid(Locale locale) {
        for (Lang lang : Lang.values()) {
            if (lang.name().equalsIgnoreCase(locale.getLanguage())) {
                return locale;
            }
        }
        return Locale.of("uz");
    }

    @Override
    public void setLocale(@NonNull HttpServletRequest request, HttpServletResponse response, Locale locale) {
    }
}
