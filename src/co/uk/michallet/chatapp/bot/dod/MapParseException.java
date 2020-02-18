package co.uk.michallet.chatapp.bot.dod;

/**
 * Thrown when the {@link MapLoader} encounters unexpected data when parsing a map.
 */
public class MapParseException extends Exception {
    public MapParseException(String message) {
        super(message);
    }
}
