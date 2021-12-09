package se.xfunserver.xplaychunks.player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FullPlayerData implements Cloneable {
    public final UUID player;
    public final String lastIgn;
    public final Set<UUID> permitted;
    public String chunkName;
    public long lastOnlineTime;

    public FullPlayerData(
            UUID player,
            String lastIgn,
            Set<UUID> permitted,
            String chunkName,
            long lastOnlineTime) {
        this.player = player;
        this.lastIgn = lastIgn;
        this.permitted = new HashSet<>(permitted);
        this.chunkName = chunkName;
        this.lastOnlineTime = lastOnlineTime;
    }

    private FullPlayerData(FullPlayerData clone) {
        this(
                clone.player,
                clone.lastIgn,
                clone.permitted,
                clone.chunkName,
                clone.lastOnlineTime);
    }

    public SimplePlayerData toSimplePlayer() {
        return new SimplePlayerData(player, lastIgn, lastOnlineTime);
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public FullPlayerData clone() {
        return new FullPlayerData(this);
    }

}
