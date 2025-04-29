package com.maks.myexperienceplugin.alchemy;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.utils.ActionBarUtils;

import java.util.UUID;

public class PhysisPercentageHealthEffect extends AlchemyEffect {
    private final double healthPercentage;
    private AttributeModifier modifier;

    public PhysisPercentageHealthEffect(Player player, double healthPercentage, long durationMillis, long cooldownMillis, String effectName) {
        super(player, durationMillis, cooldownMillis, effectName);
        this.healthPercentage = healthPercentage;
    }

    @Override
    public void apply() {
        ActionBarUtils.sendActionBar(player, "§a[" + effectName + "] Effect started: Health increased by " + (healthPercentage * 100) + "%");
        double baseHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        double bonus = baseHealth * healthPercentage;

        modifier = new AttributeModifier(UUID.randomUUID(), effectName, bonus, AttributeModifier.Operation.ADD_NUMBER);
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).addModifier(modifier);

        Bukkit.getScheduler().runTaskLater(MyExperiencePlugin.getInstance(), this::remove, durationMillis / 50);
    }

    @Override
    public void remove() {
        if (modifier != null) {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).removeModifier(modifier);
            // Upewniamy się, że HP gracza nie przekracza nowego maksimum
            double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            if (player.getHealth() > maxHealth) {
                player.setHealth(maxHealth);
            }
        }
        ActionBarUtils.sendActionBar(player, "§c[" + effectName + "] Effect ended: Percentage health bonus expired.");
    }
}
