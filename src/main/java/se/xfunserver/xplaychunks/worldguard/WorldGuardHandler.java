package se.xfunserver.xplaychunks.worldguard;

import static se.xfunserver.xplaychunks.worldguard.WorldGuardAPI.*;

import org.bukkit.Chunk;
import se.xfunserver.xplaychunks.xPlayChunks;

public class WorldGuardHandler {

    private static boolean loaded = false;

    public static boolean init(xPlayChunks chunksCore) {
        try {
            return (loaded = _init(chunksCore));
        } catch (NoClassDefFoundError ignored) {
        }
        return false;
    }

    public static boolean isAdminChunk(xPlayChunks chunksCore, Chunk chunk) {
        try {
            // If the WorldGuard API never loaded, just allow the claim.
            return (!loaded || isAdminClaim(chunksCore, chunk));
        } catch (NoClassDefFoundError ignored) {
        }

        // This should never happen, but better safe than sorry
        return true;
    }
}
