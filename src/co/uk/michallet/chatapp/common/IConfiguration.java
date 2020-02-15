package co.uk.michallet.chatapp.common;

public interface IConfiguration {
    String getString(String path);
    String getString(String path, String defaultValue);

    boolean isSet(String path);

    void setString(String path, String value);
}
