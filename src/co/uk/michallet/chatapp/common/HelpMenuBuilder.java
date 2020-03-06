package co.uk.michallet.chatapp.common;

import java.util.HashMap;
import java.util.Map;

public class HelpMenuBuilder {
    private String _title;
    private String _description;
    private Map<String, String> _entries;

    public HelpMenuBuilder() {
        _entries = new HashMap<>();
        _entries.put("help", "Shows this menu");

        _title = "Help Menu";
        _description = "Usage: -flag <value> or --flag=<value> e.g. -foo bar, --foo=bar";
    }

    public HelpMenuBuilder setTitle(String title) {
        _title = title;
        return this;
    }

    public HelpMenuBuilder setDescription(String description) {
        _description = description;
        return this;
    }

    public HelpMenuBuilder addItem(String name, String description) {
        _entries.put(name, description);
        return this;
    }

    public String build() {
        int maxLength = _description.length();
        int maxNameLength = 0;
        for (var entry : _entries.entrySet()) {
            var unpaddedEntry = String.join("", entry.getKey(), entry.getValue());
            if (maxLength < unpaddedEntry.length()) {
                maxLength = unpaddedEntry.length();
            }
            if (maxNameLength < entry.getKey().length()) {
                maxNameLength = entry.getKey().length();
            }
        }

        maxLength += 3;

        var title = padLeft(_title, (maxLength + _title.length()) / 2);

        var sb = new StringBuilder(title);
        sb.append("\r\n")
            .append(_description)
            .append("\r\n\r\n");

        for (var entry : _entries.entrySet()) {
            var key = padRight(entry.getKey(), maxNameLength);
            sb.append(String.format("%s : %s", key, entry.getValue()));
            sb.append("\r\n");
        }

        return sb.toString();
    }

    private static String padRight(String sourceString, int targetLength) {
        var sb = new StringBuilder(sourceString);
        for (int i = sb.length(); i < targetLength; i++) {
            sb.append(' ');
        }

        return sb.toString();
    }

    private static String padLeft(String sourceString, int targetLength) {
        var sb = new StringBuilder();
        for (int i = 0; i < targetLength - sourceString.length(); i++) {
            sb.append(' ');
        }
        sb.append(sourceString);
        return sb.toString();
    }
}