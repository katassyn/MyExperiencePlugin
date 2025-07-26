package com.maks.myexperienceplugin.listener;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.utils.DebugUtils;
import com.maks.myexperienceplugin.utils.PartyClassIntegration;
import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.projectiles.ProjectileSource;

/**
 * Listener that prevents damage between party members
 */
public class PartyDamagePreventionListener implements Listener {
    private final MyExperiencePlugin plugin;
    private final int debuggingFlag = 0;
    
    public PartyDamagePreventionListener(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        // Check if victim is a player
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player victim = (Player) event.getEntity();
        Player attacker = null;
        
        // Direct player attack
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        }
        // Projectile attack (arrow, snowball, etc.)
        else if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            ProjectileSource shooter = projectile.getShooter();
            
            if (shooter instanceof Player) {
                attacker = (Player) shooter;
            }
        }
        // Wolf/Pet attack
        else if (event.getDamager() instanceof Wolf) {
            Wolf wolf = (Wolf) event.getDamager();
            if (wolf.isTamed() && wolf.getOwner() instanceof Player) {
                attacker = (Player) wolf.getOwner();
            }
        }
        // Other tameable entities
        else if (event.getDamager() instanceof Tameable) {
            Tameable tameable = (Tameable) event.getDamager();
            if (tameable.isTamed() && tameable.getOwner() instanceof Player) {
                attacker = (Player) tameable.getOwner();
            }
        }
        
        // If we found an attacker, check if they're in the same party
        if (attacker != null && PartyClassIntegration.shouldPreventDamage(attacker, victim)) {
            event.setCancelled(true);
            
            // Send message to attacker
            attacker.sendMessage(ChatColor.RED + "You cannot damage party members!");
            
            if (debuggingFlag == 1) {
                DebugUtils.sendDebugMessage(attacker, "Prevented damage to party member " + victim.getName());
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPotionSplash(PotionSplashEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        // Check if thrown by a player
        ProjectileSource shooter = event.getEntity().getShooter();
        if (!(shooter instanceof Player)) {
            return;
        }
        
        Player thrower = (Player) shooter;
        
        // Check all affected entities
        for (LivingEntity entity : event.getAffectedEntities()) {
            if (entity instanceof Player) {
                Player affected = (Player) entity;
                
                // If it's a harmful potion and they're in the same party
                if (isHarmfulPotion(event) && PartyClassIntegration.shouldPreventDamage(thrower, affected)) {
                    // Set intensity to 0 to prevent effect
                    event.setIntensity(entity, 0.0);
                    
                    if (debuggingFlag == 1) {
                        DebugUtils.sendDebugMessage(thrower, "Prevented potion effect to party member " + affected.getName());
                    }
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        // Check if the cloud has a source (player who threw the potion)
        ProjectileSource source = event.getEntity().getSource();
        if (!(source instanceof Player)) {
            return;
        }
        
        Player thrower = (Player) source;
        
        // Check all affected entities
        event.getAffectedEntities().removeIf(entity -> {
            if (entity instanceof Player) {
                Player affected = (Player) entity;
                
                // If it's harmful and they're in the same party
                if (isHarmfulCloud(event) && PartyClassIntegration.shouldPreventDamage(thrower, affected)) {
                    if (debuggingFlag == 1) {
                        DebugUtils.sendDebugMessage(thrower, "Prevented area effect to party member " + affected.getName());
                    }
                    return true; // Remove from affected entities
                }
            }
            return false;
        });
    }
    
    /**
     * Checks if a potion is harmful
     */
    private boolean isHarmfulPotion(PotionSplashEvent event) {
        return event.getEntity().getEffects().stream()
            .anyMatch(effect -> {
                PotionEffectType type = effect.getType();
                if (type.equals(PotionEffectType.POISON) ||
                    type.equals(PotionEffectType.WITHER) ||
                    type.equals(PotionEffectType.HARM) ||
                    type.equals(PotionEffectType.SLOW) ||
                    type.equals(PotionEffectType.WEAKNESS) ||
                    type.equals(PotionEffectType.SLOW_DIGGING) ||
                    type.equals(PotionEffectType.CONFUSION) ||
                    type.equals(PotionEffectType.BLINDNESS) ||
                    type.equals(PotionEffectType.HUNGER) ||
                    type.equals(PotionEffectType.LEVITATION) ||
                    type.equals(PotionEffectType.BAD_OMEN) ||
                    type.equals(PotionEffectType.UNLUCK)) {
                    return true;
                }
                return false;
            });
    }
    
    /**
     * Checks if an area effect cloud is harmful
     */
    private boolean isHarmfulCloud(AreaEffectCloudApplyEvent event) {
        AreaEffectCloud cloud = event.getEntity();
        if (cloud.getBasePotionData() != null) {
            PotionType type = cloud.getBasePotionData().getType();
            if (type.equals(PotionType.POISON) ||
                type.equals(PotionType.WEAKNESS) ||
                type.equals(PotionType.SLOWNESS) ||
                type.equals(PotionType.INSTANT_DAMAGE)) {
                return true;
            }
        }
        
        // Check custom effects
        return cloud.getCustomEffects().stream()
            .anyMatch(effect -> {
                PotionEffectType type = effect.getType();
                if (type.equals(PotionEffectType.POISON) ||
                    type.equals(PotionEffectType.WITHER) ||
                    type.equals(PotionEffectType.HARM) ||
                    type.equals(PotionEffectType.SLOW) ||
                    type.equals(PotionEffectType.WEAKNESS) ||
                    type.equals(PotionEffectType.SLOW_DIGGING) ||
                    type.equals(PotionEffectType.CONFUSION) ||
                    type.equals(PotionEffectType.BLINDNESS) ||
                    type.equals(PotionEffectType.HUNGER) ||
                    type.equals(PotionEffectType.LEVITATION) ||
                    type.equals(PotionEffectType.BAD_OMEN) ||
                    type.equals(PotionEffectType.UNLUCK)) {
                    return true;
                }
                return false;
            });
    }
}
