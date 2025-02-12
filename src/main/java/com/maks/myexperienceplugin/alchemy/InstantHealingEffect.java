package com.maks.myexperienceplugin.alchemy;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public class InstantHealingEffect extends AlchemyEffect {
    private final double healPercentage; // np. 0.5 = 50% maksymalnego HP

    public InstantHealingEffect(Player player, double healPercentage, long cooldownMillis, String effectName) {
        super(player, 0, cooldownMillis, effectName);
        this.healPercentage = healPercentage;
    }

    @Override
    public void apply() {
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double healAmount = maxHealth * healPercentage;
        double newHealth = Math.min(maxHealth, player.getHealth() + healAmount);
        player.setHealth(newHealth);
        player.sendMessage("§a[" + effectName + "] Effect started: Instant healing applied.");
        // Efekt jest natychmiastowy – od razu kończymy.
        remove();
    }

    @Override
    public void remove() {
        player.sendMessage("§c[" + effectName + "] Effect ended.");
    }
}
