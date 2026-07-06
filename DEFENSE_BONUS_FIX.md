# Defense Bonus Accumulation Fix

## Issue Identified

After analyzing the code, I've identified several issues that are causing defense bonuses to accumulate to unrealistic values (over 400%):

1. **Low Health Defense** (ID 6) adds a defense bonus when health < 50% but doesn't remove it when health > 50%
2. **Desperate Defense** (ID 25) only updates when player takes damage, not when health changes for other reasons
3. Many defense bonuses are not properly tracked in a central location, making it difficult to manage them

## Proposed Solution

### 1. Track All Defense Bonuses

Create a comprehensive tracking system for all defense bonuses:

```java
// In ScaleGuardianSkillEffectsHandler.java
// Expand the playerDefenseModifier map to track bonuses by source
private final Map<UUID, Map<String, Double>> playerDefenseBonusesBySource = new ConcurrentHashMap<>();

// Helper method to add a defense bonus with source tracking
private void addDefenseBonusWithSource(Player player, String source, double value) {
    UUID playerId = player.getUniqueId();
    
    // Initialize the map if needed
    playerDefenseBonusesBySource.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
    
    // Get current stats
    SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
    
    // Get previous value for this source if any
    Double previousValue = playerDefenseBonusesBySource.get(playerId).getOrDefault(source, 0.0);
    
    // Remove previous value
    if (previousValue != 0) {
        stats.addDefenseBonus(-previousValue);
        if (debuggingFlag == 1) {
            plugin.getLogger().info("Removed previous " + source + " defense bonus of " + 
                    previousValue + "% for " + player.getName());
        }
    }
    
    // Add new value
    if (value != 0) {
        stats.addDefenseBonus(value);
        playerDefenseBonusesBySource.get(playerId).put(source, value);
        
        if (debuggingFlag == 1) {
            plugin.getLogger().info("Applied " + source + " defense bonus of " + 
                    value + "% for " + player.getName());
        }
    } else {
        // If value is 0, remove the entry
        playerDefenseBonusesBySource.get(playerId).remove(source);
    }
}

// Helper method to remove a defense bonus by source
private void removeDefenseBonusBySource(Player player, String source) {
    addDefenseBonusWithSource(player, source, 0);
}

// Helper method to get total defense bonus for a player
private double getTotalDefenseBonus(UUID playerId) {
    if (!playerDefenseBonusesBySource.containsKey(playerId)) {
        return 0.0;
    }
    
    return playerDefenseBonusesBySource.get(playerId).values().stream()
            .mapToDouble(Double::doubleValue)
            .sum();
}
```

### 2. Update Low Health Defense Implementation

Modify the Low Health Defense implementation to properly add and remove the bonus:

```java
// Check for Low Health Defense (ID 6)
if (isPurchased(playerId, ID_OFFSET + 6)) {
    // Get purchase count for stacking effect
    int purchaseCount = getPurchaseCount(playerId, ID_OFFSET + 6);
    
    // Calculate bonus
    double bonus = healthPercent < 50 ? 15 * purchaseCount : 0;
    
    // Add or remove bonus
    addDefenseBonusWithSource(player, "LowHealthDefense", bonus);
    
    if (debuggingFlag == 1 && bonus > 0) {
        plugin.getLogger().info("Low Health Defense activated for " + player.getName() + 
                " at " + healthPercent + "% health (+" + bonus + "% defense)");
    }
}
```

### 3. Update Desperate Defense Implementation

Modify the Desperate Defense implementation to use the new tracking system:

```java
// Check for Desperate Defense (ID 25)
if (isPurchased(playerId, ID_OFFSET + 25)) {
    // Calculate missing health percentage
    double missingHealthPercent = 100 - healthPercent;
    
    // Cap at 30%
    double damageReduction = Math.min(missingHealthPercent, 30);
    
    // Add or update bonus
    addDefenseBonusWithSource(player, "DesperateDefense", damageReduction);
    
    if (debuggingFlag == 1) {
        plugin.getLogger().info("Desperate Defense activated for " + player.getName() + 
                " at " + healthPercent + "% health (" + damageReduction + "% damage reduction)");
    }
}
```

### 4. Update Proximity Defense Implementation

Modify the Proximity Defense implementation to use the new tracking system:

```java
public void checkProximityDefense(Player player) {
    UUID playerId = player.getUniqueId();
    
    // Skip if player doesn't have the skill
    if (!isPurchased(playerId, ID_OFFSET + 3)) return;
    
    // Count nearby enemies
    int nearbyEnemies = 0;
    for (Entity entity : player.getNearbyEntities(8, 8, 8)) {
        if (entity instanceof LivingEntity && !(entity instanceof Player)) {
            nearbyEnemies++;
        }
    }
    
    // Cap at 5 enemies (for max 15% bonus)
    nearbyEnemies = Math.min(nearbyEnemies, 5);
    
    // Calculate defense bonus
    double defenseBonus = nearbyEnemies * 3;
    
    // Add or update bonus
    addDefenseBonusWithSource(player, "ProximityDefense", defenseBonus);
    
    if (debuggingFlag == 1 && nearbyEnemies > 0) {
        plugin.getLogger().info("Proximity Defense activated for " + player.getName() + 
                " (+" + defenseBonus + "% defense from " + nearbyEnemies + " nearby enemies)");
        player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Proximity Defense: +" + 
                defenseBonus + "% defense (" + nearbyEnemies + " enemies)");
    }
}
```

