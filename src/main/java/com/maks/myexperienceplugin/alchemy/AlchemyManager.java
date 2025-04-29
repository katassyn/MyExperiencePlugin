package com.maks.myexperienceplugin.alchemy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AlchemyManager {
    public enum AlchemyCategory {
        ELIXIR, TONIC, PHYCIS, TOTEM
    }

    // Active effects: for each player (UUID) we store an effect in a given category
    private final Map<UUID, Map<AlchemyCategory, AlchemyEffect>> activeEffects = new ConcurrentHashMap<>();
    // Cooldowns: for each player, for each category - time (in ms) until player can use an effect again
    private final Map<UUID, Map<AlchemyCategory, Long>> cooldowns = new ConcurrentHashMap<>();
    // Effect expiry times: when an effect will expire
    private final Map<UUID, Map<AlchemyCategory, Long>> effectExpiryTimes = new ConcurrentHashMap<>();
    // Scheduled removal tasks for each effect
    private final Map<UUID, Map<AlchemyCategory, BukkitTask>> removalTasks = new ConcurrentHashMap<>();

    private static AlchemyManager instance;
    private JavaPlugin plugin;
    private File storageFile;
    private FileConfiguration storage;
    private static final int debuggingFlag = 1;

    private AlchemyManager() {}

    public static AlchemyManager getInstance() {
        if (instance == null) {
            instance = new AlchemyManager();
        }
        return instance;
    }

    public void initialize(JavaPlugin plugin) {
        this.plugin = plugin;
        this.storageFile = new File(plugin.getDataFolder(), "alchemy_data.yml");
        loadData();

        // Schedule a task to periodically save data
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::saveData, 1200L, 1200L); // Every minute

        // Schedule a task to check for expired effects every minute
        Bukkit.getScheduler().runTaskTimer(plugin, this::checkExpiredEffects, 1200L, 1200L);
    }

    /**
     * Check for any effects that have expired and remove them
     */
    private void checkExpiredEffects() {
        long now = System.currentTimeMillis();

        for (UUID uuid : new HashSet<>(effectExpiryTimes.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                Map<AlchemyCategory, Long> playerExpiryTimes = effectExpiryTimes.get(uuid);
                if (playerExpiryTimes != null) {
                    for (Map.Entry<AlchemyCategory, Long> entry : new HashMap<>(playerExpiryTimes).entrySet()) {
                        AlchemyCategory category = entry.getKey();
                        long expiryTime = entry.getValue();

                        if (now >= expiryTime) {
                            // Effect has expired, remove it
                            if (debuggingFlag == 1) {
                                Bukkit.getLogger().info("[DEBUG] Effect in category " + category +
                                        " for player " + player.getName() + " has expired, removing it");
                            }
                            removeEffect(player, category);
                        }
                    }
                }
            }
        }
    }

    /** Check if player can use an effect of a given category (cooldown has passed) */
    public boolean canApplyEffect(Player player, AlchemyCategory category) {
        long now = System.currentTimeMillis();
        Map<AlchemyCategory, Long> playerCooldowns = cooldowns.getOrDefault(player.getUniqueId(), new HashMap<>());
        long availableAt = playerCooldowns.getOrDefault(category, 0L);
        boolean canApply = now >= availableAt;

        if (!canApply) {
            long remaining = (availableAt - now) / 1000;
            player.sendMessage("§cCooldown: " + remaining + " seconds remaining");

            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[DEBUG] Player " + player.getName() + " can't use " +
                        category + " due to " + remaining + "s remaining cooldown");
            }
        }

        return canApply;
    }

    /**
     * Try to apply an effect. If player already has an active effect in this category or cooldown hasn't passed,
     * send an appropriate message and don't apply the effect.
     * @return true if effect was applied, false otherwise
     */
    public boolean applyEffect(Player player, AlchemyCategory category, AlchemyEffect effect) {
        UUID uuid = player.getUniqueId();

        // If already have an active effect in this category, don't allow another one.
        Map<AlchemyCategory, AlchemyEffect> playerEffects = activeEffects.get(uuid);
        if (playerEffects != null && playerEffects.containsKey(category)) {
            player.sendMessage("§cYou already have an active effect in this category. Wait until it ends.");
            return false;
        }

        // Check cooldown
        if (!canApplyEffect(player, category)) {
            player.sendMessage("§cYou must wait for the cooldown before using another effect in this category.");
            return false;
        }

        // Save and activate effect
        if (playerEffects == null) {
            playerEffects = new HashMap<>();
            activeEffects.put(uuid, playerEffects);
        }
        playerEffects.put(category, effect);

        // Apply the effect
        effect.apply();

        // Check if this is a basic healing potion (InstantHealingEffect with duration 0)
        // If so, remove it from activeEffects immediately after applying
        if (effect instanceof InstantHealingEffect && effect.getDuration() == 0) {
            InstantHealingEffect healingEffect = (InstantHealingEffect) effect;
            if (healingEffect.isBasicHealingPotion()) {
                if (debuggingFlag == 1) {
                    Bukkit.getLogger().info("[DEBUG] Removing basic healing potion effect for " + 
                            player.getName() + " immediately after applying");
                }
                // Remove from activeEffects but keep the cooldown
                playerEffects.remove(category);
                if (playerEffects.isEmpty()) {
                    activeEffects.remove(uuid);
                }
            }
        }

        // Record when this effect will expire (if it has a duration)
        if (effect.getDuration() > 0) {
            // Schedule a task to remove the effect after its duration
            Map<AlchemyCategory, BukkitTask> playerTasks = removalTasks.getOrDefault(uuid, new HashMap<>());

            // Cancel any existing task for this category
            if (playerTasks.containsKey(category)) {
                playerTasks.get(category).cancel();
            }

            // Schedule new removal task
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin,
                    () -> removeEffect(player, category), effect.getDuration() / 50);
            playerTasks.put(category, task);
            removalTasks.put(uuid, playerTasks);

            // Track expiry time
            Map<AlchemyCategory, Long> playerExpiryTimes = effectExpiryTimes.getOrDefault(uuid, new HashMap<>());
            long expiryTime = System.currentTimeMillis() + effect.getDuration();
            playerExpiryTimes.put(category, expiryTime);
            effectExpiryTimes.put(uuid, playerExpiryTimes);

            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[DEBUG] Effect " + effect.getEffectName() + " for player " +
                        player.getName() + " will expire at " + expiryTime + " (in " + (effect.getDuration()/1000) + "s)");
            }
        }

        // Set cooldown - counted from when effect is used
        Map<AlchemyCategory, Long> playerCooldowns = cooldowns.getOrDefault(uuid, new HashMap<>());
        playerCooldowns.put(category, System.currentTimeMillis() + effect.getCooldownMillis());
        cooldowns.put(uuid, playerCooldowns);

        // Save data when an effect is applied
        saveData();

        return true;
    }

    /** Remove effect in a given category for a player (e.g., after it ends) */
    public void removeEffect(Player player, AlchemyCategory category) {
        UUID uuid = player.getUniqueId();
        Map<AlchemyCategory, AlchemyEffect> playerEffects = activeEffects.get(uuid);
        if (playerEffects != null && playerEffects.containsKey(category)) {
            AlchemyEffect effect = playerEffects.get(category);

            try {
                effect.remove();
            } catch (Exception e) {
                if (debuggingFlag == 1) {
                    Bukkit.getLogger().warning("[DEBUG] Error removing effect for " + player.getName() +
                            ": " + e.getMessage());
                }
            }

            playerEffects.remove(category);
            if (playerEffects.isEmpty()) {
                activeEffects.remove(uuid);
            }

            // Also remove from expiry times
            Map<AlchemyCategory, Long> playerExpiryTimes = effectExpiryTimes.get(uuid);
            if (playerExpiryTimes != null) {
                playerExpiryTimes.remove(category);
                if (playerExpiryTimes.isEmpty()) {
                    effectExpiryTimes.remove(uuid);
                }
            }

            // Cancel any scheduled removal task
            Map<AlchemyCategory, BukkitTask> playerTasks = removalTasks.get(uuid);
            if (playerTasks != null && playerTasks.containsKey(category)) {
                try {
                    playerTasks.get(category).cancel();
                } catch (Exception e) {
                    // Task might already be completed
                }
                playerTasks.remove(category);
                if (playerTasks.isEmpty()) {
                    removalTasks.remove(uuid);
                }
            }

            // Save data when an effect is removed
            saveData();
        }
    }

    /**
     * Get the cooldowns for a player
     * @param player The player to get cooldowns for
     * @return A map of category to cooldown end time (milliseconds since epoch)
     */
    public Map<AlchemyCategory, Long> getPlayerCooldowns(Player player) {
        return cooldowns.getOrDefault(player.getUniqueId(), new HashMap<>());
    }

    public void clearEffect(Player player, AlchemyCategory category) {
        UUID uuid = player.getUniqueId();
        Map<AlchemyCategory, AlchemyEffect> playerEffects = activeEffects.get(uuid);
        if (playerEffects != null && playerEffects.containsKey(category)) {
            AlchemyEffect effect = playerEffects.get(category);

            // Call remove() on the effect to properly clean up
            try {
                effect.remove();
                if (debuggingFlag == 1) {
                    Bukkit.getLogger().info("[DEBUG] Successfully removed effect for " + player.getName() +
                            " in category " + category);
                }
            } catch (Exception e) {
                if (debuggingFlag == 1) {
                    Bukkit.getLogger().warning("[DEBUG] Error removing effect for " + player.getName() +
                            ": " + e.getMessage());
                }
            }

            playerEffects.remove(category);
            if (playerEffects.isEmpty()) {
                activeEffects.remove(uuid);
            }

            // Also remove from expiry times
            Map<AlchemyCategory, Long> playerExpiryTimes = effectExpiryTimes.get(uuid);
            if (playerExpiryTimes != null) {
                playerExpiryTimes.remove(category);
                if (playerExpiryTimes.isEmpty()) {
                    effectExpiryTimes.remove(uuid);
                }
            }

            // Cancel any scheduled removal task
            Map<AlchemyCategory, BukkitTask> playerTasks = removalTasks.get(uuid);
            if (playerTasks != null && playerTasks.containsKey(category)) {
                try {
                    playerTasks.get(category).cancel();
                } catch (Exception e) {
                    // Task might already be completed
                }
                playerTasks.remove(category);
                if (playerTasks.isEmpty()) {
                    removalTasks.remove(uuid);
                }
            }

            // Save data after removing an effect
            saveData();
        }
    }

    /**
     * Clears all cooldowns for a player
     */
    public void clearCooldowns(Player player) {
        UUID uuid = player.getUniqueId();
        cooldowns.remove(uuid);
        saveData();
    }

    /**
     * Checks and restores effects for a player on login
     */
    public void checkAndRestoreEffects(Player player) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        // Check if player already has active effects in memory (should not happen, but just in case)
        if (activeEffects.containsKey(uuid)) {
            if (debuggingFlag == 1) {
                Bukkit.getLogger().warning("[DEBUG] Player " + player.getName() +
                        " already has active effects in memory. Removing them first.");
            }

            // Remove all active effects
            Map<AlchemyCategory, AlchemyEffect> playerEffects = activeEffects.get(uuid);
            for (Map.Entry<AlchemyCategory, AlchemyEffect> entry : new HashMap<>(playerEffects).entrySet()) {
                removeEffect(player, entry.getKey());
            }
        }

        // Check expiry times and restore effects that are still valid
        Map<AlchemyCategory, Long> playerExpiryTimes = effectExpiryTimes.get(uuid);
        if (playerExpiryTimes != null) {
            for (Map.Entry<AlchemyCategory, Long> entry : new HashMap<>(playerExpiryTimes).entrySet()) {
                AlchemyCategory category = entry.getKey();
                long expiryTime = entry.getValue();

                if (now < expiryTime) {
                    // Effect should still be active
                    String effectKey = storage.getString("effects." + uuid + "." + category);
                    if (effectKey != null) {
                        // Check if this effect is already in the activeEffects map to prevent duplication
                        Map<AlchemyCategory, AlchemyEffect> existingEffects = activeEffects.getOrDefault(uuid, new HashMap<>());
                        if (existingEffects.containsKey(category)) {
                            if (debuggingFlag == 1) {
                                Bukkit.getLogger().info("[DEBUG] Effect for category " + category + 
                                        " already exists for player " + player.getName() + ". Skipping restoration.");
                            }
                            continue;
                        }

                        AlchemyEffect effect = AlchemyEffectFactory.createEffect(effectKey, player);
                        if (effect != null) {
                            // Calculate remaining duration
                            long remainingDuration = expiryTime - now;

                            if (debuggingFlag == 1) {
                                Bukkit.getLogger().info("[DEBUG] Restoring effect " + effectKey +
                                        " for player " + player.getName() + " with " + remainingDuration/1000 + "s remaining");
                            }

                            // Apply the effect manually
                            Map<AlchemyCategory, AlchemyEffect> playerEffects = activeEffects.getOrDefault(uuid, new HashMap<>());
                            playerEffects.put(category, effect);
                            activeEffects.put(uuid, playerEffects);

                            // Apply the effect
                            effect.apply();

                            // Schedule removal task for the remaining duration
                            Map<AlchemyCategory, BukkitTask> playerTasks = removalTasks.getOrDefault(uuid, new HashMap<>());
                            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin,
                                    () -> removeEffect(player, category), remainingDuration / 50);
                            playerTasks.put(category, task);
                            removalTasks.put(uuid, playerTasks);
                        }
                    }
                } else {
                    // Effect has expired, clean it up
                    playerExpiryTimes.remove(category);
                    storage.set("effects." + uuid + "." + category, null);

                    if (debuggingFlag == 1) {
                        Bukkit.getLogger().info("[DEBUG] Removing expired effect in category " +
                                category + " for player " + player.getName());
                    }
                }
            }

            if (playerExpiryTimes.isEmpty()) {
                effectExpiryTimes.remove(uuid);
                storage.set("effect_expiry." + uuid, null);
            }
        }

        // Save after potential cleanup
        saveData();
    }

    /**
     * Called when a player disconnects
     */
    public void handlePlayerDisconnect(Player player) {
        UUID uuid = player.getUniqueId();

        // Cancel all scheduled tasks but keep the effects in memory
        Map<AlchemyCategory, BukkitTask> playerTasks = removalTasks.get(uuid);
        if (playerTasks != null) {
            for (BukkitTask task : playerTasks.values()) {
                try {
                    task.cancel();
                } catch (Exception e) {
                    // Task might already be completed
                }
            }
            removalTasks.remove(uuid);
        }

        // Save data when player disconnects
        saveData();
    }

    private void loadData() {
        if (!storageFile.exists()) {
            try {
                storageFile.getParentFile().mkdirs();
                storageFile.createNewFile();
            } catch (IOException e) {
                if (plugin != null) {
                    plugin.getLogger().severe("Could not create alchemy_data.yml: " + e.getMessage());
                }
                return;
            }
        }

        storage = YamlConfiguration.loadConfiguration(storageFile);

        // Load cooldowns
        if (storage.contains("cooldowns")) {
            for (String uuidStr : storage.getConfigurationSection("cooldowns").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    Map<AlchemyCategory, Long> playerCooldowns = cooldowns.getOrDefault(uuid, new HashMap<>());

                    for (String categoryStr : storage.getConfigurationSection("cooldowns." + uuidStr).getKeys(false)) {
                        try {
                            AlchemyCategory category = AlchemyCategory.valueOf(categoryStr);
                            long cooldownExpiry = storage.getLong("cooldowns." + uuidStr + "." + categoryStr);
                            playerCooldowns.put(category, cooldownExpiry);
                        } catch (IllegalArgumentException e) {
                            if (debuggingFlag == 1) {
                                Bukkit.getLogger().warning("[DEBUG] Invalid category in cooldowns: " + categoryStr);
                            }
                        }
                    }

                    cooldowns.put(uuid, playerCooldowns);
                } catch (IllegalArgumentException e) {
                    if (debuggingFlag == 1) {
                        Bukkit.getLogger().warning("[DEBUG] Invalid UUID in cooldowns: " + uuidStr);
                    }
                }
            }
        }

        // Load effect expiry times
        if (storage.contains("effect_expiry")) {
            for (String uuidStr : storage.getConfigurationSection("effect_expiry").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    Map<AlchemyCategory, Long> playerExpiryTimes = effectExpiryTimes.getOrDefault(uuid, new HashMap<>());

                    for (String categoryStr : storage.getConfigurationSection("effect_expiry." + uuidStr).getKeys(false)) {
                        try {
                            AlchemyCategory category = AlchemyCategory.valueOf(categoryStr);
                            long expiryTime = storage.getLong("effect_expiry." + uuidStr + "." + categoryStr);
                            playerExpiryTimes.put(category, expiryTime);
                        } catch (IllegalArgumentException e) {
                            if (debuggingFlag == 1) {
                                Bukkit.getLogger().warning("[DEBUG] Invalid category in expiry times: " + categoryStr);
                            }
                        }
                    }

                    effectExpiryTimes.put(uuid, playerExpiryTimes);
                } catch (IllegalArgumentException e) {
                    if (debuggingFlag == 1) {
                        Bukkit.getLogger().warning("[DEBUG] Invalid UUID in expiry times: " + uuidStr);
                    }
                }
            }
        }
    }

    public void saveData() {
        if (plugin == null || storage == null) return;

        // Save cooldowns
        for (UUID uuid : cooldowns.keySet()) {
            Map<AlchemyCategory, Long> playerCooldowns = cooldowns.get(uuid);
            for (AlchemyCategory category : playerCooldowns.keySet()) {
                storage.set("cooldowns." + uuid + "." + category, playerCooldowns.get(category));
            }
        }

        // Save effect expiry times
        for (UUID uuid : effectExpiryTimes.keySet()) {
            Map<AlchemyCategory, Long> playerExpiryTimes = effectExpiryTimes.get(uuid);
            for (AlchemyCategory category : playerExpiryTimes.keySet()) {
                storage.set("effect_expiry." + uuid + "." + category, playerExpiryTimes.get(category));
            }
        }

        // Save active effects (just the keys, not the objects)
        for (UUID uuid : activeEffects.keySet()) {
            Map<AlchemyCategory, AlchemyEffect> playerEffects = activeEffects.get(uuid);
            for (AlchemyCategory category : playerEffects.keySet()) {
                AlchemyEffect effect = playerEffects.get(category);
                // Store a key that can be used to recreate the effect
                String effectKey = getEffectKey(effect);
                if (effectKey != null) {
                    storage.set("effects." + uuid + "." + category, effectKey);
                }
            }
        }

        // Save to file
        try {
            storage.save(storageFile);
            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[DEBUG] Saved alchemy data to " + storageFile.getPath());
            }
        } catch (IOException e) {
            if (plugin != null) {
                plugin.getLogger().severe("Could not save alchemy_data.yml: " + e.getMessage());
            }
        }
    }

    private String getEffectKey(AlchemyEffect effect) {
        if (effect == null) return null;

        String effectName = effect.getEffectName().toLowerCase();

        if (effectName.contains("damage")) {
            if (effectName.contains("tonic")) {
                if (effectName.contains("[i]")) return "tonicdmg_inf";
                if (effectName.contains("[ii]")) return "tonicdmg_hell";
                if (effectName.contains("[iii]")) return "tonicdmg_blood";
            } else if (effectName.contains("phycis")) {
                if (effectName.contains("basic")) return "phycisdmg_inf";
                if (effectName.contains("great")) return "phycisdmg_hell";
                if (effectName.contains("excellent")) return "phycisdmg_blood";
            }
        } else if (effectName.contains("health") || effectName.contains("healing")) {
            if (effectName.contains("tonic")) {
                if (effectName.contains("[i]")) return "tonichp_inf";
                if (effectName.contains("[ii]")) return "tonichp_hell";
                if (effectName.contains("[iii]")) return "tonichp_blood";
            } else if (effectName.contains("phycis")) {
                if (effectName.contains("basic")) return "phycishp_inf";
                if (effectName.contains("great")) return "phycishp_hell";
                if (effectName.contains("excellent")) return "phycishp_blood";
            } else if (effectName.contains("potion")) {
                if (effectName.contains("small")) return "potionheal_inf";
                if (effectName.contains("medium")) return "potionheal_hell";
                if (effectName.contains("large")) return "potionheal_blood";
            }
        } else if (effectName.contains("experience") || effectName.contains("exp")) {
            if (effectName.contains("tonic")) {
                if (effectName.contains("[i]")) return "tonicexp_inf";
                if (effectName.contains("[ii]")) return "tonicexp_hell";
                if (effectName.contains("[iii]")) return "tonicexp_blood";
            } else if (effectName.contains("phycis")) {
                if (effectName.contains("basic")) return "phycisexp_inf";
                if (effectName.contains("great")) return "phycisexp_hell";
                if (effectName.contains("excellent")) return "phycisexp_blood";
            }
        }

        // Continue with other effect types as needed

        return null;
    }

    public interface AlchemyEffectWithDuration {
        long getDuration();
    }
}
