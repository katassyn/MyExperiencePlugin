package com.maks.myexperienceplugin.listener;

import com.maks.myexperienceplugin.alchemy.PhysisExpManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.Bukkit;

public class PhysisExpListener implements Listener {
    private final MyExperiencePlugin plugin;
    private static final int debuggingFlag = 1;

    public PhysisExpListener(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMythicMobDeath(MythicMobDeathEvent event) {
        if (event.getKiller() instanceof Player) {
            Player killer = (Player) event.getKiller();
            double bonus = PhysisExpManager.getInstance().getExpBonus(killer);

            // This listener should now be redundant since MythicMobXPHandler handles the bonus
            // Instead, just log debug information if the flag is enabled
            if (debuggingFlag == 1 && bonus > 0) {
                String mobName = event.getMobType().getInternalName();
                double baseXp = plugin.getXpForMob(mobName);
                double bonusXp = baseXp * bonus;

                Bukkit.getLogger().info("[DEBUG] PhysisExpListener detected bonus for " + killer.getName() +
                        ": " + (bonus * 100) + "% of " + baseXp + " = " + bonusXp + " additional XP");
                Bukkit.getLogger().info("[DEBUG] This is now handled by MythicMobXPHandler");
            }
        }
    }
}