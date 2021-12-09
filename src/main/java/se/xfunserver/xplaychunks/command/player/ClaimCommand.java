package se.xfunserver.xplaychunks.command.player;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import se.xfunserver.xplaychunks.command.Command;
import se.xfunserver.xplaychunks.xPlayChunks;

public class ClaimCommand {

    @Command(commandNames = { "claim" },
            helpMessage = "Claimar en chunk till ditt namn som tillåter dig att bygga.",
            permission = "xplaychunks.player"
    )
    public static void execute(CommandSender sender, String[] args) {
        final xPlayChunks chunksCore = xPlayChunks.getInstance();
        if (chunksCore == null) {
            Bukkit.getLogger().severe("Ett fel uppstod, kan ej claima marker. (instansen av main klassen är null?).");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(xPlayChunks.getInstance(), () -> executeAsync(sender, args, chunksCore));
    }

    private static void executeAsync(CommandSender sender, String[] args, xPlayChunks chunksCore) {
        final Player player = (Player) sender;

        chunksCore.getMainHandler().claimChunk(player, player.getLocation().getChunk());
    }
}
