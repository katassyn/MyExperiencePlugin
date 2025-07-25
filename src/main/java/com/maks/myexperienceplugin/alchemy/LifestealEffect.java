package com.maks.myexperienceplugin.alchemy;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.utils.ActionBarUtils;

public class LifestealEffect extends AlchemyEffect {
    private final double lifestealPercentage; // np. 0.01 = 1% z zadanych obrażeń

    public LifestealEffect(Player player, double lifestealPercentage, long durationMillis, long cooldownMillis, String effectName) {
        super(player, durationMillis, cooldownMillis, effectName);
        this.lifestealPercentage = lifestealPercentage;
    }

    @Override
    public void apply() {
        ActionBarUtils.sendActionBar(player, "§a[" + effectName + "] Effect started: Lifesteal activated (" + (lifestealPercentage * 100) + "% of damage dealt).");
        LifestealManager.getInstance().setLifesteal(player, lifestealPercentage);
        Bukkit.getScheduler().runTaskLater(MyExperiencePlugin.getInstance(), this::remove, durationMillis / 50);
    }

    @Override
    public void remove() {
        LifestealManager.getInstance().removeLifesteal(player);
        ActionBarUtils.sendActionBar(player, "§c[" + effectName + "] Effect ended: Lifesteal expired.");
    }
}
