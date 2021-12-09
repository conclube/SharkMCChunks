package se.xfunserver.xplaychunks.player;

import org.bukkit.entity.Player;
import se.xfunserver.xplaychunks.database.IClaimChunkDataHandler;
import se.xfunserver.xplaychunks.xPlayChunks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class PlayerHandler {

    private final IClaimChunkDataHandler dataHandler;
    private final xPlayChunks chunksCore;

    public PlayerHandler(IClaimChunkDataHandler dataHandler, xPlayChunks chunksCore) {
        this.dataHandler = dataHandler;
        this.chunksCore = chunksCore;
    }

    public Collection<SimplePlayerData> getJoinedPlayers() {
        return dataHandler.getPlayers();
    }

    public List<String> getJoinedPlayersFromName(String start) {
        List<String> output = new ArrayList<>();
        for (SimplePlayerData player : getJoinedPlayers()) {
            if (player.lastIgn != null && player.lastIgn.startsWith(start.toLowerCase())) {
                output.add(player.lastIgn);
            }
        }

        return output;
    }

    // Returns weather the player NOW has access
    public boolean toggleAccess(UUID owner, UUID accesssor) {
        boolean newVal = !hasAccess(owner, accesssor);
        dataHandler.setPlayerAccess(owner, accesssor, newVal);
        return newVal;
    }

    public boolean hasAccess(UUID owner, UUID accessor) {
        return dataHandler.playerHasAccess(owner, accessor);
    }

    public UUID[] getAccessPermitted(UUID owner) {
        return dataHandler.getPlayersWithAccess(owner);
    }

    public void setChunkName(UUID owner, String name) {
        dataHandler.setPlayerChunkName(owner, name);
    }

    public void clearChunkName(UUID owner) {
        this.setChunkName(owner, null);
    }

    public String getChunkName(UUID owner) {
        String chunkName = dataHandler.getPlayerChunkName(owner);
        if (chunkName != null) return chunkName + " (" + dataHandler.getPlayerUsername(owner) + ")";

        return dataHandler.getPlayerUsername(owner);
    }

    public boolean hasChunkName(UUID owner) {
        return dataHandler.getPlayerChunkName(owner) != null;
    }

    public String getUsername(UUID player) {
        return dataHandler.getPlayerUsername(player);
    }

    public UUID getUUID(String username) {
        return dataHandler.getPlayerUUID(username);
    }

    public void setLastJoinedTime(UUID player, long time) {
        dataHandler.setPlayerLastOnline(player, time);
    }

    public void onJoin(Player player) {
        if (!dataHandler.hasPlayer(player.getUniqueId())) {
            dataHandler.addPlayer(
                    player.getUniqueId(),
                    player.getName()
            );
        }
    }
}
