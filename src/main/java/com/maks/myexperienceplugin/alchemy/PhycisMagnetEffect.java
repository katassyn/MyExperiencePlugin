package com.maks.myexperienceplugin.alchemy;

import org.bukkit.entity.Player;
import org.bukkit.entity.Item;
import org.bukkit.Bukkit;
import com.maks.myexperienceplugin.MyExperiencePlugin;

public class PhycisMagnetEffect extends AlchemyEffect {

    public PhycisMagnetEffect(Player player, long cooldownMillis, String effectName) {
        // Efekt natychmiastowy – brak czasu trwania
        super(player, 0, cooldownMillis, effectName);
    }

    @Override
    public void apply() {
        player.sendMessage("§a[" + effectName + "] Effect started: Magnet effect activated.");
        // Znajdź przedmioty w promieniu 10 bloków i teleportuj je do gracza
        player.getWorld().getNearbyEntities(player.getLocation(), 10, 10, 10, entity -> entity instanceof Item)
                .forEach(entity -> entity.teleport(player));
        player.sendMessage("§a[" + effectName + "] Items have been attracted.");
        // Efekt jest jednorazowy – od razu kończymy
        remove();
    }

    @Override
    public void remove() {
        player.sendMessage("§c[" + effectName + "] Effect ended: Magnet effect finished.");
    }
}
