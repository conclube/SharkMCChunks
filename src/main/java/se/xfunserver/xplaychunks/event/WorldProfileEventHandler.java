package se.xfunserver.xplaychunks.event;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import se.xfunserver.xplaychunks.chunks.ChunkHandler;
import se.xfunserver.xplaychunks.chunks.ChunkPos;
import se.xfunserver.xplaychunks.utils.StringUtils;
import se.xfunserver.xplaychunks.xPlayChunks;

import java.util.*;

public class WorldProfileEventHandler implements Listener {

    private final xPlayChunks chunksCore;

    public WorldProfileEventHandler(xPlayChunks chunksCore) {
        this.chunksCore = chunksCore;
    }

    @EventHandler
    public void onEntityInteraction(PlayerInteractEntityEvent event) {
        if (event != null && !event.isCancelled()) {

            // Check if the player can interact with this entity
            onEntityEvent(
                    () -> event.setCancelled(true),
                    event.getPlayer(),
                    event.getRightClicked());
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event != null && !event.isCancelled()) {
            // CHeck if the entity is a player
            Player player = unwrapPlayer(event.getDamager());

            // If the action isn't being performed by a player, we don't
            // particularly care.
            if (player != null) {
                // Check if the player can damage this entity
                onEntityEvent(
                        () -> event.setCancelled(true),
                        player,
                        event.getEntity());
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event != null && !event.isCancelled()) {

            // Check if the player can break this block
            onBlockEvent(
                    () -> event.setCancelled(true),
                    event.getPlayer(),
                    event.getBlock().getType(),
                    event.getBlock());
        }
    }

    @EventHandler
    public void onBlockExplode(EntityChangeBlockEvent event) {
        if (event != null
            && !event.isCancelled()
            && (event.getEntityType() == EntityType.WITHER
                || event.getEntityType() == EntityType.WITHER_SKULL)) {

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event == null || event.isCancelled()) return;

        // Check to make sure this block doesn't connect to any blocks in a claim.
        onBlockAdjacentCheck(() -> event.setCancelled(true), event.getPlayer(), event.getBlock());

        // Make sure the event was not cancelled by the adjacent check.
        if (event.isCancelled()) return;

        // Check if the player can place this block
        onBlockEvent(
                () -> event.setCancelled(true),
                event.getPlayer(),
                event.getBlock().getType(),
                event.getBlock());
    }

    @EventHandler
    public void onBlockInteraction(PlayerInteractEvent event) {
        if (event != null
            && event.getClickedBlock() != null
            && event.getClickedBlock().getType() != Material.AIR
            && event.useInteractedBlock() == Event.Result.ALLOW
            && ((event.getAction() == Action.RIGHT_CLICK_BLOCK
                && (!event.isBlockInHand() || !event.getPlayer().isSneaking())
                && event.useInteractedBlock() == Event.Result.ALLOW)
                || event.getAction() == Action.PHYSICAL)) {

            // Check if the player can interact with this block
            onBlockEvent(
                    () -> event.setUseInteractedBlock(Event.Result.DENY),
                    event.getPlayer(),
                    event.getClickedBlock().getType(),
                    event.getClickedBlock());
        }
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (event != null && !event.isCancelled()) {
            // Check if the break was the result of an explosion
            if (event.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION) {
                event.setCancelled(true);
                return;
            }

            // Otherwise, check if the entity is a player
            Player player = unwrapPlayer(event.getRemover());

            // If the action isn't being performed by a player, we don't
            // particularly care now.
            if (player != null) {
                // Check if the player can damage this entity
                onEntityEvent(
                        () -> event.setCancelled(true),
                        player,
                        event.getEntity());
            }
        }
    }

    @EventHandler
    public void onLiquidPickup(PlayerBucketFillEvent event) {
        if (event == null || event.isCancelled()) return;

        // Check if the player can break this block
        onBlockEvent(
                () -> event.setCancelled(true),
                event.getPlayer(),
                event.getBlock().getType(),
                event.getBlock());
    }

    @EventHandler
    public void onLiquidPlace(PlayerBucketEmptyEvent event) {
        if (event == null || event.isCancelled()) return;

        // Determine the kind of liquid contained within the bucket
        Material bucketLiquid = null;
        if (event.getBucket() == Material.WATER_BUCKET) bucketLiquid = Material.WATER;
        if (event.getBucket() == Material.LAVA) bucketLiquid = Material.LAVA;
        if (bucketLiquid == null) return;

        // Check if the player can place this block
        onBlockEvent(
                () -> event.setCancelled(true),
                event.getPlayer(),
                bucketLiquid,
                event.getBlock());
    }

    private void onEntityEvent(
            @NotNull Runnable cancel,
            @NotNull Player player,
            @NotNull Entity entity) {

        final UUID ply = player.getUniqueId();
        // Check if the player has AdminOverride
        // (early return)
        if (chunksCore.getAdminOverride().hasOverride(ply)) return;

        final UUID chunkOwner =
                chunksCore.getChunkHandler().getOwner(entity.getLocation().getChunk());
        final boolean isOwner = (chunkOwner != null && chunkOwner.equals(ply));
        final boolean isOwnerOrAccess =
                isOwner || (chunkOwner != null
                        && chunksCore.getPlayerHandler().hasAccess(chunkOwner, ply));

        if (!isOwnerOrAccess) {
            // Send cancellation message.
            StringUtils.msg(player, chunksCore.getChunkHandler().isClaimed(entity.getLocation().getChunk())
                    ? chunksCore.getMessages().denyClaimed
                    : chunksCore.getMessages().denyWilderness);

            // cancel event
            cancel.run();
        }
    }

    @EventHandler
    public void onFishCapture(PlayerBucketEntityEvent event) {
        if (event == null || event.isCancelled()) return;

        // Delegate to interaction event permissions
        onEntityEvent(
                () -> event.setCancelled(true),
                event.getPlayer(),
                event.getEntity());
    }

    @EventHandler
    public void onLeadCreate(PlayerLeashEntityEvent event) {
        if (event == null || event.isCancelled()) return;

        // Check if the player can interact with this entity
        onEntityEvent(
                () -> event.setCancelled(true),
                event.getPlayer(),
                event.getEntity());
    }

    @EventHandler
    public void onLeadDestroy(PlayerUnleashEntityEvent event) {
        if (event == null || event.isCancelled()) return;

        // Check if the player can damage this entity
        onEntityEvent(
                () -> event.setCancelled(true),
                event.getPlayer(),
                event.getEntity());
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        if (event == null || event.isCancelled()) return;

        // Check if the player can damage this entity
        onEntityEvent(
                () -> event.setCancelled(true),
                event.getPlayer(),
                event.getRightClicked());
    }

    @EventHandler
    public void onEntityDamagedByEntityExplosion(EntityDamageByEntityEvent event) {
        if (event != null
                && !event.isCancelled()
                && (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                || event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)) {

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFireSpread(BlockSpreadEvent event) {
        if (event != null && !event.isCancelled() && event.getSource().getType() == Material.FIRE) {
            onSpreadEvent(
                    () -> event.setCancelled(true),
                    event.getSource(),
                    event.getBlock());
        }
    }

    @EventHandler
    public void onLiquidSpread(BlockFromToEvent event) {
        if (event != null && !event.isCancelled()) {
            // Get the spreading block type
            Material blockType = event.getBlock().getType();
            if (blockType != Material.WATER
                // Protection against waterlogged block water spread
                && !isWaterlogged(event.getBlock())
                && blockType != Material.LAVA) {

                return;
            }

            // Check if we need to cancel this event
            onSpreadEvent(
                    () -> event.setCancelled(true),
                    event.getBlock(),
                    event.getToBlock());
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (event != null && !event.isCancelled()) {
            onPistonAction(
                    () -> event.setCancelled(true),
                    event.getBlock(),
                    event.getDirection(),
                    event.getBlocks());
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (event != null && !event.isCancelled()) {
            onPistonAction(
                    () -> event.setCancelled(true),
                    event.getBlock(),
                    event.getDirection(),
                    event.getBlocks());
        }
    }

    @EventHandler
    public void onBonemeal(BlockFertilizeEvent event) {
        if (event == null || event.isCancelled()) return;

        // Get info
        Player player = event.getPlayer();
        if (player == null) return;
        UUID mealer = player.getUniqueId();

        // Check if admin to bypass
        if (chunksCore.getAdminOverride().hasOverride(mealer)) return;

        // Cache chunk ownership because why not
        HashMap<ChunkPos, Boolean> claimed = new HashMap<>();
        // Keep track of blocks to remove from the list
        HashSet<BlockState> remove = new HashSet<>();

        // Decide, which blocks to remove from the change list
        event.getBlocks().stream()
                .filter(
                        blockState -> {
                            ChunkPos p = new ChunkPos(blockState.getChunk());
                            return claimed.computeIfAbsent(
                                    p,
                                    pos ->
                                            // This method returns `true` if the method should
                                            // be cancelled.
                                            onBlockEvent(
                                                    player,
                                                    blockState.getType(),
                                                    blockState.getBlock()));
                        }).forEach(remove::add);
        // Remove all the blocks previously designated for removal
        event.getBlocks().removeAll(remove);
    }


    private void onBlockAdjacentCheck(
            @NotNull Runnable cancel, @NotNull Player player, @NotNull Block block) {

        final UUID ply = player.getUniqueId();
        // Check if the player has AdminOverride
        // (early return)
        if (chunksCore.getAdminOverride().hasOverride(ply)) return;

        final UUID chunkOwner = chunksCore.getChunkHandler().getOwner(block.getChunk());

        // Loop through adjacent horizontal neighbors
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                // Non-diagonal check
                if (x != z) {
                    // Get neighbor information
                    final Block neighbor = block.getRelative(x, 0, z);
                    final UUID neighborOwner =
                            chunksCore.getChunkHandler().getOwner(neighbor.getChunk());

                    final boolean isOwner = (chunkOwner != null && chunkOwner.equals(ply));
                    final boolean isOwnerOrAccess =
                            isOwner || (chunkOwner != null)
                            && chunksCore.getPlayerHandler().hasAccess(chunkOwner, ply);

                    if (neighbor.getType() == block.getType()
                        && neighborOwner != null
                        && neighborOwner != chunkOwner
                        && !isOwnerOrAccess) {

                        // cancel event
                        cancel.run();

                        // Send cancellation message.
                        StringUtils.msg(player, chunksCore.getChunkHandler().isClaimed(neighbor.getChunk())
                                ? chunksCore.getMessages().denyClaimed
                                : chunksCore.getMessages().denyWilderness);

                        // Just break here
                        return;
                    }
                }
            }
        }
    }

    private void onBlockEvent(
            @NotNull Runnable cancel,
            @NotNull Player player,
            @NotNull Material blockType,
            @NotNull Block block) {
        if (this.onBlockEvent(player, blockType, block)) {
            cancel.run();
        }
    }

    private boolean onBlockEvent(
            @NotNull Player player,
            @NotNull Material blockType,
            @NotNull Block block) {

        final UUID ply = player.getUniqueId();
        // Check if the player has AdminOverride
        // If they do, let the event pass through without being cancelled.
        if (chunksCore.getAdminOverride().hasOverride(ply)) return false;

        final UUID chunkOwner = chunksCore.getChunkHandler().getOwner(block.getChunk());
        final boolean isOwner = (chunkOwner != null && chunkOwner.equals(ply));
        final boolean isOwnerOrAccess =
                isOwner || (chunkOwner != null)
                && chunksCore.getPlayerHandler().hasAccess(chunkOwner, ply);

        // Cancel the event if the player isn't owner or have access.
        // Let the event pass if they are the owner or has access.
        return !isOwnerOrAccess;
    }

    @Deprecated
    private void onExplosionEvent(@NotNull Collection<Block> blockList) {
        // Get the chunk handler
        final ChunkHandler chunkHandler = chunksCore.getChunkHandler();

        // Cache chunks to avoid so many look-ups through the chunk handler
        // The value is a boolean representing whether to cancel the event. `true` means the
        // event will be cancelled.
        final HashMap<Chunk, Boolean> cancelChunks = new HashMap<>();
        final ArrayList<Block> blocksCopy = new ArrayList<>(blockList);

        // Loop through all the blocks
        for (Block block :  blocksCopy) {
            // Get the chunk this block is in
            final Chunk chunk = block.getChunk();

            // Check if this type of block should be protected
        }
    }

    private void onSpreadEvent(
            @NotNull Runnable cancel,
            @NotNull Block sourceBlock,
            @NotNull Block newBlock) {
        // Check chunks
        Chunk sourceChunk = sourceBlock.getChunk();
        Chunk newChunk = newBlock.getChunk();

        // Get the owners of the chunks
        UUID sourceOwner = chunksCore.getChunkHandler().getOwner(sourceChunk);
        UUID newOwner = chunksCore.getChunkHandler().getOwner(newChunk);

        if (sourceOwner != null && newOwner != null && !sourceOwner.equals(newOwner) || !chunksCore.getChunkHandler().isClaimed(newChunk)) {
            cancel.run();
        }
    }

    private void onPistonAction(
            @NotNull Runnable cancel,
            @NotNull Block piston,
            @NotNull BlockFace direction,
            @NotNull List<Block> blocks) {

        // Get the source and target chunks
        UUID sourceChunkOwner = chunksCore.getChunkHandler().getOwner(piston.getChunk());
        HashMap<Chunk, UUID> targetChunkOwners = new HashMap<>();
        // Keep a set of all blocks that will be moved, including future
        // positions of the moving blocks.
        HashSet<Block> allBlocks = new HashSet<>(blocks);
        blocks.stream().map(block -> block.getRelative(direction)).forEach(allBlocks::add);

        // Loop through all the involved blocks and fill all affected
        // chunks.
        for (Block block : allBlocks) {
            targetChunkOwners.computeIfAbsent(
                    block.getChunk(), (chunk) -> chunksCore.getChunkHandler().getOwner(chunk));
        }

        // Check if unclaimed to claimed piston actions are protected
        if (sourceChunkOwner == null) {
            for (UUID owner : targetChunkOwners.values()) {
                if (owner != null) {
                    cancel.run();
                    return;
                }
            }
        }

        // Check if claimed to unclaimed pistons actions and claimed to claimed piston actions are protected.
        for (UUID owner : targetChunkOwners.values()) {
            if (owner == null) {
                cancel.run();
                return;
            } else if (!owner.equals(sourceChunkOwner)
                    && !chunksCore.getPlayerHandler().hasAccess(owner, sourceChunkOwner)) {

                cancel.run();
                return;
            }
        }
    }

    private static boolean isWaterlogged(@NotNull Block block) {
        // Get the block data
        BlockData blockData  = block.getBlockData();

        // Check if this block can be waterlogged
        if (blockData instanceof Waterlogged) {
            // Check if the block is currently waterlogged
            return ((Waterlogged) blockData).isWaterlogged();
        }

        // Not a waterlog-able block
        return false;
    }

    private static @Nullable Player unwrapPlayer(@Nullable Entity possiblePlayer) {
        // Null check for safety
        if (possiblePlayer == null) return null;

        // Player entity
        if (possiblePlayer instanceof Player) return (Player) possiblePlayer;

        // Player shot a projectile
        if (possiblePlayer instanceof Projectile
            && ((Projectile) possiblePlayer).getShooter() instanceof Player) {

            return (Player) ((Projectile) possiblePlayer).getShooter();
        }

        // Either unimplemented or no player retrievable
        return null;
    }

}

