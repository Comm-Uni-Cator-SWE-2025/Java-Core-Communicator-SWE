package com.swe.core.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Custom formatter that writes human readable log lines while remaining easy to parse.
 *
 * <p>Format per line:
 * <pre>
 * ts=2025-11-17T08:22:51.123Z epoch=1768676571123 level=INFO module=core.analytics thread=main
 * msg="Starting session" ctx="sessionId=abc123" err=""
 * </pre>
 *
 * <p>The first five tokens never contain spaces while the quoted fields use a predictable
 * escaping scheme. {@link #parseLine(String)} provides the inverse operation.
 */
public final class SweLogFormatter extends Formatter {

    private static final DateTimeFormatter ISO_FORMAT =
        DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);

    @Override
    public synchronized String format(final LogRecord record) {
        final SweLogRecord enriched = record instanceof SweLogRecord
            ? (SweLogRecord) record
            : SweLogRecord.from(record);

        final Instant timestamp = Instant.ofEpochMilli(enriched.getMillis());
        final StringBuilder builder = new StringBuilder(256);
        builder.append("ts=").append(ISO_FORMAT.format(timestamp))
            .append(" epoch=").append(enriched.getMillis())
            .append(" level=").append(enriched.getLevel().getName())
            .append(" module=").append(enriched.getModuleTag())
            .append(" thread=").append(enriched.getThreadName())
            .append(" msg=\"").append(escape(formatMessage(enriched))).append('"')
            .append(" ctx=\"").append(escape(enriched.getContext())).append('"')
            .append(" err=\"").append(escape(stackTrace(enriched.getThrown()))).append('"')
            .append(System.lineSeparator());
        return builder.toString();
    }

    /**
     * Parses a single line emitted by {@link #format(LogRecord)} back into a structured entry.
     *
     * @throws IllegalArgumentException if the line cannot be parsed
     */
    public SweLogEntry parseLine(final String line) {
        final Map<String, String> parts = tokenize(line);
        try {
            final Instant timestamp = Instant.parse(require(parts, "ts"));
            final long epochMillis = Long.parseLong(require(parts, "epoch"));
            final Level level = Level.parse(require(parts, "level").toUpperCase(Locale.ROOT));
            final String module = require(parts, "module");
            final String thread = require(parts, "thread");
            final String message = unescape(require(parts, "msg"));
            final String context = unescape(parts.getOrDefault("ctx", ""));
            final String error = unescape(parts.getOrDefault("err", ""));
            return new SweLogEntry(timestamp, epochMillis, level, module, thread, message, context, error);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse log line: " + line, e);
        }
    }

    private static Map<String, String> tokenize(final String line) {
        final Map<String, String> parts = new LinkedHashMap<>();
        int idx = 0;
        final int len = line.length();
        while (idx < len) {
            while (idx < len && Character.isWhitespace(line.charAt(idx))) {
                idx++;
            }
            if (idx >= len) {
                break;
            }
            final int eqIdx = line.indexOf('=', idx);
            if (eqIdx < 0) {
                break;
            }
            final String key = line.substring(idx, eqIdx);
            idx = eqIdx + 1;
            final String value;
            if (idx < len && line.charAt(idx) == '"') {
                final int closing = findClosingQuote(line, idx + 1);
                value = line.substring(idx + 1, closing);
                idx = closing + 1;
            } else {
                final int nextSpace = findSpace(line, idx);
                value = line.substring(idx, nextSpace);
                idx = nextSpace;
            }
            parts.put(key, value);
        }
        return parts;
    }

    private static int findClosingQuote(final String line, final int startIdx) {
        int idx = startIdx;
        while (idx < line.length()) {
            final char ch = line.charAt(idx);
            if (ch == '"' && line.charAt(idx - 1) != '\\') {
                return idx;
            }
            idx++;
        }
        return line.length();
    }

    private static int findSpace(final String line, final int startIdx) {
        int idx = startIdx;
        while (idx < line.length() && !Character.isWhitespace(line.charAt(idx))) {
            idx++;
        }
        return idx;
    }

    private static String require(final Map<String, String> parts, final String key) {
        final String value = parts.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing key: " + key);
        }
        return value;
    }

    private static String escape(final String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        final StringBuilder builder = new StringBuilder(value.length());
        for (char ch : value.toCharArray()) {
            switch (ch) {
                case '\\' -> builder.append("\\\\");
                case '"' -> builder.append("\\\"");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> builder.append(ch);
            }
        }
        return builder.toString();
    }

    private static String unescape(final String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        final StringBuilder builder = new StringBuilder(value.length());
        boolean escaping = false;
        for (char ch : value.toCharArray()) {
            if (escaping) {
                builder.append(switch (ch) {
                    case '\\' -> '\\';
                    case '"' -> '"';
                    case 'n' -> '\n';
                    case 'r' -> '\r';
                    case 't' -> '\t';
                    default -> ch;
                });
                escaping = false;
            } else if (ch == '\\') {
                escaping = true;
            } else {
                builder.append(ch);
            }
        }
        if (escaping) {
            builder.append('\\');
        }
        return builder.toString();
    }

    private static String stackTrace(final Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        final StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString().trim();
    }
}

