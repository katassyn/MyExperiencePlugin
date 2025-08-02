package com.maks.myexperienceplugin.Class.skills;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

/**
 * Periodically reminds players if they have unspent skill points.
 */
public class PeriodicSkillPointReminder extends BukkitRunnable {

    private final MyExperiencePlugin plugin;

    public PeriodicSkillPointReminder(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        SkillTreeManager manager = plugin.getSkillTreeManager();
        if (manager == null) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();

            int unusedBasic = manager.getUnusedBasicSkillPoints(uuid);
            if (unusedBasic > 0) {
                player.sendMessage("\u00a7eYou have \u00a7c" + unusedBasic +
                        " \u00a7eskill point" + (unusedBasic > 1 ? "s" : "") +
                        " to spend! Use the \u00a7aSkill Tree \u00a7emenu to allocate them.");
            }

            int unusedAsc = manager.getUnusedAscendancySkillPoints(uuid);
            if (unusedAsc > 0) {
                player.sendMessage("\u00a7eYou have \u00a7c" + unusedAsc +
                        " \u00a7eascendancy skill point" + (unusedAsc > 1 ? "s" : "") +
                        " to spend! Use the \u00a7aAscendancy Skill Tree \u00a7emenu to allocate them.");
            }
        }
    }
}
