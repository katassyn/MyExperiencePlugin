package com.maks.myexperienceplugin.Class;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClassManager {
    private final MyExperiencePlugin plugin;
    private final ConcurrentHashMap<UUID, String> playerClassCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, String> playerAscendancyCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Integer> playerSkillPointsCache = new ConcurrentHashMap<>();

    public ClassManager(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    public void loadPlayerClassData(Player player) {
        UUID uuid = player.getUniqueId();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT class, ascendancy, skill_points FROM player_classes WHERE uuid = ?")) {

                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String clazz = rs.getString("class");
                        String ascend = rs.getString("ascendancy");
                        int points = rs.getInt("skill_points");

                        // Convert null to "NoClass" or "" so we never store null in a CHM
                        if (clazz == null) clazz = "NoClass";
                        if (ascend == null) ascend = "";

                        playerClassCache.put(uuid, clazz);
                        playerAscendancyCache.put(uuid, ascend);
                        playerSkillPointsCache.put(uuid, points);
                    } else {
                        // Insert default
                        insertDefaultClassRow(uuid);
                        playerClassCache.put(uuid, "NoClass");
                        playerAscendancyCache.put(uuid, "");
                        playerSkillPointsCache.put(uuid, 0);
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to load class data for player " + player.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void insertDefaultClassRow(UUID uuid) {
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO player_classes (uuid) VALUES (?)")) {

            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to insert default class row for UUID " + uuid + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void savePlayerClassData(UUID uuid, String clazz, String ascendancy, int skillPoints) {
        // Convert any null to valid non-null
        if (clazz == null) clazz = "NoClass";
        if (ascendancy == null) ascendancy = "";

        final String finalClass = clazz;
        final String finalAscendancy = ascendancy;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "REPLACE INTO player_classes (uuid, class, ascendancy, skill_points) VALUES (?, ?, ?, ?)")) {

                stmt.setString(1, uuid.toString());
                stmt.setString(2, finalClass);
                stmt.setString(3, finalAscendancy);
                stmt.setInt(4, skillPoints);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save class data for UUID " + uuid + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public String getPlayerClass(UUID uuid) {
        return playerClassCache.getOrDefault(uuid, "NoClass");
    }

    public String getPlayerAscendancy(UUID uuid) {
        return playerAscendancyCache.getOrDefault(uuid, "");
    }

    public int getPlayerSkillPoints(UUID uuid) {
        return playerSkillPointsCache.getOrDefault(uuid, 0);
    }

    public void setPlayerClass(Player player, String clazz) {
        UUID uuid = player.getUniqueId();
        if (clazz == null) {
            clazz = "NoClass";
        }
        playerClassCache.put(uuid, clazz);
        // Reset ascendancy when class changes
        playerAscendancyCache.put(uuid, "");
        savePlayerClassData(uuid, clazz, "", getPlayerSkillPoints(uuid));
    }

    public void setPlayerAscendancy(Player player, String ascendancy) {
        UUID uuid = player.getUniqueId();
        if (ascendancy == null) {
            ascendancy = "";
        }
        playerAscendancyCache.put(uuid, ascendancy);
        savePlayerClassData(uuid, getPlayerClass(uuid), ascendancy, getPlayerSkillPoints(uuid));
    }

    public void addSkillPoints(Player player, int amount) {
        UUID uuid = player.getUniqueId();
        int newTotal = getPlayerSkillPoints(uuid) + amount;
        playerSkillPointsCache.put(uuid, newTotal);
        savePlayerClassData(uuid, getPlayerClass(uuid), getPlayerAscendancy(uuid), newTotal);
    }

    public void resetClassData(Player player) {
        UUID uuid = player.getUniqueId();

        // For debugging
        int debuggingFlag = 1;
        if (debuggingFlag == 1) {
            plugin.getLogger().info("Resetting class data for player: " + player.getName());
            plugin.getLogger().info("Current level: " + plugin.getPlayerLevel(player));
        }

        // Reset class and ascendancy in cache
        playerClassCache.put(uuid, "NoClass");
        playerAscendancyCache.put(uuid, "");
        playerSkillPointsCache.put(uuid, 0);

        // Reset purchased skills in database
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                // Delete purchased skills
                try (PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM player_purchased_skills WHERE uuid = ?")) {
                    stmt.setString(1, uuid.toString());
                    stmt.executeUpdate();

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Deleted purchased skills for " + player.getName());
                    }
                }

                // Reset skill points table but DON'T set to 0
                // Instead of setting points to 0, we'll recalculate them later
                try (PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM player_skill_points WHERE uuid = ?")) {
                    stmt.setString(1, uuid.toString());
                    stmt.executeUpdate();

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Deleted skill points entry for " + player.getName());
                    }
                }

                // Save reset class
                try (PreparedStatement stmt = conn.prepareStatement(
                        "REPLACE INTO player_classes (uuid, class, ascendancy, skill_points) VALUES (?, ?, ?, ?)")) {
                    stmt.setString(1, uuid.toString());
                    stmt.setString(2, "NoClass");
                    stmt.setString(3, "");
                    stmt.setInt(4, 0);
                    stmt.executeUpdate();

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Saved NoClass status for " + player.getName());
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to reset player data for " + player.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Clear caches in SkillTreeManager
        if (plugin.getSkillTreeManager() != null) {
            plugin.getSkillTreeManager().clearPlayerSkillData(uuid);

            // THIS IS THE KEY FIX - recalculate skill points based on player level
            plugin.getSkillTreeManager().updateSkillPoints(player);

            if (debuggingFlag == 1) {
                int basicPoints = plugin.getSkillTreeManager().getBasicSkillPoints(uuid);
                int ascendancyPoints = plugin.getSkillTreeManager().getAscendancySkillPoints(uuid);
                plugin.getLogger().info("After reset - recalculated skill points for " + player.getName() +
                        ": basic=" + basicPoints + ", ascendancy=" + ascendancyPoints);
            }
        }

        // Refresh player stats
        if (plugin.getSkillEffectsHandler() != null) {
            plugin.getSkillEffectsHandler().refreshPlayerStats(player);
        }

        // Inform player
        player.sendMessage(ChatColor.RED + "Your class has been reset!");
        player.sendMessage(ChatColor.GREEN + "Your skill points have been recalculated based on your level.");
    }}