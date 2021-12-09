package se.xfunserver.xplaychunks.command;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import se.xfunserver.xplaychunks.Econ;
import se.xfunserver.xplaychunks.chunks.ChunkHandler;
import se.xfunserver.xplaychunks.chunks.ChunkPos;
import se.xfunserver.xplaychunks.service.PrereqChecker;
import se.xfunserver.xplaychunks.service.claim.*;
import se.xfunserver.xplaychunks.utils.Messages;
import se.xfunserver.xplaychunks.utils.PluginSettings;
import se.xfunserver.xplaychunks.utils.StringUtils;
import se.xfunserver.xplaychunks.xPlayChunks;

import java.util.ArrayList;
import java.util.UUID;

public final class MainHandler {

    private final xPlayChunks chunksCore;

    public MainHandler(xPlayChunks chunksCore) {
        this.chunksCore = chunksCore;
    }

    public void claimChunk(Player player, Chunk location) {
        final ArrayList<IClaimPrereq> claimPrereqs = new ArrayList<>();

        // Check to see if the chunk is already claimed.
        claimPrereqs.add(new UnclaimedPrereq());

        // Check if the player has room for more chunk claims
        claimPrereqs.add(new MaxChunksPrereq());

        // Check if the chunk has a WorldGuard region in it.
        claimPrereqs.add(new WorldGuardPrereq());

        // Check if economy should be used
        if (chunksCore.useEconomy()) {
            claimPrereqs.add(new EconPrereq());
        }

        // Create the prereq checker object for claiming
        final PrereqChecker<IClaimPrereq, PrereqClaimData> PREREQ =
                new PrereqChecker<>(claimPrereqs);

        final xPlayChunks XPLAY_CHUNKS = chunksCore;
        final ChunkHandler CHUNK_HANDLE = XPLAY_CHUNKS.getChunkHandler();

        PREREQ.check(
                new PrereqClaimData(XPLAY_CHUNKS, location, player.getUniqueId(), player),
                Messages.CLAIMED_CHUNK.getMessage()
                        .replace("%chunkid%", String.format("%s, %s", location.getX(), location.getZ()))
                        .replace("%price%", String.valueOf(PluginSettings.getChunkPrice())),
                errorMsg -> errorMsg.ifPresent(player::sendMessage),
                successMsg -> {
                    // Claim the chunk if nothing is wrong
                    ChunkPos pos =
                            CHUNK_HANDLE.claimChunk(
                                    location.getWorld(), location.getX(), location.getZ(), player.getUniqueId());

                    successMsg.ifPresent(player::sendMessage);

                    // Error check, though it *shouldn't* occur.
                    if (pos == null) {
                        XPLAY_CHUNKS.getLogger().severe(String.format(
                                "Misslyckades med att claima chunk (%s, %s) i världen %s för spelare %s. Data"
                                 + " handler gav tillbaka en null position?",
                                location.getX(), location.getZ(), location.getWorld().getName(), player.getName()));
                    }
                }
        );
    }

    @SuppressWarnings("unused")
    @Deprecated
    public void toggleTnt(Player exeuctor) {
        ChunkHandler chunkHandler = chunksCore.getChunkHandler();
        Chunk chunk = exeuctor.getLocation().getChunk();

        if (chunkHandler.isOwner(chunk, exeuctor)) {
            exeuctor.sendMessage(
                    (chunkHandler.toggleTnt(chunk)
                            ? Messages.TNT_ENABLED.getMessage()
                            : Messages.TNT_DISABLED.getMessage()));
            return;
        }

        exeuctor.sendMessage(Messages.CLAIMED_DENY.getMessage());
    }

    public boolean unclaimChunk(
            boolean adminOverride, Player player, String world, int x, int z) {
        try {
            // Check if the chunk isn't claimed
            ChunkHandler chunkHandler = chunksCore.getChunkHandler();
            World w = Bukkit.getWorld(world);

            if (w == null) {
                chunksCore.getLogger().severe("Failed to locate world: " + world);
                return false;
            }

            if (!chunkHandler.isClaimed(w, x, z)) {
                player.sendMessage(Messages.NOT_OWNER.getMessage());
                return false;
            }

            // Check if the unclaiming player is the owner or admin override is enabled.
            if (!adminOverride && !chunkHandler.isOwner(w, x, z, player)) {
                player.sendMessage(Messages.NOT_OWNER.getMessage());
                return false;
            }

            if (!adminOverride
                && chunksCore.useEconomy()
                && chunkHandler.getClaimed(player.getUniqueId()) > 1) {

                Econ economy = chunksCore.getEconomy();
                double reward = PluginSettings.getChunkPrice();
                if (reward > 0) {
                    economy.addMoney(player.getUniqueId(), reward);
                    player.sendMessage(Messages.UNCLAIMED_CHUNK.getMessage()
                            .replace("%price%", String.valueOf(reward)));
                }
            }

            // Unclaim the chunk
            chunkHandler.unclaimChunk(w, x, z);

            return true;
        } catch (Exception e) {
            this.chunksCore.getLogger().severe(
                    String.format(
                            "Misslyckades med att unclaima chunk för spelare %s vid %s,%s i %s",
                            player.getDisplayName(), x, z, world));
            e.printStackTrace();
        }
        return false;
    }

    public void unclaimChunk(boolean adminOverride, Player p) {
        Chunk chunk = p.getLocation().getChunk();
        unclaimChunk(adminOverride, p, p.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    private void accessChunk(Player ply, String player, boolean multiple) {
        Player other = chunksCore.getServer().getPlayer(player);
        if (other != null) {
            toggleAccess(ply, other.getUniqueId(), other.getName(), multiple);
        } else {
            UUID otherId = chunksCore.getPlayerHandler().getUUID(player);
            if (otherId == null) {
                ply.sendMessage(Messages.COULD_NOT_FIND_PLAYER.getMessage());
                return;
            }

            toggleAccess(ply, otherId, player, multiple);
        }
    }

    public void accessChunk(Player ply, String[] players) {
        for (String player : players) accessChunk(ply, player, players.length > 1);
    }

    private void toggleAccess(Player owner, UUID other, String otherName, boolean multiple) {
        if (owner.getUniqueId().equals(other)) {
            owner.sendMessage(Messages.CANNOT_TRUST_YOURSELF.getMessage());
            return;
        }

        boolean hasAccess = chunksCore.getPlayerHandler().toggleAccess(owner.getUniqueId(), other);
        if (hasAccess) {
            owner.sendMessage(
                    (multiple
                            ? Messages.TRUSTED_MULTIPLE.getMessage()
                            : Messages.ADDED_TRUSTED.getMessage()
                            .replace("%target%", otherName)));
            return;
        }

        owner.sendMessage(
                (multiple
                        ? Messages.TRUSTED_MULTIPLE.getMessage()
                        : Messages.REMOVED_TRUSTED.getMessage()
                        .replace("%target%", otherName)));
    }

    public void listAccesssors(Player executor) {
        boolean anyOthersHaveAccess = false;

        for (UUID player : chunksCore.getPlayerHandler().getAccessPermitted(executor.getUniqueId())) {
            String name = chunksCore.getPlayerHandler().getUsername(player);
            if (name != null) {
                executor.sendMessage(name);
                anyOthersHaveAccess = true;
            }
        }

        if (!anyOthersHaveAccess) {
            executor.sendMessage(StringUtils.color("&cInga spelare har tillgång."));
        }
    }


}
