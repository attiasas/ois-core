package org.ois.core.utils.log;

/**
 * Interface for logging messages at various levels of severity.
 * Implementations of this interface should provide functionality to log
 * messages categorized by different log levels, including debug, info,
 * warn, and error.
 */
public interface ILogger {
    /** Environment variable used by OIS products to set the log level **/
    String ENV_LOG_LEVEL = "OIS_LOG_LEVEL";
    /** Environment variable used by OIS products to set the log topics **/
    String ENV_LOG_TOPICS = "OIS_LOG_TOPICS";
    /** The default level of the logger **/
    Level DEFAULT_LEVEL = Level.Info;

    /**
     * Enumeration representing the different log levels.
     */
    enum Level {
        Debug, Info, Warn, Error
    }

    /**
     * Converts a string representation of a log level to its corresponding
     * {@link Level} enum value.
     *
     * @param logLevel the string representation of the log level (e.g., "debug", "warn", "error")
     * @return the corresponding {@link Level} enum value
     */
    static Level toLogLevel(String logLevel) {
        if (logLevel == null) {
            return DEFAULT_LEVEL;
        }
        switch (logLevel.trim().toLowerCase()) {
            case "debug":
                return Level.Debug;
            case "info":
                return Level.Info;
            case "warn":
                return Level.Warn;
            case "error":
                return Level.Error;
            default:
                return DEFAULT_LEVEL;
        }
    }

    /**
     * Logs a debug message with a specified topic.
     *
     * @param topic   the topic associated with the log message
     * @param message the debug message to be logged
     */
    void debug(String topic, String message, Object ...args);
    /**
     * Logs a debug message without a specific topic.
     *
     * @param message the debug message to be logged
     */
    void debug(String message, Object ...args);
    /**
     * Logs an informational message with a specified topic.
     *
     * @param topic   the topic associated with the log message
     * @param message the informational message to be logged
     */
    void info(String topic, String message, Object ...args);
    /**
     * Logs an informational message without a specific topic.
     *
     * @param message the informational message to be logged
     */
    void info(String message, Object ...args);
    /**
     * Logs a warning message.
     *
     * @param message the warning message to be logged
     */
    void warn(String message, Object ...args);
    /**
     * Logs an error message.
     *
     * @param message the error message to be logged
     */
    void error(String message, Object ...args);
    /**
     * Logs an error message along with the associated exception.
     *
     * @param message   the error message to be logged
     * @param exception the throwable associated with the error
     */
    void error(String message, Throwable exception);
}
