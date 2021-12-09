package se.xfunserver.xplaychunks.command.admin;

import org.bukkit.command.CommandSender;
import se.xfunserver.xplaychunks.command.Command;
import se.xfunserver.xplaychunks.utils.StringUtils;
import se.xfunserver.xplaychunks.xPlayChunks;

public class AdminReloadCommand {

    @Command(
            commandNames = "chunkreload",
            permission = "xplaychunks.reload",
            helpMessage = "Laddar om pluginet och dess konfigurations filer."
    )
    public static void execute(CommandSender commandSender, String[] args) {
        // Simulate a restart
        xPlayChunks.getInstance().onDisable();
        xPlayChunks.getInstance().onLoad();
        xPlayChunks.getInstance().onEnable();

        commandSender.sendMessage(StringUtils.color("&aDu laddade om pluginet 'xPlayChunks'"));
    }
}
