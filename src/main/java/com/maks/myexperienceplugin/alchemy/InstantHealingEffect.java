package com.maks.myexperienceplugin.alchemy;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public class InstantHealingEffect extends AlchemyEffect {
    private final double healPercentage; // For percentage healing
    private final double healAmount;     // For flat amount healing
    private final boolean isPercentage;  // To track which mode to use

    // Constructor for flat healing
    public InstantHealingEffect(Player player, double healAmount, long cooldownMillis, String effectName) {
        super(player, 0, cooldownMillis, effectName);
        this.healAmount = healAmount;
        this.healPercentage = 0.0;
        this.isPercentage = false;
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
            player.sendMessage("§a[" + effectName + "] Effect started: Healed " + healPercentage + "% of max health.");
        } else {
            // Flat amount healing
            healingAmount = healAmount;
            player.sendMessage("§a[" + effectName + "] Effect started: Healed " + healAmount/2 + " hearts.");
        }

        double newHealth = Math.min(maxHealth, player.getHealth() + healingAmount);
        player.setHealth(newHealth);

        remove();
    }

    @Override
    public void remove() {
        player.sendMessage("§c[" + effectName + "] Effect ended.");
        AlchemyManager.getInstance().clearEffect(player, AlchemyManager.AlchemyCategory.ELIXIR);
    }
}