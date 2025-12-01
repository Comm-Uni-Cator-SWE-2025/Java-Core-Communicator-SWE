package com.swe.core.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;

/**
 * Determines where log files should live on each operating system.
 * <p>
 * A custom directory can be provided via the {@code swecomm.logdir} system property which
 * is mainly useful for tests and CI runs. When the property is absent the resolver attempts
 * to follow the platform conventions described in the product spec.
 */
final class LogPathResolver {

    static final String LOG_DIR_OVERRIDE_PROP = "swecomm.logdir";

    private final String appName;

    LogPathResolver(final String appName) {
        this.appName = Objects.requireNonNull(appName, "appName");
    }

    /**
     * Resolves the final log file location for a given start timestamp.
     *
     * @param appStartMillis application start timestamp in epoch millis
     * @return path to the log file, guaranteed to have its parent directories created
     * @throws IOException if the directories cannot be created
     */
    Path resolve(final long appStartMillis) throws IOException {
        final Path baseDir = resolveBaseDirectory();
        Files.createDirectories(baseDir);
        return baseDir.resolve(appStartMillis + ".log").toAbsolutePath();
    }

    private Path resolveBaseDirectory() {
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

    private static boolean nonEmpty(final String value) {
        return value != null && !value.isBlank();
    }

    static Path resolveBaseDirectory(
        final String osName,
        final Path homePath,
        final String localAppData,
        final String appData,
        final String appName
    ) {
        final String normalizedOs = osName == null
            ? "generic"
            : osName.toLowerCase(Locale.ROOT);

        if (normalizedOs.contains("linux")) {
            return homePath.resolve(".local/share").resolve(appName).resolve("logs");
        }
        if (normalizedOs.contains("mac") || normalizedOs.contains("darwin")) {
            return homePath.resolve("Library/Logs").resolve(appName);
        }
        if (normalizedOs.contains("win")) {
            final Path base = nonEmpty(localAppData)
                ? Paths.get(localAppData)
                : nonEmpty(appData) ? Paths.get(appData) : homePath;
            return base.resolve(appName).resolve("logs");
        }

        return homePath.resolve(appName).resolve("logs");
    }
}

