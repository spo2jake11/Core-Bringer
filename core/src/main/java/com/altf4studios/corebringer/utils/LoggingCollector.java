package com.altf4studios.corebringer.utils;

import java.util.ArrayList;
import java.util.List;

public class LoggingCollector {
    private static final List<String> logs = new ArrayList<>();

    public static void add(String message) {
        logs.add(message);
    }

    public static List<String> getLogs() {
        return new ArrayList<>(logs); // return a copy to prevent external modification
    }

    public static String getLogsAsString() {
        return String.join("\n", logs);
    }

    public static void clear() {
        logs.clear();
    }
}
