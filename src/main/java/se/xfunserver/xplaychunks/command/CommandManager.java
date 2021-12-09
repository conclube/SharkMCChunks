package se.xfunserver.xplaychunks.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import se.xfunserver.xplaychunks.command.admin.AdminNameCommand;
import se.xfunserver.xplaychunks.command.admin.AdminOverrideCommand;
import se.xfunserver.xplaychunks.command.admin.AdminReloadCommand;
import se.xfunserver.xplaychunks.command.player.ClaimCommand;
import se.xfunserver.xplaychunks.command.player.ShowClaimedCommand;
import se.xfunserver.xplaychunks.command.player.TrustCommand;
import se.xfunserver.xplaychunks.command.player.UnclaimCommand;
import se.xfunserver.xplaychunks.player.AdminOverride;
import se.xfunserver.xplaychunks.utils.Messages;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class CommandManager implements TabCompleter {

    private final Map<String, Method> commands = new HashMap<>();

    public CommandManager() {
        List<Class<?>> commandClasses = Arrays.asList(
                ClaimCommand.class,
                ShowClaimedCommand.class,
                TrustCommand.class,
                UnclaimCommand.class,
                AdminOverrideCommand.class,
                AdminReloadCommand.class,
                AdminNameCommand.class
        );

        for (Class<?> clazz : commandClasses) {
            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(Command.class)) continue; // Make sure the method is marked as a command.

                if (method.getParameters().length != 2) {
                    Bukkit.getLogger().info("Method " + method.toGenericString().replace("public static void ", "") + " annotated as command but parameters count != 2");
                    continue;
                }
                if (method.getParameters()[0].getType() != CommandSender.class && method.getParameters()[0].getType() != Player.class) {
                    Bukkit.getLogger().info("Method " + method.toGenericString().replace("public static void ", "") + " annotated as command but parameter 1's type != CommandSender || Player");
                    continue;
                }
                if (method.getParameters()[1].getType() != String[].class) {
                    Bukkit.getLogger().info("Method " + method.toGenericString().replace("public static void ", "") + " annotated as command but parameter 2's type != String[]");
                    continue;
                }

                Command annotation = method.getAnnotation(Command.class);
                for (String commandName : annotation.commandNames()) commands.put(commandName.toLowerCase(), method);
            }
        }
    }

    public boolean handle(CommandSender sender, String command, String[] args) {
        if (command == null) {
            sender.sendMessage("Ogiltligt kommando. (skicka hjälp här)");
            return true;
        }

        if (commands.containsKey(command.toLowerCase())) {
            try {
                Method commandMethod = commands.get(command.toLowerCase());
                Command commandAnnotation = commandMethod.getAnnotation(Command.class);

                if (!sender.hasPermission(commandAnnotation.permission())) {
                    sender.sendMessage(Messages.NO_PERMISSSION.getMessage());
                    return true;
                }

                if (commandMethod.getParameters()[0].getType() == Player.class && !(sender instanceof Player)) {
                    sender.sendMessage("Du måste vara en spelare.");
                    return true;
                }

                commandMethod.invoke(null, sender, args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], commands.keySet(), completions);
            Collections.sort(completions);
        } else {
            return null; // player names
        }

        return completions;
    }

    public Map<String, Method> getCommands() {
        return commands;
    }
}
