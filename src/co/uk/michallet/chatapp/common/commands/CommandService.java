package co.uk.michallet.chatapp.common.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Maps, stores and executes method callbacks associated with a string.
 * @param <TContext> The context command modules will be instantiated with.
 */
public class CommandService<TContext extends CommandContext> {
    private Map<String, CommandInfo> _commands;

    public CommandService() {
        _commands = new HashMap<>();
    }

    /**
     * Maps methods in a provided type to CommandInfo.
     * @param typeInfo The Type to crawl the methods of.
     */
    public <T> void registerCommands(Class<T> typeInfo) {
        var methodList = Arrays.stream(typeInfo.getMethods()) // Yield an iterator to do bulk operations on
                // Filter the returned stream to include only methods with the characteristics of a command we can execute
                .filter(m -> m.getParameterCount() == 1 &&
                        m.getParameterTypes()[0] == String[].class &&
                        m.isAnnotationPresent(Command.class) &&
                        Modifier.isPublic(m.getModifiers())
                )
                // Enumerate the stream and return a collection we can iterate over.
                .collect(Collectors.toUnmodifiableList());

        // Populate the map
        for (var method : methodList) {
            var command = method.getAnnotation(Command.class);
            _commands.put(command.value(), new CommandInfo(method, method.getDeclaringClass()));
        }
    }

    /**
     * Asynchronously execute a command.
     * @param context Context instance to use when instantiating the module.
     * @param name The name of the command to execute.
     * @param args Arguments supplied to the command.
     */
    public CompletableFuture<IResult> execute(TContext context, String name, String[] args) {
        // If it doesn't exist, we can return a completed future.
        if (!_commands.containsKey(name)) {
            return CompletableFuture.completedFuture(new CommandNotFoundResult());
        }

        // Run the command on the thread pool and return it as a Future
        return CompletableFuture.supplyAsync(() -> {
            try {
                var info = _commands.get(name);
                var instance = info.getModule().getConstructors()[0].newInstance(context);
                var result = info.getMethod().invoke(instance, (Object) args);

                return (IResult)result;
            }
            catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                return new ExecutionResult(false, "");
            }
        });
    }
}
