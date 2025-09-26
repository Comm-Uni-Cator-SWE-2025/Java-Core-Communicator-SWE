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
    /** Properties loaded from environment file. */
    private static Properties properties = new Properties();
    /** Flag to track if configuration has been loaded. */
    private static boolean loaded = false;

    /** Default environment file name. */
    private static final String DEFAULT_ENV_FILE = ".env.temp";

    /**
     * Private constructor to prevent instantiation.
     */
    private EnvConfig() {

    }

    /**
     * Load environment variables from the default .env.temp file.
     */
    public static void loadEnv() {
        loadEnv(DEFAULT_ENV_FILE);
    }

    /**
     * Load environment variables from specified file.
     *
     * @param envFileName Name of the environment file
     */
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

    /**
     * Get environment variable value.
     *
     * @param key Environment variable key
     * @return Value from env file, system env, or null
     */
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

    /**
     * Get environment variable with default value.
     *
     * @param key Environment variable key
     * @param defaultValue Default value if not found
     * @return Value from env file, system env, or default value
     */
    public static String getEnv(final String key, final String defaultValue) {
        final String value = getEnv(key);
        if (value != null) {
            return value;
        } else {
            return defaultValue;
        }
    }

    /**
     * Get environment variable as integer.
     *
     * @param key Environment variable key
     * @param defaultValue Default value if not found or not a valid integer
     * @return Integer value from env or default
     */
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

    /**
     * Check if a required environment variable is set.
     *
     * @param key Environment variable key
     * @throws RuntimeException if the required variable is not set
     */
    public static void requireEnv(final String key) {
        final String value = getEnv(key);
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException("Required environment variable not set: " + key);
        }
    }

    /**
     * Reload environment configuration (useful for testing).
     */
    public static void reload() {
        properties.clear();
        loaded = false;
        loadEnv();
    }
}