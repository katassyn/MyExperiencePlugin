package com.maks.myexperienceplugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DatabaseManager {
    private String host;
    private String port;
    private String database;
    private String username;
    private String password;

    public DatabaseManager(String host, String port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection connection = getConnection()) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS players (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "name VARCHAR(16)," +
                    "level INT," +
                    "xp DOUBLE)";
            try (PreparedStatement stmt = connection.prepareStatement(createTableSQL)) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://" + host + ":" + port + "/" + database, username, password
        );
    }

    public void savePlayerData(Player player, int level, double xp) {
        Bukkit.getScheduler().runTaskAsynchronously(MyExperiencePlugin.getInstance(), () -> {
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
                e.printStackTrace();
            }
        });
    }

    public void loadPlayerData(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(MyExperiencePlugin.getInstance(), () -> {
            try (Connection connection = getConnection()) {
                String sql = "SELECT level, xp FROM players WHERE uuid = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, player.getUniqueId().toString());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            int level = rs.getInt("level");
                            double xp = rs.getDouble("xp");

                            Bukkit.getScheduler().runTask(MyExperiencePlugin.getInstance(), () -> {
                                // Załaduj dane gracza do pamięci
                                MyExperiencePlugin.getInstance().playerLevels.put(player.getUniqueId(), level);
                                MyExperiencePlugin.getInstance().playerCurrentXP.put(player.getUniqueId(), xp);
                                MyExperiencePlugin.getInstance().updatePlayerXPBar(player); // Aktualizuj pasek XP
                            });
                        } else {
                            // Gracz nie istnieje w bazie, ustaw dane domyślne
                            savePlayerData(player, 1, 0.0);
                            Bukkit.getScheduler().runTask(MyExperiencePlugin.getInstance(), () -> {
                                MyExperiencePlugin.getInstance().playerLevels.put(player.getUniqueId(), 1);
                                MyExperiencePlugin.getInstance().playerCurrentXP.put(player.getUniqueId(), 0.0);
                                MyExperiencePlugin.getInstance().updatePlayerXPBar(player); // Aktualizuj pasek XP
                            });
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

}

