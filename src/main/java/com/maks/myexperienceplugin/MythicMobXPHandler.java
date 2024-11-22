package com.maks.myexperienceplugin;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class MythicMobXPHandler implements Listener {

    private final MyExperiencePlugin plugin;

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
            double finalXpReward = baseXpReward * bonusMultiplier;

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

                                // Inform party member (optional)
//                                member.sendMessage("§aYou received §6" + MyExperiencePlugin.formatNumber(partyXpReward)
//                                        + " XP §afrom " + killer.getName() + "'s kill.");
                            }
                        }
                    }
                }
            }

            // Inform killer about XP received (optional)
            killer.sendMessage("§aYou received §6" + MyExperiencePlugin.formatNumber(finalXpReward)
                    + " XP §afrom killing " + mobName + "!");
        }
    }
}
