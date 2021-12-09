package se.xfunserver.xplaychunks.event;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.StringUtil;
import se.xfunserver.xplaychunks.chunks.ChunkHandler;
import se.xfunserver.xplaychunks.player.PlayerHandler;
import se.xfunserver.xplaychunks.utils.StringUtils;
import se.xfunserver.xplaychunks.worldguard.WorldGuardHandler;
import se.xfunserver.xplaychunks.xPlayChunks;

import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
public class PlayerMovementHandler implements Listener {

    private final xPlayChunks chunksCore;

    public PlayerMovementHandler(xPlayChunks chunksCore) {
        this.chunksCore = chunksCore;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event != null && !event.isCancelled() && event.getTo() != null) {
            // Get the previous and current chunks.
            Chunk prev = event.getFrom().getChunk();
            Chunk to = event.getTo().getChunk();

            // Make sure the player moved into a new chunk
            if (prev.getX() != to.getX() || prev.getZ() != to.getZ()) {
                ChunkHandler chunkHandler = chunksCore.getChunkHandler();

                // Check if the previous chunk was already claimed
                boolean lastClaimed = chunkHandler.isClaimed(prev.getWorld(), prev.getX(), prev.getZ());
                boolean lastAdminClaimed = WorldGuardHandler.isAdminChunk(chunksCore, prev);

                // Check if the new chunk is already claimed
                if (chunkHandler.isClaimed(to.getWorld(), to.getX(), to.getZ())) {
                    // If the new chunk and the previous chunk were claimed, check if the owners
                    // differ.
                    if (lastClaimed) {
                        UUID prevOwner = chunkHandler.getOwner(prev.getWorld(), prev.getX(), prev.getZ());
                        UUID newOwner = chunkHandler.getOwner(to.getWorld(), to.getX(), to.getZ());

                        // Only display the new chunk's owner if they differ from the previous
                        // chunk owner.
                        if ((prevOwner == null && newOwner == null)
                            || (prevOwner != null && !prevOwner.equals(newOwner))) {
                            showTitle(event.getPlayer(), to);
                        }
                    } else {
                        // Show the player the chunk's owner
                        showTitle(event.getPlayer(), to);
                    }
                } else {
                    // The player entered an unclaimed chunk from a claimed chunk
                    if (lastClaimed && !lastAdminClaimed) {
                        showTitle(event.getPlayer(), to);
                    }
                }
            }
        }
    }

    private void showTitle(Player player, Chunk newChunk) {
        this.chunksCore.getLogger().info("En title visas nu för " + player.getName() + " för chunken (" + newChunk.getX() + ", " + newChunk.getZ() + ")");

        // Get the UUID of the new chunk owner
        UUID newOwner =
                chunksCore
                        .getChunkHandler()
                        .getOwner(newChunk.getWorld(), newChunk.getX(), newChunk.getZ());

        if (WorldGuardHandler.isAdminChunk(chunksCore, newChunk)) {
            showTitleRaw(player, StringUtils.color("&bxFun Server"));
            return;
        }

        if (!chunksCore.getChunkHandler().isClaimed(
                newChunk.getWorld(), newChunk.getX(), newChunk.getZ())) {
            showTitleRaw(player, StringUtils.color("&2Vildmarken"));
            return;
        }

        // Check if this player doesn't own the new chunk
        if (newOwner != null && !player.getUniqueId().equals(newOwner)) {
            // Get the name of the chunks for the owner of this chunk and display it.
            PlayerHandler playerHandler = chunksCore.getPlayerHandler();
            String newName = playerHandler.getChunkName(newOwner);
            String text =
                    ((newName == null)
                            ? "Okänd Spelare"
                            : newName);
            showTitleRaw(player, text);
        } else {
            // Chunken ägs utav spelaren själv.
            showTitleRaw(player, StringUtils.color("&a" + chunksCore.getPlayerHandler().getChunkName(player.getUniqueId())));
        }
    }

    private void showTitleRaw(Player player, String msg) {
        try {
            // Title configs
            int in = this.chunksCore.getConfig().getInt("titleFadeInTime");
            int stay = this.chunksCore.getConfig().getInt("titleStayTime");
            int out = this.chunksCore.getConfig().getInt("titleFadeOutTime");

            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(TextComponent.fromLegacyText(StringUtils.color(msg))));
        } catch (Exception e) {
            e.printStackTrace();

            // An error occurred, use chat
            player.sendMessage(msg);
        }
    }


}
