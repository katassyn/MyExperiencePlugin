package com.maks.myexperienceplugin.alchemy;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.utils.ActionBarUtils;

import java.util.UUID;

public class TonicHealthEffect extends AlchemyEffect {
    private final double healthBonus;
    private AttributeModifier modifier;

    public TonicHealthEffect(Player player, double healthBonus, long durationMillis, long cooldownMillis, String effectName) {
        super(player, durationMillis, cooldownMillis, effectName);
        this.healthBonus = healthBonus;
    }

    @Override
    public void apply() {
        ActionBarUtils.sendActionBar(player, "§a[" + effectName + "] Effect started: Max health increased by " + healthBonus);
        modifier = new AttributeModifier(UUID.randomUUID(), effectName, healthBonus, AttributeModifier.Operation.ADD_NUMBER);
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).addModifier(modifier);

        // Po czasie trwania usuń efekt
        Bukkit.getScheduler().runTaskLater(MyExperiencePlugin.getInstance(), this::remove, durationMillis / 50);
    }

    @Override
    public void remove() {
        if (modifier != null) {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).removeModifier(modifier);
        }
        ActionBarUtils.sendActionBar(player, "§c[" + effectName + "] Effect ended: Health bonus expired.");
    }
}
