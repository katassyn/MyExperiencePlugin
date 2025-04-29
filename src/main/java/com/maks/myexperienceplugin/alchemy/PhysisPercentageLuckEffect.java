package com.maks.myexperienceplugin.alchemy;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.utils.ActionBarUtils;

import java.util.UUID;

public class PhysisPercentageLuckEffect extends AlchemyEffect {
    private final double luckPercentage;
    private AttributeModifier modifier;

    public PhysisPercentageLuckEffect(Player player, double luckPercentage, long durationMillis, long cooldownMillis, String effectName) {
        super(player, durationMillis, cooldownMillis, effectName);
        this.luckPercentage = luckPercentage;
    }

    @Override
    public void apply() {
        ActionBarUtils.sendActionBar(player, "§a[" + effectName + "] Effect started: Luck increased by " + (luckPercentage * 100) + "%");
        double baseLuck = player.getAttribute(Attribute.GENERIC_LUCK).getBaseValue();
        double bonus = baseLuck * luckPercentage;

        modifier = new AttributeModifier(UUID.randomUUID(), effectName, bonus, AttributeModifier.Operation.ADD_NUMBER);
        player.getAttribute(Attribute.GENERIC_LUCK).addModifier(modifier);

        Bukkit.getScheduler().runTaskLater(MyExperiencePlugin.getInstance(), this::remove, durationMillis / 50);
    }

    @Override
    public void remove() {
        if (modifier != null) {
            player.getAttribute(Attribute.GENERIC_LUCK).removeModifier(modifier);
        }
        ActionBarUtils.sendActionBar(player, "§c[" + effectName + "] Effect ended: Luck bonus expired.");
    }
}
