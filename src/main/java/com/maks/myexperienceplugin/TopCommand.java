package com.maks.myexperienceplugin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TopCommand implements CommandExecutor {
    private final MyExperiencePlugin plugin;

    public TopCommand(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try (Connection connection = plugin.getDatabaseManager().getConnection()) {
                    String query = "SELECT name, level FROM players ORDER BY level DESC LIMIT 10";
                    try (PreparedStatement stmt = connection.prepareStatement(query);
                         ResultSet rs = stmt.executeQuery()) {

                        StringBuilder message = new StringBuilder();
                        message.append("§6===== §aTop 10 Players by Level§6 =====\n");

                        int rank = 1;
                        while (rs.next()) {
                            String name = rs.getString("name");
                            int level = rs.getInt("level");

                            message.append(String.format("§6#%d: §a%s §7- Level §b%d\n", rank, name, level));
                            rank++;
                        }

                        message.append("§6=============================");

                        Bukkit.getScheduler().runTask(plugin, () -> {
                            player.sendMessage(message.toString());
                        });

                    } catch (SQLException e) {
                        plugin.getLogger().severe("Error fetching top players: " + e.getMessage());
                        e.printStackTrace();
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            player.sendMessage("§cAn error occurred while fetching the top players.");
                        });
                    }
                } catch (SQLException e) {
                    plugin.getLogger().severe("Could not connect to the database: " + e.getMessage());
                    e.printStackTrace();
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.sendMessage("§cCould not connect to the database.");
                    });
                }
            });

            return true;
        } else {
            sender.sendMessage("This command is only available for players.");
            return false;
        }
    }
}

