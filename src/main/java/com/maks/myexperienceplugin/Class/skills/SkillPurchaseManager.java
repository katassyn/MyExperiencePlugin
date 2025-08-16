package com.maks.myexperienceplugin.Class.skills;

import com.maks.myexperienceplugin.Class.skills.base.SkillNode;
import com.maks.myexperienceplugin.Class.skills.base.SkillTree;
import com.maks.myexperienceplugin.Class.skills.effects.ascendancy.BeastmasterSkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.gui.AscendancySkillTreeGUI;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Central manager for all skill purchases to prevent duplicate/multiple purchases
 * and handle synchronization issues.
 */
public class SkillPurchaseManager {
    private final MyExperiencePlugin plugin;
    private final SkillTreeManager skillTreeManager;
    private final SkillTreeGUI skillTreeGUI;
    private final AscendancySkillTreeGUI ascendancySkillTreeGUI;

    // Strong synchronization with explicit locks for each player
    private final Map<UUID, ReentrantLock> playerLocks = new ConcurrentHashMap<>();

    // Tracking pending purchase tasks by player
    private final Map<UUID, BukkitTask> pendingTasks = new ConcurrentHashMap<>();

    // Tracking when a player's last purchase completed
    private final Map<UUID, Long> lastPurchaseTime = new ConcurrentHashMap<>();

    // Cooldown configuration
    private static final long PURCHASE_COOLDOWN = 200; // 2 seconds

    // Debugging
    private final int debuggingFlag = 0;

    public SkillPurchaseManager(MyExperiencePlugin plugin,
                                SkillTreeManager skillTreeManager,
                                SkillTreeGUI skillTreeGUI,
                                AscendancySkillTreeGUI ascendancySkillTreeGUI) {
        this.plugin = plugin;
        this.skillTreeManager = skillTreeManager;
        this.skillTreeGUI = skillTreeGUI;
        this.ascendancySkillTreeGUI = ascendancySkillTreeGUI;
    }

    /**
     * Request a basic skill purchase. This method guarantees that only one purchase
     * will be processed at a time for each player.
     */
    public void requestSkillPurchase(Player player, int nodeId) {
        UUID uuid = player.getUniqueId();

        // Check cooldown first - fastest check
        long currentTime = System.currentTimeMillis();
        if (lastPurchaseTime.containsKey(uuid) &&
                currentTime - lastPurchaseTime.get(uuid) < PURCHASE_COOLDOWN) {
            if (debuggingFlag == 1) {
                plugin.getLogger().warning("BLOCKED (Cooldown): " + player.getName() +
                        " attempted to purchase skill " + nodeId + " too quickly");
            }
            player.sendMessage(ChatColor.RED + "Please wait before purchasing another skill.");
            return;
        }

        // Try to acquire lock
        ReentrantLock lock = playerLocks.computeIfAbsent(uuid, k -> new ReentrantLock());
        if (!lock.tryLock()) {
            if (debuggingFlag == 1) {
                plugin.getLogger().warning("BLOCKED (Lock): " + player.getName() +
                        " attempted concurrent skill purchase " + nodeId);
            }
            player.sendMessage(ChatColor.RED + "Another skill purchase is in progress.");
            return;
        }

        try {
            // Cancel any pending purchase tasks
            cancelPendingTasks(uuid);

            // Schedule the purchase at a later tick
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                processSkillPurchase(player, nodeId);
                pendingTasks.remove(uuid);
            }, 5L); // 5 ticks (250ms) delay to bundle multiple clicks

            pendingTasks.put(uuid, task);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("SCHEDULED: Skill purchase for " + player.getName() +
                        ", skill " + nodeId);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Request an ascendancy skill purchase.
     */
    public void requestAscendancyPurchase(Player player, int nodeId, int branchIndex) {
        UUID uuid = player.getUniqueId();

        // Check cooldown first - fastest check
        long currentTime = System.currentTimeMillis();
        if (lastPurchaseTime.containsKey(uuid) &&
                currentTime - lastPurchaseTime.get(uuid) < PURCHASE_COOLDOWN) {
            if (debuggingFlag == 1) {
                plugin.getLogger().warning("BLOCKED (Cooldown): " + player.getName() +
                        " attempted to purchase ascendancy skill " + nodeId + " too quickly");
            }
            player.sendMessage(ChatColor.RED + "Please wait before purchasing another skill.");
            return;
        }

        // Try to acquire lock
        ReentrantLock lock = playerLocks.computeIfAbsent(uuid, k -> new ReentrantLock());
        if (!lock.tryLock()) {
            if (debuggingFlag == 1) {
                plugin.getLogger().warning("BLOCKED (Lock): " + player.getName() +
                        " attempted concurrent ascendancy purchase " + nodeId);
            }
            player.sendMessage(ChatColor.RED + "Another skill purchase is in progress.");
            return;
        }

        try {
            // Cancel any pending purchase tasks
            cancelPendingTasks(uuid);

            // Schedule the purchase at a later tick
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                processAscendancyPurchase(player, nodeId, branchIndex);
                pendingTasks.remove(uuid);
            }, 5L); // 5 ticks (250ms) delay

