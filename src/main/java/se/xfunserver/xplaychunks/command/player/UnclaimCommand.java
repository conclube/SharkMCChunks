package se.xfunserver.xplaychunks.command.player;

import org.bukkit.entity.Player;
import se.xfunserver.xplaychunks.command.Command;
import se.xfunserver.xplaychunks.xPlayChunks;

public class UnclaimCommand {

    @Command(commandNames = "unclaim",
            permission = "xplaychunks.player",
            helpMessage = "Unclaimar den chunk som du nuvarande står i.")
    public static void execute(Player player, String[] args) {
        xPlayChunks.getInstance().getMainHandler().unclaimChunk(false, player);
    }
}
