package com.maks.myexperienceplugin.listener;

import com.maks.myexperienceplugin.alchemy.AlchemyManager;
import com.maks.myexperienceplugin.party.PartyManager;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.Class.skills.effects.BaseSkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.effects.ascendancy.*;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerDisconnectListener implements Listener {
    private final PartyManager partyManager;
    private final MyExperiencePlugin plugin;
    private static final int debuggingFlag = 0;

    public PlayerDisconnectListener(PartyManager partyManager, MyExperiencePlugin plugin) {
        this.partyManager = partyManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        
        // Handle party removal
        partyManager.removePlayerFromParty(event.getPlayer());

        // Handle alchemy effects - now they persist, but we need to cancel scheduled tasks
        AlchemyManager.getInstance().handlePlayerDisconnect(event.getPlayer());

        // Clean up all skill handler data to prevent memory leaks
        cleanupAllSkillHandlers(playerId);

        // Clear base effect state
        BaseSkillEffectsHandler.getPerformanceMonitor().invalidateSkillCache(playerId);

        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[DEBUG] Handled disconnect and cleanup for player: " + event.getPlayer().getName());
        }
    }

    /**
     * Clean up data from all skill effect handlers to prevent memory leaks
     */
    private void cleanupAllSkillHandlers(UUID playerId) {
        try {
            // Get all ascendancy handlers from the integrator
            if (plugin.getAscendancySkillEffectIntegrator() != null) {
                // Clean up Beastmaster data
                BaseSkillEffectsHandler beastmasterHandler = plugin.getAscendancySkillEffectIntegrator().getHandler("Beastmaster");
                if (beastmasterHandler instanceof BeastmasterSkillEffectsHandler) {
                    ((BeastmasterSkillEffectsHandler) beastmasterHandler).clearPlayerData(playerId);
                }

                // Clean up Berserker data  
                BaseSkillEffectsHandler berserkerHandler = plugin.getAscendancySkillEffectIntegrator().getHandler("Berserker");
                if (berserkerHandler instanceof BerserkerSkillEffectsHandler) {
                    ((BerserkerSkillEffectsHandler) berserkerHandler).clearPlayerData(playerId);
                }

                // Clean up other handlers
                cleanupHandler("Shadowstalker", ShadowstalkerSkillEffectsHandler.class, playerId);
                cleanupHandler("Earthwarden", EarthwardenSkillEffectsHandler.class, playerId);
                cleanupHandler("FlameWarden", FlameWardenSkillEffectsHandler.class, playerId);
                cleanupHandler("ScaleGuardian", ScaleGuardianSkillEffectsHandler.class, playerId);
                cleanupHandler("Elementalist", ElementalistSkillEffectsHandler.class, playerId);
                cleanupHandler("Chronomancer", ChronomancerSkillEffectsHandler.class, playerId);
                cleanupHandler("ArcaneProtector", ArcaneProtectorSkillEffectsHandler.class, playerId);
            }

            // Clear from base handler unified effect state
            if (BaseSkillEffectsHandler.getPerformanceMonitor() != null) {
                BaseSkillEffectsHandler.getPerformanceMonitor().invalidateSkillCache(playerId);
            }

            if (debuggingFlag == 1) {
                plugin.getLogger().info("[CLEANUP] Successfully cleaned up all skill handler data for player: " + playerId);
            }

        } catch (Exception e) {
            plugin.getLogger().warning("[CLEANUP ERROR] Failed to cleanup skill handler data for player " + playerId + ": " + e.getMessage());
            if (debuggingFlag == 1) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Generic cleanup helper for handlers that have clearPlayerData method
     */
    private void cleanupHandler(String handlerName, Class<?> handlerClass, UUID playerId) {
        try {
            BaseSkillEffectsHandler handler = plugin.getAscendancySkillEffectIntegrator().getHandler(handlerName);
            if (handler != null && handlerClass.isInstance(handler)) {
                // Use reflection to call clearPlayerData if it exists
                try {
                    handler.getClass().getMethod("clearPlayerData", UUID.class).invoke(handler, playerId);
                } catch (NoSuchMethodException e) {
                    // Handler doesn't have clearPlayerData method, call base cleanup
                    handler.clearAllPlayerData(playerId);
                }
            }
        } catch (Exception e) {
            if (debuggingFlag == 1) {
                plugin.getLogger().warning("[CLEANUP] Failed to cleanup " + handlerName + " data: " + e.getMessage());
            }
        }
    }
}
