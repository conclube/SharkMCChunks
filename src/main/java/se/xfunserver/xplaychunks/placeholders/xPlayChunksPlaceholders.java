package se.xfunserver.xplaychunks.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import se.xfunserver.xplaychunks.xPlayChunks;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class xPlayChunksPlaceholders extends PlaceholderExpansion {

    private final xPlayChunks chunksCore;

    private final HashMap<String, Supplier<Object>> placeholders = new HashMap<>();
    private final HashMap<String, Function<OfflinePlayer, Object>> offlinePlayerPlaceholders = new HashMap<>();
    private final HashMap<String, Function<Player, Object>> playerPlaceholders = new HashMap<>();
    private final HashMap<String, BiFunction<Player, Optional<UUID>, Object>> playerOwnerPlaceholders = new HashMap<>();

    public xPlayChunksPlaceholders(xPlayChunks chunksCore) {
        this.chunksCore = chunksCore;

        /* General Placeholders */

        /* Offline Player Placeholders */

        // This player's chunk name
        offlinePlayerPlaceholders.put(
                "my_name", player -> chunksCore.getPlayerHandler().getChunkName(player.getUniqueId()));

        // This player's total number of claimed chunks
        offlinePlayerPlaceholders.put(
                "my_claims", player -> chunksCore.getChunkHandler().getClaimed(player.getUniqueId()));

        /* Online Player Placeholders */

        // Whether this player has permission to edit in this chunk
        playerOwnerPlaceholders.put(
                "am_trusted",
                (player, owner) ->
                        owner.isPresent() && chunksCore
                                .getPlayerHandler()
                                .hasAccess(owner.get(), player.getUniqueId())
                              ? "Tillåten"
                              : "Ej tillåten");

        // Get the username of the owner for this chunk
        playerOwnerPlaceholders.put(
                "current_owner",
                (player, owner) ->
                        owner.map(o -> chunksCore.getPlayerHandler().getUsername(o))
                                .orElse("Ingen (Vildmarken)"));

        // Get the display name for this chunk
        playerOwnerPlaceholders.put(
                "current_name",
                (player, owner) ->
                        owner.map(o -> chunksCore.getPlayerHandler().getChunkName(o))
                                .orElse("Ingen (Vildmarken)"));
    }

    @Override
    public @NotNull String getIdentifier() {
        return "xplaychunks";
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @NotNull String getAuthor() {
        return Arrays.toString(chunksCore.getDescription().getAuthors().toArray(new String[0]));
    }

    @Override
    public @NotNull String getVersion() {
        return chunksCore.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        // Check for a general placeholder
        Optional<Object> replacement =
                Optional.ofNullable(placeholders.get(params)).map(Supplier::get);
        if (replacement.isPresent()) {
            return Objects.toString(replacement.get());
        }

        // Check for an offline player placeholder
        replacement =
                Optional.ofNullable(offlinePlayerPlaceholders.get(params))
                        .map(f -> f.apply(player));
        if (replacement.isPresent()) {
            return Objects.toString(replacement.get());
        }

        // If the player is online, try some other placeholders
        if (player instanceof Player) {
            return onPlaceholderRequest((Player) player, params);
        }

        // No placeholder found
        return null;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        // Check for an online player placeholders
        Optional<Object> replacement =
                Optional.ofNullable(playerPlaceholders.get(params))
                        .map(f -> f.apply(player));
        if (replacement.isPresent()) {
            return Objects.toString(replacement.get());
        }

        // Get the owner of the chunk in which `player` is standing
        UUID chunkOwner =
                chunksCore.getChunkHandler().getOwner(player.getLocation().getChunk());
        return Optional.ofNullable(playerOwnerPlaceholders.get(params))
                .map(f -> f.apply(player, Optional.ofNullable(chunkOwner)))
                .map(Object::toString)
                .orElse(null);
    }
}
