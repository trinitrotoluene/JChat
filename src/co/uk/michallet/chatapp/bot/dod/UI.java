package co.uk.michallet.chatapp.bot.dod;

import java.util.Scanner;

/**
 * Static-only class wrapping UI functionality.
 */
public final class UI {
    private static final String MOTD = "\n" +
            "________                                                     _____  ________                         \n" +
            "\\______ \\  __ __  ____    ____   ____  ____   ____     _____/ ____\\ \\______ \\   ____   ____   _____  \n" +
            " |    |  \\|  |  \\/    \\  / ___\\_/ __ \\/  _ \\ /    \\   /  _ \\   __\\   |    |  \\ /  _ \\ /  _ \\ /     \\ \n" +
            " |    `   \\  |  /   |  \\/ /_/  >  ___(  <_> )   |  \\ (  <_> )  |     |    `   (  <_> |  <_> )  Y Y  \\\n" +
            "/_______  /____/|___|  /\\___  / \\___  >____/|___|  /  \\____/|__|    /_______  /\\____/ \\____/|__|_|  /\n" +
            "        \\/           \\//_____/      \\/           \\/                         \\/                    \\/ \n" +
            "\n";

    private static final String WIN = "\n" +
            " __      __.__        \n" +
            "/  \\    /  \\__| ____  \n" +
            "\\   \\/\\/   /  |/    \\ \n" +
            " \\        /|  |   |  \\\n" +
            "  \\__/\\  / |__|___|  /\n" +
            "       \\/          \\/ \n" +
            "\n";

    private static final String LOSE = "\n" +
            ".____                        \n" +
            "|    |    ____  ______ ____  \n" +
            "|    |   /  _ \\/  ___// __ \\ \n" +
            "|    |__(  <_> )___ \\\\  ___/ \n" +
            "|_______ \\____/____  >\\___  >\n" +
            "        \\/         \\/     \\/ \n" +
            "\n";

    private static final Scanner _scanner = new Scanner(System.in);

    private UI() {
    }

    /**
     * @return The scanner used to read in console input.
     */
    public static Scanner getScanner() {
        return _scanner;
    }

    /**
     * Writes a formatted string to the UI output.
     * @param message Format string to use.
     * @param values Values to substitute into the format string.
     */
    public static void write(String message, Object...values) {
        System.out.println(String.format(message, values));
    }

    /**
     * Prints an error to the UI output.
     * @param ex Exception to pring.
     */
    public static void error(Exception ex) {
        System.out.println(String.format("An exception was thrown during execution!\r\n%s", ex.getMessage()));
    }

    /**
     * Prints the MOTD banner to the UI output.
     */
    public static void motd() {
        System.out.print(MOTD);
    }

    /**
     * Prints the WIN banner to the UI output.
     */
    public static void win() {
        System.out.print(WIN);
    }

    /**
     * Prints the LOSE banner to the UI output.
     */
    public static void lose() {
        System.out.print(LOSE);
    }

    /**
     * Clears the UI output window.
     */
    public static void cls() {
        System.out.println(System.lineSeparator().repeat(50));
    }
}
