package se.xfunserver.xplaychunks.chunks;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import se.xfunserver.xplaychunks.database.IClaimChunkDataHandler;
import se.xfunserver.xplaychunks.xPlayChunks;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ChunkHandler {

    private final IClaimChunkDataHandler dataHandler;
    private final xPlayChunks chunkCore;

    public ChunkHandler(IClaimChunkDataHandler dataHandler, xPlayChunks chunkCore) {
        this.dataHandler = dataHandler;
        this.chunkCore = chunkCore;
    }

    public static enum FloodClaimResult {
        SUCCESSFUL,

        TOO_MANY_RECURSIONS,

        COLLECTION_TOO_BIG,

        HIT_NONPLAYER_CLAIM;
    }

    private void claimAll(Collection<ChunkPos> chunks, UUID player) {
        for (ChunkPos chunk : chunks) {
            dataHandler.addClaimedChunk(chunk, player);
        }
    }

    public ChunkPos claimChunk(String world, int x, int z, UUID player) {
        if (isClaimed(world, x, z)) {
            return null;
        }

        // Create a chunk position representation
        ChunkPos chunkPos = new ChunkPos(world, x, z);

        // Add the chunk to the claimed chunk.
        dataHandler.addClaimedChunk(chunkPos, player);

        // Return the chunk position.
        return chunkPos;
    }

    public ChunkPos claimChunk(World world, int x, int z, UUID player) {
        return claimChunk(world.getName(), x, z, player);
    }

    public void unclaimChunk(World world, int x, int z) {
        if (isClaimed(world, x, z)) {
            // If the chunk is claimed, remove it from the claimed chunk list.
            dataHandler.removeClaimedChunk(new ChunkPos(world.getName(), x ,z));
        }
    }

    public void unclaimChunk(String world, int x, int z) {
        if (isClaimed(world, x, z)) {
            // If the chunk is claimed, remove it from the claimed chunk list.
            dataHandler.removeClaimedChunk(new ChunkPos(world, x, z));
        }
    }

    public int getClaimed(UUID player) {
        int count = 0;

        // Loop through all chunks
        for (DataChunk chunk : dataHandler.getClaimedChunks()) {
            // Increment for all chunks owner by this player.
            if (chunk.player.equals(player)) count++;
        }

        return count;
    }

    public ChunkPos[] getClaimedChunks(UUID player) {
        // Create a set for the chunks
        Set<ChunkPos> chunks = new HashSet<>();

        // Loop through all chunks
        for (DataChunk chunk : dataHandler.getClaimedChunks()) {
            // Add chunks that are owner by this player
            if (chunk.player.equals(player)) chunks.add(chunk.chunk);
        }

        // Convert the set into an array
        return chunks.toArray(new ChunkPos[0]);
    }

    public boolean getHasAllFreeChunks(UUID ply) {
        return getHasAllFreeChunks(ply, 1);
    }

    public boolean getHasAllFreeChunks(UUID player, int count) {
        // Counter
        int total = 0;

        // If there are no free chunks, there's no point in checking.
        if (count <= 0) return true;

        // Loop through all claimed chunks
        for (DataChunk chunk : dataHandler.getClaimedChunks()) {
            if (chunk.player.equals(player)) {
                // If this player is the owner, increment the counter.
                total++;

                // If they have the max (or more), they have claimed all the free
                // chunks they are supposed to be able to claim.
                if (total >= count) return true;
            }
        }

        // They have not claimed all the chunks that they can claim.
        return false;
    }

    public boolean isClaimed(World world, int x, int z) {
        return dataHandler.isChunkClaimed(new ChunkPos(world.getName(), x, z));
    }

    public boolean isClaimed(String world, int x, int z) {
        return dataHandler.isChunkClaimed(new ChunkPos(world, x, z));
    }

    public boolean isClaimed(Chunk chunk) {
        return isClaimed(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    public boolean isOwner(World world, int x, int z, UUID player) {
        ChunkPos pos = new ChunkPos(world.getName(), x, z);
        UUID owner = dataHandler.getChunkOwner(pos);

        return owner != null && owner.equals(player);
    }

    public boolean isOwner(World world, int x, int z, Player player) {
        return this.isOwner(world, x, z, player.getUniqueId());
    }

    public boolean isOwner(Chunk chunk, UUID player) {
        return this.isOwner(chunk.getWorld(), chunk.getX(), chunk.getZ(), player);
    }

    public boolean isOwner(Chunk chunk, Player ply) {
        return isOwner(chunk.getWorld(), chunk.getX(), chunk.getZ(), ply);
    }

    public UUID getOwner(World world, int x, int z) {
        ChunkPos pos = new ChunkPos(world.getName(), x, z);
        return !dataHandler.isChunkClaimed(pos) ? null : dataHandler.getChunkOwner(pos);
    }

    public UUID getOwner(Chunk chunk) {
        ChunkPos pos = new ChunkPos(chunk);
        return !dataHandler.isChunkClaimed(pos) ? null : dataHandler.getChunkOwner(pos);
    }

    public UUID getOwner(ChunkPos pos) {
        return !dataHandler.isChunkClaimed(pos) ? null : dataHandler.getChunkOwner(pos);
    }

    public boolean toggleTnt(Chunk chunk) {
        return dataHandler.toggleTnt(new ChunkPos(chunk));
    }

    public boolean isTntEnabled(Chunk chunk) {
        return dataHandler.isTntEnabled(new ChunkPos(chunk));
    }
}
