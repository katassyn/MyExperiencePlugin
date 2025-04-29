package com.maks.myexperienceplugin.alchemy;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.utils.ActionBarUtils;

public class TonicExpEffect extends AlchemyEffect {
    private final double bonusExpPercentage; // e.g., 0.003 = 0.3% of required XP
    private static final int debuggingFlag = 1;

    public TonicExpEffect(Player player, double bonusExpPercentage, long durationMillis, long cooldownMillis, String effectName) {
        super(player, durationMillis, cooldownMillis, effectName);
        this.bonusExpPercentage = bonusExpPercentage;
    }

    @Override
    public void apply() {
        // Convert to percentage for display (e.g., 0.003 -> 0.3%)
        double displayPercentage = bonusExpPercentage * 100;

        ActionBarUtils.sendActionBar(player, "§a[" + effectName + "] Bonus XP +" + String.format("%.1f", displayPercentage) + "% of required XP per mob kill.");
        TonicExpManager.getInstance().setBonus(player, bonusExpPercentage);

        if (debuggingFlag == 1) {
            int playerLevel = MyExperiencePlugin.getInstance().getPlayerLevel(player);
            double requiredXP = MyExperiencePlugin.getInstance().getXpPerLevel().getOrDefault(playerLevel, 100.0);
            double bonusAmount = requiredXP * bonusExpPercentage;

            Bukkit.getLogger().info("[DEBUG] Applied Tonic Exp bonus for player " + player.getName() +
                    ": " + String.format("%.1f", displayPercentage) + "% of required XP (" + requiredXP +
                    ") = " + bonusAmount + " per mob kill");
        }

        // Removed self-scheduling to avoid duplicate removal
        // AlchemyManager now handles scheduling the removal
    }

    @Override
    public void remove() {
        TonicExpManager.getInstance().removeBonus(player);
        ActionBarUtils.sendActionBar(player, "§c[" + effectName + "] Bonus XP expired.");

        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[DEBUG] Removed Tonic Exp bonus from player " + player.getName());
        }
    }
}
