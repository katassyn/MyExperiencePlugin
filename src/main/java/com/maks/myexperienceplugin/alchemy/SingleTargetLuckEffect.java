package com.maks.myexperienceplugin.alchemy;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.utils.ActionBarUtils;

import java.util.UUID;

public class SingleTargetLuckEffect extends AlchemyEffect implements Listener {
    private final double luckMultiplier;
    private AttributeModifier modifier;
    private boolean used = false;

    public SingleTargetLuckEffect(Player player, double luckMultiplier, long durationMillis, long cooldownMillis, String effectName) {
        super(player, durationMillis, cooldownMillis, effectName);
        this.luckMultiplier = luckMultiplier;
    }

    @Override
    public void apply() {
        ActionBarUtils.sendActionBar(player, "§a[" + effectName + "] Effect started: Next mob kill will have " + luckMultiplier + "x luck!");
        modifier = new AttributeModifier(UUID.randomUUID(), effectName, luckMultiplier, AttributeModifier.Operation.ADD_NUMBER);
        player.getAttribute(Attribute.GENERIC_LUCK).addModifier(modifier);

        // Rejestrujemy listener do śledzenia pierwszego zabicia moba
        Bukkit.getPluginManager().registerEvents(this, MyExperiencePlugin.getInstance());

        // Usuń efekt po czasie, jeśli nie został wykorzystany
        Bukkit.getScheduler().runTaskLater(MyExperiencePlugin.getInstance(), this::remove, durationMillis / 50);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!used && event.getEntity().getKiller() != null && event.getEntity().getKiller().equals(player)) {
            used = true;
            remove();
        }
    }

    @Override
    public void remove() {
        if (modifier != null) {
            player.getAttribute(Attribute.GENERIC_LUCK).removeModifier(modifier);
        }
        ActionBarUtils.sendActionBar(player, "§c[" + effectName + "] Effect ended: One-time luck bonus expired.");
    }
}
