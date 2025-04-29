package com.maks.myexperienceplugin.alchemy;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.utils.ActionBarUtils;

import java.util.UUID;

public class PhysisPercentageDamageEffect extends AlchemyEffect {
    private final double damagePercentage; // np. 0.10 dla 10%
    private AttributeModifier modifier;

    public PhysisPercentageDamageEffect(Player player, double damagePercentage, long durationMillis, long cooldownMillis, String effectName) {
        super(player, durationMillis, cooldownMillis, effectName);
        this.damagePercentage = damagePercentage;
    }

    @Override
    public void apply() {
        ActionBarUtils.sendActionBar(player, "§a[" + effectName + "] Effect started: Damage increased by " + (damagePercentage * 100) + "%");

        // Apply damage boost as a percentage multiplier
        modifier = new AttributeModifier(UUID.randomUUID(), effectName, damagePercentage, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
        player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).addModifier(modifier);

        Bukkit.getScheduler().runTaskLater(MyExperiencePlugin.getInstance(), this::remove, durationMillis / 50);
    }

    @Override
    public void remove() {
        if (modifier != null) {
            player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).removeModifier(modifier);
        }
        ActionBarUtils.sendActionBar(player, "§c[" + effectName + "] Effect ended: Percentage damage bonus expired.");
    }
}
