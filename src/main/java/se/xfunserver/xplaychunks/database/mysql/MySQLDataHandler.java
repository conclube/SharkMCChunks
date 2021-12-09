package se.xfunserver.xplaychunks.database.mysql;

import static se.xfunserver.xplaychunks.database.mysql.SQLBacking.*;

import org.jetbrains.annotations.Nullable;
import se.xfunserver.xplaychunks.chunks.ChunkPos;
import se.xfunserver.xplaychunks.chunks.DataChunk;
import se.xfunserver.xplaychunks.database.IClaimChunkDataHandler;
import se.xfunserver.xplaychunks.player.FullPlayerData;
import se.xfunserver.xplaychunks.player.SimplePlayerData;
import se.xfunserver.xplaychunks.utils.PluginSettings;
import se.xfunserver.xplaychunks.xPlayChunks;

import javax.xml.transform.Result;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MySQLDataHandler<T extends IClaimChunkDataHandler> implements IClaimChunkDataHandler {

    static final String CLAIMED_CHUNKS_TABLE_NAME = "claimed_chunks";
    static final String PLAYERS_TABLE_NAME = "joined_players";

    private static final String CLAIMED_CHUNKS_ID = "id";
    private static final String CLAIMED_CHUNKS_WORLD = "world_name";
    private static final String CLAIMED_CHUNKS_X = "chunk_x_pos";
    private static final String CLAIMED_CHUNKS_Z = "chunk_z_pos";
    private static final String CLAIMED_CHUNKS_TNT = "tnt_enabled";
    private static final String CLAIMED_CHUNKS_OWNER = "owner_uuid";
    private static final String PLAYERS_UUID = "uuid";
    private static final String PLAYERS_IGN = "last_in_game_name";
    private static final String PLAYERS_NAME = "chunk_name";
    private static final String PLAYERS_LAST_JOIN = "last_join_time_ms";

    private static final String ACCESS_TABLE_NAME = "access_granted";
    private static final String ACCESS_ACCESS_ID = "access_id";
    private static final String ACCESS_CHUNK_ID = "chunk_id";
    private static final String ACCESS_OWNER = "owner_uuid";
    private static final String ACCESS_OTHER = "other_uuid";

    private final xPlayChunks chunksCore;
    private Connection connectionSupplier;
    private String dbName;
    private boolean init;

    public MySQLDataHandler(xPlayChunks chunksCore) {
        this.chunksCore = chunksCore;
    }

    @Override
    public void init() throws Exception {
        init = true;

        dbName = PluginSettings.getDatabaseName();
        connectionSupplier = connect(
                PluginSettings.getDatabaseHostname(),
                PluginSettings.getDatabasePort(),
                dbName,
                PluginSettings.getDatabaseUsername(),
                PluginSettings.getDatabasePassword(),
                PluginSettings.getUseSsl(),
                PluginSettings.getAllowPublicKeyRetrieval());


        if (getTableDoesntExist(chunksCore, connectionSupplier, dbName, CLAIMED_CHUNKS_TABLE_NAME)) {
            this.createClaimedChunksTable();
        }

        if (getTableDoesntExist(chunksCore, connectionSupplier, dbName, PLAYERS_TABLE_NAME)) {
            this.createJoinedPlayersTable();
        }

        if (getTableDoesntExist(chunksCore, connectionSupplier, dbName, ACCESS_TABLE_NAME)) {
            this.createAccessTable();
        }
    }

    @Override
    public boolean getHasInit() {
        return init;
    }

    @Override
    public void exit() throws Exception {

    }

    @Override
    public void save() throws Exception {

    }

    @Override
    public void load() throws Exception {

    }

    @Override
    public void addClaimedChunk(ChunkPos pos, UUID player) {
        final String sql =
                String.format(
                        "INSERT INTO `%s` (`%s`, `%s`, `%s`, `%s`) VALUES (?, ?, ?, ?)",
                        CLAIMED_CHUNKS_TABLE_NAME,
                        CLAIMED_CHUNKS_WORLD,
                        CLAIMED_CHUNKS_X,
                        CLAIMED_CHUNKS_Z,
                        CLAIMED_CHUNKS_OWNER);
        try (PreparedStatement statement = prep(chunksCore, connectionSupplier, sql)) {
            statement.setString(1, pos.getWorld());
            statement.setInt(2, pos.getX());
            statement.setInt(3, pos.getZ());
            statement.setString(4, player.toString());
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addClaimedChunks(DataChunk[] chunks) {
        if (chunks.length == 0) return;

        StringBuilder sql =
                new StringBuilder(
                        String.format(
                                "INSERT INTO `%s` (`%s`, `%s`, `%s`, `%s`) VALUES",
                                CLAIMED_CHUNKS_TABLE_NAME,
                                CLAIMED_CHUNKS_WORLD,
                                CLAIMED_CHUNKS_X,
                                CLAIMED_CHUNKS_Z,
                                CLAIMED_CHUNKS_OWNER));
        for (int i = 0; i < chunks.length; i++) {
            sql.append(" (?, ?, ?, ?)");
            if (i != chunks.length - 1) sql.append(',');
        }

        try (PreparedStatement statement = prep(chunksCore, connectionSupplier, sql.toString())) {
            int i = 0;
            for (DataChunk chunk : chunks) {
                statement.setString( 4 * i + 1, chunk.chunk.getWorld());
                statement.setInt(4 * i + 2, chunk.chunk.getX());
                statement.setInt(4 * i + 3, chunk.chunk.getZ());
                statement.setString(4 * i + 4, chunk.player.toString());
                i++;
            }

            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeClaimedChunk(ChunkPos pos) {
        String sql =
                String.format(
                        "DELETE FROM `%s` WHERE `%s`=? AND `%s`=? AND `%s`=?",
                        CLAIMED_CHUNKS_TABLE_NAME,
                        CLAIMED_CHUNKS_WORLD,
                        CLAIMED_CHUNKS_X,
                        CLAIMED_CHUNKS_Z);
        try (PreparedStatement statement = prep(chunksCore, connectionSupplier, sql)) {
            statement.setString(1, pos.getWorld());
            statement.setInt(2, pos.getX());
            statement.setInt(3, pos.getZ());
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isChunkClaimed(ChunkPos pos) {
        String sql =
                String.format(
                        "SELECT count(*) FROM `%s` WHERE `%s`=? AND `%s`=? AND `%s`=?",
                        CLAIMED_CHUNKS_TABLE_NAME,
                        CLAIMED_CHUNKS_WORLD,
                        CLAIMED_CHUNKS_X,
                        CLAIMED_CHUNKS_Z);
        try (PreparedStatement statement = prep(chunksCore, connectionSupplier, sql)) {
            statement.setString(1, pos.getWorld());
            statement.setInt(2, pos.getX());
            statement.setInt(3, pos.getZ());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return result.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public @Nullable UUID getChunkOwner(ChunkPos pos) {
        String sql =
                String.format(
                        "SELECT `%s` FROM `%s` WHERE `%s`=? AND `%s`=? AND `%s`=? LIMIT 1",
                        CLAIMED_CHUNKS_OWNER,
                        CLAIMED_CHUNKS_TABLE_NAME,
                        CLAIMED_CHUNKS_WORLD,
                        CLAIMED_CHUNKS_X,
                        CLAIMED_CHUNKS_Z);
        try (PreparedStatement statement = prep(chunksCore, connectionSupplier, sql)) {
            statement.setString(1, pos.getWorld());
            statement.setInt(2, pos.getX());
            statement.setInt(3, pos.getZ());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return UUID.fromString(resultSet.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public DataChunk[] getClaimedChunks() {
        String sql =
                String.format(
                        "SELECT `%s`, `%s`, `%s`, `%s`, `%s` FROM `%s`",
                        CLAIMED_CHUNKS_WORLD,
                        CLAIMED_CHUNKS_X,
                        CLAIMED_CHUNKS_Z,
                        CLAIMED_CHUNKS_TNT,
                        CLAIMED_CHUNKS_OWNER,
                        CLAIMED_CHUNKS_TABLE_NAME);

        List<DataChunk> chunks = new ArrayList<>();
        try (PreparedStatement statement = prep(chunksCore, connectionSupplier, sql)) {
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                chunks.add(new DataChunk(
                        new ChunkPos(result.getString(1), result.getInt(2), result.getInt(3)),
                                UUID.fromString(result.getString(5)),
                                result.getBoolean(4)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return chunks.toArray(new DataChunk[0]);
    }

    @Override
    public boolean toggleTnt(ChunkPos pos) {
        boolean current = this.isTntEnabled(pos);
        String sql =
                String.format(
                        "UPDATE `%s` SET `%s`=? WHERE (`%s`=?) AND (`%s`=?) AND (`%s`=?)",
                        CLAIMED_CHUNKS_TABLE_NAME,
                        CLAIMED_CHUNKS_TNT,
                        CLAIMED_CHUNKS_WORLD,
                        CLAIMED_CHUNKS_X,
                        CLAIMED_CHUNKS_Z);
        try (PreparedStatement statement = prep(chunksCore, connectionSupplier, sql)) {
            statement.setBoolean(1, !current);
            statement.setString(3, pos.getWorld());
            statement.setInt(3, pos.getX());
            statement.setInt(4, pos.getZ());
            statement.execute();
            return !current;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return current;
    }

    @Override
    public boolean isTntEnabled(ChunkPos pos) {
        String sql =
                String.format(
                        "UPDATE `%s` SET `%s`=? WHERE (`%s`=?) AND (`%s`=?) AND (`%s`=?)",
                        CLAIMED_CHUNKS_TNT,
                        CLAIMED_CHUNKS_TABLE_NAME,
                        CLAIMED_CHUNKS_WORLD,
                        CLAIMED_CHUNKS_X,
                        CLAIMED_CHUNKS_Z);
        try (PreparedStatement statement = prep(chunksCore, connectionSupplier, sql)) {
            statement.setString(1, pos.getWorld());
            statement.setInt(2, pos.getX());
            statement.setInt(3, pos.getX());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return result.getBoolean(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void addPlayer(UUID player, String lastIgn, Set<UUID> permitted, @Nullable String chunkName, long lastOnlineTime) {
        String sql =
                String.format(
                        "INSERT INTO `%s` (`%s`, `%s`, `%s`, `%s`) VALUES (?, ?, ?, ?)",
                        PLAYERS_TABLE_NAME,
                        PLAYERS_UUID,
                        PLAYERS_IGN,
                        PLAYERS_NAME,
                        PLAYERS_LAST_JOIN);
        try (PreparedStatement statement = prep(chunksCore, connectionSupplier, sql)) {
            statement.setString(1, player.toString());
            statement.setString(2, lastIgn);
            statement.setString(3, chunkName);
            statement.setLong(4, lastOnlineTime);
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.givePlayersAccess(player, permitted.toArray(new UUID[0]));
    }

    @Override
    public void addPlayers(FullPlayerData[] players) {
        if (players.length == 0) return;

        StringBuilder sql = new StringBuilder(
                String.format(
                        "INSERT INTO `%s` (`%s`, `%s`, `%s`, `%s`) VALUES (?, ?, ?, ?)",
                        PLAYERS_TABLE_NAME,
                        PLAYERS_UUID,
                        PLAYERS_IGN,
                        PLAYERS_NAME,
                        PLAYERS_LAST_JOIN));

        for (int i = 0; i < players.length; i++) {
            this.givePlayersAccess(players[i].player, players[i].permitted.toArray(new UUID[0]));
            sql.append(" (?, ?, ?, ?)");
            if (i != players.length - 1) sql.append(',');
        }

        try (PreparedStatement statement = prep(chunksCore, connectionSupplier, sql.toString())) {
            int i = 0;
            for (FullPlayerData player : players) {
                statement.setString(5 * i + 1, player.player.toString());
                statement.setString(5 * i + 2, player.lastIgn);
                statement.setString(5 * i + 3, player.chunkName);
                statement.setLong(5 * i + 4, player.lastOnlineTime);
                i++;
            }
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public @Nullable String getPlayerUsername(UUID player) {
        String sql =
                String.format(
                        "SELECT `%s` FROM `%s` WHERE `%s`=?",
                        PLAYERS_IGN, PLAYERS_TABLE_NAME, PLAYERS_UUID);

        try (PreparedStatement statement = prep(chunksCore, connectionSupplier, sql)) {
            statement.setString(1, player.toString());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return result.getString(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public @Nullable UUID getPlayerUUID(String username) {
        String sql =
                String.format(
                        "SELECT `%s` FROM `%s` WHERE `%s`=?",
                        PLAYERS_UUID, PLAYERS_TABLE_NAME, PLAYERS_IGN);

        try (PreparedStatement statement = prep(chunksCore, connectionSupplier, sql)) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return UUID.fromString(result.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setPlayerLastOnline(UUID playerLastOnline, long time) {
        String sql =
                String.format(
                        "UPDATE `%s` SET `%s`=? WHERE `%s`=?",
                        PLAYERS_TABLE_NAME, PLAYERS_LAST_JOIN, PLAYERS_UUID);

        try (PreparedStatement statement = prep(chunksCore, connectionSupplier, sql)) {
            statement.setLong(1, time);
            statement.setString(2, playerLastOnline.toString());
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setPlayerChunkName(UUID player, @Nullable String name) {
        String sql =
                String.format(
                        "UPDATE `%s` SET `%s`=? WHERE `%s`=?",
                        PLAYERS_TABLE_NAME, PLAYERS_NAME, PLAYERS_UUID);
        try (PreparedStatement statement = prep(chunksCore, connectionSupplier, sql)) {
            statement.setString(1, name);
            statement.setString(2, player.toString());
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public @Nullable String getPlayerChunkName(UUID player) {
        String sql =
                String.format(
                        "SELECT `%s` FROM `%s` WHERE `%s`=?",
                        PLAYERS_NAME, PLAYERS_TABLE_NAME, PLAYERS_UUID);
        try (PreparedStatement statement = prep(chunksCore, connectionSupplier, sql)) {
            statement.setString(1, player.toString());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return result.getString(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean hasPlayer(UUID player) {
        String sql =
                String.format(
                        "SELECT count(*) FROM `%s` WHERE `%s`=?", PLAYERS_TABLE_NAME, PLAYERS_UUID);

        try (PreparedStatement statement = prep(chunksCore, connectionSupplier, sql)) {
            statement.setString(1, player.toString());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return result.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Collection<SimplePlayerData> getPlayers() {
        String sql =
                String.format(
                        "SELECT `%s`, `%s, `%s`, FROM `%s` LIMIT 1",
                        PLAYERS_UUID, PLAYERS_IGN, PLAYERS_LAST_JOIN, PLAYERS_TABLE_NAME);

        ArrayList<SimplePlayerData> players = new ArrayList<>();
        try (PreparedStatement statement = prep(chunksCore, connectionSupplier, sql)) {
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                players.add(
                        new SimplePlayerData(
                                UUID.fromString(result.getString(1)),
                                result.getString(2),
                                result.getLong(3)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return players;
    }

    @Override
    public FullPlayerData[] getFullPlayerData() {
        String sql =
                String.format(
                        "SELECT `%s`, `%s`, `%s`, `%s` FROM `%s` LIMIT 1",
                        PLAYERS_UUID, PLAYERS_IGN, PLAYERS_NAME, PLAYERS_LAST_JOIN, PLAYERS_TABLE_NAME);

        ArrayList<FullPlayerData> players = new ArrayList<>();
        try (PreparedStatement statement = prep(chunksCore, connectionSupplier, sql)) {
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                UUID uuid = UUID.fromString(result.getString(1));
                players.add(
                        new FullPlayerData(
                                uuid,
                                result.getString(2),
                                new HashSet<>(Arrays.asList(this.getPlayersWithAccess(uuid))),
                                result.getString(3),
                                result.getLong(4)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return players.toArray(new FullPlayerData[0]);
    }

    @Override
    public void setPlayerAccess(UUID owner, UUID accessor, boolean access) {
        if (access == this.playerHasAccess(owner, accessor)) return;

        if (access) {
            String sql =
                    String.format(
                            "INSERT INTO `%s` (`%s`, `%s`) VALUES (?, ?)",
                            ACCESS_TABLE_NAME, ACCESS_OWNER, ACCESS_OTHER);

            try (PreparedStatement statement = prep(chunksCore, connectionSupplier, sql)) {
                statement.setString(1, owner.toString());
                statement.setString(2, accessor.toString());
                statement.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            String sql =
                    String.format(
                            "DELETE FROM `%s` WHERE `%s`=? AND `%s`=?",
                            ACCESS_TABLE_NAME, ACCESS_OWNER, ACCESS_OTHER);

            try (PreparedStatement statement = prep(chunksCore, connectionSupplier, sql)) {
                statement.setString(1, owner.toString());
                statement.setString(2, accessor.toString());
                statement.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void givePlayersAccess(UUID owner, UUID[] accesssors) {
        if (accesssors.length == 0) return;

        // Determine which of the provided accessors actually need to be GIVEN access.
        HashSet<UUID> withAccess = new HashSet<>(Arrays.asList(this.getPlayersWithAccess(owner)));
        HashSet<UUID> needAccess = new HashSet<>();

        for (UUID accessor : accesssors) {
            if (!withAccess.contains(accessor)) needAccess.add(accessor);
        }

        StringBuilder sql = new StringBuilder(
                String.format(
                        "INSERT INTO `%s` (`%s`, `%s`) VALUES",
                        ACCESS_TABLE_NAME, ACCESS_OWNER, ACCESS_OTHER));

        for (int i = 0; i < needAccess.size(); i++) {
            sql.append(" (?, ?)");
            if (i != needAccess.size() - 1) sql.append(',');
        }

        try (PreparedStatement statement = prep(chunksCore, connectionSupplier, sql.toString())) {
            int i = 0;
            for (UUID accessor : needAccess) {
                statement.setString(2 * i + 1, owner.toString());
                statement.setString(2 * i + 2, accessor.toString());
                i++;
            }
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void takePlayersAccess(UUID owner, UUID[] accessors) {
        if (accessors.length == 0) return;


        StringBuilder sql = new StringBuilder(
                String.format(
                        "DELETE FROM `%s` WHERE (`%s`, `%s`) in (",
                        ACCESS_TABLE_NAME, ACCESS_OWNER, ACCESS_OTHER));

        for (int i = 0; i < accessors.length; i++) {
            sql.append(" (?, ?)");
            if (i != accessors.length - 1) sql.append(',');
        }

        try (PreparedStatement statement = prep(chunksCore, connectionSupplier, sql.toString())) {
            int i = 0;
            for (UUID accessor : accessors) {
                statement.setString(2 * i + 1, owner.toString());
                statement.setString(2 * i + 2, accessor.toString());
                i++;
            }
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public UUID[] getPlayersWithAccess(UUID owner) {
        String sql =
                String.format(
                        "SELECT `%s` FROM `%s` WHERE `%s`=?",
                        ACCESS_OTHER, ACCESS_TABLE_NAME, ACCESS_OWNER);

        List<UUID> accessors = new ArrayList<>();
        try (PreparedStatement statement = prep(chunksCore, connectionSupplier, sql)) {
            statement.setString(1, owner.toString());
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    accessors.add(UUID.fromString(result.getString(1)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return accessors.toArray(new UUID[0]);
    }

    @Override
    public boolean playerHasAccess(UUID owner, UUID accessor) {
        String sql =
                String.format(
                        "SELECT count(*) FROM `%s` WHERE `%s`=? AND `%s`=?",
                        ACCESS_TABLE_NAME, ACCESS_OWNER, ACCESS_OTHER);

        try (PreparedStatement statement = prep(chunksCore, connectionSupplier, sql)) {
            statement.setString(1, owner.toString());
            statement.setString(2, accessor.toString());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return result.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private void createClaimedChunksTable() throws Exception {
        String sql =
                String.format(
                        "CREATE TABLE `%s` ("
                                + "`%s` INT NOT NULL AUTO_INCREMENT," // ID (for per-chunk access)
                                + "`%s` VARCHAR(64) NOT NULL," // World
                                + "`%s` INT NOT NULL," // X
                                + "`%s` INT NOT NULL," // Z
                                + "`%s` BOOL NOT NULL DEFAULT 0," // TNT
                                + "`%s` VARCHAR(36) NOT NULL," // Owner (UUIDs are always 36 chars)
                                + "PRIMARY KEY (`%2$s`)"
                                + ") ENGINE = InnoDB",
                        CLAIMED_CHUNKS_TABLE_NAME,
                        CLAIMED_CHUNKS_ID,
                        CLAIMED_CHUNKS_WORLD,
                        CLAIMED_CHUNKS_X,
                        CLAIMED_CHUNKS_Z,
                        CLAIMED_CHUNKS_TNT,
                        CLAIMED_CHUNKS_OWNER);
        try (PreparedStatement statement = prep(chunksCore, connectionSupplier, sql)) {
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void createJoinedPlayersTable() throws Exception {
        String sql =
                String.format(
                        "CREATE TABLE `%s` ("
                                + "`%s` VARCHAR(36) NOT NULL," // UUID
                                + "`%s` VARCHAR(64) NOT NULL," // In-game name
                                + "`%s` VARCHAR(64) NULL DEFAULT NULL," // Chunk display name
                                + "`%s` BIGINT NOT NULL," // Last join time in ms
                                + "PRIMARY KEY (`%2$s`)"
                                + ") ENGINE = InnoDB",
                        PLAYERS_TABLE_NAME,
                        PLAYERS_UUID,
                        PLAYERS_IGN,
                        PLAYERS_NAME,
                        PLAYERS_LAST_JOIN);
        try (PreparedStatement statement = prep(chunksCore, connectionSupplier, sql)) {
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void createAccessTable() throws Exception {
        String sql =
                String.format(
                        "CREATE TABLE `%s` ("
                                + "`%s` INT NOT NULL AUTO_INCREMENT," // Access ID (for primary key)
                                + "`%s` INT NULL DEFAULT NULL," // Chunk ID (for per-chunk access)
                                + "`%s` VARCHAR(36) NOT NULL," // Granter
                                + "`%s` VARCHAR(36) NOT NULL," // Granted
                                + "PRIMARY KEY (`%2$s`)"
                                + ") ENGINE = InnoDB",
                        ACCESS_TABLE_NAME,
                        ACCESS_ACCESS_ID,
                        ACCESS_CHUNK_ID,
                        ACCESS_OWNER,
                        ACCESS_OTHER);
        try (PreparedStatement statement = prep(chunksCore, connectionSupplier, sql)) {
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
