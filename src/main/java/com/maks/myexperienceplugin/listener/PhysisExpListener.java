package com.maks.myexperienceplugin.listener;

import com.maks.myexperienceplugin.alchemy.PhysisExpManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import com.maks.myexperienceplugin.MyExperiencePlugin;

public class PhysisExpListener implements Listener {
    private final MyExperiencePlugin plugin;

    public PhysisExpListener(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMythicMobDeath(MythicMobDeathEvent event) {
        if (event.getKiller() instanceof Player) {
            Player killer = (Player) event.getKiller();
            double bonus = PhysisExpManager.getInstance().getExpBonus(killer);

            if (bonus > 0) {
                String mobName = event.getMobType().getInternalName();
                double baseXp = plugin.getXpForMob(mobName);
                double bonusXp = baseXp * bonus;
                plugin.addXP(killer, bonusXp);
            }
        }
    }
}