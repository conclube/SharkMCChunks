package se.xfunserver.xplaychunks.command.player;

import org.bukkit.entity.Player;
import se.xfunserver.xplaychunks.command.Command;
import se.xfunserver.xplaychunks.xPlayChunks;

public class TrustCommand {

    @Command(commandNames = "trust",
            permission = "xplaychunks.player",
            helpMessage = "Ett hjälp meddelande för alla chunk kommandon.")
    public static void execute(Player player, String[] args) {
        if (args.length == 0) {
            xPlayChunks.getInstance().getMainHandler().listAccesssors(player);
        } else {
            xPlayChunks.getInstance().getMainHandler().accessChunk(player, args[0].split(","));
        }
    }
}
