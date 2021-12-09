package se.xfunserver.xplaychunks.command.admin;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import se.xfunserver.xplaychunks.command.Command;
import se.xfunserver.xplaychunks.utils.StringUtils;
import se.xfunserver.xplaychunks.xPlayChunks;

public class AdminOverrideCommand {

    private static xPlayChunks chunksCore = xPlayChunks.getInstance();

    @Command(commandNames = { "chunkadmin" },
            helpMessage = "Togglar på / av admin override, vilket låter dig bygga / förstöra på andra plots.",
            permission = "xplaychunks.adminoverride"
    )
    public static void execute(Player player, String[] args) {
        if (xPlayChunks.getInstance().getAdminOverride().toggle(player.getUniqueId()))
            StringUtils.msg(player, chunksCore.getMessages().adminOverrideEnable);
        else
            StringUtils.msg(player, chunksCore.getMessages().adminOverrideDisabled);
    }
}