            pendingTasks.put(uuid, task);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("SCHEDULED: Ascendancy purchase for " + player.getName() +
                        ", skill " + nodeId);
            }
        } finally {
            lock.unlock();
        }
    }

    private void processSkillPurchase(Player player, int nodeId) {
        UUID uuid = player.getUniqueId();
        ReentrantLock lock = playerLocks.computeIfAbsent(uuid, k -> new ReentrantLock());

        lock.lock();
        try {
            if (debuggingFlag == 1) {
                plugin.getLogger().info("PROCESSING: Skill purchase for " + player.getName() +
                        ", skill " + nodeId);
            }

            boolean success = false;
            int purchaseCount = skillTreeManager.getSkillPurchaseCount(uuid, nodeId);

            // Check if the player has already purchased this skill (for upgrades)
            if (skillTreeManager.getPurchasedSkills(uuid).contains(nodeId)) {
                SkillTree tree = skillTreeManager.getSkillTree(
                        plugin.getClassManager().getPlayerClass(uuid),
                        "basic");

                if (tree != null) {
                    SkillNode node = tree.getNode(nodeId);

                    if (node != null && purchaseCount < node.getMaxPurchases() &&
                            skillTreeManager.getUnusedBasicSkillPoints(uuid) >=
                                    (node.getMaxPurchases() > 1 ? 1 : node.getCost())) {

                        success = skillTreeManager.purchaseSkill(player, nodeId);

                        if (success) {
                            player.playSound(player.getLocation(),
                                    org.bukkit.Sound.ENTITY_PLAYER_LEVELUP,
                                    1.0f, 1.0f);
                            player.sendMessage(ChatColor.GREEN + "Skill upgraded successfully!");

                            if (debuggingFlag == 1) {
                                plugin.getLogger().info("UPGRADE: " + player.getName() +
                                        " upgraded skill " + nodeId + " to level " + (purchaseCount + 1));
                            }
                        }
                    } else if (node != null && purchaseCount >= node.getMaxPurchases()) {
                        player.sendMessage(ChatColor.RED + "This skill is already fully upgraded!");
                    } else {
                        player.sendMessage(ChatColor.RED + "You don't have enough skill points!");
                    }
                }
            }
            // Try normal purchase if not already purchased or not an upgrade
            else if (skillTreeManager.canPurchaseSkill(player, nodeId)) {
                success = skillTreeManager.purchaseSkill(player, nodeId);

                if (success) {
                    player.playSound(player.getLocation(),
                            org.bukkit.Sound.ENTITY_PLAYER_LEVELUP,
                            1.0f, 1.0f);
                    player.sendMessage(ChatColor.GREEN + "Skill purchased successfully!");

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("SUCCESS: " + player.getName() +
                                " purchased skill " + nodeId);
                    }
                }
            } else {
                player.sendMessage(ChatColor.RED + "You need to unlock connected skills first!");
            }

            // Record the purchase time and then refresh GUI ONLY if successful
            if (success) {
                lastPurchaseTime.put(uuid, System.currentTimeMillis());

                // Wait 1 tick before refreshing GUI to avoid race conditions
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    skillTreeGUI.openSkillTreeGUI(player);
                }, 1L);
            }
        } finally {
            lock.unlock();
        }
    }

    private void processAscendancyPurchase(Player player, int nodeId, int branchIndex) {
        UUID uuid = player.getUniqueId();
        ReentrantLock lock = playerLocks.computeIfAbsent(uuid, k -> new ReentrantLock());

        lock.lock();
        try {
            if (debuggingFlag == 1) {
                plugin.getLogger().info("PROCESSING: Ascendancy purchase for " + player.getName() +
                        ", skill " + nodeId);
            }

            boolean success = false;

            // Try to purchase the ascendancy skill
            if (skillTreeManager.canPurchaseAscendancySkill(player, nodeId)) {
                success = skillTreeManager.purchaseAscendancySkill(player, nodeId);

                if (success) {
                    player.playSound(player.getLocation(),
                            org.bukkit.Sound.ENTITY_PLAYER_LEVELUP,
                            1.0f, 1.0f);
                    player.sendMessage(ChatColor.GREEN + "Ascendancy skill purchased successfully!");

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("SUCCESS: " + player.getName() +
                                " purchased ascendancy skill " + nodeId);
                    }
                }
            } else {
                // Check if it's an upgrade for an already purchased skill
                if (skillTreeManager.getPurchasedSkills(uuid).contains(nodeId)) {
                    int purchaseCount = skillTreeManager.getSkillPurchaseCount(uuid, nodeId);
                    String playerClass = plugin.getClassManager().getPlayerClass(uuid);
                    String ascendancy = plugin.getClassManager().getPlayerAscendancy(uuid);

                    SkillTree tree = skillTreeManager.getSkillTree(
                            playerClass,
                            "ascendancy",
                            ascendancy);

                    if (tree != null) {
                        SkillNode node = tree.getNode(nodeId);

                        if (node != null && purchaseCount < node.getMaxPurchases() &&
                                skillTreeManager.getUnusedAscendancySkillPoints(uuid) >=
                                        (node.getMaxPurchases() > 1 ? 1 : node.getCost())) {

                            success = skillTreeManager.purchaseAscendancySkill(player, nodeId);

                            if (success) {
                                player.playSound(player.getLocation(),
                                        org.bukkit.Sound.ENTITY_PLAYER_LEVELUP,
                                        1.0f, 1.0f);
                                player.sendMessage(ChatColor.GREEN +
                                        "Ascendancy skill upgraded successfully!");

                                if (debuggingFlag == 1) {
                                    plugin.getLogger().info("UPGRADE: " + player.getName() +
                                            " upgraded ascendancy skill " + nodeId +
                                            " to level " + (purchaseCount + 1));
                                }
                            }
                        } else if (node != null && purchaseCount >= node.getMaxPurchases()) {
                            player.sendMessage(ChatColor.RED + "This skill is already fully upgraded!");
                        } else {
                            player.sendMessage(ChatColor.RED + "You don't have enough ascendancy points!");
                        }
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You need to unlock connected skills first!");
                }
            }

            // Record the purchase time and then refresh GUI ONLY if successful
            if (success) {
                lastPurchaseTime.put(uuid, System.currentTimeMillis());

                // Wait 1 tick before refreshing GUI to avoid race conditions
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    ascendancySkillTreeGUI.openAscendancySkillTreeGUI(player, branchIndex);

                    // Add this block to check for Beastmaster skills
                    String ascendancy = plugin.getClassManager().getPlayerAscendancy(player.getUniqueId());
                    if ("Beastmaster".equals(ascendancy)) {
                        BeastmasterSkillEffectsHandler handler = 
                            (BeastmasterSkillEffectsHandler) plugin.getAscendancySkillEffectIntegrator().getHandler("Beastmaster");

                        if (handler != null) {
                            // If it's a summon skill, trigger auto-summon
                            if (nodeId == 100001 || nodeId == 100002 || nodeId == 100003) {
                                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                    handler.checkAndSummonCreatures(player);
                                }, 20L); // 1 second after purchase
                            } 
                            // For any Beastmaster skill, refresh existing summons to update their stats
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                handler.refreshSummons(player);
                            }, 20L); // 1 second after purchase
                        }
                    }
                }, 1L);
            }
        } finally {
            lock.unlock();
        }
    }

    private void cancelPendingTasks(UUID uuid) {
        BukkitTask pendingTask = pendingTasks.get(uuid);
        if (pendingTask != null) {
            pendingTask.cancel();
            pendingTasks.remove(uuid);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("CANCELLED: Pending skill purchase task for " + uuid);
            }
        }
    }

    /**
     * Called when a player closes the skill tree inventory
     */
    public void handleInventoryClose(Player player) {
        UUID uuid = player.getUniqueId();
        cancelPendingTasks(uuid);
    }

    /**
     * Called during plugin shutdown or player logout
     */
    public void cleanup(UUID uuid) {
        cancelPendingTasks(uuid);
        playerLocks.remove(uuid);
        lastPurchaseTime.remove(uuid);
    }

    /**
     * Called during plugin shutdown
     */
    public void cleanup() {
        for (BukkitTask task : pendingTasks.values()) {
            task.cancel();
        }
        pendingTasks.clear();
        playerLocks.clear();
        lastPurchaseTime.clear();
    }
}
