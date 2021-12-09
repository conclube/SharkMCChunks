package se.xfunserver.xplaychunks.command.admin;

import org.bukkit.entity.Player;
import se.xfunserver.xplaychunks.command.Command;
import se.xfunserver.xplaychunks.player.PlayerHandler;
import se.xfunserver.xplaychunks.utils.Messages;
import se.xfunserver.xplaychunks.xPlayChunks;

public class AdminNameCommand {

    @Command(commandNames = "setname",
            permission = "xplaychunks.setname",
            helpMessage = "Sätter namnet på en chunk.")
    public static void execute(Player player, String[] args) {
        PlayerHandler playerHandler = xPlayChunks.getInstance().getPlayerHandler();
        try {
            if (args.length == 0) {
                if (playerHandler.hasChunkName(player.getUniqueId())) {
                    playerHandler.clearChunkName(player.getUniqueId());
                    player.sendMessage(Messages.CLEARED_PLOT_NAME.getMessage());
                } else {
                    player.sendMessage(Messages.NO_NAME_SET.getMessage());
                }
            } else {
                playerHandler.setChunkName(player.getUniqueId(), args[0].trim());
                player.sendMessage(Messages.NAME_SET.getMessage()
                        .replace("%name%", args[0].trim()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
