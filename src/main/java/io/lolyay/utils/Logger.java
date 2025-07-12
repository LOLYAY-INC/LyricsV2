package io.lolyay.utils;


import java.time.LocalTime;

public class Logger {
    private static String formatTime() {
        LocalTime time = LocalTime.now();
        return String.format("[%02d:%02d:%02d]", time.getHour(), time.getMinute(), time.getSecond());
    }

    private static String formatLogMessage(String colorCode, String level, String message) {
        String consoleMessage = colorCode + formatTime() + " [" + level + "] " + message;
        String fileMessage = formatTime() + " [" + level + "] " + message;
        return consoleMessage + Color.END.getCode(); // Reset color
    }

    public static void log(String message) {
        String logMessage = formatLogMessage(Color.LIGHT_WHITE.getCode(), "LOG", message);
        System.out.println(logMessage);
    }

    public static void warn(String message) {
        String warnMessage = formatLogMessage(Color.YELLOW.getCode(), "WARN", message);
        System.out.println(warnMessage);
    }

    public static void err(String message) {
        String errMessage = formatLogMessage(Color.RED.getCode(), "ERR", message);
        System.out.println(errMessage);
    }

    public static void debug(String message) {
        String debugMessage = formatLogMessage(Color.LIGHT_GRAY.getCode(), "DEBUG", message);
        System.out.println(debugMessage);
    }

    public static void success(String message) {
        String successMessage = formatLogMessage(Color.GREEN.getCode(), "SUCCESS", message);
        System.out.println(successMessage);
    }

}