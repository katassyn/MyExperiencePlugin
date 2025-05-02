package com.maks.myexperienceplugin.Class.skills;

import com.maks.myexperienceplugin.Class.skills.base.BaseSkillManager;
import com.maks.myexperienceplugin.Class.skills.effects.ascendancy.BeastmasterSkillEffectsHandler;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Listener to apply all skill effects when a player logs in
 */
public class PlayerSkillEffectsListener implements Listener {

    private final MyExperiencePlugin plugin;
    private final SkillTreeManager skillTreeManager;
    private final SkillEffectsHandler skillEffectsHandler;
    private final int debuggingFlag = 1;

    public PlayerSkillEffectsListener(MyExperiencePlugin plugin,
                                      SkillTreeManager skillTreeManager,
                                      SkillEffectsHandler skillEffectsHandler) {
        this.plugin = plugin;
        this.skillTreeManager = skillTreeManager;
        this.skillEffectsHandler = skillEffectsHandler;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Wait a bit to ensure all data is loaded first
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            applyAllSkillEffects(player);

            // Add this block: Check for Beastmaster and trigger summons
            String ascendancy = plugin.getClassManager().getPlayerAscendancy(player.getUniqueId());
            if ("Beastmaster".equals(ascendancy)) {
                plugin.getLogger().info("[BEASTMASTER DEBUG] Checking for auto-summons for " + player.getName());
                BeastmasterSkillEffectsHandler handler = 
                    (BeastmasterSkillEffectsHandler) plugin.getAscendancySkillEffectIntegrator().getHandler("Beastmaster");

                if (handler != null) {
                    // Delay summons to ensure all skills are loaded
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        handler.checkAndSummonCreatures(player);
                    }, 20L); // 1 second after effects are applied
                }
            }
        }, 40L); // 2 seconds delay
    }

    /**
     * Apply all purchased skill effects for a player
     */
    public void applyAllSkillEffects(Player player) {
        UUID uuid = player.getUniqueId();
        String playerClass = plugin.getClassManager().getPlayerClass(uuid);
        String ascendancy = plugin.getClassManager().getPlayerAscendancy(uuid);

        if ("NoClass".equalsIgnoreCase(playerClass)) {
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Not applying skill effects for " + player.getName() + " - No class selected");
            }
            return;
        }

        // Get all purchased skills
        Set<Integer> purchasedSkills = skillTreeManager.getPurchasedSkills(uuid);
        Map<Integer, Integer> purchaseCounts = skillTreeManager.getPurchaseCountMap(uuid);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Applying " + purchasedSkills.size() + " skill effects for " +
                    player.getName() + " (" + playerClass +
                    (ascendancy.isEmpty() ? "" : "/" + ascendancy) + ")");
        }

        // Get the base class manager
        BaseSkillManager classManager = skillTreeManager.getClassManager(playerClass);
        if (classManager != null) {
            // Apply base class skills
            for (int skillId : purchasedSkills) {
                // Only process base class skills (IDs below 100000)
                if (skillId < 100000) {
                    int count = purchaseCounts.getOrDefault(skillId, 1);
                    classManager.applySkillEffects(player, skillId, count);

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Applied base skill " + skillId +
                                " (count: " + count + ") for " + player.getName());
                    }
                }
            }
        }

        // Get the ascendancy manager (if applicable)
        if (!ascendancy.isEmpty()) {
            BaseSkillManager ascendancyManager = skillTreeManager.getAscendancyManager(playerClass, ascendancy);
            if (ascendancyManager != null) {
                // Apply ascendancy skills
                for (int skillId : purchasedSkills) {
                    // Only process ascendancy skills (IDs 100000+)
                    if (skillId >= 100000) {
                        int count = purchaseCounts.getOrDefault(skillId, 1);
                        ascendancyManager.applySkillEffects(player, skillId, count);

                        if (debuggingFlag == 1) {
                            plugin.getLogger().info("Applied ascendancy skill " + skillId +
                                    " (count: " + count + ") for " + player.getName());
                        }
                    }
                }
            }
        }

        // Force a stats recalculation to ensure everything is applied
        skillEffectsHandler.refreshPlayerStats(player);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Completed applying all skill effects for " + player.getName());
        }
    }
}
