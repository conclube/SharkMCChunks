package se.xfunserver.xplaychunks.service.claim;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import se.xfunserver.xplaychunks.xPlayChunks;

import java.util.UUID;

public final class PrereqClaimData {

    public final xPlayChunks chunksCore;

    public final Chunk chunk;
    public final UUID playerId;
    public final Player player;

    // Automatically loaded
    public final int claimedBefore;
    public final int maxClaimed;
    public final int freeClaims;

    public PrereqClaimData(
            @NotNull xPlayChunks chunksCore,
            @NotNull Chunk chunk,
            @NotNull UUID playerId,
            @NotNull Player player) {

        this.chunksCore = chunksCore;
        this.chunk = chunk;
        this.playerId = playerId;
        this.player = player;

        this.claimedBefore = chunksCore.getChunkHandler().getClaimed(playerId);
        this.maxClaimed = chunksCore.getRankHandler().getMaxClaimsForPlayer(player);
        this.freeClaims = 1;
    }
}
