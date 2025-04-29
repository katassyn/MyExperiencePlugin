package com.maks.myexperienceplugin.alchemy;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Bukkit;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.utils.ActionBarUtils;

public class OverTimeHealingEffect extends AlchemyEffect {
    private final double healPercentagePerSecond; // np. 0.10 = 10% maks. HP na sekundę
    private BukkitTask task;

    public OverTimeHealingEffect(Player player, double healPercentagePerSecond, long durationMillis, long cooldownMillis, String effectName) {
        super(player, durationMillis, cooldownMillis, effectName);
        this.healPercentagePerSecond = healPercentagePerSecond;
    }

    @Override
    public void apply() {
        ActionBarUtils.sendActionBar(player, "§a[" + effectName + "] Effect started: Healing over time.");
        // Leczenie co 20 ticków (1 sekunda)
        task = Bukkit.getScheduler().runTaskTimer(MyExperiencePlugin.getInstance(), () -> {
            double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            double healAmount = maxHealth * healPercentagePerSecond;
            double newHealth = Math.min(maxHealth, player.getHealth() + healAmount);
            player.setHealth(newHealth);
        }, 0L, 20L);

        // Po upływie czasu trwania efektu usuwamy efekt
        Bukkit.getScheduler().runTaskLater(MyExperiencePlugin.getInstance(), this::remove, durationMillis / 50);
    }

    @Override
    public void remove() {
        if (task != null) {
            task.cancel();
        }
        ActionBarUtils.sendActionBar(player, "§c[" + effectName + "] Effect ended: Healing over time finished.");
    }
}
