package com.maks.myexperienceplugin.exp;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class PlayerLevelDisplayHandler implements Listener {

    private final MyExperiencePlugin plugin;
    private final Essentials essentials;
    private final LuckPerms luckPerms;

    public PlayerLevelDisplayHandler(MyExperiencePlugin plugin) {
        this.plugin = plugin;
        this.essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        this.luckPerms = plugin.getLuckPerms();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        updatePlayerTab(player);

        // Aktualizacja wszystkich graczy w tabie po dołączeniu nowego gracza
        Bukkit.getScheduler().runTaskLater(plugin, this::updateAllPlayerTabs, 20L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removePlayerTeam(event.getPlayer());
    }

    public void updatePlayerTab(Player player) {
        int level = plugin.getPlayerLevel(player);

        // Użyj scoreboardu przypisanego do gracza (LuckPerms może używać własnego)
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard == null) {
            plugin.getLogger().warning("Scoreboard not available. Skipping tab update for " + player.getName());
            return;
        }

        String teamName = "level_" + player.getName();
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
            team.setAllowFriendlyFire(true);
            team.setCanSeeFriendlyInvisibles(false);
        }
        team.setPrefix(String.format("§b[ %d ] §r", level));
        team.setSuffix("");
        team.addEntry(player.getName());

        String display = player.getName();
        if (essentials != null) {
            User user = essentials.getUser(player);
            if (user != null) {
                String nick = user.getNick();
                if (nick != null && !nick.isEmpty()) {
                    display = ChatColor.translateAlternateColorCodes('&', nick);
                }
            }
        }

        String rankPrefix = "";
        if (luckPerms != null) {
            net.luckperms.api.model.user.User lpUser = luckPerms.getPlayerAdapter(Player.class).getUser(player);
            if (lpUser != null) {
                String lpPrefix = lpUser.getCachedData().getMetaData().getPrefix();
                if (lpPrefix != null) {
                    rankPrefix = ChatColor.translateAlternateColorCodes('&', lpPrefix) + " ";
                }
            }
        }

        // Ensure the nickname does not already contain the rank prefix
        if (!rankPrefix.isEmpty() && display.startsWith(rankPrefix)) {
            display = display.substring(rankPrefix.length()).trim();
        }

        String tabName = rankPrefix + display;
        player.setPlayerListName(tabName);

        // Show level and nick above the player's head without any rank prefix
        player.setCustomName(String.format("§b[ %d ] §r%s", level, display));
        player.setCustomNameVisible(true);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> updatePlayerTab(event.getPlayer()), 1L);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Bukkit.getScheduler().runTaskLater(plugin, () -> updatePlayerTab(player), 1L);
    }

    public void updateAllPlayerTabs() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerTab(player);
        }
    }

    private void removePlayerTeam(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard == null) {
            return;
        }
        Team team = scoreboard.getTeam("level_" + player.getName());
        if (team != null) {
            team.unregister();
        }
    }
}
