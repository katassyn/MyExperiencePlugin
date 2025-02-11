package com.maks.myexperienceplugin;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
                    "xp DOUBLE)";

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
                String sql = "REPLACE INTO players (uuid, name, level, xp) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, player.getUniqueId().toString());
                    stmt.setString(2, player.getName());
                    stmt.setInt(3, level);
                    stmt.setDouble(4, xp);
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save player data: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void loadPlayerData(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = getConnection()) {
                String sql = "SELECT level, xp FROM players WHERE uuid = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, player.getUniqueId().toString());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            int level = rs.getInt("level");
                            double xp = rs.getDouble("xp");

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
}