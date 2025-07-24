package com.altf4studios.corebringer.utils;

import com.badlogic.gdx.Gdx;

public class LoggingUtils {
    public static void log(String tag, String message) {
        String formatted = "[" + formatTag(tag) + "] " + message;
        Gdx.app.log(formatTag(tag), message);
        LoggingCollector.add(formatted);
    }

    public static void debug(String tag, String message) {
        String formatted = "[" + formatTag(tag) + " DEBUG] " + message;
        Gdx.app.debug(formatTag(tag), message);
        LoggingCollector.add(formatted);
    }

    public static void error(String tag, String message) {
        String formatted = "[" + formatTag(tag) + " ERROR] " + message;
        Gdx.app.error(formatTag(tag), message);
        LoggingCollector.add(formatted);
    }

    public static void error(String tag, String message, Throwable throwable) {
        String formatted = "[" + formatTag(tag) + " ERROR] " + message + " :: " + throwable.getMessage();
        Gdx.app.error(formatTag(tag), message, throwable);
        LoggingCollector.add(formatted);
    }

    private static String formatTag(String tag) {
        return "CoreBringer/" + tag;
    }
}
