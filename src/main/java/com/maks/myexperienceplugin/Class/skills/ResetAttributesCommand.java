package com.maks.myexperienceplugin.Class.skills;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ResetAttributesCommand implements CommandExecutor, TabCompleter {

    private final MyExperiencePlugin plugin;
    private final SkillEffectsHandler skillEffectsHandler;

    // Default vanilla attribute values
    private static final double DEFAULT_MAX_HEALTH = 20.0;
    private static final double DEFAULT_ATTACK_DAMAGE = 2.0;
    private static final double DEFAULT_ATTACK_SPEED = 4.0;
    private static final double DEFAULT_MOVEMENT_SPEED = 0.1;
    private static final double DEFAULT_ARMOR = 0.0;
    private static final double DEFAULT_ARMOR_TOUGHNESS = 0.0;
    private static final double DEFAULT_KNOCKBACK_RESISTANCE = 0.0;
    private static final double DEFAULT_LUCK = 0.0;

    public ResetAttributesCommand(MyExperiencePlugin plugin, SkillEffectsHandler skillEffectsHandler) {
        this.plugin = plugin;
        this.skillEffectsHandler = skillEffectsHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Check if sender is a player or console
        if (!(sender instanceof Player) && args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Console must specify a player name.");
            return true;
        }

        // Handle target player
        Player target;
        if (args.length > 0) {
            // Check permission for resetting others
            if (sender instanceof Player && !sender.hasPermission("myplugin.resetattributes.others")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to reset other players' attributes.");
                return true;
            }

            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
                return true;
            }
        } else {
            target = (Player) sender;
        }

        // Check operation type
        boolean resetAll = args.length <= 1 || !args[1].equalsIgnoreCase("vanilla");

        // Reset attributes
        if (resetAll) {
            // Reset skill-related attributes via recalculation
            skillEffectsHandler.refreshPlayerStats(target);
        }

        // Reset vanilla attributes to default values
        resetVanillaAttributes(target);

        if (resetAll) {
            sender.sendMessage(ChatColor.GREEN + "Reset all attributes for " + target.getName() + ".");
            if (target != sender) {
                target.sendMessage(ChatColor.YELLOW + "Your attributes have been reset.");
            }
        } else {
            sender.sendMessage(ChatColor.GREEN + "Reset vanilla attributes for " + target.getName() + ".");
            if (target != sender) {
                target.sendMessage(ChatColor.YELLOW + "Your vanilla attributes have been reset.");
            }
        }

        return true;
    }

    private void resetVanillaAttributes(Player player) {
        // Reset all vanilla attributes to default values and clear ALL modifiers
        resetAttributeCompletely(player, Attribute.GENERIC_MAX_HEALTH, DEFAULT_MAX_HEALTH);
        resetAttributeCompletely(player, Attribute.GENERIC_ATTACK_DAMAGE, DEFAULT_ATTACK_DAMAGE);
        resetAttributeCompletely(player, Attribute.GENERIC_ATTACK_SPEED, DEFAULT_ATTACK_SPEED);
        resetAttributeCompletely(player, Attribute.GENERIC_MOVEMENT_SPEED, DEFAULT_MOVEMENT_SPEED);
        resetAttributeCompletely(player, Attribute.GENERIC_ARMOR, DEFAULT_ARMOR);
        resetAttributeCompletely(player, Attribute.GENERIC_ARMOR_TOUGHNESS, DEFAULT_ARMOR_TOUGHNESS);
        resetAttributeCompletely(player, Attribute.GENERIC_KNOCKBACK_RESISTANCE, DEFAULT_KNOCKBACK_RESISTANCE);
        resetAttributeCompletely(player, Attribute.GENERIC_LUCK, DEFAULT_LUCK);

        // Heal player to full after reset
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
    }

    private void setAttributeIfExists(Player player, Attribute attribute, double value) {
        if (player.getAttribute(attribute) != null) {
            player.getAttribute(attribute).setBaseValue(value);
        }
    }

    private void resetAttributeCompletely(Player player, Attribute attribute, double defaultValue) {
        if (player.getAttribute(attribute) != null) {
            // First, remove ALL modifiers
            player.getAttribute(attribute).getModifiers().forEach(modifier -> {
                player.getAttribute(attribute).removeModifier(modifier);
            });

            // Then set the base value to default
            player.getAttribute(attribute).setBaseValue(defaultValue);

            // Debug message
            plugin.getLogger().info("Reset " + attribute.name() + " for " + player.getName() +
                    " to " + defaultValue + ", removed " +
                    player.getAttribute(attribute).getModifiers().size() + " remaining modifiers");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> playerNames = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());

            if (args[0].isEmpty()) {
                return playerNames;
            }

            return playerNames.stream()
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            List<String> options = Arrays.asList("all", "vanilla");

            if (args[1].isEmpty()) {
                return options;
            }

            return options.stream()
                    .filter(option -> option.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
