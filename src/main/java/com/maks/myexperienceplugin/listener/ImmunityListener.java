package com.maks.myexperienceplugin.listener;

import com.maks.myexperienceplugin.alchemy.ImmunityManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class ImmunityListener implements Listener {
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (ImmunityManager.getInstance().isImmune(player)) {
            // Możesz dodatkowo sprawdzić przyczynę obrażeń – np. tylko dla mobów
            if (event.getCause() == DamageCause.ENTITY_ATTACK || event.getCause() == DamageCause.PROJECTILE) {
                event.setCancelled(true);
            }
        }
    }
}
