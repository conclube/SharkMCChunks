package se.xfunserver.xplaychunks;

import lombok.Getter;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import se.xfunserver.xplaychunks.chunks.ChunkHandler;
import se.xfunserver.xplaychunks.chunks.ChunkOutlineHandler;
import se.xfunserver.xplaychunks.command.CommandManager;
import se.xfunserver.xplaychunks.command.MainHandler;
import se.xfunserver.xplaychunks.database.IClaimChunkDataHandler;
import se.xfunserver.xplaychunks.database.mysql.MySQLDataHandler;
import se.xfunserver.xplaychunks.event.PlayerConnectionHandler;
import se.xfunserver.xplaychunks.event.PlayerMovementHandler;
import se.xfunserver.xplaychunks.event.WorldProfileEventHandler;
import se.xfunserver.xplaychunks.player.AdminOverride;
import se.xfunserver.xplaychunks.player.PlayerHandler;
import se.xfunserver.xplaychunks.worldguard.WorldGuardHandler;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;

public final class xPlayChunks extends JavaPlugin {

    private static xPlayChunks instance;
    private static boolean worldGuardRegisteredFlag = false;

    private IClaimChunkDataHandler dataHandler;

    private boolean useEconomy = false;
    private Econ economy;

    private ChunkHandler chunkHandler;
    private PlayerHandler playerHandler;
    private WorldGuardHandler worldGuardHandler;

    private MainHandler mainHandler;
    @Getter private ChunkOutlineHandler chunkOutlineHandler;

    @Getter private final CommandManager commandManager = new CommandManager();
    @Getter private final AdminOverride adminOverride = new AdminOverride();;

    @Override
    public void onLoad() {
        instance = this;

        if (!worldGuardRegisteredFlag) {
            // Enable WorldGuard support if possible
            if (WorldGuardHandler.init(this)) {
                worldGuardRegisteredFlag = true;
                this.getLogger().info("WorldGuard stöttas nu av xPlayChunks.");
            } else {
                this.getLogger().severe(
                        "Hittade inte pluginet WorldGuard.");
            }
        }
    }

    @Override
    public void onEnable() {
        if (!initDataHandler()) {
            disable();
            return;
        }

        this.saveDefaultConfig();
        this.reloadConfig();

        economy = new Econ();

        this.setupEvents();
        this.initEconomy();

        chunkHandler = new ChunkHandler(dataHandler, this);
        playerHandler = new PlayerHandler(dataHandler, this);
        mainHandler = new MainHandler(this);

        try {
            dataHandler.load();
        } catch (Exception e) {
            this.getLogger().severe("Failed to load the data handler, xPlayChunks will be disabled!");
            this.getLogger().severe("Here is the error for reference:");
            e.printStackTrace();

            this.disable();
        }

        Particle particle;
        try {
            particle = Particle.valueOf(this.getConfig().getString("chunkOutline.name"));
        } catch (Exception e) {
            particle = Particle.SMOKE_NORMAL;
        }

        chunkOutlineHandler =
                new ChunkOutlineHandler(
                        this,
                        particle,
                        20 / this.getConfig().getInt("chunkOutline.spawnsPerSecond"),
                        this.getConfig().getInt("chunkOutline.heightRadius"),
                        this.getConfig().getInt("chunkOutline.particlesPerSpawn"));

    }

    private boolean initDataHandler() {
        if (dataHandler == null) {
            dataHandler = new MySQLDataHandler<>(this);
        }

        this.getLogger().info("Using data handler: " + dataHandler.getClass().getName());

        try {
            dataHandler.init();
            return true;
        } catch (Exception e) {
            this.getLogger().severe(
                    "Failed to initialize data storage system \"" + dataHandler.getClass().getName() + "\", disable xPlayChunks."
            );

            e.printStackTrace();
        }

        return false;
    }

    private void initEconomy() {
        // Check if the economy is enabled and vault is present
        useEconomy = this.getServer().getPluginManager().getPlugin("Vault") != null;

        // Try to initialize the economy if it exists
        if (useEconomy) {
            // Try to set up the Vault economy
            if (economy.setupEconomy(this)) {
                // It was successful
                this.getLogger().info("Hittade och registrerade Vault till ( xPlayChunks ).");

                // Display the money format as an economy debug
                this.getServer()
                        .getScheduler()
                        .scheduleSyncDelayedTask(
                                this,
                                () -> this.getLogger().info(String.format("Pengar Format: %s", economy.format(99132.76d))),
                                0L);
                return;
            }

            this.getLogger().severe("Vault pluginet kunde inte sättas upp. Se till att du har ett ekonomi"
                    + " plugin (som Essentials) installerat. Ekonomi funktioner har blivit"
                    + " avstängd; chunk claiming och unclaiming kommer vara gratis.");
            useEconomy = false;
        }

        // Something prevented the economy from being enabled
        this.getLogger().info("Economy blev inte startad.");
    }

    private void setupEvents() {
        // Register all the event handlers
        this.getServer().getPluginManager().registerEvents(new WorldProfileEventHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerConnectionHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerMovementHandler(this), this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Supplier<Boolean> handle = () -> commandManager.handle(sender, args[0], Arrays.stream(args).skip(1).toArray(String[]::new));

        if (args.length == 0) {
            return commandManager.handle(sender, null, new String[]{});
        } else {
            return handle.get();
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command bukkitCommand, @NotNull String alias, @NotNull String[] args) {
        if (!isEnabled()) return Collections.emptyList();

        String command = args[0];
        String[] commandArgs = Arrays.stream(args).skip(1).toArray(String[]::new);

        if (command.equals(""))
            return new ArrayList<String>() {{
                for (Map.Entry<String, Method> command : commandManager.getCommands().entrySet())
                    if (sender.hasPermission(command.getValue().getAnnotation(se.xfunserver.xplaychunks.command.Command.class).permission()))
                        add(command.getKey());
            }};
        if (commandArgs.length == 0)
            return new ArrayList<>() {{
                for (Map.Entry<String, Method> commandPair : commandManager.getCommands().entrySet())
                    if (commandPair.getKey().startsWith(command.toLowerCase()))
                        if (sender.hasPermission(commandPair.getValue().getAnnotation(se.xfunserver.xplaychunks.command.Command.class).permission()))
                            add(commandPair.getKey());
            }};
        return null;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public ChunkOutlineHandler getChunkOutlineHandler() {
        return chunkOutlineHandler;
    }

    public WorldGuardHandler getWorldGuardHandler() {
        return worldGuardHandler;
    }

    public boolean useEconomy() {
        return useEconomy;
    }

    public Econ getEconomy() {
        return economy;
    }

    private void disable() {
        this.getServer().getPluginManager().disablePlugin(this);
    }

    public ChunkHandler getChunkHandler() {
        return chunkHandler;
    }

    public IClaimChunkDataHandler getDataHandler() {
        return dataHandler;
    }

    public PlayerHandler getPlayerHandler() {
        return playerHandler;
    }

    public AdminOverride getAdminOverride() {
        return adminOverride;
    }

    public MainHandler getMainHandler() {
        return mainHandler;
    }

    public static xPlayChunks getInstance() {
        return instance;
    }
}
