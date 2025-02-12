package com.maks.myexperienceplugin.alchemy;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import com.maks.myexperienceplugin.MyExperiencePlugin;

public class PhysisPercentageExpEffect extends AlchemyEffect {
    private final double expPercentage;

    public PhysisPercentageExpEffect(Player player, double expPercentage, long durationMillis, long cooldownMillis, String effectName) {
        super(player, durationMillis, cooldownMillis, effectName);
        this.expPercentage = expPercentage;
    }

    @Override
    public void apply() {
        player.sendMessage("§a[" + effectName + "] Effect started: Experience gain increased by " + (expPercentage * 100) + "%");
        PhysisExpManager.getInstance().setExpBonus(player, expPercentage);

        Bukkit.getScheduler().runTaskLater(MyExperiencePlugin.getInstance(), this::remove, durationMillis / 50);
    }

    @Override
    public void remove() {
        PhysisExpManager.getInstance().removeExpBonus(player);
        player.sendMessage("§c[" + effectName + "] Effect ended: Experience bonus expired.");
    }
}