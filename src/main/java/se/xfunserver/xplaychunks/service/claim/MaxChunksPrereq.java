package se.xfunserver.xplaychunks.service.claim;

import org.jetbrains.annotations.NotNull;
import se.xfunserver.xplaychunks.utils.Messages;

import java.util.Optional;

public final class MaxChunksPrereq implements IClaimPrereq {

    @Override
    public int getWeight() {
        return 200;
    }

    @Override
    public boolean getPassed(@NotNull PrereqClaimData data) {
        return !(data.maxClaimed > 0 && data.claimedBefore >= data.maxClaimed);
    }

    @Override
    public Optional<String> getErrorMessage(@NotNull PrereqClaimData data) {
        return Optional.of(Messages.REACHED_MAX_CHUNKS.getMessage());
    }
}
