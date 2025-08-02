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

        // Use nickname alone in the tab list; prefix comes from scoreboard team
        player.setPlayerListName(display);

        // Set the player's display name first so plugins like Essentials update correctly
        player.setDisplayName(rankPrefix + display);

        // Reapply the custom name on the next tick to ensure it isn't overwritten
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.setCustomName(String.format("§b[ %d ] §r%s", level, display));
            player.setCustomNameVisible(true);
        });
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
