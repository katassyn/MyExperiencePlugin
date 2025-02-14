package com.maks.myexperienceplugin.exp;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class PlayerLevelDisplayHandler implements Listener {

    private final MyExperiencePlugin plugin;

    public PlayerLevelDisplayHandler(MyExperiencePlugin plugin) {
        this.plugin = plugin;
        setupScoreboardTeam();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        updatePlayerTab(player);

        // Aktualizacja wszystkich graczy w tabie po dołączeniu nowego gracza
        Bukkit.getScheduler().runTaskLater(plugin, this::updateAllPlayerTabs, 20L);
    }

    public void updatePlayerTab(Player player) {
        int level = plugin.getPlayerLevel(player);

        // Pobierz główny scoreboard serwera
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        if (scoreboard == null) {
            plugin.getLogger().warning("Scoreboard not available. Skipping tab update for " + player.getName());
            return;
        }

        // Pobierz team "level_display"
        Team team = scoreboard.getTeam("level_display");
        if (team == null) {
            plugin.getLogger().warning("Team 'level_display' not found. Creating a new one.");
            team = scoreboard.registerNewTeam("level_display");
            team.setAllowFriendlyFire(true);
            team.setCanSeeFriendlyInvisibles(false);
        }

        // Dodaj gracza do teamu
        team.addEntry(player.getName());

        // Zaktualizuj Tab
        player.setPlayerListName(String.format("§b[ %d ] §r%s", level, player.getName()));
    }

    public void updateAllPlayerTabs() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerTab(player);
        }
    }

    private void setupScoreboardTeam() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        if (scoreboard == null) {
            plugin.getLogger().warning("Scoreboard not available. Team setup skipped.");
            return;
        }

        // Utwórz team "level_display", jeśli nie istnieje
        if (scoreboard.getTeam("level_display") == null) {
            Team team = scoreboard.registerNewTeam("level_display");
            team.setAllowFriendlyFire(true);
            team.setCanSeeFriendlyInvisibles(false);
        }
    }
}
