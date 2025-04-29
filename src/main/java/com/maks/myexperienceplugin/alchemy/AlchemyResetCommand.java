package com.maks.myexperienceplugin.alchemy;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.attribute.Attribute;

public class AlchemyResetCommand implements CommandExecutor {
    private static final int debuggingFlag = 1;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check permission
        if (!sender.hasPermission("myplugin.alchemy.reset")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        // Handle different command cases
        if (args.length == 0) {
            // If no arguments, reset for the sender if it's a player
            if (sender instanceof Player) {
                resetForPlayer((Player) sender);
                sender.sendMessage("§aYour alchemy effects and cooldowns have been reset!");
            } else {
                sender.sendMessage("§cUsage: /alchemy_reset [player]");
            }
            return true;
        } else {
            // Reset for the specified player
            String targetName = args[0];
            Player target = Bukkit.getPlayer(targetName);

            if (target == null) {
                sender.sendMessage("§cPlayer not found: " + targetName);
                return true;
            }

            resetForPlayer(target);
            sender.sendMessage("§aAlchemy effects and cooldowns have been reset for " + target.getName() + "!");
            target.sendMessage("§aYour alchemy effects and cooldowns have been reset by " + sender.getName() + "!");
            return true;
        }
    }

    private void resetForPlayer(Player player) {
        AlchemyManager manager = AlchemyManager.getInstance();

        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[DEBUG] Alchemy reset started for player: " + player.getName());
        }

        // Force remove all attribute modifiers related to alchemy effects
        cleanAttributeModifiers(player, Attribute.GENERIC_ATTACK_DAMAGE, "Damage");
        cleanAttributeModifiers(player, Attribute.GENERIC_MAX_HEALTH, "Health");
        cleanAttributeModifiers(player, Attribute.GENERIC_MOVEMENT_SPEED, "Movement");
        cleanAttributeModifiers(player, Attribute.GENERIC_LUCK, "Luck");

        // Remove potion effects that might have been applied
        player.removePotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION);
        player.removePotionEffect(org.bukkit.potion.PotionEffectType.ABSORPTION);

        // Remove all active effects explicitly from AlchemyManager
        for (AlchemyManager.AlchemyCategory category : AlchemyManager.AlchemyCategory.values()) {
            manager.removeEffect(player, category);
        }

        // Clear other managers too
        ImmunityManager.getInstance().setImmune(player, false);
        LifestealManager.getInstance().removeLifesteal(player);
        TotemManager.getInstance().clearTotem(player);
        PhysisExpManager.getInstance().removeExpBonus(player);
        TonicExpManager.getInstance().removeBonus(player);

        // Clear cooldowns for the player
        manager.clearCooldowns(player);

        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[DEBUG] Alchemy reset completed for player: " + player.getName());
        }
    }

    private void cleanAttributeModifiers(Player player, Attribute attribute, String debugName) {
        try {
            if (player.getAttribute(attribute) != null) {
                // Clean all modifiers from this attribute that might be related to alchemy
                player.getAttribute(attribute).getModifiers().stream()
                        .filter(mod -> mod.getName().contains("Tonic") ||
                                mod.getName().contains("Potion") ||
                                mod.getName().contains("Phycis") ||
                                mod.getName().contains("Berserk"))
                        .forEach(mod -> {
                            player.getAttribute(attribute).removeModifier(mod);
                            if (debuggingFlag == 1) {
                                Bukkit.getLogger().info("[DEBUG] Removed " + debugName + " modifier: " +
                                        mod.getName() + " from player " + player.getName());
                            }
                        });
            }
        } catch (Exception e) {
            if (debuggingFlag == 1) {
                Bukkit.getLogger().warning("[DEBUG] Error cleaning " + debugName + " modifiers: " + e.getMessage());
            }
        }
    }
}