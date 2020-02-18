package co.uk.michallet.chatapp.common.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CommandService<TContext extends CommandContext> {
    private Map<String, CommandInfo> _commands;

    public CommandService() {
        _commands = new HashMap<>();
    }

    public <T> void registerCommands(Class<T> typeInfo) {
        var methodList = Arrays.stream(typeInfo.getMethods())
                .filter(m -> m.getParameterCount() == 1 &&
                        m.getParameterTypes()[0] == String[].class &&
                        m.isAnnotationPresent(Command.class) &&
                        Modifier.isPublic(m.getModifiers())
                )
                .collect(Collectors.toUnmodifiableList());

        for (var method : methodList) {
            var command = method.getAnnotation(Command.class);
            _commands.put(command.value(), new CommandInfo(method, method.getDeclaringClass()));
        }
    }

    public CompletableFuture<IResult> execute(TContext context, String name, String[] args) {
        if (!_commands.containsKey(name)) {
            return CompletableFuture.completedFuture(new CommandNotFoundResult());
        }

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
