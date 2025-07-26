package com.maks.myexperienceplugin.alchemy;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.utils.ActionBarUtils;

import java.util.UUID;

public class BerserkerEffect extends AlchemyEffect {
    private final double bonusDamage;
    private final double healthPenaltyPercentage; // np. 0.60 = 60%
    private AttributeModifier damageModifier;
    private AttributeModifier healthModifier;
    private static final int debuggingFlag = 0;

    public BerserkerEffect(Player player, double bonusDamage, double healthPenaltyPercentage, long durationMillis, long cooldownMillis, String effectName) {
        super(player, durationMillis, cooldownMillis, effectName);
        this.bonusDamage = bonusDamage;
        this.healthPenaltyPercentage = healthPenaltyPercentage;
    }

    @Override
    public void apply() {
        // Use action bar instead of chat message
        ActionBarUtils.sendActionBar(player, "§a[" + effectName + "] Berserker mode activated. +" + (bonusDamage * 100) + "% damage, -" + (healthPenaltyPercentage * 100) + "% max health.");

        // Apply damage boost as a percentage multiplier
        // For example, if bonusDamage is 0.9, it will increase damage by 90%

        // Create and apply damage modifier
        damageModifier = new AttributeModifier(
                UUID.randomUUID(),
                effectName + "_damage",
                bonusDamage,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1
        );
        player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).addModifier(damageModifier);

        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[DEBUG] Applied Berserker damage modifier: +" + (bonusDamage * 100) + "% damage" +
                    " to player " + player.getName());
        }

        // Apply health penalty
        double baseMaxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        double penalty = baseMaxHealth * healthPenaltyPercentage;
        healthModifier = new AttributeModifier(
                UUID.randomUUID(),
                effectName + "_health",
                -penalty,
                AttributeModifier.Operation.ADD_NUMBER
        );
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).addModifier(healthModifier);

        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[DEBUG] Applied Berserker health penalty: " + penalty +
                    " to player " + player.getName());
        }

        // If health exceeds new max, set to new max
        double newMax = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        if (player.getHealth() > newMax) {
            player.setHealth(newMax);
        }

        // Removed self-scheduling to avoid duplicate removal
        // AlchemyManager now handles scheduling the removal
    }

    @Override
    public void remove() {
        if (damageModifier != null) {
            try {
                player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).removeModifier(damageModifier);
                if (debuggingFlag == 1) {
                    Bukkit.getLogger().info("[DEBUG] Removed Berserker damage modifier from player " + player.getName());
                }
            } catch (Exception e) {
                if (debuggingFlag == 1) {
                    Bukkit.getLogger().warning("[DEBUG] Failed to remove Berserker damage modifier: " + e.getMessage());
                }
            }
        }

        if (healthModifier != null) {
            try {
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).removeModifier(healthModifier);
                if (debuggingFlag == 1) {
                    Bukkit.getLogger().info("[DEBUG] Removed Berserker health modifier from player " + player.getName());
                }
            } catch (Exception e) {
                if (debuggingFlag == 1) {
                    Bukkit.getLogger().warning("[DEBUG] Failed to remove Berserker health modifier: " + e.getMessage());
                }
            }
        }

        ActionBarUtils.sendActionBar(player, "§c[" + effectName + "] Berserker mode expired.");
    }
}
