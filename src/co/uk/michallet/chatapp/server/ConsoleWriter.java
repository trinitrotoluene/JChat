package co.uk.michallet.chatapp.server;

import co.uk.michallet.chatapp.common.IOutputWriter;

public class ConsoleWriter implements IOutputWriter {
    @Override
    public synchronized void write(String message) {
        System.out.println(message);
    }

    @Override
    public synchronized void clearAll() {
        System.out.print("\033[2J\033[H");
    }

    public synchronized void reset() {
        clearAll();
    }
}
