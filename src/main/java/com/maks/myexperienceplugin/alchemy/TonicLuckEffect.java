package com.maks.myexperienceplugin.alchemy;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import com.maks.myexperienceplugin.MyExperiencePlugin;

import java.util.UUID;

public class TonicLuckEffect extends AlchemyEffect {
    private final double bonusLuck;
    private AttributeModifier modifier;

    public TonicLuckEffect(Player player, double bonusLuck, long durationMillis, long cooldownMillis, String effectName) {
        super(player, durationMillis, cooldownMillis, effectName);
        this.bonusLuck = bonusLuck;
    }

    @Override
    public void apply() {
        player.sendMessage("§a[" + effectName + "] Effect started: Luck increased by " + bonusLuck + ".");
        modifier = new AttributeModifier(UUID.randomUUID(), effectName, bonusLuck, AttributeModifier.Operation.ADD_NUMBER);
        player.getAttribute(Attribute.GENERIC_LUCK).addModifier(modifier);
        Bukkit.getScheduler().runTaskLater(MyExperiencePlugin.getInstance(), this::remove, durationMillis / 50);
    }

    @Override
    public void remove() {
        if (modifier != null) {
            player.getAttribute(Attribute.GENERIC_LUCK).removeModifier(modifier);
        }
        player.sendMessage("§c[" + effectName + "] Effect ended: Luck bonus expired.");
    }
}
