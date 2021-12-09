package se.xfunserver.xplaychunks.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import se.xfunserver.xplaychunks.xPlayChunks;

public class PlayerConnectionHandler implements Listener {

    private final xPlayChunks chunksCore;

    public PlayerConnectionHandler(xPlayChunks chunksCore) {
        this.chunksCore = chunksCore;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        chunksCore.getPlayerHandler().onJoin(e.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        chunksCore.getAdminOverride().remove(e.getPlayer().getUniqueId());
    }
}
