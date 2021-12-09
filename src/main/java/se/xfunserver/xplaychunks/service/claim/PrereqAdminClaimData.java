package se.xfunserver.xplaychunks.service.claim;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import se.xfunserver.xplaychunks.xPlayChunks;

import java.util.UUID;

public class PrereqAdminClaimData {

    public final xPlayChunks chunksCore;

    public final Chunk chunk;
    public final UUID playerId;

    public PrereqAdminClaimData(
            @NotNull xPlayChunks chunksCore,
            @NotNull Chunk chunk,
            @NotNull UUID playerId) {

        this.chunksCore = chunksCore;
        this.chunk = chunk;
        this.playerId = playerId;
    }
}
