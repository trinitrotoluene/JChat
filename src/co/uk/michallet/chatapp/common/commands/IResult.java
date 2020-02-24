package co.uk.michallet.chatapp.common.commands;

/**
 * Base interface returned by all Commands
 */
public interface IResult {
    boolean isSuccess();
    String getReason();
}
