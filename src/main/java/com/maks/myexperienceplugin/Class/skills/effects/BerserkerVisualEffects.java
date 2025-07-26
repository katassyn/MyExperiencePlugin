package com.maks.myexperienceplugin.Class.skills.effects;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * Visual effects for Berserker skills
 */
public class BerserkerVisualEffects {

    /**
     * Play visual and sound effects for Berserker rage
     * @param player The player to play effects for
     */
    public static void playRageEffect(Player player) {
        if (player == null || !player.isOnline()) return;
        
        Location location = player.getLocation().add(0, 1, 0);
        
        // Play particle effects
        player.getWorld().spawnParticle(
                Particle.FLAME,
                location,
                20, // amount
                0.5, 0.5, 0.5, // offset
                0.1 // speed
        );
        
        // Play sound effect
        player.getWorld().playSound(
                location,
                Sound.ENTITY_BLAZE_SHOOT,
                1.0f, // volume
                0.5f  // pitch (lower for deeper sound)
        );
    }
    
    /**
     * Play visual and sound effects for critical hits
     * @param player The player who scored the critical hit
     * @param target The entity that was hit
     */
    public static void playCriticalHitEffect(Player player, LivingEntity target) {
        if (player == null || !player.isOnline() || target == null || !target.isValid()) return;
        
        Location targetLocation = target.getLocation().add(0, 1, 0);
        
        // Play particle effects at target
        target.getWorld().spawnParticle(
                Particle.CRIT,
                targetLocation,
                15, // amount
                0.5, 0.5, 0.5, // offset
                0.1 // speed
        );
        
        // Play additional red particles for blood effect
        target.getWorld().spawnParticle(
                Particle.REDSTONE,
                targetLocation,
                10, // amount
                0.5, 0.5, 0.5, // offset
                1, // speed
                new Particle.DustOptions(Color.RED, 1.0f)
        );
        
        // Play sound effect
        target.getWorld().playSound(
                targetLocation,
                Sound.ENTITY_PLAYER_ATTACK_CRIT,
                1.0f, // volume
                1.0f  // pitch
        );
    }
    
    /**
     * Play visual and sound effects for Death Defiance skill
     * @param player The player who activated Death Defiance
     * @param plugin The plugin instance
     */
    public static void playDeathDefianceEffect(Player player, MyExperiencePlugin plugin) {
        if (player == null || !player.isOnline()) return;
        
        Location location = player.getLocation();
        
        // Play intense particle effects
        player.getWorld().spawnParticle(
                Particle.EXPLOSION_LARGE,
                location,
                3, // amount
                0.5, 0.5, 0.5, // offset
                0.1 // speed
        );
        
        player.getWorld().spawnParticle(
                Particle.FLAME,
                location,
                30, // amount
                0.8, 0.8, 0.8, // offset
                0.2 // speed
        );
        
        // Red particles for blood/rage effect
        player.getWorld().spawnParticle(
                Particle.REDSTONE,
                location.add(0, 1, 0),
                20, // amount
                0.5, 0.5, 0.5, // offset
                1, // speed
                new Particle.DustOptions(Color.RED, 1.5f)
        );
        
        // Play dramatic sound effects
        player.getWorld().playSound(
                location,
                Sound.ENTITY_WITHER_SPAWN,
                1.0f, // volume
                1.5f  // pitch (higher for more urgent sound)
        );
        
        player.getWorld().playSound(
                location,
                Sound.ENTITY_PLAYER_LEVELUP,
                1.0f, // volume
                0.5f  // pitch (lower for more dramatic effect)
        );
    }
}
