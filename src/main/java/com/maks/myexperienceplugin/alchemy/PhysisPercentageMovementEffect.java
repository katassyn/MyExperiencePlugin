package com.maks.myexperienceplugin.alchemy;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import com.maks.myexperienceplugin.MyExperiencePlugin;

import java.util.UUID;

public class PhysisPercentageMovementEffect extends AlchemyEffect {
    private final double speedPercentage;
    private AttributeModifier modifier;

    public PhysisPercentageMovementEffect(Player player, double speedPercentage, long durationMillis, long cooldownMillis, String effectName) {
        super(player, durationMillis, cooldownMillis, effectName);
        this.speedPercentage = speedPercentage;
    }

    @Override
    public void apply() {
        player.sendMessage("§a[" + effectName + "] Effect started: Movement speed increased by " + (speedPercentage * 100) + "%");
        double baseSpeed = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue();
        double bonus = baseSpeed * speedPercentage;

        modifier = new AttributeModifier(UUID.randomUUID(), effectName, bonus, AttributeModifier.Operation.ADD_NUMBER);
        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(modifier);

        Bukkit.getScheduler().runTaskLater(MyExperiencePlugin.getInstance(), this::remove, durationMillis / 50);
    }

    @Override
    public void remove() {
        if (modifier != null) {
            player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).removeModifier(modifier);
        }
        player.sendMessage("§c[" + effectName + "] Effect ended: Movement speed bonus expired.");
    }
}