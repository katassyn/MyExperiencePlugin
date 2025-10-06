package com.maks.myexperienceplugin.alchemy;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import com.maks.myexperienceplugin.utils.ActionBarUtils;
import org.bukkit.Bukkit;

public class InstantHealingEffect extends AlchemyEffect {
    private final double healPercentage; // For percentage healing
    private final double healAmount;     // For flat amount healing
    private final boolean isPercentage;  // To track which mode to use
    private static final int debuggingFlag = 0;

    // Constructor for flat healing
    public InstantHealingEffect(Player player, double healAmount, long cooldownMillis, String effectName) {
        super(player, 0, cooldownMillis, effectName);
        this.healAmount = healAmount;
        this.healPercentage = 0.0;
        this.isPercentage = false;
    }

    // Check if this is a basic healing potion (5, 10, or 15 HP with 10s cooldown)
    public boolean isBasicHealingPotion() {
        return !isPercentage && 
               (healAmount == 5.0 || healAmount == 10.0 || healAmount == 15.0) && 
               getCooldownMillis() == 10 * 1000L;
    }

    // Constructor for percentage healing
    public InstantHealingEffect(Player player, double healPercentage, long cooldownMillis, String effectName, boolean isPercentage) {
        super(player, 0, cooldownMillis, effectName);
        this.healPercentage = healPercentage;
        this.healAmount = 0.0;
        this.isPercentage = true;
    }

    @Override
    public void apply() {
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double healingAmount;

        if (isPercentage) {
            // Percentage-based healing
            healingAmount = maxHealth * (healPercentage / 100.0); // Convert from percentage (e.g., 50%) to fraction (0.5)
            ActionBarUtils.sendActionBar(player, "§a[" + effectName + "] Healed " + healPercentage + "% of max health.");

            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[DEBUG] Applied percentage healing to " + player.getName() +
                        ": " + healPercentage + "% of " + maxHealth + " = " + healingAmount);
            }
        } else {
            // Flat amount healing
            healingAmount = healAmount;

            // Only send action bar message if it's not a basic healing potion
            if (!isBasicHealingPotion()) {
                ActionBarUtils.sendActionBar(player, "§a[" + effectName + "] Healed " + healAmount/2 + " hearts.");
            } else {
                if (debuggingFlag == 1) {
                    Bukkit.getLogger().info("[DEBUG] Applied basic healing potion to " + player.getName());
                }
            }

            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[DEBUG] Applied flat healing to " + player.getName() +
                        ": " + healAmount + " health (" + healAmount/2 + " hearts)");
            }
        }

        double newHealth = Math.min(maxHealth, player.getHealth() + healingAmount);
        player.setHealth(newHealth);

        // For instant healing potions, we don't call remove() here
        // The AlchemyManager will handle the removal immediately after applying
        // This prevents the issue where the effect is removed before it's fully registered
    }

    @Override
    public void remove() {
        // For instant healing potions, this method is typically not called
        // since the effect is removed immediately after applying in AlchemyManager
        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[DEBUG] Remove called for instant healing effect: " + effectName +
                    " for player " + player.getName());
        }
    }
}
