package chatsummary;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Utility class to load configuration from environment file.
 */
public final class EnvConfig {
    private static Properties properties = new Properties();

    private static boolean loaded = false;

    private static final String DEFAULT_ENV_FILE = ".env.temp";

    private EnvConfig() {

    }

    public static void loadEnv() {
        loadEnv(DEFAULT_ENV_FILE);
    }

    public static void loadEnv(final String envFileName) {
        if (loaded) {
            return; // Already loaded
        }

        try {

            if (Files.exists(Paths.get(envFileName))) {

                try (InputStream input = Files.newInputStream(Paths.get(envFileName))) {
                    properties.load(input);
                    loaded = true;
                    System.out.println("Environment configuration loaded from project root: "
                            + envFileName);
                    return;
                }
            }


            try (InputStream input = EnvConfig.class.getClassLoader().getResourceAsStream(envFileName)) {
                if (input != null) {
                    properties.load(input);
                    loaded = true;
                    System.out.println("Environment configuration loaded from resources: "
                            + envFileName);
                    return;
                }
            }


            try (InputStream input = EnvConfig.class.getResourceAsStream("/" + envFileName)) {
                if (input != null) {
                    properties.load(input);
                    loaded = true;
                    System.out.println("Environment configuration loaded from classpath root: "
                            + envFileName);
                    return;
                }
            }

            // If nothing found, show warning
            System.err.println("Warning: Environment file not found: " + envFileName);
            System.err.println("Searched in:");
            System.err.println("  1. Project root: ./" + envFileName);
            System.err.println("  2. Resources: src/main/resources/" + envFileName);
            System.err.println("  3. Classpath: /" + envFileName);
            System.err.println("Using system environment variables as fallback");

        } catch (IOException e) {
            System.err.println("Error loading environment file: " + e.getMessage());
            System.err.println("Using system environment variables as fallback");
        }
    }

    public static String getEnv(final String key) {
        if (!loaded) {
            loadEnv();
        }


        String value = properties.getProperty(key);


        if (value == null) {
            value = System.getenv(key);
        }

        return value;
    }


    public static String getEnv(final String key, final String defaultValue) {
        final String value = getEnv(key);
        if (value != null) {
            return value;
        } else {
            return defaultValue;
        }
    }


    public static int getEnvAsInt(final String key, final int defaultValue) {
        final String value = getEnv(key);
        if (value == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.err.println("Warning: Invalid integer value for " + key + ": " + value);
            return defaultValue;
        }
    }


    public static void requireEnv(final String key) {
        final String value = getEnv(key);
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException("Required environment variable not set: " + key);
        }
    }


    public static void reload() {
        properties.clear();
        loaded = false;
        loadEnv();
    }
}