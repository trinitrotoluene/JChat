package co.uk.michallet.chatapp.common.commands;

import java.lang.reflect.Method;

/**
 * Stores the type and method information of a command.
 */
public class CommandInfo {
    private final Class _module;
    private final Method _method;

    public CommandInfo(Method method, Class module) {
        _method = method;
        _module = module;
    }

    public Method getMethod() {
        return _method;
    }

    public Class getModule() {
        return _module;
    }
}
