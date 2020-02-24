package co.uk.michallet.chatapp.common.logging;

import co.uk.michallet.chatapp.common.ILogger;
import co.uk.michallet.chatapp.common.IOutputWriter;

import java.util.concurrent.Semaphore;
import java.util.logging.Level;

/**
 * Provides a default console logging implementation.
 */
public class DefaultLogger implements ILogger {
    // Colour codes from ECMA-048 SGR - SELECT GRAPHIC RENDITION
    // https://www.ecma-international.org/publications/files/ECMA-ST/Ecma-048.pdf Page 61
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_ORANGE = "\u001B[33m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_WHITE = "\u001B[37m";

    private final String _name;
    private final Level _level;
    private final Semaphore _outLock;
    private final IOutputWriter _writer;

    public DefaultLogger(String name, Level level, IOutputWriter writer) {
        _name = name;
        _level = level;
        _writer = writer;
        _outLock = new Semaphore(1);
    }

    @Override
    public void debug(String format, Object... params) {
        log(Level.FINE, format, params);
    }

    @Override
    public void info(String format, Object... params) {
        log(Level.INFO, format, params);
    }

    @Override
    public void warn(String format, Object... params) {
        log(Level.WARNING, format, params);
    }

    @Override
    public void error(String format, Object... params) {
        log(Level.SEVERE, format, params);
    }

    private void log(Level level, String format, Object... params) {
        if (_level.intValue() > level.intValue()) {
            return;
        }
        // To get colour formatting without tinting unrelated text, you need ANSI_RESET<ANSI_COLOUR><CONTENT><ANSI_RESET>
        String colourFormat = "";
        switch (level.getName()) {
            case "FINE":
                colourFormat = ANSI_WHITE;
                break;
            case "INFO":
                colourFormat = ANSI_GREEN;
                break;
            case "WARNING":
                colourFormat = ANSI_ORANGE;
                break;
            case "SEVERE":
                colourFormat = ANSI_RED;
                break;
        }

        var finalFormat = new StringBuilder()
                .append(ANSI_RESET)
                .append(colourFormat)
                .append("[")
                .append(level.getName());

        for (int i = level.getName().length(); i < 7; i++) {
            finalFormat.append(" ");
        }
        finalFormat.append("] ")
                .append(_name)
                .append(" ")
                .append(String.format(format, params))
                .append(ANSI_RESET);

        try {
            // Only this portion is not threadsafe, writing to the console from multiple threads simultaneously
            // may result in garbled output or incorrect colour formatting.
            _outLock.acquire();
            _writer.write(finalFormat.toString());
            _outLock.release();
        }
        catch (InterruptedException e) {
        }
    }
}
