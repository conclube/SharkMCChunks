package se.xfunserver.xplaychunks.command.admin;

import org.bukkit.entity.Player;
import se.xfunserver.xplaychunks.command.Command;
import se.xfunserver.xplaychunks.player.PlayerHandler;
import se.xfunserver.xplaychunks.utils.StringUtils;
import se.xfunserver.xplaychunks.xPlayChunks;

public class AdminNameCommand {

    private static xPlayChunks chunksCore = xPlayChunks.getInstance();

    @Command(commandNames = "setname",
            permission = "xplaychunks.setname",
            helpMessage = "Sätter namnet på en chunk.")
    public static void execute(Player player, String[] args) {
        PlayerHandler playerHandler = xPlayChunks.getInstance().getPlayerHandler();
        try {
            if (args.length == 0) {
                if (playerHandler.hasChunkName(player.getUniqueId())) {
                    playerHandler.clearChunkName(player.getUniqueId());
                    StringUtils.msg(player, chunksCore.getMessages().nameClear);
                } else {
                    StringUtils.msg(player, chunksCore.getMessages().nameNotSet);
                }
            } else {
                playerHandler.setChunkName(player.getUniqueId(), args[0].trim());
                StringUtils.msg(player, chunksCore.getMessages().nameSet
                        .replace("%name%", args[0].trim()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
