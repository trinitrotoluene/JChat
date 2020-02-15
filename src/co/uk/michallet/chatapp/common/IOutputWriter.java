package co.uk.michallet.chatapp.common;

public interface IOutputWriter {
    void write(String message);
    void clearAll();
    void reset();
}
