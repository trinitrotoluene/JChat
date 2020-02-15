package co.uk.michallet.chatapp.common;

import co.uk.michallet.chatapp.common.config.Configuration;
import co.uk.michallet.chatapp.common.config.ConsoleConfigurationProvider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ConfigurationBuilder {
    private Set<IConfigurationProvider> _providers;

    public ConfigurationBuilder() {
        _providers = new HashSet<>();
    }

    public IConfiguration build() {
        var map = new HashMap<String, String>();
        for (var provider: _providers) {
            for (var kvp : provider.getValues().entrySet()) {
                map.put(kvp.getKey(), kvp.getValue());
            }
        }
        return new Configuration(map);
    }

    public ConfigurationBuilder addConsole(String[] args) {
        var provider = new ConsoleConfigurationProvider(args);
        _providers.add(provider);

        return this;
    }
}
