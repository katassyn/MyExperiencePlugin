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
            double xpReward = plugin.getXpForMob(mobName);

            // Give full XP to killer
            plugin.addXP(killer, xpReward);

            // Share XP with party members
            Party party = plugin.getPartyManager().getParty(killer);
            if (party != null) {
                for (UUID memberId : party.getMembers()) {
                    if (!memberId.equals(killer.getUniqueId())) {
                        Player member = Bukkit.getPlayer(memberId);
                        if (member != null && member.getWorld().equals(killer.getWorld())) {
                            if (member.getLocation().distance(killer.getLocation()) <= 25) {
                                // Give 30% of XP to party member
                                plugin.addXP(member, xpReward * 0.3);
                                member.sendMessage("§aYou received §6" + MyExperiencePlugin.formatNumber(xpReward * 0.3)
                                        + " XP §afrom " + killer.getName() + "'s kill.");
                            }
                        }
                    }
                }
            }
        }
    }
}
