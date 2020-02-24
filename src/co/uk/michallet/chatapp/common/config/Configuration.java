package co.uk.michallet.chatapp.common.config;

import co.uk.michallet.chatapp.common.IConfiguration;

import java.util.Map;

/**
 * Default configuration implementation.
 */
public class Configuration implements IConfiguration {
    private final Map<String, String> _values;

    public Configuration(Map<String, String> values) {
        _values = values;
    }

    @Override
    public String getString(String path) {
        return _values.get(path);
    }

    @Override
    public String getString(String path, String defaultValue) {
        return _values.getOrDefault(path, defaultValue);
    }

    @Override
    public void setString(String path, String value) {
        _values.put(path, value);
    }

    @Override
    public boolean isSet(String path) {
        return _values.containsKey(path);
    }
}
