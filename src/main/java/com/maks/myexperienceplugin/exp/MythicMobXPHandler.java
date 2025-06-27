package com.maks.myexperienceplugin.exp;

import com.maks.myexperienceplugin.Class.skills.effects.ascendancy.BeastmasterSkillEffectsHandler;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.alchemy.PhysisExpManager;
import com.maks.myexperienceplugin.alchemy.TonicExpManager;
import com.maks.myexperienceplugin.party.Party;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class MythicMobXPHandler implements Listener {

    private final MyExperiencePlugin plugin;
    private static final int debuggingFlag = 0;

    public MythicMobXPHandler(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMythicMobDeath(MythicMobDeathEvent event) {
        Entity killerEntity = event.getKiller();
        Player killer = null;

        // Check if the killer is a player
        if (killerEntity instanceof Player) {
            killer = (Player) killerEntity;
        } 
        // Check if the killer is a summon
        else if (killerEntity != null) {
            // Check if the AscendancySkillEffectIntegrator is available
            if (plugin.getAscendancySkillEffectIntegrator() != null) {
                // Get the BeastmasterSkillEffectsHandler
                BeastmasterSkillEffectsHandler beastmasterHandler = 
                    (BeastmasterSkillEffectsHandler) plugin.getAscendancySkillEffectIntegrator().getHandler("Beastmaster");

                if (beastmasterHandler != null) {
                    // Check if the killer is a summon and get its owner
                    killer = beastmasterHandler.getSummonOwner(killerEntity);

                    if (killer != null && debuggingFlag == 1) {
                        Bukkit.getLogger().info("[DEBUG] MythicMob killed by a summon owned by " + killer.getName());
                    }
                }
            }
        }

        // Log if no valid killer was found
        if (killer == null && debuggingFlag == 1) {
            String mobName = event.getMobType().getInternalName();
            Bukkit.getLogger().info("[DEBUG] MythicMob " + mobName + " died but no valid killer (player or summon owner) was found");
            if (killerEntity != null) {
                Bukkit.getLogger().info("[DEBUG] Killer entity type: " + killerEntity.getType().toString());
            } else {
                Bukkit.getLogger().info("[DEBUG] Killer entity is null");
            }
        }

        // If we have a valid killer (either player or summon owner), award XP
        if (killer != null) {
            // Get mob's XP value
            String mobName = event.getMobType().getInternalName();
            double baseXpReward = plugin.getXpForMob(mobName);

            // Apply bonus XP multiplier if enabled
            double bonusMultiplier = plugin.isBonusExpEnabled() ? plugin.getBonusExpValue() / 100.0 : 1.0;

            // Apply Physis percentage bonus (directly multiply the base XP)
            double physisBonus = PhysisExpManager.getInstance().getExpBonus(killer);
            double physisMultiplier = 1.0 + physisBonus;

            // Apply Tonic bonus (based on required XP for next level)
            double tonicBonusPercentage = TonicExpManager.getInstance().getBonus(killer);
            int playerLevel = plugin.getPlayerLevel(killer);
            double requiredXP = plugin.getXpPerLevel().getOrDefault(playerLevel, 100.0);
            double tonicBonusXP = requiredXP * tonicBonusPercentage;

            // Apply Player Exp Boost (individual player boost)
            double playerExpBoost = plugin.getPlayerExpBoost(killer);
            double playerBoostMultiplier = 1.0 + (playerExpBoost / 100.0);

            // Calculate final XP reward with all bonuses
            double finalXpReward = (baseXpReward * bonusMultiplier * physisMultiplier * playerBoostMultiplier) + tonicBonusXP;

            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[DEBUG] XP Calculation for " + killer.getName() + " killing " + mobName);
                Bukkit.getLogger().info("  Base XP: " + baseXpReward);
                Bukkit.getLogger().info("  Server Bonus Multiplier: " + bonusMultiplier);
                Bukkit.getLogger().info("  Physis Bonus: " + (physisBonus * 100) + "% (multiplier: " + physisMultiplier + ")");
                Bukkit.getLogger().info("  Player Exp Boost: " + playerExpBoost + "% (multiplier: " + playerBoostMultiplier + ")");
                Bukkit.getLogger().info("  Tonic Bonus: " + (tonicBonusPercentage * 100) + "% of " + requiredXP + " = " + tonicBonusXP);
                Bukkit.getLogger().info("  Final XP: " + finalXpReward);
            }

            // Give full XP to killer (but use direct method to avoid double boost application)
            UUID killerId = killer.getUniqueId();
            double currentXP = plugin.getPlayerCurrentXP().getOrDefault(killerId, 0.0);
            currentXP += finalXpReward;
            plugin.getPlayerCurrentXP().put(killerId, currentXP);
            plugin.checkLevelUp(killer);
            plugin.updatePlayerXPBar(killer);

            // Share XP with party members
            Party party = plugin.getPartyManager().getParty(killer);
            if (party != null) {
                for (UUID memberId : party.getMembers()) {
                    if (!memberId.equals(killer.getUniqueId())) {
                        Player member = Bukkit.getPlayer(memberId);
                        if (member != null && member.getWorld().equals(killer.getWorld())) {
                            if (member.getLocation().distance(killer.getLocation()) <= 25) {
                                // Give 30% of BASE XP to party member (they get their own boosts applied)
                                double partyBaseXpReward = baseXpReward * bonusMultiplier * physisMultiplier * 0.3;
                                plugin.addXP(member, partyBaseXpReward); // This will apply member's own player boost

                                if (debuggingFlag == 1) {
                                    Bukkit.getLogger().info("[DEBUG] Party member " + member.getName() +
                                            " received base " + partyBaseXpReward + " XP (30% of killer's base, member's own boosts will be applied)");
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
