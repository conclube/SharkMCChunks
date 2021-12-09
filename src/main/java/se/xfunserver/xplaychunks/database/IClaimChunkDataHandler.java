package se.xfunserver.xplaychunks.database;

import org.jetbrains.annotations.Nullable;
import se.xfunserver.xplaychunks.chunks.ChunkPos;
import se.xfunserver.xplaychunks.chunks.DataChunk;
import se.xfunserver.xplaychunks.player.FullPlayerData;
import se.xfunserver.xplaychunks.player.SimplePlayerData;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public interface IClaimChunkDataHandler {

    void init() throws Exception;

    boolean getHasInit();

    void exit() throws Exception;

    void save() throws Exception;

    void load() throws Exception;

    void addClaimedChunk(ChunkPos pos, UUID player);

    void addClaimedChunks(DataChunk[] chunks);

    void removeClaimedChunk(ChunkPos pos);

    boolean isChunkClaimed(ChunkPos pos);

    @Nullable
    UUID getChunkOwner(ChunkPos pos);

    DataChunk[] getClaimedChunks();

    boolean toggleTnt(ChunkPos pos);

    boolean isTntEnabled(ChunkPos pos);

    void addPlayer(UUID player,
                   String lastIgn,
                   Set<UUID> permitted,
                   @Nullable String chunkName,
                   long lastOnlineTime);

    default void addPlayer(FullPlayerData playerData) {
        this.addPlayer(
                playerData.player,
                playerData.lastIgn,
                playerData.permitted,
                playerData.chunkName,
                playerData.lastOnlineTime);
    }

    default void addPlayer(UUID player, String lastIgn) {
        this.addPlayer(player, lastIgn, new HashSet<>(), null, 0L);
    }

    void addPlayers(FullPlayerData[] players);

    @Nullable
    String getPlayerUsername(UUID player);

    @Nullable
    UUID getPlayerUUID(String username);

    void setPlayerLastOnline(UUID playerLastOnline, long time);

    void setPlayerChunkName(UUID player, @Nullable String name);

    @Nullable
    String getPlayerChunkName(UUID player);

    boolean hasPlayer(UUID player);

    Collection<SimplePlayerData> getPlayers();

    FullPlayerData[] getFullPlayerData();

    void setPlayerAccess(UUID owner, UUID accessor, boolean access);

    void givePlayersAccess(UUID owner, UUID[] accesssors);

    void takePlayersAccess(UUID owner, UUID[] accessors);

    UUID[] getPlayersWithAccess(UUID owner);

    boolean playerHasAccess(UUID owner, UUID accessor);
}
