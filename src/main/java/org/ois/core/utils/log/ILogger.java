package org.ois.core.utils.log;

public interface ILogger {
    enum Level {
        Debug, Info, Warn, Error
    }

    void debug(String message);
    void info(String message);
    void warn(String message);
    void error(String message);
    void error(String message, Throwable exception);
}
