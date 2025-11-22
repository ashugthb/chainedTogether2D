package com.chainedclimber;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;

public class MinimalLauncher {

    private static final DateTimeFormatter TF = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());

    public static void main(String[] args) {
        log("Starting MinimalLauncher");

        log("Timestamp: " + TF.format(Instant.now()));

        logSection("Java & OS Info");
        logProp("java.version");
        logProp("java.vendor");
        logProp("java.home");
        logProp("os.name");
        logProp("os.version");
        logProp("os.arch");

        logSection("Working Directories");
        log("user.dir: " + System.getProperty("user.dir"));
        log("user.home: " + System.getProperty("user.home"));

        logSection("Classpath (first 2000 chars)");
        String cp = System.getProperty("java.class.path");
        if (cp == null) cp = "(null)";
        log(cp.length() > 2000 ? cp.substring(0, 2000) + "..." : cp);

        logSection("Attempting to detect LibGDX classes (Class.forName)");
        tryLoadClass("com.badlogic.gdx.Application");
        tryLoadClass("com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application");
        tryLoadClass("com.chainedclimber.ChainedClimberGame");

        logSection("Environment variables (selected)");
        Map<String, String> env = System.getenv();
        for (String k : new String[]{"PATH","JAVA_HOME","GRADLE_HOME","HOME","USERPROFILE"}) {
            log(k + "=" + env.get(k));
        }

        logSection("Project build/libs and desktop/build/libs contents (if present)");
        listDir("./core/build/libs");
        listDir("./desktop/build/libs");
        listDir("./build/libs");

        logSection("Quick classpath jar probe (searching for libgdx jars)");
        probeClasspathForJar("libgdx");

        log("MinimalLauncher completed.");
    }

    private static void log(String s) {
        System.out.println(TF.format(Instant.now()) + " [MinimalLauncher] " + s);
    }

    private static void logProp(String key) {
        log(key + ": " + System.getProperty(key));
    }

    private static void logSection(String title) {
        log("---- " + title + " ----");
    }

    private static void tryLoadClass(String fqcn) {
        try {
            Class.forName(fqcn);
            log("Class available: " + fqcn);
        } catch (Throwable t) {
            log("Class NOT available: " + fqcn + " -> " + t.getClass().getSimpleName() + ": " + t.getMessage());
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            String trace = sw.toString();
            if (trace.length() > 2000) trace = trace.substring(0, 2000) + "...";
            log(trace);
        }
    }

    private static void listDir(String path) {
        try {
            File d = new File(path);
            if (!d.exists()) {
                log(path + " -> (not found)");
                return;
            }
            if (!d.isDirectory()) {
                log(path + " -> (exists, not a directory)");
                return;
            }
            String[] children = d.list();
            if (children == null) children = new String[0];
            Arrays.sort(children);
            log(path + " -> " + children.length + " entries");
            for (String c : children) {
                log("  - " + c);
            }
        } catch (Throwable t) {
            log(path + " -> (error) " + t.getMessage());
        }
    }

    private static void probeClasspathForJar(String substring) {
        String cp = System.getProperty("java.class.path");
        if (cp == null) cp = "";
        String[] parts = cp.split(File.pathSeparator);
        int found = 0;
        for (String p : parts) {
            if (p.toLowerCase().contains(substring.toLowerCase())) {
                log("classpath contains: " + p);
                found++;
            }
        }
        if (found == 0) log("No classpath entries containing '" + substring + "' found.");
    }
}
