package com.maks.myexperienceplugin;

import com.maks.myexperienceplugin.alchemy.TotemManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.entity.Player;
import org.bukkit.attribute.Attribute;

public class TotemEffectListener implements Listener {
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        // Jeśli obrażenia spowodowałyby śmierć gracza
        if (player.getHealth() - event.getFinalDamage() <= 0) {
            boolean activated = TotemManager.getInstance().tryActivateTotem(player);
            if (activated) {
                event.setCancelled(true);
                // Przywracamy graczowi pełne zdrowie
                double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                player.setHealth(maxHealth);
                player.sendMessage("§aTotem effect activated: You were saved from death!");
            }
        }
    }
}
