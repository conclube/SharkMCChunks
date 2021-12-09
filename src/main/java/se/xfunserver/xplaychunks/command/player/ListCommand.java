package se.xfunserver.xplaychunks.command.player;

import net.royawesome.jlibnoise.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import se.xfunserver.xplaychunks.chunks.ChunkHandler;
import se.xfunserver.xplaychunks.chunks.ChunkPos;
import se.xfunserver.xplaychunks.command.Command;
import se.xfunserver.xplaychunks.player.PlayerHandler;
import se.xfunserver.xplaychunks.xPlayChunks;

import java.util.UUID;

public class ListCommand {

    private static xPlayChunks chunksCore = xPlayChunks.getInstance();

    @Command(commandNames = { "list" },
            helpMessage = "Visar en lista med alla chunks som du äger.",
            permission = "xplaychunks.player"
    )
    public static void execute(Player sender, String[] args) {
        PlayerHandler playerHandler = chunksCore.getPlayerHandler();
        ChunkHandler chunkHandler = chunksCore.getChunkHandler();

        String ownerName = playerHandler.getUsername(sender.getUniqueId());
        if (ownerName == null) ownerName = "Vildmarken";

        ChunkPos[] chunks = chunkHandler.getClaimedChunks(sender.getUniqueId());
        int page = 0;
        final int maxPerPage = Utils.ClampValue(chunksCore.getConfig().getInt("chunks.maxPerListPage"), 2, 10);
        final int maxPage = Integer.max(0, (chunks.length - 1) / maxPerPage);

        if (args.length == 1) {
            try {
                page = Utils.ClampValue(Integer.parseInt(args[0]) - 1, 0, maxPage);
            } catch (Exception ignored) {
                sender.sendMessage(chunksCore.getMessages().errEnterValidNum);
                return;
            }
        }

        sender.sendMessage();
    }
}
