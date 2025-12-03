package com.swe.core.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;

/**
 * Resolves where logs should be stored on the file system for the current
 * operating system and application name.
 *
 * <p>
 * A custom directory may be provided via the {@code swecomm.logdir} system
 * property (useful for tests and CI). When that property is absent the
 * resolver follows platform conventions for Linux, macOS and Windows.
 * </p>
 */
public final class LogPathResolver {

    /**
     * System property used to override log directory location (mainly for tests).
     */
    public static final String LOG_DIR_OVERRIDE_PROP = "swecomm.logdir";

    private final String appName;

    /**
     * Creates a new resolver for the given application name.
     *
     * @param applicationName application name used in the resolved path
     */
    public LogPathResolver(final String applicationName) {
        this.appName = Objects.requireNonNull(applicationName, "applicationName");
    }

    /**
     * Resolve the final log file path for a specific application start timestamp.
     * The method ensures the parent directories exist.
     *
     * @param appStartMillis application start timestamp in epoch millis
     * @return path to the log file
     * @throws IOException if the directories cannot be created
     */
    public Path resolve(final long appStartMillis) throws IOException {
        final Path baseDir = resolveBaseDirectory();
        Files.createDirectories(baseDir);
        return baseDir.resolve(appStartMillis + ".log").toAbsolutePath();
    }

    /**
     * Determine the base directory where logs should be written.
     *
     * @return the base directory path (not including the filename)
     */
    public Path resolveBaseDirectory() {
        final String override = System.getProperty(LOG_DIR_OVERRIDE_PROP);
        if (override != null && !override.isBlank()) {
            return Paths.get(override).toAbsolutePath();
        }

        final String osName = System.getProperty("os.name", "generic");
        final String home = System.getProperty("user.home", ".");
        final String localAppData = System.getenv("LOCALAPPDATA");
        final String appData = System.getenv("APPDATA");

        return resolveBaseDirectory(osName, Paths.get(home), localAppData, appData, appName);
    }

    /**
     * Helper to test whether a string is non-empty (not null and not blank).
     *
     * @param value candidate string
     * @return {@code true} if the value is non-null and not blank
     */
    private static boolean nonEmpty(final String value) {
        return value != null && !value.isBlank();
    }

    /**
     * Platform-specific base directory resolver.
     *
     * @param osName       operating system name (may be null)
     * @param homePath     home directory path
     * @param localAppData LOCALAPPDATA environment variable
     * @param appData      APPDATA environment variable
     * @param appNameParam application name to include in path
     * @return resolved base directory path
     */
    public static Path resolveBaseDirectory(
            final String osName,
            final Path homePath,
            final String localAppData,
            final String appData,
            final String appNameParam) {

        final String normalizedOs = osName == null ? "generic" : osName.toLowerCase(Locale.ROOT);

        if (normalizedOs.contains("linux")) {
            return homePath.resolve(".local/share").resolve(appNameParam).resolve("logs");
        }
        if (normalizedOs.contains("mac") || normalizedOs.contains("darwin")) {
            return homePath.resolve("Library/Logs").resolve(appNameParam);
        }
        if (normalizedOs.contains("win")) {
            final Path base = nonEmpty(localAppData)
                    ? Paths.get(localAppData)
                    : nonEmpty(appData) ? Paths.get(appData) : homePath;
            return base.resolve(appNameParam).resolve("logs");
        }

        return homePath.resolve(appNameParam).resolve("logs");
    }

    // Prevent instantiation without app name
    // (constructor above already enforces non-null appName).
}
