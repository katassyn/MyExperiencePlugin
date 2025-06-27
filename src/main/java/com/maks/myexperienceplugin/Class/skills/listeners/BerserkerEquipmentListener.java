package com.maks.myexperienceplugin.Class.skills.listeners;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.Class.skills.effects.BerserkerVisualEffects;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

/**
 * Handles equipment restrictions for Berserker skills
 */
public class BerserkerEquipmentListener implements Listener {
    
    private final MyExperiencePlugin plugin;
    private static final int BERSERKER_OFFSET = 200000;
    
    public BerserkerEquipmentListener(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        UUID playerId = player.getUniqueId();
        
        // Check if player is a Berserker
        String ascendancy = plugin.getClassManager().getPlayerAscendancy(playerId);
        if (!"Berserker".equals(ascendancy)) return;
        
        // Check slot types
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            ItemStack item = event.getCursor();
            if (item == null || item.getType() == Material.AIR) return;
            
            // Check for chestplate restriction (Skill 1)
            if (event.getSlot() == 38 && isChestplate(item.getType())) {
                if (isPurchased(player, BERSERKER_OFFSET + 1)) {
                    event.setCancelled(true);
                    BerserkerVisualEffects.playEquipmentWarning(player, "chestplate");
                    player.sendMessage(ChatColor.RED + "Unarmored Rage prevents wearing chestplates!");
                    return;
                }
            }
            
            // Check for boots restriction (Skill 25)
            if (event.getSlot() == 36 && isBoots(item.getType())) {
                if (isPurchased(player, BERSERKER_OFFSET + 25)) {
                    event.setCancelled(true);
                    BerserkerVisualEffects.playEquipmentWarning(player, "boots");
                    player.sendMessage(ChatColor.RED + "Lightfoot Rage prevents wearing boots!");
                    return;
                }
            }
        }
        
        // Check shift-click
        if (event.isShiftClick() && event.getCurrentItem() != null) {
            ItemStack item = event.getCurrentItem();
            
            // Check chestplate
            if (isChestplate(item.getType()) && isPurchased(player, BERSERKER_OFFSET + 1)) {
                event.setCancelled(true);
                BerserkerVisualEffects.playEquipmentWarning(player, "chestplate");
                player.sendMessage(ChatColor.RED + "Unarmored Rage prevents wearing chestplates!");
                return;
            }
            
            // Check boots
            if (isBoots(item.getType()) && isPurchased(player, BERSERKER_OFFSET + 25)) {
                event.setCancelled(true);
                BerserkerVisualEffects.playEquipmentWarning(player, "boots");
                player.sendMessage(ChatColor.RED + "Lightfoot Rage prevents wearing boots!");
                return;
            }
        }
    }
    
    @EventHandler
    public void onArmorEquip(PlayerInteractEvent event) {
        if (event.getItem() == null) return;
        
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Check if player is a Berserker
        String ascendancy = plugin.getClassManager().getPlayerAscendancy(playerId);
        if (!"Berserker".equals(ascendancy)) return;
        
        ItemStack item = event.getItem();
        
        // Check for right-click armor equip
        if (event.getAction().name().contains("RIGHT")) {
            // Check chestplate
            if (isChestplate(item.getType()) && isPurchased(player, BERSERKER_OFFSET + 1)) {
                event.setCancelled(true);
                BerserkerVisualEffects.playEquipmentWarning(player, "chestplate");
                player.sendMessage(ChatColor.RED + "Unarmored Rage prevents wearing chestplates!");
                return;
            }
            
            // Check boots
            if (isBoots(item.getType()) && isPurchased(player, BERSERKER_OFFSET + 25)) {
                event.setCancelled(true);
                BerserkerVisualEffects.playEquipmentWarning(player, "boots");
                player.sendMessage(ChatColor.RED + "Lightfoot Rage prevents wearing boots!");
                return;
            }
        }
    }
    
    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Check if player is a Berserker
        String ascendancy = plugin.getClassManager().getPlayerAscendancy(playerId);
        if (!"Berserker".equals(ascendancy)) return;
        
        ItemStack item = event.getPlayerItem();
        if (item == null || item.getType() == Material.AIR) return;
        
        // Prevent equipping restricted armor from armor stand
        if ((isChestplate(item.getType()) && isPurchased(player, BERSERKER_OFFSET + 1)) ||
            (isBoots(item.getType()) && isPurchased(player, BERSERKER_OFFSET + 25))) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Your Berserker skills prevent wearing this equipment!");
        }
    }
    
    /**
     * Remove equipped items when player purchases restriction skills
     */
    public void checkAndRemoveRestrictedEquipment(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Check if player is a Berserker
        String ascendancy = plugin.getClassManager().getPlayerAscendancy(playerId);
        if (!"Berserker".equals(ascendancy)) return;
        
        // Delay check to ensure skill purchase is processed
        new BukkitRunnable() {
            @Override
            public void run() {
                // Check chestplate
                if (isPurchased(player, BERSERKER_OFFSET + 1)) {
                    ItemStack chestplate = player.getInventory().getChestplate();
                    if (chestplate != null && chestplate.getType() != Material.AIR) {
                        player.getInventory().setChestplate(null);
                        
                        // Add to inventory or drop
                        if (player.getInventory().firstEmpty() != -1) {
                            player.getInventory().addItem(chestplate);
                        } else {
                            player.getWorld().dropItemNaturally(player.getLocation(), chestplate);
                        }
                        
                        player.sendMessage(ChatColor.RED + "Your chestplate was removed due to Unarmored Rage!");
                        BerserkerVisualEffects.playRageEffect(player);
                    }
                }
                
                // Check boots
                if (isPurchased(player, BERSERKER_OFFSET + 25)) {
                    ItemStack boots = player.getInventory().getBoots();
                    if (boots != null && boots.getType() != Material.AIR) {
                        player.getInventory().setBoots(null);
                        
                        // Add to inventory or drop
                        if (player.getInventory().firstEmpty() != -1) {
                            player.getInventory().addItem(boots);
                        } else {
                            player.getWorld().dropItemNaturally(player.getLocation(), boots);
                        }
                        
                        player.sendMessage(ChatColor.RED + "Your boots were removed due to Lightfoot Rage!");
                        BerserkerVisualEffects.playRageEffect(player);
                    }
                }
            }
        }.runTaskLater(plugin, 5L);
    }
    
    private boolean isChestplate(Material material) {
        return material.name().endsWith("_CHESTPLATE");
    }
    
    private boolean isBoots(Material material) {
        return material.name().endsWith("_BOOTS");
    }
    
    private boolean isPurchased(Player player, int skillId) {
        return plugin.getSkillTreeManager().getPurchasedSkills(player.getUniqueId()).contains(skillId);
    }
}