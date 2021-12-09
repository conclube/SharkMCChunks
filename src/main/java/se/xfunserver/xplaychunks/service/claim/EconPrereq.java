package se.xfunserver.xplaychunks.service.claim;

import org.jetbrains.annotations.NotNull;
import se.xfunserver.xplaychunks.utils.Messages;
import se.xfunserver.xplaychunks.utils.PluginSettings;

import java.util.Optional;

public class EconPrereq implements IClaimPrereq {

    @Override
    public int getWeight() {
        return 300;
    }

    @Override
    public boolean getPassed(@NotNull PrereqClaimData data) {
        if (data.chunksCore.useEconomy()
                && data.chunksCore.getChunkHandler().getHasAllFreeChunks(data.playerId)) {
            double cost = PluginSettings.getChunkPrice();

            // Check if the chunk is free or the player has enough money
            return cost <= 0 || data.chunksCore.getEconomy().getMoney(data.playerId) >= cost;
        }
        return true;
    }

    @Override
    public Optional<String> getErrorMessage(@NotNull PrereqClaimData data) {
        return Optional.of(Messages.CANNOT_AFFORD.getMessage());
    }

    @Override
    public Optional<String> getSuccessMessage(@NotNull PrereqClaimData data) {
        if (!data.chunksCore.useEconomy()) {
            return Optional.empty();
        }

        if (!data.chunksCore
                .getChunkHandler()
                .getHasAllFreeChunks(data.playerId, data.freeClaims)) {
            if (data.freeClaims <= 1) {
                // Only one free chunk (or error?)
                // We shouldn't get this far if players can't claim free chunks.
                return Optional.of(Messages.CLAIMED_FREE_CHUNK.getMessage());
            }

            return Optional.of(Messages.CLAIMED_FREE_CHUNK.getMessage());
        } else {
            double cost = data.chunksCore.getConfig().getDouble("claim-price");

            return Optional.of(
                    Messages.CLAIMED_CHUNK.getMessage()
                            .replace("%chunkid%", String.format("%s, %s", data.chunk.getX(), data.chunk.getZ()))
                            .replace("%price%", String.valueOf(PluginSettings.getChunkPrice()))
            );
        }
    }

    @Override
    public void onSuccess(@NotNull PrereqClaimData data) {
        if (data.chunksCore.useEconomy()) {
            if (data.claimedBefore < data.freeClaims) {
                // This chunk is free!
                return;
            }

            double cost = PluginSettings.getChunkPrice();

            if (!data.chunksCore.getEconomy().buy(data.playerId, cost)) {
                // Error check
                data.chunksCore.getLogger().severe(String.format("Misslyckades med att köpa chunk (%s, %s) i värld %s för spelare %s",
                        data.chunk.getX(),
                        data.chunk.getZ(),
                        data.chunk.getWorld().getName(),
                        data.player.getName()));
            }
        }
    }
}
