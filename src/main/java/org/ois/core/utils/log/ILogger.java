package org.ois.core.utils.log;

public interface ILogger {
    enum Level {
        Debug, Info, Warn, Error
    }

    static Level toLogLevel(String logLevel) {
        switch (logLevel.trim().toLowerCase()) {
            case "debug":
                return Level.Debug;
            case "warn":
                return Level.Warn;
            case "error":
                return Level.Error;
            default:
                return Level.Info;
        }
    }

    void debug(String topic, String message);
    void debug(String message);
    void info(String topic, String message);
    void info(String message);
    void warn(String message);
    void error(String message);
    void error(String message, Throwable exception);
}
