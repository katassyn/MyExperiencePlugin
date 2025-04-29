package com.maks.myexperienceplugin.alchemy;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.utils.ActionBarUtils;

public class PhysisPercentageExpEffect extends AlchemyEffect {
    private final double expPercentage;
    private static final int debuggingFlag = 1;

    public PhysisPercentageExpEffect(Player player, double expPercentage, long durationMillis, long cooldownMillis, String effectName) {
        super(player, durationMillis, cooldownMillis, effectName);
        this.expPercentage = expPercentage;
    }

    @Override
    public void apply() {
        ActionBarUtils.sendActionBar(player, "§a[" + effectName + "] Experience gain increased by " + (expPercentage * 100) + "%");
        PhysisExpManager.getInstance().setExpBonus(player, expPercentage);

        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[DEBUG] Applied Physis Exp bonus of " + (expPercentage * 100) +
                    "% to player " + player.getName());
        }

        // Removed self-scheduling to avoid duplicate removal
        // AlchemyManager now handles scheduling the removal
    }

    @Override
    public void remove() {
        PhysisExpManager.getInstance().removeExpBonus(player);
        ActionBarUtils.sendActionBar(player, "§c[" + effectName + "] Experience bonus expired.");

        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[DEBUG] Removed Physis Exp bonus from player " + player.getName());
        }
    }
}
