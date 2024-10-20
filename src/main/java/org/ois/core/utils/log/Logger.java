package org.ois.core.utils.log;

import com.badlogic.gdx.Gdx;

import java.util.HashMap;
import java.util.Map;

public class Logger<T> implements ILogger {
    private static final Map<Class, Logger> logMap = new HashMap<>();
    private static int minLogLevel = Level.Debug.ordinal();

    private final Class<T> logClass;

    private Logger(Class<T> logClass) {
        this.logClass = logClass;
    }

    public static <T>Logger<T> get(Class<T> c)
    {
        if(!logMap.containsKey(c)) {
            logMap.put(c,new Logger<T>(c));
        }
        return logMap.get(c);
    }

    public static void setLogLevel(Level logLevel) {
        minLogLevel = logLevel.ordinal();
    }

    private void log(Logger.Level level, String message, Throwable exception) {
        if (minLogLevel > level.ordinal()) {
            return;
        }
        String format = "[" + logClass.getSimpleName() + "] : " + message;
        if (exception != null && (level.equals(Level.Error) || level.equals(Level.Warn))) {
            Gdx.app.error(level.name(), format, exception);
        }
        switch (level) {
            case Warn:
            case Error: Gdx.app.error(level.name(), format); break;
            default: Gdx.app.log(level.name(), format); break;
        }
    }

    @Override
    public void debug(String message) {
        log(Level.Debug, message, null);
    }

    @Override
    public void info(String message) {
        log(Level.Info, message, null);
    }

    @Override
    public void warn(String message) {
        log(Level.Warn, message, null);
    }

    @Override
    public void error(String message) {
        log(Level.Error, message, null);
    }

    @Override
    public void error(String message, Throwable exception) {
        log(Level.Error, message, exception);
    }
}
