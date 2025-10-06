package com.maks.myexperienceplugin.listener;

import com.maks.myexperienceplugin.protection.ProtectionScalingService;
import com.maks.myexperienceplugin.protection.ProtectionScalingService.ProtectionScalingResult;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class ProtectionScalingListener implements Listener {

    private final ProtectionScalingService protectionScalingService;

    public ProtectionScalingListener(ProtectionScalingService protectionScalingService) {
        this.protectionScalingService = protectionScalingService;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        if (!protectionScalingService.isEnabled()) {
            return;
        }

        ProtectionScalingResult scalingResult = protectionScalingService.calculateFor(player);
        if (!scalingResult.hasProtection()) {
            return;
        }

        double vanillaMultiplier = scalingResult.safeVanillaMultiplier();
        double finalMultiplier = scalingResult.finalMultiplier();
        if (vanillaMultiplier <= 0.0 && finalMultiplier <= 0.0) {
            event.setDamage(0.0);
            return;
        }

        double ratio = scalingResult.adjustmentRatio();
        double finalDamage = event.getDamage() * ratio;
        if (finalDamage < 0.0) {
            finalDamage = 0.0;
        }

        event.setDamage(finalDamage);
    }
}
