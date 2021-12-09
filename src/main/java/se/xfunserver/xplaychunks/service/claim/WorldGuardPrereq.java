package se.xfunserver.xplaychunks.service.claim;

import org.jetbrains.annotations.NotNull;
import se.xfunserver.xplaychunks.utils.Messages;
import se.xfunserver.xplaychunks.worldguard.WorldGuardHandler;

import java.util.Optional;

public class WorldGuardPrereq implements IClaimPrereq {

    @Override
    public int getWeight() {
        return 100;
    }

    @Override
    public boolean getPassed(@NotNull PrereqClaimData data) {
        boolean allowedToClaimWorldGuard = !WorldGuardHandler.isAdminChunk(data.chunksCore, data.chunk);
        boolean hasAdminOverride = data.chunksCore.getAdminOverride().hasOverride(data.playerId);

        return allowedToClaimWorldGuard || hasAdminOverride;
    }

    @Override
    public Optional<String> getErrorMessage(@NotNull PrereqClaimData data) {
        return Optional.of(Messages.CLAIMED_DENY.getMessage());
    }
}
