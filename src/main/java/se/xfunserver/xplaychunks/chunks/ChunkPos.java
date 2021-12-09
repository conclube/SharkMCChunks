package se.xfunserver.xplaychunks.chunks;

import org.bukkit.Chunk;

import java.util.Objects;

public final class ChunkPos {

    private final String world;
    private final int x;
    private final int z;

    public ChunkPos(String world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public ChunkPos(Chunk chunk) {
        this(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public ChunkPos north() {
        return new ChunkPos(world, x, z - 1);
    }

    public ChunkPos south() {
        return new ChunkPos(world, x, z + 1);
    }

    public ChunkPos east() {
        return new ChunkPos(world, x - 1, z);
    }

    public ChunkPos west() {
        return new ChunkPos(world, x + 1, z);
    }

    @Override
    public String toString() {
        return String.format("%s, %s i %s", x, z, world);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkPos chunkPos = (ChunkPos) o;
        return x == chunkPos.x && z == chunkPos.z && Objects.equals(world, chunkPos.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, z);
    }

}
