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

        // Use the main scoreboard so all players share the same teams
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        player.setScoreboard(scoreboard);

        String teamName = "level_" + player.getName();
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
            team.setAllowFriendlyFire(true);
            team.setCanSeeFriendlyInvisibles(false);
        }
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
                    rankPrefix = ChatColor.translateAlternateColorCodes('&', lpPrefix);
                }
            }
        }

        // Build team prefix combining level and rank prefix
        String teamPrefix = String.format("§b[ %d ] §r", level);
        if (!rankPrefix.isEmpty()) {
            teamPrefix += rankPrefix + " ";
        }
        team.setPrefix(teamPrefix);
        team.setSuffix("");

        team.addEntry(player.getName());

        // Ensure the nickname does not already contain the rank prefix
        if (!rankPrefix.isEmpty() && display.startsWith(rankPrefix)) {
            display = display.substring(rankPrefix.length()).trim();
        }

        // Set the player's display name for chat to show level + rank prefix + nickname
        player.setDisplayName(String.format("§b[ %d ] §r%s%s", level, rankPrefix, display));

        // Set the tab list name to show level + prefix + nickname
        // We need to include the level and rank prefix in the player list name
        player.setPlayerListName(String.format("§b[ %d ] §r%s%s", level, rankPrefix, display));

        // Create final copies of variables for use in lambda
        final int finalLevel = level;
        final String finalDisplay = display;
        final String finalRankPrefix = rankPrefix != null ? rankPrefix : "";
        
        // Set the player's display name again to ensure it's not overwritten by other plugins
        // Run with a small delay to ensure it's not immediately overwritten
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.setDisplayName(String.format("§b[ %d ] §r%s%s", finalLevel, finalRankPrefix, finalDisplay));
        }, 2L);
        
        // Reapply the custom name with a slightly longer delay to ensure it isn't overwritten
        // This ensures the name above the player's head shows ONLY level + nickname (no rank prefix)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.setCustomName(String.format("§b[ %d ] §r%s", finalLevel, finalDisplay));
            player.setCustomNameVisible(true);
            
            // Force EssentialsX to not override our custom name
            if (essentials != null) {
                User user = essentials.getUser(player);
                if (user != null) {
                    // This will prevent Essentials from changing the custom name
                    user.setDisplayNick();
                }
            }
        }, 5L);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        removePlayerTeam(player);
        Bukkit.getScheduler().runTaskLater(plugin, () -> updatePlayerTab(player), 1L);
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
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam("level_" + player.getName());
        if (team != null) {
            team.unregister();
        }
    }
}
