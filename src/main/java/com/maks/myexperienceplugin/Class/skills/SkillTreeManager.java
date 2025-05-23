package com.maks.myexperienceplugin.Class.skills;

import com.maks.myexperienceplugin.Class.skills.base.BaseSkillManager;
import com.maks.myexperienceplugin.Class.skills.base.SkillNode;
import com.maks.myexperienceplugin.Class.skills.base.SkillTree;
import com.maks.myexperienceplugin.Class.skills.classes.dragonknight.DragonKnightSkillManager;
import com.maks.myexperienceplugin.Class.skills.classes.ranger.RangerSkillManager;
import com.maks.myexperienceplugin.Class.skills.classes.ranger.ascendancy.BeastmasterSkillManager;
import com.maks.myexperienceplugin.Class.skills.classes.ranger.ascendancy.EarthwardenSkillManager;
import com.maks.myexperienceplugin.Class.skills.classes.ranger.ascendancy.ShadowstalkerSkillManager;
import com.maks.myexperienceplugin.Class.skills.classes.dragonknight.ascendancy.BerserkerSkillManager;
import com.maks.myexperienceplugin.Class.skills.classes.dragonknight.ascendancy.FlameWardenSkillManager;
import com.maks.myexperienceplugin.Class.skills.classes.dragonknight.ascendancy.ScaleGuardianSkillManager;
import com.maks.myexperienceplugin.Class.skills.classes.spellweaver.SpellWeaverSkillManager;
import com.maks.myexperienceplugin.Class.skills.events.SkillPurchasedEvent;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SkillTreeManager {
    private final MyExperiencePlugin plugin;

    // Debugging flag
    private final int debuggingFlag = 1;

    // Managers for base classes
    private final Map<String, BaseSkillManager> classManagers;

    // Managers for ascendancy classes
    private final Map<String, Map<String, BaseSkillManager>> ascendancyManagers;

    // Skill trees for each class
    private final Map<String, SkillTree> basicSkillTrees;
    private final Map<String, Map<String, SkillTree>> ascendancySkillTrees;

    // Cache player data
    private final ConcurrentHashMap<UUID, Integer> basicSkillPointsCache;
    private final ConcurrentHashMap<UUID, Integer> ascendancySkillPointsCache;
    private final ConcurrentHashMap<UUID, Set<Integer>> purchasedSkillsCache;
    private final ConcurrentHashMap<UUID, Map<Integer, Integer>> skillPurchaseCountCache;

    public SkillTreeManager(MyExperiencePlugin plugin) {
        this.plugin = plugin;
        this.classManagers = new HashMap<>();
        this.ascendancyManagers = new HashMap<>();
        this.basicSkillTrees = new HashMap<>();
        this.ascendancySkillTrees = new HashMap<>();
        this.basicSkillPointsCache = new ConcurrentHashMap<>();
        this.ascendancySkillPointsCache = new ConcurrentHashMap<>();
        this.purchasedSkillsCache = new ConcurrentHashMap<>();
        this.skillPurchaseCountCache = new ConcurrentHashMap<>();

        // Initialize database tables
        initializeDatabase();

        // Initialize class managers
        initializeClassManagers();

        // Initialize skill trees
        initializeSkillTrees();

        if (debuggingFlag == 1) {
            plugin.getLogger().info("SkillTreeManager initialized");
            plugin.getLogger().info("Basic skill trees: " + basicSkillTrees.keySet());
            plugin.getLogger().info("Ascendancy skill trees: " + ascendancySkillTrees.keySet());
        }
    }

    private void initializeClassManagers() {
        // Create base class managers
        RangerSkillManager rangerManager = new RangerSkillManager(plugin);
        DragonKnightSkillManager dragonKnightManager = new DragonKnightSkillManager(plugin);
        // Add SpellweaverSkillManager when implemented
        SpellWeaverSkillManager spellWeaverManager = new SpellWeaverSkillManager(plugin);

        // Add to map
        classManagers.put("Ranger", rangerManager);
        classManagers.put("DragonKnight", dragonKnightManager);
        // Add SpellweaverSkillManager when implemented
        classManagers.put("SpellWeaver", spellWeaverManager);

        // Create ascendancy managers
        // Ranger ascendancies
        Map<String, BaseSkillManager> rangerAscendancies = new HashMap<>();
        rangerAscendancies.put("Beastmaster", new BeastmasterSkillManager(plugin));
        rangerAscendancies.put("Shadowstalker", new ShadowstalkerSkillManager(plugin));
        rangerAscendancies.put("Earthwarden", new EarthwardenSkillManager(plugin));
        // Add other Ranger ascendancies when implemented

        // DragonKnight ascendancies
        Map<String, BaseSkillManager> dragonKnightAscendancies = new HashMap<>();
        dragonKnightAscendancies.put("Berserker", new BerserkerSkillManager(plugin));
        dragonKnightAscendancies.put("FlameWarden", new FlameWardenSkillManager(plugin));
        dragonKnightAscendancies.put("ScaleGuardian", new ScaleGuardianSkillManager(plugin));
        // Add other DragonKnight ascendancies when implemented

        // Add to map
        ascendancyManagers.put("Ranger", rangerAscendancies);
        ascendancyManagers.put("DragonKnight", dragonKnightAscendancies);
        // Add SpellweaverSkillManager ascendancies when implemented

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Initialized class managers: " + classManagers.keySet());
            plugin.getLogger().info("Initialized Ranger ascendancy managers: " +
                    (ascendancyManagers.containsKey("Ranger") ?
                            ascendancyManagers.get("Ranger").keySet() : "none"));
            plugin.getLogger().info("Initialized DragonKnight ascendancy managers: " +
                    (ascendancyManagers.containsKey("DragonKnight") ?
                            ascendancyManagers.get("DragonKnight").keySet() : "none"));
        }
    }

    private void initializeSkillTrees() {
        // Create basic skill trees
        for (Map.Entry<String, BaseSkillManager> entry : classManagers.entrySet()) {
            String className = entry.getKey();
            BaseSkillManager manager = entry.getValue();

            SkillTree tree = manager.createSkillTree("basic");
            basicSkillTrees.put(className, tree);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Created basic skill tree for " + className +
                        " with " + tree.getAllNodes().size() + " nodes");
            }
        }

        // Create ascendancy skill trees
        for (Map.Entry<String, Map<String, BaseSkillManager>> entry : ascendancyManagers.entrySet()) {
            String className = entry.getKey();
            Map<String, BaseSkillManager> ascendancyMap = entry.getValue();

            Map<String, SkillTree> ascendancyTrees = new HashMap<>();
            for (Map.Entry<String, BaseSkillManager> ascEntry : ascendancyMap.entrySet()) {
                String ascendancyName = ascEntry.getKey();
                BaseSkillManager manager = ascEntry.getValue();

                SkillTree tree = manager.createSkillTree("ascendancy");
                ascendancyTrees.put(ascendancyName, tree);

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Created ascendancy skill tree for " + className +
                            "/" + ascendancyName + " with " +
                            tree.getAllNodes().size() + " nodes");
                }
            }

            ascendancySkillTrees.put(className, ascendancyTrees);
        }
    }

    private void initializeDatabase() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                // Table for skill points
                String createSkillPointsTable = "CREATE TABLE IF NOT EXISTS player_skill_points (" +
                        "uuid VARCHAR(36) PRIMARY KEY," +
                        "basic_points INT DEFAULT 0," +
                        "ascendancy_points INT DEFAULT 0)";

                // Table for purchased skills
                String createPurchasedSkillsTable = "CREATE TABLE IF NOT EXISTS player_purchased_skills (" +
                        "uuid VARCHAR(36)," +
                        "skill_id INT," +
                        "purchase_count INT DEFAULT 1," +
                        "PRIMARY KEY (uuid, skill_id))";

                try (PreparedStatement stmt = conn.prepareStatement(createSkillPointsTable)) {
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(createPurchasedSkillsTable)) {
                    stmt.executeUpdate();
                }

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Database tables initialized");
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to initialize skill tree database tables: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void loadPlayerSkillData(Player player) {
        UUID uuid = player.getUniqueId();

        // For immediate response, load default values directly
        basicSkillPointsCache.put(uuid, 0);
        ascendancySkillPointsCache.put(uuid, 0);
        purchasedSkillsCache.put(uuid, new HashSet<>());
        skillPurchaseCountCache.put(uuid, new HashMap<>());

        // Then update skill points based on player level
        updateSkillPoints(player);

        // Then load actual data from database
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                // Load skill points
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT basic_points, ascendancy_points FROM player_skill_points WHERE uuid = ?")) {
                    stmt.setString(1, uuid.toString());

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            int basicPoints = rs.getInt("basic_points");
                            int ascendancyPoints = rs.getInt("ascendancy_points");

                            // Run on main thread to avoid race conditions
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                basicSkillPointsCache.put(uuid, basicPoints);
                                ascendancySkillPointsCache.put(uuid, ascendancyPoints);

                                if (debuggingFlag == 1) {
                                    plugin.getLogger().info("Loaded skill points for " + player.getName() +
                                            ": basic=" + basicPoints + ", ascendancy=" + ascendancyPoints);
                                }
                            });
                        }
                    }
                }

                // Load purchased skills
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT skill_id, purchase_count FROM player_purchased_skills WHERE uuid = ?")) {
                    stmt.setString(1, uuid.toString());

                    final Set<Integer> purchasedSkills = new HashSet<>();
                    final Map<Integer, Integer> purchaseCounts = new HashMap<>();

                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            int skillId = rs.getInt("skill_id");
                            int purchaseCount = rs.getInt("purchase_count");

                            purchasedSkills.add(skillId);
                            purchaseCounts.put(skillId, purchaseCount);

                            if (debuggingFlag == 1) {
                                plugin.getLogger().info("Loaded skill " + skillId +
                                        " for " + player.getName() +
                                        " with count " + purchaseCount);
                            }
                        }
                    }

                    // Store in cache (on main thread to avoid race conditions)
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        purchasedSkillsCache.put(uuid, purchasedSkills);
                        skillPurchaseCountCache.put(uuid, purchaseCounts);

                        if (debuggingFlag == 1) {
                            plugin.getLogger().info("Completed loading " + purchasedSkills.size() +
                                    " skills for " + player.getName());
                        }
                    });
                }

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Loaded skill data for player " + player.getName());
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to load skill data for player " + player.getName() +
                        ": " + e.getMessage());
                e.printStackTrace();
            }
        });
    }    /**
     * Calculate and update skill points based on player level
     */
    /**
     * Calculate and update skill points based on player level
     */
    /**
     * Calculate and update skill points based on player level
     */
    public void updateSkillPoints(Player player) {
        UUID uuid = player.getUniqueId();
        int level = plugin.getPlayerLevel(player);

        // Define debugging flag if not already defined at class level
        final int DEBUGGING_FLAG = 1;

        // Calculate basic class points (1-20)
        // For levels 1-20, you get 1 point per level (max 20 points)
        int basicPoints = Math.min(level, 20);

        // Calculate ascendancy points (20-100)
        int ascendancyPoints = 0;
        if (level >= 20) {
            // 1 point per level from 20-50 (31 points total)
            ascendancyPoints += Math.min(level - 19, 31);

            // 1 point every 2 levels from 51-99 (25 points more)
            if (level > 50) {
                ascendancyPoints += Math.min((level - 50 + 1) / 2, 25);

                // 5 points at level 100
                if (level == 100) {
                    ascendancyPoints += 5;
                }
            }
        }

        if (DEBUGGING_FLAG == 1) {
            plugin.getLogger().info("Calculating skill points for " + player.getName() +
                    " (level " + level + "): basic=" + basicPoints +
                    ", ascendancy=" + ascendancyPoints);
        }

        // Update cache
        basicSkillPointsCache.put(uuid, basicPoints);
        ascendancySkillPointsCache.put(uuid, ascendancyPoints);

        // Save to database
        final int finalBasicPoints = basicPoints;
        final int finalAscendancyPoints = ascendancyPoints;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "REPLACE INTO player_skill_points (uuid, basic_points, ascendancy_points) VALUES (?, ?, ?)")) {
                    stmt.setString(1, uuid.toString());
                    stmt.setInt(2, finalBasicPoints);
                    stmt.setInt(3, finalAscendancyPoints);
                    stmt.executeUpdate();

                    if (DEBUGGING_FLAG == 1) {
                        plugin.getLogger().info("Saved skill points to database for player " + player.getName() +
                                ": basic=" + finalBasicPoints +
                                ", ascendancy=" + finalAscendancyPoints);
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save skill points for player " + player.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
    }    public int getBasicSkillPoints(UUID uuid) {
        return basicSkillPointsCache.getOrDefault(uuid, 0);
    }

    public int getAscendancySkillPoints(UUID uuid) {
        return ascendancySkillPointsCache.getOrDefault(uuid, 0);
    }

    public int getUnusedBasicSkillPoints(UUID uuid) {
        int totalPoints = getBasicSkillPoints(uuid);
        int usedPoints = 0;

        Set<Integer> purchasedSkills = purchasedSkillsCache.getOrDefault(uuid, new HashSet<>());
        Map<Integer, Integer> purchaseCounts = skillPurchaseCountCache.getOrDefault(uuid, new HashMap<>());

        // Sum up costs of all purchased basic skills
        String playerClass = plugin.getClassManager().getPlayerClass(uuid);
        if (!basicSkillTrees.containsKey(playerClass)) {
            if (debuggingFlag == 1) {
                plugin.getLogger().warning("No basic skill tree found for class: " + playerClass);
            }
            return totalPoints;
        }

        SkillTree tree = basicSkillTrees.get(playerClass);
        BaseSkillManager manager = classManagers.get(playerClass);

        if (manager == null) {
            if (debuggingFlag == 1) {
                plugin.getLogger().warning("No skill manager found for class: " + playerClass);
            }
            return totalPoints;
        }

        // Create a new set with only basic skill IDs (below 100000)
        Set<Integer> basicSkills = new HashSet<>();
        for (int skillId : purchasedSkills) {
            if (skillId < 100000) { // Only count basic class skills, not ascendancy
                basicSkills.add(skillId);
            }
        }

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Calculating used points for " + playerClass +
                    ", total points: " + totalPoints +
                    ", purchased basic skills: " + basicSkills.size());
        }

        for (int skillId : basicSkills) {
            SkillNode node = tree.getNode(skillId);
            if (node != null) {
                int purchaseCount = purchaseCounts.getOrDefault(skillId, 1);

                // Determine actual cost
                int actualCost;
                if (manager.isMultiPurchaseDiscountSkill(skillId)) {
                    actualCost = 1; // Special skills cost 1 point per purchase
                } else {
                    actualCost = node.getCost();
                }

                int skillCost = actualCost * purchaseCount;
                usedPoints += skillCost;

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("  Skill " + skillId + " (" + node.getName() +
                            "): cost=" + actualCost +
                            ", count=" + purchaseCount +
                            ", total=" + skillCost);
                }
            } else {
                if (debuggingFlag == 1) {
                    plugin.getLogger().warning("  Purchased skill " + skillId +
                            " not found in skill tree for " + playerClass);
                }
            }
        }

        // Ensure we never return a negative value
        int remainingPoints = Math.max(0, totalPoints - usedPoints);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Final calculation: " + totalPoints + " - " +
                    usedPoints + " = " + remainingPoints + " unused points");
        }

        return remainingPoints;
    }    public int getUnusedAscendancySkillPoints(UUID uuid) {
        int totalPoints = getAscendancySkillPoints(uuid);
        int usedPoints = 0;

        Set<Integer> purchasedSkills = purchasedSkillsCache.getOrDefault(uuid, new HashSet<>());
        Map<Integer, Integer> purchaseCounts = skillPurchaseCountCache.getOrDefault(uuid, new HashMap<>());

        // Calculate used points for ascendancy skills
        String playerClass = plugin.getClassManager().getPlayerClass(uuid);
        String ascendancy = plugin.getClassManager().getPlayerAscendancy(uuid);

        if (ascendancy == null || ascendancy.isEmpty() ||
                !ascendancySkillTrees.containsKey(playerClass) ||
                !ascendancySkillTrees.get(playerClass).containsKey(ascendancy)) {
            return totalPoints;
        }

        SkillTree tree = ascendancySkillTrees.get(playerClass).get(ascendancy);

        // Only count skill IDs that are part of ascendancy trees (IDs 100000+ for Beastmaster, 200000+ for Berserker)
        for (int skillId : purchasedSkills) {
            // Only process ascendancy skill IDs (100000+ or other high ranges)
            if (skillId >= 100000) {
                SkillNode node = tree.getNode(skillId);
                if (node != null) {
                    int purchaseCount = purchaseCounts.getOrDefault(skillId, 1);
                    usedPoints += purchaseCount; // Ascendancy skills always cost 1 point

                }
            }
        }

        int unusedPoints = totalPoints - usedPoints;


        return unusedPoints;
    }
    public Set<Integer> getPurchasedSkills(UUID uuid) {
        return purchasedSkillsCache.getOrDefault(uuid, new HashSet<>());
    }

    public int getSkillPurchaseCount(UUID uuid, int skillId) {
        Map<Integer, Integer> purchaseCounts = skillPurchaseCountCache.getOrDefault(uuid, new HashMap<>());
        return purchaseCounts.getOrDefault(skillId, 0);
    }

    public boolean canPurchaseSkill(Player player, int skillId) {
        UUID uuid = player.getUniqueId();
        String playerClass = plugin.getClassManager().getPlayerClass(uuid);

        if (!basicSkillTrees.containsKey(playerClass)) {
            return false;
        }

        SkillTree tree = basicSkillTrees.get(playerClass);
        BaseSkillManager manager = classManagers.get(playerClass);

        if (manager == null) {
            return false;
        }

        SkillNode node = tree.getNode(skillId);
        if (node == null) {
            return false;
        }

        // Check if player has already maxed out this skill
        int currentPurchases = getSkillPurchaseCount(uuid, skillId);
        if (currentPurchases >= node.getMaxPurchases()) {
            return false;
        }

        // Check if player has enough points
        int unusedPoints = getUnusedBasicSkillPoints(uuid);
        int actualCost;
        if (manager.isMultiPurchaseDiscountSkill(skillId)) {
            actualCost = 1; // Special skills cost 1 point per purchase
        } else {
            actualCost = node.getCost();
        }

        if (unusedPoints < actualCost) {
            return false;
        }

        // Check if player has purchased a connected node
        Set<Integer> purchasedSkills = getPurchasedSkills(uuid);
        return tree.canPurchaseNode(player, skillId, purchasedSkills);
    }

    public boolean purchaseSkill(Player player, int skillId) {
        if (!canPurchaseSkill(player, skillId)) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        String playerClass = plugin.getClassManager().getPlayerClass(uuid);

        SkillTree tree = basicSkillTrees.get(playerClass);
        BaseSkillManager manager = classManagers.get(playerClass);
        SkillNode node = tree.getNode(skillId);

        // Determine actual cost
        int actualCost;
        if (manager.isMultiPurchaseDiscountSkill(skillId)) {
            actualCost = 1; // Special skills cost 1 point per purchase
        } else {
            actualCost = node.getCost();
        }

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Purchasing skill " + skillId + " for player " + player.getName());
            plugin.getLogger().info("Class: " + playerClass + ", Displayed cost: " + node.getCost() +
                    ", Actual cost: " + actualCost);
        }

        // Update cache
        Set<Integer> purchasedSkills = purchasedSkillsCache.computeIfAbsent(uuid, k -> new HashSet<>());
        purchasedSkills.add(skillId);

        Map<Integer, Integer> purchaseCounts = skillPurchaseCountCache.computeIfAbsent(uuid, k -> new HashMap<>());
        int currentCount = purchaseCounts.getOrDefault(skillId, 0);
        purchaseCounts.put(skillId, currentCount + 1);

        // Save to database
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "REPLACE INTO player_purchased_skills (uuid, skill_id, purchase_count) VALUES (?, ?, ?)")) {
                    stmt.setString(1, uuid.toString());
                    stmt.setInt(2, skillId);
                    stmt.setInt(3, currentCount + 1);
                    stmt.executeUpdate();

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Saved purchased skill " + skillId +
                                " for player " + player.getName() +
                                " (count: " + (currentCount + 1) + ")");
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save purchased skill for player " +
                        player.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Apply skill effects
        manager.applySkillEffects(player, skillId, currentCount + 1);

        // Broadcast an event for skill purchase
        SkillPurchasedEvent event = new SkillPurchasedEvent(player, skillId);
        Bukkit.getPluginManager().callEvent(event);

        return true;
    }

    public boolean canPurchaseAscendancySkill(Player player, int skillId) {
        UUID uuid = player.getUniqueId();
        String playerClass = plugin.getClassManager().getPlayerClass(uuid);
        String ascendancy = plugin.getClassManager().getPlayerAscendancy(uuid);

        if ("NoClass".equalsIgnoreCase(playerClass) || ascendancy == null || ascendancy.isEmpty() ||
                !ascendancySkillTrees.containsKey(playerClass) ||
                !ascendancySkillTrees.get(playerClass).containsKey(ascendancy)) {
            return false;
        }

        SkillTree tree = ascendancySkillTrees.get(playerClass).get(ascendancy);
        BaseSkillManager manager = ascendancyManagers.get(playerClass).get(ascendancy);

        if (manager == null) {
            return false;
        }

        SkillNode node = tree.getNode(skillId);
        if (node == null) {
            return false;
        }

        // Check if player has already maxed out this skill
        int currentPurchases = getSkillPurchaseCount(uuid, skillId);
        if (currentPurchases >= node.getMaxPurchases()) {
            return false;
        }

        // Check if player has enough points
        int unusedPoints = getUnusedAscendancySkillPoints(uuid);
        if (unusedPoints < 1) { // Ascendancy skills always cost 1 point per purchase
            return false;
        }

        // Get purchased skills
        Set<Integer> purchasedSkills = getPurchasedSkills(uuid);

        // Special check for Beastmaster summon skills
        if ("Beastmaster".equals(ascendancy)) {
            // Check if this is one of the summon skills (Wolf, Boar, Bear)
            if (skillId == 100001 || skillId == 100002 || skillId == 100003) {
                // If player already has this skill, allow the purchase (for upgrades)
                if (purchasedSkills.contains(skillId)) {
                    return true;
                }

                // Count how many summon skills the player already has
                int summonSkillsCount = 0;
                if (purchasedSkills.contains(100001)) summonSkillsCount++;
                if (purchasedSkills.contains(100002)) summonSkillsCount++;
                if (purchasedSkills.contains(100003)) summonSkillsCount++;

                // If player already has 2 summon skills, don't allow purchasing a third
                if (summonSkillsCount >= 2) {
                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Player " + player.getName() + 
                            " already has 2 summon skills, cannot purchase a third one.");
                    }
                    return false;
                }
            }
        }

        // Check if player has purchased a connected node
        return tree.canPurchaseNode(player, skillId, purchasedSkills);
    }

    public boolean purchaseAscendancySkill(Player player, int skillId) {
        if (!canPurchaseAscendancySkill(player, skillId)) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        String playerClass = plugin.getClassManager().getPlayerClass(uuid);
        String ascendancy = plugin.getClassManager().getPlayerAscendancy(uuid);

        SkillTree tree = ascendancySkillTrees.get(playerClass).get(ascendancy);
        BaseSkillManager manager = ascendancyManagers.get(playerClass).get(ascendancy);
        SkillNode node = tree.getNode(skillId);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Purchasing ascendancy skill " + skillId +
                    " for player " + player.getName() +
                    " (class: " + playerClass + "/" + ascendancy + ")");
        }

        // Update cache
        Set<Integer> purchasedSkills = purchasedSkillsCache.computeIfAbsent(uuid, k -> new HashSet<>());
        purchasedSkills.add(skillId);

        Map<Integer, Integer> purchaseCounts = skillPurchaseCountCache.computeIfAbsent(uuid, k -> new HashMap<>());
        int currentCount = purchaseCounts.getOrDefault(skillId, 0);
        purchaseCounts.put(skillId, currentCount + 1);

        // Save to database
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "REPLACE INTO player_purchased_skills (uuid, skill_id, purchase_count) VALUES (?, ?, ?)")) {
                    stmt.setString(1, uuid.toString());
                    stmt.setInt(2, skillId);
                    stmt.setInt(3, currentCount + 1);
                    stmt.executeUpdate();

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Saved purchased ascendancy skill " + skillId +
                                " for player " + player.getName() +
                                " (count: " + (currentCount + 1) + ")");
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save purchased ascendancy skill for player " +
                        player.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Apply skill effects
        manager.applySkillEffects(player, skillId, currentCount + 1);

        // Broadcast an event for skill purchase
        SkillPurchasedEvent event = new SkillPurchasedEvent(player, skillId);
        Bukkit.getPluginManager().callEvent(event);

        return true;
    }

    /**
     * Get a skill tree for a specific class and type
     */
    public SkillTree getSkillTree(String className, String treeType) {
        if ("basic".equals(treeType)) {
            return basicSkillTrees.get(className);
        }
        return null;
    }

    /**
     * Get a skill tree for a specific class, type, and ascendancy
     */
    public SkillTree getSkillTree(String className, String treeType, String ascendancy) {
        if ("basic".equals(treeType)) {
            return basicSkillTrees.get(className);
        } else if ("ascendancy".equals(treeType) && ascendancy != null && !ascendancy.isEmpty()) {
            Map<String, SkillTree> classAscendancies = ascendancySkillTrees.get(className);
            if (classAscendancies != null) {
                return classAscendancies.get(ascendancy);
            }
        }
        return null;
    }

    public Map<String, SkillTree> getBasicSkillTrees() {
        return basicSkillTrees;
    }

    public Map<String, Map<String, SkillTree>> getAscendancySkillTrees() {
        return ascendancySkillTrees;
    }

    /**
     * Get a class manager by class name
     */
    public BaseSkillManager getClassManager(String className) {
        return classManagers.get(className);
    }

    /**
     * Get an ascendancy manager by class name and ascendancy name
     */
    public BaseSkillManager getAscendancyManager(String className, String ascendancy) {
        if (ascendancyManagers.containsKey(className)) {
            return ascendancyManagers.get(className).get(ascendancy);
        }
        return null;
    }
    public void clearPlayerSkillData(UUID uuid) {
        // Usuń dane z wszystkich cache'y
        basicSkillPointsCache.remove(uuid);
        ascendancySkillPointsCache.remove(uuid);
        purchasedSkillsCache.remove(uuid);
        skillPurchaseCountCache.remove(uuid);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Cleared all skill data for player UUID: " + uuid);
        }
    }
    /**
     * Get the purchase count map for a player
     * @param uuid Player UUID
     * @return Map of skill IDs to purchase counts
     */
    public Map<Integer, Integer> getPurchaseCountMap(UUID uuid) {
        // Return a copy of the map to prevent direct modification
        return new HashMap<>(skillPurchaseCountCache.getOrDefault(uuid, new HashMap<>()));
    }
}
