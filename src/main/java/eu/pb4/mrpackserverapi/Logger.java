package eu.pb4.mrpackserverapi;

import java.util.Arrays;

public interface Logger {
    String LOG_PREFIX = "";
    String LOG_WARN_PREFIX = "[WARN] ";
    String LOG_ERROR_PREFIX = "[ERROR] ";
    boolean ALLOW_INLINE = false;
    static void info(String text, Object... objects) {
        System.out.printf(LOG_PREFIX + (text) + " %n", objects);
    }

    static void label(String text, Object... objects) {
        System.out.append((text + " ").formatted(objects));
    }

    static void warn(String text, Object... objects) {
        System.out.printf(LOG_WARN_PREFIX + (text) + " %n", objects);
    }

    static void error(String text, Object... objects) {
        Throwable throwable = null;
        if (objects.length > 0 && objects[objects.length - 1] instanceof Throwable x) {
            objects = Arrays.copyOf(objects, objects.length - 1);
            throwable = x;
        }

        System.err.printf(LOG_ERROR_PREFIX + (text) + " %n", objects);
        if (throwable != null) {
            try {
                throwable.printStackTrace(System.err);
            } catch (Throwable e) {
                System.err.println("Failed to write exception! Using a fallback!");
                System.err.println("Class: " + throwable.getClass().getName());
                for (var traceElement : e.getStackTrace()) {
                    System.err.println("\tat " + traceElement);
                }
            }
        }
    }
}
