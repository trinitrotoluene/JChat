package co.uk.michallet.chatapp.common.config;

import co.uk.michallet.chatapp.common.IConfigurationProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ConsoleConfigurationProvider implements IConfigurationProvider {
    private static final Pattern UnixFlagPattern = Pattern.compile("--(\\w+)(=(?<==)(\\w+))?");
    private static final Pattern PosixFlagPattern = Pattern.compile("^-(\\w+)");

    private final String[] _args;

    public ConsoleConfigurationProvider(String[] args) {
        _args = args;
    }

    @Override
    public Map<String, String> getValues() {
        var valueMap = new HashMap<String, String>();

        for (int i = 0; i < _args.length; i++) {
            var isLast = i == _args.length - 1;

            var unixMatcher = UnixFlagPattern.matcher(_args[i]);
            if (unixMatcher.find()) {
                var flagName = unixMatcher.group(1);
                var flagValue = unixMatcher.group(3);
                if (flagValue == null) {
                    flagValue = "";
                }

                valueMap.put(flagName, flagValue);

                continue;
            }

            var posixMatcher = PosixFlagPattern.matcher(_args[i]);
            if (posixMatcher.find()) {
                var flagName = posixMatcher.group(1);
                if (isLast) {
                    valueMap.put(flagName, "");
                }

                var nextValue = _args[i + 1];
                if (PosixFlagPattern.matcher(nextValue).find() || UnixFlagPattern.matcher(nextValue).find()) {
                    valueMap.put(flagName, "");
                }

                valueMap.put(flagName, nextValue);
            }
        }

        return valueMap;
    }
}
