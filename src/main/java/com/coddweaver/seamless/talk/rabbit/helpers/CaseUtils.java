package com.coddweaver.seamless.talk.rabbit.helpers;

import com.google.common.base.CaseFormat;

import java.util.regex.Pattern;

/**
 * Contains some useful methods for case translating. Based on {@link CaseFormat}.
 * <p>{@link CaseUtils#convert(String, CaseFormat)} can automatically detect current CaseFormat using regex.
 *
 * @author Andrey Buturlakin
 * @see CaseFormat
 */
public class CaseUtils {

    private final static Pattern camelPattern = Pattern.compile("[a-z]+(?:[A-Z0-9]+[a-z0-9]+[A-Za-z0-9]*)*");
    private final static Pattern pascalPattern = Pattern.compile("(?:[A-Z][a-z0-9]+)(?:[A-Z]+[a-z0-9]*)*");
    private final static Pattern snakePattern = Pattern.compile("[a-z0-9]+(?:_[a-z0-9]+)*");
    private final static Pattern screamingSnakePattern = Pattern.compile("[A-Z0-9]+(?:_[A-Z0-9]+)*");
    private final static Pattern kebabPattern = Pattern.compile("[a-z0-9]+(?:-[a-z0-9]+)*");

    /**
     * Converts string from specified to selected format.
     *
     * @param source    source string.
     * @param srcFormat specified format.
     * @param dstFormat selected format.
     *
     * @return converted string.
     */
    public static String convert(String source, CaseFormat srcFormat, CaseFormat dstFormat) {
        return srcFormat.to(dstFormat, source);
    }

    /**
     * Converts string to selected format.
     *
     * @param source    source string.
     * @param dstFormat selected format.
     *
     * @return converted string.
     */
    public static String convert(String source, CaseFormat dstFormat) {
        return detectCase(source).to(dstFormat, source);
    }

    public static String firstToUpper(String str) {
        return str.substring(0, 1)
                  .toUpperCase() + str.substring(1);
    }

    public static String firstToLower(String str) {
        return str.substring(0, 1)
                  .toUpperCase() + str.substring(1);
    }

    private static CaseFormat detectCase(String source) {
        if (camelPattern.matcher(source)
                        .replaceAll("")
                        .length() == 0) {
            return CaseFormat.LOWER_CAMEL;
        }
        if (pascalPattern.matcher(source)
                         .replaceAll("")
                         .length() == 0) {
            return CaseFormat.UPPER_CAMEL;
        }
        if (snakePattern.matcher(source)
                        .replaceAll("")
                        .length() == 0) {
            return CaseFormat.LOWER_UNDERSCORE;
        }
        if (screamingSnakePattern.matcher(source)
                                 .replaceAll("")
                                 .length() == 0) {
            return CaseFormat.UPPER_UNDERSCORE;
        }
        if (kebabPattern.matcher(source)
                        .replaceAll("")
                        .length() == 0) {
            return CaseFormat.LOWER_HYPHEN;
        }

        throw new UnsupportedOperationException("Undetected case of string " + source);
    }

}
