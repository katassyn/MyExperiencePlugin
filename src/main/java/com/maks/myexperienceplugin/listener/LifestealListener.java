package com.maks.myexperienceplugin.listener;

import com.maks.myexperienceplugin.alchemy.LifestealManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.attribute.Attribute;

public class LifestealListener implements Listener {
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        double lifesteal = LifestealManager.getInstance().getLifesteal(player);
        if (lifesteal > 0) {
            double damage = event.getFinalDamage();
            double healAmount = damage * lifesteal;
            double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            player.setHealth(Math.min(maxHealth, player.getHealth() + healAmount));
        }
    }
}
