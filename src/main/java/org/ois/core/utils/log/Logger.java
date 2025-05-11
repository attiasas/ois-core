package org.ois.core.utils.log;

import com.badlogic.gdx.Gdx;
import org.ois.core.OIS;
import org.ois.core.runner.RunnerConfiguration;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Logger implementation for logging messages with various severity levels.
 * This class allows logging messages categorized by debug, info, warn, and error levels.
 * It supports filtering logs by topics and setting the minimum log level.
 *
 * @param <T> the type of the class for which logging is performed
 */
public class Logger<T> implements ILogger {
    private static final Map<Class, Logger> logMap = new HashMap<>();

    private static final Set<String> allowedTopics = getInitialTopics();
    private static int minLogLevel = ILogger.toLogLevel(System.getenv(ENV_LOG_LEVEL)).ordinal();

    private final Class<T> logClass;

    private static Set<String> getInitialTopics() {
        String topics = System.getenv(ENV_LOG_TOPICS);
        if (topics == null) {
            return new HashSet<>();
        }
        return Arrays.stream(topics.split(";")).collect(Collectors.toSet());
    }

    /**
     * Private constructor for creating a logger instance.
     *
     * @param logClass the class for which logging is being created
     */
    private Logger(Class<T> logClass) {
        this.logClass = logClass;
    }

    /**
     * Retrieves a logger instance for the specified class.
     *
     * @param c the class for which to retrieve the logger
     * @param <T> the type of the class
     * @return a logger instance for the specified class
     */
    public static <T>Logger<T> get(Class<T> c)
    {
        if(!logMap.containsKey(c)) {
            logMap.put(c,new Logger<>(c));
        }
        return logMap.get(c);
    }

    /**
     * Sets the minimum log level for this logger.
     *
     * @param logLevel the minimum log level to be set
     */
    public static void setLogLevel(Level logLevel) {
        if (logLevel == null) {
             logLevel = DEFAULT_LEVEL;
        }
        minLogLevel = logLevel.ordinal();
    }

    /**
     * Sets the allowed topics for logging. Only messages with these topics will be logged.
     *
     * @param topics the topics to be allowed for logging
     */
    public static void setTopics(String... topics) {
        allowedTopics.clear();
        if (topics == null) {
            return;
        }
        allowedTopics.addAll(List.of(topics));
    }

    /**
     * Determines if a message should be logged based on its topic.
     *
     * @param topic the topic of the message
     * @return true if the message should be logged, false otherwise
     */
    private boolean shouldLog(String topic) {
        if (allowedTopics.isEmpty() || topic.isEmpty()) {
            // Allow logging for all messages if the topic is empty or no topics are set
            return true;
        }
        return allowedTopics.contains(topic) || allowedTopics.contains(logClass.getName());
    }

    /**
     * Logs a message with the specified log level, topic, and optional exception.
     *
     * @param level     the log level of the message
     * @param topic     the topic associated with the message
     * @param message   the message to be logged
     * @param exception the optional exception to be logged (if any)
     */
    private void log(Logger.Level level, String topic, String message, Throwable exception) {
        if (minLogLevel > level.ordinal() || !shouldLog(topic) || Gdx.app == null || OIS.engine == null) {
            return;
        }

        // Prepare the log format
        String format;
        if (topic.isEmpty()) {
            format = "[" + logClass.getSimpleName() + "] : " + message;
        } else {
            format = "[" + logClass.getSimpleName() + "] [" + topic + "] : " + message;
        }

        if (exception != null && (level.equals(Level.Error) || level.equals(Level.Warn))) {
            Gdx.app.error(level.name(), format, exception);
            return;
        }
        if (RunnerConfiguration.RunnerType.Html.equals(OIS.engine.getRunnerConfig().getType())) {
            Gdx.app.error(level.name(), format);
            return;
        }
        switch (level) {
            case Warn:
            case Error:
                Gdx.app.error(level.name(), format);
                break;
            default:
                Gdx.app.log(level.name(), format);
                break;
        }
    }

    @Override
    public void debug(String topic, String message, Object ...args) { log(Level.Debug, topic, String.format(message, args), null); }

    @Override
    public void info(String topic, String message, Object ...args) { log(Level.Info, topic, String.format(message, args), null); }

    @Override
    public void debug(String message, Object ...args) {
        log(Level.Debug, "", String.format(message, args), null);
    }

    @Override
    public void info(String message, Object ...args) {
        log(Level.Info, "", String.format(message, args), null);
    }

    @Override
    public void warn(String message, Object ...args) {
        log(Level.Warn, "", String.format(message, args), null);
    }

    @Override
    public void error(String message, Object ...args) {
        log(Level.Error, "", String.format(message, args), null);
    }

    @Override
    public void error(String message, Throwable exception) {
        log(Level.Error, "", message, exception);
    }
}
