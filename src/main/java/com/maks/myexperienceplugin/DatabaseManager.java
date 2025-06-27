package com.maks.myexperienceplugin;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {
    private final HikariDataSource dataSource;
    private final MyExperiencePlugin plugin;

    public DatabaseManager(MyExperiencePlugin plugin, String host, String port, String database, String username, String password) {
        this.plugin = plugin;

        // Configure HikariCP
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        config.setUsername(username);
        config.setPassword(password);

        // Essential settings
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setIdleTimeout(300000); // 5 minutes
        config.setMaxLifetime(600000); // 10 minutes
        config.setConnectionTimeout(10000); // 10 seconds
        config.setPoolName("MyExperiencePlugin-Pool");

        // MySQL specific settings
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        // Create the datasource
        dataSource = new HikariDataSource(config);

        // Initialize database tables
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection connection = getConnection()) {
            // Players table
            String createPlayersTable = "CREATE TABLE IF NOT EXISTS players (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "name VARCHAR(16)," +
                    "level INT," +
                    "xp DOUBLE," +
                    "rank_position INT DEFAULT 0," +
                    "is_admin BOOLEAN DEFAULT FALSE)";

            // Player classes table
            String createClassesTable = "CREATE TABLE IF NOT EXISTS player_classes (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "class VARCHAR(32)," +
                    "ascendancy VARCHAR(32)," +
                    "skill_points INT)";

            try (PreparedStatement stmt = connection.prepareStatement(createPlayersTable)) {
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = connection.prepareStatement(createClassesTable)) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize database tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public void savePlayerData(Player player, int level, double xp) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = getConnection()) {
                String sql = "REPLACE INTO players (uuid, name, level, xp, is_admin) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, player.getUniqueId().toString());
                    stmt.setString(2, player.getName());
                    stmt.setInt(3, level);
                    stmt.setDouble(4, xp);
                    stmt.setBoolean(5, player.isOp());
                    stmt.executeUpdate();
                }

                // Update player rankings after saving data
                updatePlayerRankings();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save player data: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Updates the rank_position for all players based on their level.
     * Admin players are placed at the end of the ranking.
     */
    public void updatePlayerRankings() {
        try (Connection connection = getConnection()) {
            // First, get all players sorted by level (non-admins first, then admins)
            String selectSql = "SELECT uuid FROM players ORDER BY is_admin ASC, level DESC, xp DESC";
            List<String> playerUuids = new ArrayList<>();

            try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        playerUuids.add(rs.getString("uuid"));
                    }
                }
            }

            // Then update each player's rank_position
            String updateSql = "UPDATE players SET rank_position = ? WHERE uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
                for (int i = 0; i < playerUuids.size(); i++) {
                    stmt.setInt(1, i + 1); // Rank positions start at 1
                    stmt.setString(2, playerUuids.get(i));
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to update player rankings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadPlayerData(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = getConnection()) {
                String sql = "SELECT level, xp, rank_position, is_admin FROM players WHERE uuid = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, player.getUniqueId().toString());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            int level = rs.getInt("level");
                            double xp = rs.getDouble("xp");
                            int rankPosition = rs.getInt("rank_position");
                            boolean isAdmin = rs.getBoolean("is_admin");

                            // Update is_admin if it doesn't match current op status
                            if (isAdmin != player.isOp()) {
                                updatePlayerAdminStatus(player);
                            }

                            Bukkit.getScheduler().runTask(plugin, () -> {
                                plugin.playerLevels.put(player.getUniqueId(), level);
                                plugin.playerCurrentXP.put(player.getUniqueId(), xp);
                                plugin.updatePlayerXPBar(player);
                            });
                        } else {
                            // Player doesn't exist, set defaults
                            savePlayerData(player, 1, 0.0);
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                plugin.playerLevels.put(player.getUniqueId(), 1);
                                plugin.playerCurrentXP.put(player.getUniqueId(), 0.0);
                                plugin.updatePlayerXPBar(player);
                            });
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to load player data: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Updates the admin status of a player in the database.
     * This is called when a player's op status changes.
     */
    public void updatePlayerAdminStatus(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = getConnection()) {
                String sql = "UPDATE players SET is_admin = ? WHERE uuid = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setBoolean(1, player.isOp());
                    stmt.setString(2, player.getUniqueId().toString());
                    stmt.executeUpdate();
                }

                // Update rankings after changing admin status
                updatePlayerRankings();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to update player admin status: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
