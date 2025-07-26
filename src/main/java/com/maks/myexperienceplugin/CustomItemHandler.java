package com.maks.myexperienceplugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import net.milkbowl.vault.economy.Economy;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.NodeType;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.ItemMeta;

public class CustomItemHandler implements Listener {
    private final MyExperiencePlugin plugin;
    private final Economy economy;
    private final LuckPerms luckPerms;
    private final int debuggingFlag = 0; // Set to 1 for debugging, 0 for production

    public CustomItemHandler(MyExperiencePlugin plugin, Economy economy, LuckPerms luckPerms) {
        this.plugin = plugin;
        this.economy = economy;
        this.luckPerms = luckPerms;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Check if right-click with item
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Check if player has an item in hand
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return;
        }

        Player player = event.getPlayer();
        ItemMeta meta = item.getItemMeta();
        String displayName = meta.getDisplayName();

        // Debug information
        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[CustomItem] Player " + player.getName() + " right-clicked with item: " + displayName);
        }

        // Check if item has required level and if player meets it
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            for (String line : lore) {
                if (line.contains("Required Level")) {
                    Pattern pattern = Pattern.compile(".*Required Level.*\\s+(\\d+)");
                    Matcher matcher = pattern.matcher(ChatColor.stripColor(line));
                    if (matcher.find()) {
                        int requiredLevel = Integer.parseInt(matcher.group(1));
                        int playerLevel = plugin.getPlayerLevel(player);

                        if (playerLevel < requiredLevel) {
                            player.sendMessage("§cYou need to be level " + requiredLevel + " to use this item!");
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }

        // Process different item types
        if (displayName.contains("Gold Coin")) {
            handleGoldCoins(player, item, displayName);
            event.setCancelled(true);
        } else if (displayName.contains("Exp Package")) {
            handleExpPackage(player, item, displayName);
            event.setCancelled(true);
        } else if (displayName.contains("Premium Package")) {
            // Prevent using Premium if player has Deluxe
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null && hasPermissionGroup(user, "deluxe")) {
                player.sendMessage("§cYou already have Deluxe rank which is better than Premium!");
                event.setCancelled(true);
                return;
            }

            handlePremiumPackage(player, item, displayName);
            event.setCancelled(true);
        } else if (displayName.contains("Deluxe Package")) {
            handleDeluxePackage(player, item, displayName);
            event.setCancelled(true);
        } else if (displayName.contains("Exp Boost")) {
            handleExpBoost(player, item, displayName);
            event.setCancelled(true);
        }
    }

    private void handleGoldCoins(Player player, ItemStack item, String displayName) {
        // Extract amount from displayName (e.g. "§eGold Coin`s - 1k $" -> 1000)
        double amount = parseAmount(displayName);

        if (amount > 0) {
            // Deposit money to player's account
            economy.depositPlayer(player, amount);

            // Send success message
            player.sendMessage("§aYou received §6" + formatCurrency(amount) + "§a!");

            // Consume the item (subtract 1 from stack)
            consumeItem(player, item);

            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[CustomItem] Player " + player.getName() + " received " + amount + " currency");
            }
        } else {
            player.sendMessage("§cInvalid gold coin item!");
        }
    }

    private void handleExpPackage(Player player, ItemStack item, String displayName) {
        if (displayName.contains("%")) {
            // Handle percentage-based EXP
            double percentage = parsePercentage(displayName);
            if (percentage > 0) {
                // Get required XP for the next level
                int currentLevel = plugin.getPlayerLevel(player);
                double requiredXP = plugin.getXpPerLevel().getOrDefault(currentLevel, 100.0);
                double expToGive = (requiredXP * percentage) / 100.0;

                // Add experience
                plugin.addXP(player, expToGive);

                // Send success message
                player.sendMessage("§aYou received §6" + MyExperiencePlugin.formatNumber(expToGive) + " XP §a(" + percentage + "% of required XP)!");

                // Consume the item
                consumeItem(player, item);

                if (debuggingFlag == 1) {
                    Bukkit.getLogger().info("[CustomItem] Player " + player.getName() + " received " + expToGive +
                            " XP (" + percentage + "% of " + requiredXP + ")");
                }
            } else {
                player.sendMessage("§cInvalid EXP package!");
            }
        } else {
            // Handle fixed amount EXP
            double expAmount = parseAmount(displayName);
            if (expAmount > 0) {
                // Add experience
                plugin.addXP(player, expAmount);

                // Send success message
                player.sendMessage("§aYou received §6" + MyExperiencePlugin.formatNumber(expAmount) + " XP§a!");

                // Consume the item
                consumeItem(player, item);

                if (debuggingFlag == 1) {
                    Bukkit.getLogger().info("[CustomItem] Player " + player.getName() + " received " + expAmount + " XP");
                }
            } else {
                player.sendMessage("§cInvalid EXP package!");
            }
        }
    }

    private void handlePremiumPackage(Player player, ItemStack item, String displayName) {
        int days = parseDuration(displayName);
        if (days > 0) {
            // Check if player already has more than 90 days of Premium
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                long remainingDays = getRemainingDays(user, "premium");
                if (remainingDays >= 90) {
                    player.sendMessage("§cYou already have the maximum number of days for §6Premium§c rank (90 days). You cannot add more days!");
                    return;
                }
            }

            // Add Premium group for the specified duration
            addPermissionGroup(player, "premium", days);

            // Consume the item
            consumeItem(player, item);

            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[CustomItem] Player " + player.getName() + " received Premium rank for " + days + " days");
            }
        } else {
            player.sendMessage("§cInvalid Premium package!");
        }
    }

    private void handleDeluxePackage(Player player, ItemStack item, String displayName) {
        int days = parseDuration(displayName);
        if (days > 0) {
            // Check if player already has more than 90 days of Deluxe
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                long remainingDays = getRemainingDays(user, "deluxe");
                if (remainingDays >= 90) {
                    player.sendMessage("§cYou already have the maximum number of days for §4Deluxe§c rank (90 days). You cannot add more days!");
                    return;
                }
            }

            // Add Deluxe group for the specified duration
            addPermissionGroup(player, "deluxe", days);

            // Consume the item
            consumeItem(player, item);

            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[CustomItem] Player " + player.getName() + " received Deluxe rank for " + days + " days");
            }
        } else {
            player.sendMessage("§cInvalid Deluxe package!");
        }
    }

    private double parseAmount(String displayName) {
        // Extract numeric value with suffix (k, m, b)
        try {
            // Remove color codes and extract just the number and suffix part
            String stripped = ChatColor.stripColor(displayName);
            Pattern pattern = Pattern.compile(".*?-(\\s*)(\\d+[kmb]?)(\\s*\\$)?");
            Matcher matcher = pattern.matcher(stripped);

            if (matcher.find()) {
                String amountStr = matcher.group(2).trim().toLowerCase();
                if (amountStr.endsWith("k")) {
                    return Double.parseDouble(amountStr.substring(0, amountStr.length() - 1)) * 1_000;
                } else if (amountStr.endsWith("m") || amountStr.endsWith("kk")) {
                    return Double.parseDouble(amountStr.substring(0, amountStr.length() - 1)) * 1_000_000;
                } else if (amountStr.endsWith("b") || amountStr.endsWith("kkk")) {
                    return Double.parseDouble(amountStr.substring(0, amountStr.length() - 1)) * 1_000_000_000;
                } else {
                    return Double.parseDouble(amountStr);
                }
            }
        } catch (Exception e) {
            if (debuggingFlag == 1) {
                Bukkit.getLogger().warning("[CustomItem] Failed to parse amount from: " + displayName);
                e.printStackTrace();
            }
        }
        return 0;
    }

    private double parsePercentage(String displayName) {
        try {
            // Remove color codes and extract percentage value
            String stripped = ChatColor.stripColor(displayName);
            Pattern pattern = Pattern.compile(".*-(\\s*)(\\d+)(%)?");
            Matcher matcher = pattern.matcher(stripped);

            if (matcher.find()) {
                return Double.parseDouble(matcher.group(2));
            }
        } catch (Exception e) {
            if (debuggingFlag == 1) {
                Bukkit.getLogger().warning("[CustomItem] Failed to parse percentage from: " + displayName);
                e.printStackTrace();
            }
        }
        return 0;
    }

    private int parseDuration(String displayName) {
        try {
            // Remove color codes and extract duration value
            String stripped = ChatColor.stripColor(displayName);
            Pattern pattern = Pattern.compile(".*-(\\s*)(\\d+)d");
            Matcher matcher = pattern.matcher(stripped);

            if (matcher.find()) {
                return Integer.parseInt(matcher.group(2));
            }
        } catch (Exception e) {
            if (debuggingFlag == 1) {
                Bukkit.getLogger().warning("[CustomItem] Failed to parse duration from: " + displayName);
                e.printStackTrace();
            }
        }
        return 0;
    }

    // Alternatywne podejście używające innej metody LuckPerms API
    private void addPermissionGroup(Player player, String group, int daysToAdd) {
        try {
            // Get user from LuckPerms
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user == null) {
                Bukkit.getLogger().warning("User not found in LuckPerms: " + player.getName());
                return;
            }

            // Check if player already has Deluxe (no downgrade to Premium)
            if (group.equalsIgnoreCase("premium")) {
                if (hasPermissionGroup(user, "deluxe")) {
                    player.sendMessage("§cYou already have the Deluxe rank, which is better than Premium!");
                    return;
                }
            }

            // Check if player already has this rank and how many days are left
            long existingDaysLeft = getRemainingDays(user, group);
            boolean isExtending = existingDaysLeft > 0;

            // Calculate total days (existing + new)
            int totalDays = isExtending ? (int)(existingDaysLeft + daysToAdd) : daysToAdd;

            // Limit maximum number of days to 90
            final int finalDaysToAdd = Math.min(totalDays, 90);

            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("Adding group " + group + " for " + player.getName() + 
                    (isExtending ? " (extension from " + existingDaysLeft + " days)" : "") + 
                    " for a total of " + finalDaysToAdd + " days");
            }

            // Remove all existing permissions for this group
            user.data().clear(NodeType.INHERITANCE.predicate(n -> n.getGroupName().equalsIgnoreCase(group)));

            // Remove premium if we're adding deluxe
            if (group.equalsIgnoreCase("deluxe")) {
                user.data().clear(NodeType.INHERITANCE.predicate(n -> n.getGroupName().equalsIgnoreCase("premium")));
            }

            // Use LuckPerms API directly to add permission
            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("Adding group permission " + group + " for " + player.getName() + " via API");
            }

            // Create a task that will add permissions directly through the API
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    // Create a node with expiration time
                    InheritanceNode node = InheritanceNode.builder(group)
                            .expiry(finalDaysToAdd, java.util.concurrent.TimeUnit.DAYS)
                            .build();

                    // Add node to user
                    user.data().add(node);

                    // Save changes
                    luckPerms.getUserManager().saveUser(user);

                    if (debuggingFlag == 1) {
                        Bukkit.getLogger().info("Added group permission " + group + " for " + player.getName() + " for " + finalDaysToAdd + " days");
                    }

                    // Reload user to have up-to-date data
                    luckPerms.getUserManager().loadUser(player.getUniqueId()).thenAccept(reloadedUser -> {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            // Send message to player
                            String rankColor = group.equalsIgnoreCase("deluxe") ? "§4" : "§6";
                            String message;

                            if (isExtending) {
                                message = "§aYour " + rankColor + capitalizeFirstLetter(group) +
                                        "§a rank has been extended by §6" + daysToAdd + " days§a! It now expires in §6" + finalDaysToAdd + " days§a.";
                            } else {
                                message = "§aYou have received the " + rankColor + capitalizeFirstLetter(group) +
                                        "§a rank for §6" + finalDaysToAdd + " days§a!";
                            }

                            player.sendMessage(message);
                        });
                    });
                } catch (Exception e) {
                    if (debuggingFlag == 1) {
                        Bukkit.getLogger().severe("Error while adding permission: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            if (debuggingFlag == 1) {
                Bukkit.getLogger().severe("Error while adding rank: " + e.getMessage());
                e.printStackTrace();
            }
            player.sendMessage("§cAn error occurred while adding the rank. Please contact an administrator.");
        }
    }

    /**
     * Checks if a user has a specific permission group
     */
    private boolean hasPermissionGroup(User user, String group) {
        return user.getNodes(NodeType.INHERITANCE).stream()
                .anyMatch(node -> node.getGroupName().equalsIgnoreCase(group));
    }

    /**
     * Gets the remaining days from a timestamp
     */
    private long getRemainingDays(long expiryTimeMillis) {
        long now = System.currentTimeMillis();
        long diffMillis = expiryTimeMillis - now;

        // Convert to days and add 1 to include the current day
        return (diffMillis / (1000 * 60 * 60 * 24)) + 1;
    }

    /**
     * Gets the remaining days for a specific permission group
     */
    private long getRemainingDays(User user, String group) {
        return user.getNodes(NodeType.INHERITANCE).stream()
                .filter(node -> node.getGroupName().equalsIgnoreCase(group))
                .filter(node -> node.hasExpiry())
                .mapToLong(node -> getRemainingDays(node.getExpiry().toEpochMilli()))
                .max()
                .orElse(0);
    }

    /**
     * Capitalizes the first letter of a string
     */
    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    private void consumeItem(Player player, ItemStack item) {
        // Subtract 1 from item stack or remove if only 1 remains
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            // Find the slot with this item and set it to air
            if (player.getInventory().getItemInMainHand().equals(item)) {
                player.getInventory().setItemInMainHand(null);
            } else if (player.getInventory().getItemInOffHand().equals(item)) {
                player.getInventory().setItemInOffHand(null);
            }
        }
    }

    private String formatCurrency(double amount) {
        if (amount >= 1_000_000_000) {
            return String.format("%.2fB", amount / 1_000_000_000);
        } else if (amount >= 1_000_000) {
            return String.format("%.2fM", amount / 1_000_000);
        } else if (amount >= 1_000) {
            return String.format("%.2fK", amount / 1_000);
        } else {
            return String.format("%.2f", amount);
        }
    }

    private void handleExpBoost(Player player, ItemStack item, String displayName) {
        try {
            // Wyciągnij procent z nazwy itemu
            double boostPercent = 0;
            int hours = 6; // Domyślnie 6 godzin

            if (displayName.contains("+10%")) {
                boostPercent = 10.0;
            } else if (displayName.contains("+50%")) {
                boostPercent = 50.0;
            } else {
                // Spróbuj sparsować z nazwy - backup metoda
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\+(\\d+)%");
                java.util.regex.Matcher matcher = pattern.matcher(org.bukkit.ChatColor.stripColor(displayName));
                if (matcher.find()) {
                    boostPercent = Double.parseDouble(matcher.group(1));
                }
            }

            // Wyciągnij godziny z nazwy (jeśli różne od 6h)
            java.util.regex.Pattern hourPattern = java.util.regex.Pattern.compile("(\\d+)h");
            java.util.regex.Matcher hourMatcher = hourPattern.matcher(org.bukkit.ChatColor.stripColor(displayName));
            if (hourMatcher.find()) {
                hours = Integer.parseInt(hourMatcher.group(1));
            }

            if (boostPercent > 0) {
                // Dodaj exp boost dla gracza
                plugin.addExpBoost(player, boostPercent, hours);

                // Skonsumuj item
                consumeItem(player, item);

                if (debuggingFlag == 1) {
                    Bukkit.getLogger().info("[CustomItem] Player " + player.getName() + 
                            " activated " + boostPercent + "% EXP boost for " + hours + " hours");
                }
            } else {
                player.sendMessage("§cInvalid EXP boost item!");
                if (debuggingFlag == 1) {
                    Bukkit.getLogger().warning("[CustomItem] Could not parse EXP boost from: " + displayName);
                }
            }

        } catch (Exception e) {
            player.sendMessage("§cError activating EXP boost!");
            if (debuggingFlag == 1) {
                Bukkit.getLogger().severe("[CustomItem] Error handling EXP boost: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