### 5. Update Stationary Defense Implementation

Modify the Stationary Defense implementation to use the new tracking system:

```java
// In checkStationaryPlayers method
// When applying stationary defense:
if (!isStationary.getOrDefault(playerId, false)) {
    // Get purchase count for stacking effect
    int purchaseCount = getPurchaseCount(playerId, ID_OFFSET + 8);
    
    // Calculate bonus
    double bonus = 10 * purchaseCount;
    
    // Add bonus
    addDefenseBonusWithSource(player, "StationaryDefense", bonus);
    
    // Set stationary flag
    isStationary.put(playerId, true);
    
    // Show notification
    ActionBarUtils.sendActionBar(player, ChatColor.AQUA + "Stationary Defense! +" + 
            bonus + "% Defense");
    
    if (debuggingFlag == 1) {
        plugin.getLogger().info("Stationary Defense activated for " + player.getName() + 
                " (+" + bonus + "% defense)");
        player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Stationary Defense activated!");
    }
}

// When removing stationary defense:
if (isStationary.getOrDefault(playerId, false)) {
    // Remove bonus
    removeDefenseBonusBySource(player, "StationaryDefense");
    
    // Reset stationary flag
    isStationary.put(playerId, false);
    
    if (debuggingFlag == 1) {
        plugin.getLogger().info("Stationary Defense deactivated for " + player.getName());
        player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Stationary Defense deactivated");
    }
}
```

### 6. Update Reactive Defense Implementation

Modify the Reactive Defense implementation to use the new tracking system:

```java
// Check for Reactive Defense (ID 15)
if (isPurchased(playerId, ID_OFFSET + 15) && damagePercent >= 20) {
    // Apply defense bonus
    addDefenseBonusWithSource(player, "ReactiveDefense", 30);
    
    // Set expiry
    reactiveDefenseExpiry.put(playerId, System.currentTimeMillis() + REACTIVE_DEFENSE_DURATION);
    
    // Cancel existing task
    BukkitTask existingTask = reactiveDefenseTasks.get(playerId);
    if (existingTask != null) {
        existingTask.cancel();
    }
    
    // Schedule buff removal
    BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
        // Check if buff is still active
        Long expiry = reactiveDefenseExpiry.get(playerId);
        if (expiry != null && System.currentTimeMillis() >= expiry) {
            // Remove the buff
            removeDefenseBonusBySource(player, "ReactiveDefense");
            
            reactiveDefenseExpiry.remove(playerId);
            reactiveDefenseTasks.remove(playerId);
            
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Reactive Defense expired for " + player.getName());
                if (player.isOnline()) {
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Reactive Defense expired");
                }
            }
        }
    }, REACTIVE_DEFENSE_DURATION / 50); // Convert ms to ticks
    
    reactiveDefenseTasks.put(playerId, task);
    
    // Show notification
    ActionBarUtils.sendActionBar(player, ChatColor.AQUA + "Reactive Defense! +30% Defense");
    
    if (debuggingFlag == 1) {
        plugin.getLogger().info("Reactive Defense activated for " + player.getName() + " from " + damagePercent + "% damage hit");
        player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Reactive Defense activated!");
    }
}
```

### 7. Update Last Resort Implementation

Modify the Last Resort implementation to use the new tracking system:

```java
// Check for Last Resort (ID 27)
if (isPurchased(playerId, ID_OFFSET + 27) && healthPercent <= 10 &&
        !isOnCooldown(playerId, lastResortCooldown, LAST_RESORT_COOLDOWN)) {
    
    // Check if Last Resort is already active
    if (lastResortExpiry.containsKey(playerId)) {
        // Already active, just extend the duration
        lastResortExpiry.put(playerId, System.currentTimeMillis() + LAST_RESORT_DURATION);
        
        if (debuggingFlag == 1) {
            plugin.getLogger().info("Last Resort duration extended for " + player.getName());
            player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Last Resort duration extended");
        }
    } else {
        // Apply damage reduction
        addDefenseBonusWithSource(player, "LastResort", 70);
        
        // Set expiry
        lastResortExpiry.put(playerId, System.currentTimeMillis() + LAST_RESORT_DURATION);
        
        // Show notification
        ActionBarUtils.sendActionBar(player, ChatColor.AQUA + "Last Resort Activated! 70% Damage Reduction");
        
        if (debuggingFlag == 1) {
            plugin.getLogger().info("Last Resort activated for " + player.getName() + " at " + healthPercent + "% health");
            player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Last Resort activated!");
        }
    }
    
    // Set cooldown
    lastResortCooldown.put(playerId, System.currentTimeMillis());
    
    // Cancel existing task
    BukkitTask existingTask = lastResortTasks.get(playerId);
    if (existingTask != null) {
        existingTask.cancel();
    }
    
    // Schedule buff removal
    BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
        // Check if buff is still active
        Long expiry = lastResortExpiry.get(playerId);
        if (expiry != null && System.currentTimeMillis() >= expiry) {
            // Remove the buff
            removeDefenseBonusBySource(player, "LastResort");
            
            lastResortExpiry.remove(playerId);
            lastResortTasks.remove(playerId);
            
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Last Resort expired for " + player.getName());
                if (player.isOnline()) {
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Last Resort expired");
                }
            }
        }
    }, LAST_RESORT_DURATION / 50); // Convert ms to ticks
    
    lastResortTasks.put(playerId, task);
}
```

