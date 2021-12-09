package se.xfunserver.xplaychunks.chunks;

import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import se.xfunserver.xplaychunks.xPlayChunks;

import java.util.Set;

public class ChunkOutlineHandler {

    private xPlayChunks chunksCore;

    private Particle particle;
    private long particlePeriodTicks;
    private int yHeight;
    private int particleCount;

    public ChunkOutlineHandler(
            xPlayChunks chunksCore,
            Particle particle,
            long particlePeriodTicks,
            int yHeight,
            int particleCount) {

        this.chunksCore = chunksCore;
        this.particle = particle;
        this.particlePeriodTicks = particlePeriodTicks;
        this.yHeight = yHeight;
        this.particleCount = particleCount;
    }


    public record OutlineSides(boolean north, boolean south, boolean east, boolean west) {
        public boolean empty() {
            return !(north || south || east || west);
        }

        public static OutlineSides makeAll(boolean show) {
            return new OutlineSides(show, show, show, show);
        }
    }

    public void showChunkFor(
            @NotNull ChunkPos chunkPos,
            @NotNull Player player,
            int durationInSeconds,
            @NotNull OutlineSides outlineSides) {
        if (!player.isOnline() || outlineSides.empty()) return;

        ChunkOutlineEntry entry =
                new ChunkOutlineEntry(
                        chunkPos, player, outlineSides, player.getLocation().getBlockY());
        entry.cyclesLeft = durationInSeconds * 20L / particlePeriodTicks;
        entry.taskId =
                chunksCore
                        .getServer()
                        .getScheduler()
                        .scheduleSyncRepeatingTask(
                                // Slight delay (1 tick), just in case. ;)
                                chunksCore, entry::onParticle, 1L, particlePeriodTicks);


    }

    public void showChunksFor(
            @NotNull Set<ChunkPos> chunksPos, @NotNull Player player, int durationInSeconds) {
        if (!player.isOnline()) return;

        for (ChunkPos chunkPos : chunksPos) {
            boolean north = !chunksPos.contains(chunkPos.north());
            boolean south = !chunksPos.contains(chunkPos.south());
            boolean east = !chunksPos.contains(chunkPos.east());
            boolean west = !chunksPos.contains(chunkPos.west());
            OutlineSides outlineSides = new OutlineSides(north, south, east, west);
            if (!outlineSides.empty()) {
                this.showChunkFor(chunkPos, player, durationInSeconds, outlineSides);
            }
        }
    }


    private class ChunkOutlineEntry {

        final ChunkPos chunkPos;
        final Player player;
        final OutlineSides sidesShown;
        final int plyY;

        int taskId = -1;
        long cyclesLeft;

        private ChunkOutlineEntry(ChunkPos chunkPos, Player player, OutlineSides sidesShown, int plyY) {
            this.chunkPos = chunkPos;
            this.player = player;
            this.sidesShown = sidesShown;
            this.plyY = plyY;
        }

        void onParticle() {
            // Loop through 'yHeight * 2 + 1' y-levels.
            for (int y = plyY - yHeight; y <= plyY + yHeight; y++) {
                int zAt = chunkPos.getZ() << 4;
                // Spawn particles along the x-axis
                if (sidesShown.north | sidesShown.south) {
                    for (int x = chunkPos.getX() << 4; x < ((chunkPos.getX() + 1) << 4); x++) {
                        if (sidesShown.north) this.spawnParticle(player, x, y, zAt);
                        if (sidesShown.south) this.spawnParticle(player, x, y, zAt + 15);
                    }
                }

                // Spawn particle along the z-axis (offset because corners were handled long the
                // x-axis)
                if (sidesShown.east | sidesShown.west) {
                    // TODO: Ignore these offsets for now, overdraw doesn't concern me very much lol
                    for (int z = zAt /* + 1 */; z < zAt + 16 /* - 1 */; z++) {
                        int xAt = chunkPos.getX() << 4;
                        if (sidesShown.east) this.spawnParticle(player, xAt, y, z);
                        if (sidesShown.west) this.spawnParticle(player, xAt + 15, y, z);
                    }
                }
            }

            // Cancel this task once its time has run out
            if (--cyclesLeft == 0) {
                chunksCore.getServer().getScheduler().cancelTask(taskId);
            }
        }

        private void spawnParticle(Player player, int x, int y, int z) {
            player.spawnParticle(
                    particle, x + 0.5d, y + 0.5d, z + 0.5d, particleCount, 0.0, 0.0, 0.0, 0.0);
        }
    }
}
