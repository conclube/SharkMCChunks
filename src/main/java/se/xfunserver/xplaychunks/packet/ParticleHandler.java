package se.xfunserver.xplaychunks.packet;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

@Deprecated
public final class ParticleHandler {

    public static void spawnParticleForPlayers(
            Particle particle, Location loc, int count, Player... players) {
        for (Player player : players) {
            if (player != null && player.isOnline()) {
                player.spawnParticle(particle, loc, count);
            }
        }
    }

}
