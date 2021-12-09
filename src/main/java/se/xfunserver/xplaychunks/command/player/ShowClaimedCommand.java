package se.xfunserver.xplaychunks.command.player;

import org.bukkit.entity.Player;
import se.xfunserver.xplaychunks.chunks.ChunkPos;
import se.xfunserver.xplaychunks.command.Command;
import se.xfunserver.xplaychunks.xPlayChunks;

import java.util.Arrays;
import java.util.HashSet;

public class ShowClaimedCommand {

    public static int maxSeconds = 60;
    public static int maxRadius = 6;

    @Command(
            commandNames = "visachunks",
            helpMessage = "Visualisera vart alla chunks som du äger är.",
            permission = "xplaychunks.visualize"
    )
    public static void execute(Player player, String[] args) {
        xPlayChunks.getInstance().getLogger().info(Arrays.toString(args));

        ChunkPos chunkPos = new ChunkPos(player.getLocation().getChunk());
        int showForSeconds = 5;
        int radius = 3;

        // Optional argument to show chunks within a given radius (up to the max defined)
        if (args.length >= 1) {
            try {
                radius = Integer.min(Integer.parseInt(args[0]), maxRadius);
            } catch (Exception e) {
                return;
            }
        }

        // Optional argument to show for a given time (up to the max defined)
        if (args.length >= 2) {
            try {
                showForSeconds = Integer.min(Integer.parseInt(args[1]), maxSeconds);
            } catch (Exception e) {
                return;
            }
        }

        // Create a set of this player's claimed chunks within the given radius
        HashSet<ChunkPos> claimedChunks = new HashSet<>();
        for (int x = chunkPos.getX() - radius; x <= chunkPos.getX() + radius; x++) {
            for (int z = chunkPos.getZ() - radius; z <= chunkPos.getZ() + radius; z++) {
                if (xPlayChunks.getInstance()
                        .getChunkHandler()
                        .isOwner(player.getWorld(), x, z, player.getUniqueId())) {
                    claimedChunks.add(new ChunkPos(player.getWorld().getName(), x, z));
                }
            }
        }

        // Use the new particle system!
        xPlayChunks.getInstance().getChunkOutlineHandler().showChunksFor(claimedChunks, player, showForSeconds);
    }
}
