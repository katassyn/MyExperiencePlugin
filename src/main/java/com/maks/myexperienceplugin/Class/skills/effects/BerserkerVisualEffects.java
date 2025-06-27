package com.maks.myexperienceplugin.Class.skills.effects;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import com.maks.myexperienceplugin.MyExperiencePlugin;

/**
 * Visual and sound effects for Berserker skills
 */
public class BerserkerVisualEffects {

    /**
     * Play rage activation effect
     */
    public static void playRageEffect(Player player) {
        // Red particle burst
        player.getWorld().spawnParticle(
            Particle.REDSTONE, 
            player.getLocation().add(0, 1, 0), 
            50, 1, 1, 1, 0,
            new Particle.DustOptions(Color.RED, 2)
        );

        // Sound effect
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 2.0f);
    }

    /**
     * Play critical hit effect
     */
    public static void playCriticalHitEffect(Player player, LivingEntity target) {
        // Blood splash effect
        target.getWorld().spawnParticle(
            Particle.BLOCK_CRACK,
            target.getLocation().add(0, 1, 0),
            30, 0.5, 0.5, 0.5, 0.1,
            Material.REDSTONE_BLOCK.createBlockData()
        );

        // Critical hit sound
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.0f);
    }

    /**
     * Play bleeding effect on target
     */
    public static void playBleedingEffect(LivingEntity target) {
        // Blood drip particles
        target.getWorld().spawnParticle(
            Particle.DRIP_LAVA,
            target.getLocation().add(0, 1, 0),
            5, 0.2, 0.5, 0.2, 0
        );
    }

    /**
     * Play Death Defiance activation
     */
    public static void playDeathDefianceEffect(Player player, MyExperiencePlugin plugin) {
        // Initial explosion effect
        player.getWorld().spawnParticle(
            Particle.EXPLOSION_LARGE,
            player.getLocation(),
            1, 0, 0, 0, 0
        );

        // Rage aura effect
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 200 || !player.isOnline()) { // 10 seconds
                    cancel();
                    return;
                }

                // Red aura around player
                player.getWorld().spawnParticle(
                    Particle.FLAME,
                    player.getLocation().add(0, 0.5, 0),
                    10, 0.5, 0.5, 0.5, 0.02
                );

                // Dark red particles
                player.getWorld().spawnParticle(
                    Particle.REDSTONE,
                    player.getLocation().add(0, 1, 0),
                    5, 0.5, 1, 0.5, 0,
                    new Particle.DustOptions(Color.fromRGB(139, 0, 0), 1.5f)
                );

                ticks += 2;
            }
        }.runTaskTimer(plugin, 0, 2);

        // Powerful sound
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 2.0f);
    }

    /**
     * Play Kill Frenzy stack effect
     */
    public static void playKillFrenzyEffect(Player player, int stacks) {
        // Speed lines effect
        player.getWorld().spawnParticle(
            Particle.VILLAGER_ANGRY,
            player.getLocation().add(0, 2, 0),
            stacks, 0.5, 0.5, 0.5, 0
        );

        // Stack sound (higher pitch with more stacks)
        float pitch = 0.5f + (stacks * 0.1f);
        player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 0.5f, pitch);
    }

    /**
     * Play Combat Momentum effect
     */
    public static void playCombatMomentumEffect(Player player, int stacks) {
        // Fire spiral effect
        Location loc = player.getLocation();
        for (int i = 0; i < 20; i++) {
            double angle = i * Math.PI / 10;
            double x = Math.cos(angle) * 1.5;
            double z = Math.sin(angle) * 1.5;
            double y = i * 0.1;

            loc.getWorld().spawnParticle(
                Particle.FLAME,
                loc.clone().add(x, y, z),
                1, 0, 0, 0, 0
            );
        }

        // Power up sound
        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 1.0f, 1.5f);
    }

    /**
     * Play Trophy Head collection effect
     */
    public static void playTrophyCollectEffect(Player player) {
        // Golden particles rising
        Location loc = player.getLocation();
        for (int i = 0; i < 30; i++) {
            double offsetX = (Math.random() - 0.5) * 2;
            double offsetZ = (Math.random() - 0.5) * 2;
            double offsetY = Math.random() * 2;

            loc.getWorld().spawnParticle(
                Particle.TOTEM,
                loc.clone().add(offsetX, offsetY, offsetZ),
                1, 0, 0.1, 0, 0
            );
        }

        // Achievement sound
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    }

    /**
     * Play equipment restriction warning
     */
    public static void playEquipmentWarning(Player player, String itemType) {
        // Red warning particles
        player.getWorld().spawnParticle(
            Particle.REDSTONE,
            player.getLocation().add(0, 2, 0),
            5, 0.2, 0.2, 0.2, 0,
            new Particle.DustOptions(Color.RED, 2)
        );

        // Warning sound
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);

        // Warning message
        player.sendMessage(ChatColor.RED + "⚠ Berserker skill prevents wearing " + itemType + "!");
    }
}
