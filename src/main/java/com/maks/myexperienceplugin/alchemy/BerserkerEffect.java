package com.maks.myexperienceplugin.alchemy;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import com.maks.myexperienceplugin.MyExperiencePlugin;

import java.util.UUID;

public class BerserkerEffect extends AlchemyEffect {
    private final double bonusDamage;
    private final double healthPenaltyPercentage; // np. 0.60 = 60%
    private AttributeModifier damageModifier;
    private AttributeModifier healthModifier;

    public BerserkerEffect(Player player, double bonusDamage, double healthPenaltyPercentage, long durationMillis, long cooldownMillis, String effectName) {
        super(player, durationMillis, cooldownMillis, effectName);
        this.bonusDamage = bonusDamage;
        this.healthPenaltyPercentage = healthPenaltyPercentage;
    }

    @Override
    public void apply() {
        player.sendMessage("§a[" + effectName + "] Effect started: Berserker mode activated. +" + bonusDamage + " damage, -" + (healthPenaltyPercentage * 100) + "% max health.");
        damageModifier = new AttributeModifier(UUID.randomUUID(), effectName + "_damage", bonusDamage, AttributeModifier.Operation.ADD_NUMBER);
        player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).addModifier(damageModifier);

        double baseMaxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        double penalty = baseMaxHealth * healthPenaltyPercentage;
        healthModifier = new AttributeModifier(UUID.randomUUID(), effectName + "_health", -penalty, AttributeModifier.Operation.ADD_NUMBER);
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).addModifier(healthModifier);

        // Jeśli obecne zdrowie przekracza nowy max, ustaw na nowy max.
        double newMax = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        if (player.getHealth() > newMax) {
            player.setHealth(newMax);
        }

        Bukkit.getScheduler().runTaskLater(MyExperiencePlugin.getInstance(), this::remove, durationMillis / 50);
    }

    @Override
    public void remove() {
        if (damageModifier != null) {
            player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).removeModifier(damageModifier);
        }
        if (healthModifier != null) {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).removeModifier(healthModifier);
        }
        player.sendMessage("§c[" + effectName + "] Effect ended: Berserker mode expired.");
    }
}
