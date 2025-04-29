package com.maks.myexperienceplugin.alchemy;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.utils.ActionBarUtils;

public class ImmunityEffect extends AlchemyEffect {
    public ImmunityEffect(Player player, long durationMillis, long cooldownMillis, String effectName) {
        super(player, durationMillis, cooldownMillis, effectName);
    }

    @Override
    public void apply() {
        ActionBarUtils.sendActionBar(player, "§a[" + effectName + "] Effect started: You are immune to mob damage for " + (durationMillis / 1000) + " seconds.");
        ImmunityManager.getInstance().setImmune(player, true);
        Bukkit.getScheduler().runTaskLater(MyExperiencePlugin.getInstance(), this::remove, durationMillis / 50);
    }

    @Override
    public void remove() {
        ImmunityManager.getInstance().setImmune(player, false);
        ActionBarUtils.sendActionBar(player, "§c[" + effectName + "] Effect ended: Immunity expired.");
    }
}