### 8. Add Periodic Health Check

Add a periodic task to check player health and update health-based defense bonuses:

```java
// In constructor
Bukkit.getScheduler().runTaskTimer(plugin, this::checkPlayerHealth, 20, 20); // Check every second

// Add method to check player health
private void checkPlayerHealth() {
    for (Player player : Bukkit.getOnlinePlayers()) {
        UUID playerId = player.getUniqueId();
        
        // Skip if player doesn't have any relevant skills
        if (!isPurchased(playerId, ID_OFFSET + 6) && !isPurchased(playerId, ID_OFFSET + 25)) {
            continue;
        }
        
        // Get current health percentage
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double currentHealth = player.getHealth();
        double healthPercent = (currentHealth / maxHealth) * 100;
        
        // Update Low Health Defense
        if (isPurchased(playerId, ID_OFFSET + 6)) {
            int purchaseCount = getPurchaseCount(playerId, ID_OFFSET + 6);
            double bonus = healthPercent < 50 ? 15 * purchaseCount : 0;
            addDefenseBonusWithSource(player, "LowHealthDefense", bonus);
        }
        
        // Update Desperate Defense
        if (isPurchased(playerId, ID_OFFSET + 25)) {
            double missingHealthPercent = 100 - healthPercent;
            double damageReduction = Math.min(missingHealthPercent, 30);
            addDefenseBonusWithSource(player, "DesperateDefense", damageReduction);
        }
    }
}
```

### 9. Update clearPlayerData Method

Update the clearPlayerData method to clear all defense bonuses:

```java
public void clearPlayerData(UUID playerId) {
    // Clear all maps
    playerDefenseModifier.remove(playerId);
    playerDamageModifier.remove(playerId);
    playerHealingModifier.remove(playerId);
    
    // Clear defense bonuses by source
    playerDefenseBonusesBySource.remove(playerId);
    
    // Clear Desperate Defense values
    desperateDefenseValues.remove(playerId);
    
    // Rest of the method remains the same...
}
```

### 10. Add Debug Command

Add a debug command to display all active defense bonuses for a player:

```java
// In MyExperiencePlugin.java
@Override
public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (command.getName().equalsIgnoreCase("debugdefense")) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players");
            return true;
        }
        
        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();
        
        // Get the ScaleGuardian handler
        ScaleGuardianSkillEffectsHandler handler = (ScaleGuardianSkillEffectsHandler) 
                skillEffectsHandler.getHandlerForPlayer(player);
        
        if (handler == null) {
            player.sendMessage("No handler found for your class");
            return true;
        }
        
        // Display all defense bonuses
        handler.displayDefenseBonuses(player);
        return true;
    }
    
    // Handle other commands...
}

// In ScaleGuardianSkillEffectsHandler.java
public void displayDefenseBonuses(Player player) {
    UUID playerId = player.getUniqueId();
    
    player.sendMessage(ChatColor.GOLD + "=== Defense Bonuses ===");
    
    if (!playerDefenseBonusesBySource.containsKey(playerId)) {
        player.sendMessage(ChatColor.GRAY + "No active defense bonuses");
        return;
    }
    
    Map<String, Double> bonuses = playerDefenseBonusesBySource.get(playerId);
    
    for (Map.Entry<String, Double> entry : bonuses.entrySet()) {
        player.sendMessage(ChatColor.YELLOW + entry.getKey() + ": " + 
                ChatColor.WHITE + "+" + entry.getValue() + "%");
    }
    
    double total = getTotalDefenseBonus(playerId);
    player.sendMessage(ChatColor.GOLD + "Total: " + ChatColor.WHITE + "+" + total + "%");
}
```

## Expected Results

These changes should:

1. Properly track all defense bonuses by source
2. Ensure bonuses are correctly added and removed based on conditions
3. Prevent defense bonuses from accumulating to unrealistic values
4. Provide better debugging tools to identify issues

The key improvement is the centralized tracking of all defense bonuses by source, which allows us to properly manage them and prevent accumulation.