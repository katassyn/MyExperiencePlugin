package com.maks.myexperienceplugin.alchemy;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import com.maks.myexperienceplugin.MyExperiencePlugin;

public class TonicExpEffect extends AlchemyEffect {
    private final double bonusExpPercentage; // np. 0.001 = 0.1%

    public TonicExpEffect(Player player, double bonusExpPercentage, long durationMillis, long cooldownMillis, String effectName) {
        super(player, durationMillis, cooldownMillis, effectName);
        this.bonusExpPercentage = bonusExpPercentage;
    }

    @Override
    public void apply() {
        player.sendMessage("§a[" + effectName + "] Effect started: Bonus XP +" + (bonusExpPercentage * 100) + "% per mob kill.");
        TonicExpManager.getInstance().setBonus(player, bonusExpPercentage);
        Bukkit.getScheduler().runTaskLater(MyExperiencePlugin.getInstance(), this::remove, durationMillis / 50);
    }

    @Override
    public void remove() {
        TonicExpManager.getInstance().removeBonus(player);
        player.sendMessage("§c[" + effectName + "] Effect ended: Bonus XP expired.");
    }
}
