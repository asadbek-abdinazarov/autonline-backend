package uz.javachi.autonline.enums;


import java.util.Arrays;

public enum Lang {

    UZ("UZ"),
    RU("RU"),
    OZ("OZ");

    private final String value;

    Lang(String value) {
        this.value = value;
    }

    public static String getValue(Lang lang) {
        return null;
    }

    public static String getValue(String lang) {

        return null;
    }

    public static Lang getLangEnum(String lang) {
        return Arrays.stream(Lang.values()).filter(
                        (l) -> l.value.equalsIgnoreCase(lang))
                .findFirst()
                .orElse(Lang.UZ);
    }

    public static String getLang(String lang) {
        return Arrays.stream(Lang.values()).filter(
                        (l) -> l.value.equalsIgnoreCase(lang))
                .findFirst()
                .orElse(Lang.UZ).value;
    }

    public static String getLang(Lang lang) {
        return lang.value;
    }

}
