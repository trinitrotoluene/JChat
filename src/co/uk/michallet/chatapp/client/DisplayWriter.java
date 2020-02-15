package co.uk.michallet.chatapp.client;

import co.uk.michallet.chatapp.common.IOutputWriter;

import java.util.LinkedList;

public class DisplayWriter implements IOutputWriter {
    private final int _height;
    private final LinkedList<String> _bufferedLines;

    public DisplayWriter(int height) {
        _height = height;
        _bufferedLines = new LinkedList<>();
    }

    public synchronized void write(String line) {
        if (_bufferedLines.size() >= _height) {
            _bufferedLines.removeFirst();
        }
        _bufferedLines.add(line);
        clearAndResetConsole();
        printBuffer();
    }

    public synchronized void clearAll() {
        _bufferedLines.clear();
        clearAndResetConsole();
    }

    public synchronized void reset() {
        clearAndResetConsole();
        printBuffer();
    }

    private synchronized void clearAndResetConsole() {
        System.out.print("\033[2J\033[H");
    }

    private synchronized void printBuffer() {
        int writtenLines = 0;
        for (var line : _bufferedLines) {
            writtenLines++;
            System.out.println(line);
        }

        for (int i = writtenLines; i < _height; i++) {
            System.out.println();
        }
        System.out.println("");
        System.out.print(">");
    }
}
