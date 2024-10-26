package org.ois.core.utils.log;

import com.badlogic.gdx.Gdx;
import org.ois.core.OIS;
import org.ois.core.runner.RunnerConfiguration;

import java.util.*;

public class Logger<T> implements ILogger {
    private static final Map<Class, Logger> logMap = new HashMap<>();

    private static final Set<String> allowedTopics = new HashSet<>();
    private static final ILogger.Level DEFAULT_LEVEL = Level.Info;
    private static int minLogLevel = DEFAULT_LEVEL.ordinal();

    private final Class<T> logClass;

    private Logger(Class<T> logClass) {
        this.logClass = logClass;
    }

    public static <T>Logger<T> get(Class<T> c)
    {
        if(!logMap.containsKey(c)) {
            logMap.put(c,new Logger<>(c));
        }
        return logMap.get(c);
    }

    public static void setLogLevel(Level logLevel) {
        if (logLevel == null) {
             logLevel = DEFAULT_LEVEL;
        }
        minLogLevel = logLevel.ordinal();
    }

    public static void setTopics(String... topics) {
        allowedTopics.clear();
        if (topics == null) {
            return;
        }
        allowedTopics.addAll(List.of(topics));
    }

    private boolean shouldLog(String topic) {
        if (allowedTopics.isEmpty() || topic.isEmpty()) {
            // Allow logging for all messages if the topic is empty or no topics are set
            return true;
        }
        return allowedTopics.contains(topic);
    }

    private void log(Logger.Level level, String topic, String message, Throwable exception) {
        if (minLogLevel > level.ordinal() || !shouldLog(topic)) {
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
    public void debug(String topic, String message) {
        log(Level.Debug, topic, message, null);
    }

    @Override
    public void info(String topic, String message) {
        log(Level.Info, topic, message, null);
    }

    @Override
    public void debug(String message) {
        log(Level.Debug, "", message, null);
    }

    @Override
    public void info(String message) {
        log(Level.Info, "", message, null);
    }

    @Override
    public void warn(String message) {
        log(Level.Warn, "", message, null);
    }

    @Override
    public void error(String message) {
        log(Level.Error, "", message, null);
    }

    @Override
    public void error(String message, Throwable exception) {
        log(Level.Error, "", message, exception);
    }
}
