package co.uk.michallet.chatapp.common;

/**
 * Basic scrolling console writer
 */
public class ConsoleWriter implements IOutputWriter {
    @Override
    public synchronized void write(String message) {
        System.out.println(message);
    }

    // Escape sequences from http://ascii-table.com/ansi-escape-sequences-vt-100.php
    // [2J = clear entire display, [H reset cursor to top left corner (home).
    @Override
    public synchronized void clearAll() {
        System.out.print("\033[2J\033[H");
    }

    public synchronized void reset() {
        clearAll();
    }
}
