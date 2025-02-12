package com.maks.myexperienceplugin.alchemy;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import com.maks.myexperienceplugin.MyExperiencePlugin;

import java.util.UUID;

public class TonicDamageEffect extends AlchemyEffect {
    private final double bonusDamage;
    private AttributeModifier modifier;

    public TonicDamageEffect(Player player, double bonusDamage, long durationMillis, long cooldownMillis, String effectName) {
        super(player, durationMillis, cooldownMillis, effectName);
        this.bonusDamage = bonusDamage;
    }

    @Override
    public void apply() {
        player.sendMessage("§a[" + effectName + "] Effect started: Damage increased by " + bonusDamage + ".");
        modifier = new AttributeModifier(UUID.randomUUID(), effectName, bonusDamage, AttributeModifier.Operation.ADD_NUMBER);
        player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).addModifier(modifier);

        // Po czasie trwania efektu usuwamy modyfikator
        Bukkit.getScheduler().runTaskLater(MyExperiencePlugin.getInstance(), this::remove, durationMillis / 50);
    }

    @Override
    public void remove() {
        if (modifier != null) {
            player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).removeModifier(modifier);
        }
        player.sendMessage("§c[" + effectName + "] Effect ended: Damage bonus expired.");
    }
}
