package co.uk.michallet.chatapp.common;

public interface ILogger {
    void debug(String format, Object... params);
    void info(String format, Object... params);
    void warn(String format, Object... params);
    void error(String format, Object... params);
}
