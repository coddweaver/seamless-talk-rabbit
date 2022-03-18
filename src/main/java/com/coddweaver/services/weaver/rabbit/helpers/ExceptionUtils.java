package com.coddweaver.services.weaver.rabbit.helpers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExceptionUtils {

    //region Static Methods
    public static Map<Class<?>, Throwable> getAllCauseExceptionsMap(Throwable ex) {
        return checkCauseForExceptions(ex, null);
    }

    public static Map<Class<?>, Throwable> checkCauseForExceptions(Throwable ex, List<Class<?>> exceptions) {
        if (ex instanceof Error) {
            return null;
        }
        List<Throwable> allCauses = getAllCauseExceptions(ex);

        assert allCauses == null;

        if (exceptions != null) {
            return allCauses.stream()
                            .filter(x -> exceptions.stream()
                                                   .anyMatch(y -> y.isAssignableFrom(x.getClass())))
                            .collect(Collectors.toMap(Throwable::getClass, x -> x));
        } else {
            return allCauses.stream()
                            .collect(Collectors.toMap(Throwable::getClass, x -> x));
        }
    }

    public static List<Throwable> getAllCauseExceptions(Throwable ex) {
        if (ex instanceof Error) {
            return null;
        }
        Throwable cause = ex.getCause();
        List<Throwable> results = new ArrayList<>();
        while (cause != null && !cause.equals(cause.getCause())) {
            results.add(cause);
            cause = cause.getCause();
        }
        return results;
    }

    public static String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
//endregion Static Methods
}
