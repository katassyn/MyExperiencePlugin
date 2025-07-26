package com.maks.myexperienceplugin.alchemy;

import org.bukkit.entity.Player;
import org.bukkit.entity.Item;
import org.bukkit.Bukkit;
import org.bukkit.util.Vector;
import org.bukkit.scheduler.BukkitTask;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.utils.ActionBarUtils;

public class PhycisMagnetEffect extends AlchemyEffect {
    private static final int debuggingFlag = 0;
    private final double radius;
    private BukkitTask magnetTask;

    public PhycisMagnetEffect(Player player, long durationMillis, long cooldownMillis, String effectName) {
        // Set the duration for the magnet effect based on the parameter
        super(player, durationMillis, cooldownMillis, effectName);
        this.radius = 10.0; // Can be parametrized if needed
    }

    // Keep backward compatibility with existing code
    public PhycisMagnetEffect(Player player, long cooldownMillis, String effectName) {
        // Default to 5 minutes if not specified
        this(player, 5 * 60 * 1000, cooldownMillis, effectName);
    }

    @Override
    public void apply() {
        int durationMinutes = (int) (durationMillis / (60 * 1000));
        ActionBarUtils.sendActionBar(player, "§a[" + effectName + "] Magnet effect activated for " + durationMinutes + " minutes.");

        // Create a runnable to attract items
        Runnable attractItems = () -> {
            // Check if player is still online
            if (!player.isOnline()) {
                if (magnetTask != null) {
                    magnetTask.cancel();
                    magnetTask = null;
                }
                return;
            }

            // Get nearby items within radius
            int itemCount = 0;

            for (Item item : player.getWorld().getEntitiesByClass(Item.class)) {
                // Check if item is within radius
                if (item.getLocation().distance(player.getLocation()) <= radius) {
                    // Calculate vector from item to player
                    Vector direction = player.getLocation().toVector().subtract(item.getLocation().toVector());

                    // Pull items toward player with increased force
                    double distance = direction.length();
                    direction.normalize();

                    // Stronger pull for closer items to prevent them from overshooting
                    double speed = Math.max(0.3, Math.min(1.0, 0.3 + (radius - distance) / radius));
                    direction.multiply(speed);

                    // Apply the velocity to the item
                    item.setVelocity(direction);
                    itemCount++;

                    if (debuggingFlag == 1) {
                        Bukkit.getLogger().info("[DEBUG] Attracting item " + item.getItemStack().getType() +
                                " to player " + player.getName() + " with velocity " + direction);
                    }
                }
            }

            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[DEBUG] Magnet effect found " + itemCount + " items within radius " +
                        radius + " of player " + player.getName());
            }

            // Only show action bar message if items were found
            if (itemCount > 0) {
                ActionBarUtils.sendActionBar(player, "§a[" + effectName + "] " + itemCount + " items attracted.");
            }
        };

        // Schedule the task to run repeatedly
        magnetTask = Bukkit.getScheduler().runTaskTimer(MyExperiencePlugin.getInstance(), attractItems, 0L, 10L);
    }

    @Override
    public void remove() {
        // Cancel the task if it's still running
        if (magnetTask != null) {
            magnetTask.cancel();
            magnetTask = null;

            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[DEBUG] Cancelled magnet effect task for player " + player.getName());
            }
        }

        int durationMinutes = (int) (durationMillis / (60 * 1000));
        ActionBarUtils.sendActionBar(player, "§c[" + effectName + "] Magnet effect finished after " + durationMinutes + " minutes.");
    }
}
