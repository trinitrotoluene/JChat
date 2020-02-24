package co.uk.michallet.chatapp.common.config;

import co.uk.michallet.chatapp.common.IConfigurationProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Configuration provider which parses values from command line arguments.
 */
public class ConsoleConfigurationProvider implements IConfigurationProvider {
    // Regex for GNU coreutils style --foo=bar CLI arguments
    private static final Pattern GNUFlagPattern = Pattern.compile("--(\\w+)(=(?<==)(\\w+))?");
    // Regex for POSIX style -foo bar CLI arguments
    private static final Pattern PosixFlagPattern = Pattern.compile("^-(\\w+)");
    // Arguments to parse
    private final String[] _args;

    public ConsoleConfigurationProvider(String[] args) {
        _args = args;
    }

    @Override
    public Map<String, String> getValues() {
        var valueMap = new HashMap<String, String>();

        for (int i = 0; i < _args.length; i++) {
            var isLast = i == _args.length - 1; // protection against reading OOB

            // First we check for the GNU pattern
            var unixMatcher = GNUFlagPattern.matcher(_args[i]);
            if (unixMatcher.find()) {
                // If it matches, fetch the values of relevant groups and put them into the map.
                var flagName = unixMatcher.group(1);
                var flagValue = unixMatcher.group(3);
                if (flagValue == null) {
                    flagValue = "";
                }

                valueMap.put(flagName, flagValue);

                continue;
            }
            // Next check for the POSIX style
            var posixMatcher = PosixFlagPattern.matcher(_args[i]);
            if (posixMatcher.find()) {
                var flagName = posixMatcher.group(1);
                // If there is no next value, fill in the default and we're done.
                if (isLast) {
                    valueMap.put(flagName, "");
                    continue;
                }
                // If the next value is itself a flag then we don't parse it as an argument to the one we're currently processing.
                var nextValue = _args[i + 1];
                if (PosixFlagPattern.matcher(nextValue).find() || GNUFlagPattern.matcher(nextValue).find()) {
                    valueMap.put(flagName, "");
                }

                valueMap.put(flagName, nextValue);
            }
        }

        return valueMap;
    }
}
