package com.coddweaver.seamless.talk.rabbit.helpers;

import org.springframework.core.NestedExceptionUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Contains some useful methods for exceptions.
 *
 * @author Andrey Buturlakin
 */
public class ExceptionUtils {

    public static String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    public static Throwable getRootCause(Throwable e) {
        if (e.getCause() == null) {
            return e;
        }

        return NestedExceptionUtils.getRootCause(e);
    }
}
