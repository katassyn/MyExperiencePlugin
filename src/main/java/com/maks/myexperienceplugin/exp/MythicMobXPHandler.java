package com.maks.myexperienceplugin.exp;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.alchemy.PhysisExpManager;
import com.maks.myexperienceplugin.alchemy.TonicExpManager;
import com.maks.myexperienceplugin.party.Party;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class MythicMobXPHandler implements Listener {

    private final MyExperiencePlugin plugin;
    private static final int debuggingFlag = 1;

    public MythicMobXPHandler(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMythicMobDeath(MythicMobDeathEvent event) {
        if (event.getKiller() instanceof Player) {
            Player killer = (Player) event.getKiller();

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

            // Calculate final XP reward
            double finalXpReward = (baseXpReward * bonusMultiplier * physisMultiplier) + tonicBonusXP;

            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[DEBUG] XP Calculation for " + killer.getName() + " killing " + mobName);
                Bukkit.getLogger().info("  Base XP: " + baseXpReward);
                Bukkit.getLogger().info("  Server Bonus Multiplier: " + bonusMultiplier);
                Bukkit.getLogger().info("  Physis Bonus: " + (physisBonus * 100) + "% (multiplier: " + physisMultiplier + ")");
                Bukkit.getLogger().info("  Tonic Bonus: " + (tonicBonusPercentage * 100) + "% of " + requiredXP + " = " + tonicBonusXP);
                Bukkit.getLogger().info("  Final XP: " + finalXpReward);
            }

            // Give full XP to killer
            plugin.addXP(killer, finalXpReward);

            // Share XP with party members
            Party party = plugin.getPartyManager().getParty(killer);
            if (party != null) {
                for (UUID memberId : party.getMembers()) {
                    if (!memberId.equals(killer.getUniqueId())) {
                        Player member = Bukkit.getPlayer(memberId);
                        if (member != null && member.getWorld().equals(killer.getWorld())) {
                            if (member.getLocation().distance(killer.getLocation()) <= 25) {
                                // Give 30% of XP to party member, with bonus applied
                                double partyXpReward = finalXpReward * 0.3;
                                plugin.addXP(member, partyXpReward);

                                if (debuggingFlag == 1) {
                                    Bukkit.getLogger().info("[DEBUG] Party member " + member.getName() +
                                            " received " + partyXpReward + " XP (30% of " + finalXpReward + ")");
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}