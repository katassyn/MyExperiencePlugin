package com.maks.myexperienceplugin.alchemy;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.utils.ActionBarUtils;

import java.util.UUID;

public class TonicMovementSpeedEffect extends AlchemyEffect {
    private final double bonusSpeed;
    private AttributeModifier modifier;

    public TonicMovementSpeedEffect(Player player, double bonusSpeed, long durationMillis, long cooldownMillis, String effectName) {
        super(player, durationMillis, cooldownMillis, effectName);
        this.bonusSpeed = bonusSpeed;
    }

    @Override
    public void apply() {
        ActionBarUtils.sendActionBar(player, "§a[" + effectName + "] Effect started: Movement speed increased by " + bonusSpeed + ".");
        modifier = new AttributeModifier(UUID.randomUUID(), effectName, bonusSpeed, AttributeModifier.Operation.ADD_NUMBER);
        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(modifier);
        Bukkit.getScheduler().runTaskLater(MyExperiencePlugin.getInstance(), this::remove, durationMillis / 50);
    }

    @Override
    public void remove() {
        if (modifier != null) {
            player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).removeModifier(modifier);
        }
        ActionBarUtils.sendActionBar(player, "§c[" + effectName + "] Effect ended: Movement speed bonus expired.");
    }
}
