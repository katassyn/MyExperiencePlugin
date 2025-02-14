package com.maks.myexperienceplugin.exp;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TopCommand implements CommandExecutor {
    private final MyExperiencePlugin plugin;

    public TopCommand(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    // Klasa pomocnicza do przechowywania danych gracza
    private static class PlayerData {
        private final String name;
        private final int level;

        public PlayerData(String name, int level) {
            this.name = name;
            this.level = level;
        }

        public String getName() {
            return name;
        }

        public int getLevel() {
            return level;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Upewnij się, że polecenie wykonuje gracz
        if (!(sender instanceof Player)) {
            sender.sendMessage("To polecenie jest dostępne tylko dla graczy.");
            return true;
        }

        Player player = (Player) sender;

        // Wykonaj operacje na bazie danych asynchronicznie
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<PlayerData> playerDataList = new ArrayList<>();

            try (Connection connection = plugin.getDatabaseManager().getConnection();
                 PreparedStatement stmt = connection.prepareStatement(
                         "SELECT name, level FROM players ORDER BY level DESC, xp DESC LIMIT 30")) {

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String name = rs.getString("name");
                    int level = rs.getInt("level");
                    playerDataList.add(new PlayerData(name, level));
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Błąd przy pobieraniu danych top graczy: " + e.getMessage());
                e.printStackTrace();
                Bukkit.getScheduler().runTask(plugin, () ->
                        player.sendMessage("§cError"));
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                List<String> topPlayers = new ArrayList<>();

                for (PlayerData data : playerDataList) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(data.getName());
                    if (!offlinePlayer.isOp()) {
                        topPlayers.add(String.format("§6#%d: §a%s §7- Level §b%d",
                                topPlayers.size() + 1, data.getName(), data.getLevel()));
                        if (topPlayers.size() >= 10) {
                            break;
                        }
                    }
                }
                while (topPlayers.size() < 10) {
                    topPlayers.add(String.format("§6#%d: §7- Empty -", topPlayers.size() + 1));
                }

                StringBuilder message = new StringBuilder();
                message.append("§6===== §aTop 10 Players§6 =====\n");
                for (String line : topPlayers) {
                    message.append(line).append("\n");
                }
                message.append("§6=============================");

                player.sendMessage(message.toString());
            });
        });

        return true;
    }
}