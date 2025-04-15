package com.maks.myexperienceplugin.Class.skills.gui;

import com.maks.myexperienceplugin.Class.skills.SkillPurchaseManager;
import com.maks.myexperienceplugin.Class.skills.SkillTreeManager;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AscendancySkillTreeGUIListener implements Listener {
    private final MyExperiencePlugin plugin;
    private final SkillTreeManager skillTreeManager;
    private final AscendancySkillTreeGUI ascendancySkillTreeGUI;
    private final SkillPurchaseManager purchaseManager;

    // Debugging flag
    private final int debuggingFlag = 1;

    // Mappings to track branches for each ascendancy
    private final Map<String, List<String>> ascendancyBranches;

    // Mappings to track node positions for each branch
    private final Map<String, Map<String, Map<Integer, Integer>>> branchNodePositions;

    public AscendancySkillTreeGUIListener(MyExperiencePlugin plugin,
                                          SkillTreeManager skillTreeManager,
                                          AscendancySkillTreeGUI ascendancySkillTreeGUI,
                                          SkillPurchaseManager purchaseManager) {
        this.plugin = plugin;
        this.skillTreeManager = skillTreeManager;
        this.ascendancySkillTreeGUI = ascendancySkillTreeGUI;
        this.purchaseManager = purchaseManager;

        // Initialize branch mappings - these should match AscendancySkillTreeGUI
        this.ascendancyBranches = new HashMap<>();
        this.branchNodePositions = new HashMap<>();

        // Initialize branch names
        initializeBranchNames();
        // Initialize node positions
        initializeNodePositions();
    }

    private void initializeBranchNames() {
        // For Beastmaster - define the three branches
        List<String> beastmasterBranches = new java.util.ArrayList<>();
        beastmasterBranches.add("Wolf Path");
        beastmasterBranches.add("Boar Path");
        beastmasterBranches.add("Bear Path");
        ascendancyBranches.put("Beastmaster", beastmasterBranches);

        // For Berserker - define branches
        List<String> berserkerBranches = new java.util.ArrayList<>();
        berserkerBranches.add("Rage Path");
        berserkerBranches.add("Critical Path");
        berserkerBranches.add("Frenzy Path");
        ascendancyBranches.put("Berserker", berserkerBranches);
    }

    private void initializeNodePositions() {
        // Beastmaster - Wolf Path
        Map<String, Map<Integer, Integer>> beastmasterBranches = new HashMap<>();

        // Wolf Path nodes
        Map<Integer, Integer> wolfPathNodes = new HashMap<>();
        wolfPathNodes.put(100001, 10);  // Wolf Summon at slot 10 (top)
        wolfPathNodes.put(100004, 19);  // Wolf Speed at slot 19 (second row)
        wolfPathNodes.put(100007, 28);  // Wolf Attack Speed at slot 28 (third row)
        wolfPathNodes.put(100011, 37);  // Wolf Critical at slot 37 (fourth row)
        wolfPathNodes.put(100012, 38);  // Wolf Vitality at slot 38 (fourth row)
        wolfPathNodes.put(100017, 46);  // Wolf Healing at slot 46 (fifth row)
        wolfPathNodes.put(100022, 47);  // Wolf Health at slot 47 (fifth row)
        wolfPathNodes.put(100025, 49);  // Wolf Pack at slot 49 (bottom center)
        beastmasterBranches.put("Wolf Path", wolfPathNodes);

        // Boar Path nodes
        Map<Integer, Integer> boarPathNodes = new HashMap<>();
        boarPathNodes.put(100002, 12);  // Boar Summon at slot 12 (top)
        boarPathNodes.put(100005, 21);  // Boar Damage at slot 21 (second row)
        boarPathNodes.put(100008, 30);  // Boar Attack Speed at slot 30 (third row)
        boarPathNodes.put(100009, 31);  // Pack Damage at slot 31 (third row)
        boarPathNodes.put(100013, 39);  // Boar Critical at slot 39 (fourth row)
        boarPathNodes.put(100014, 40);  // Pack Damage Plus at slot 40 (fourth row)
        boarPathNodes.put(100019, 48);  // Boar Frenzy at slot 48 (fifth row)
        boarPathNodes.put(100023, 50);  // Boar Speed at slot 50 (fifth row)
        boarPathNodes.put(100026, 51);  // Boar Rage at slot 51 (bottom)
        beastmasterBranches.put("Boar Path", boarPathNodes);

        // Bear Path nodes
        Map<Integer, Integer> bearPathNodes = new HashMap<>();
        bearPathNodes.put(100003, 14);  // Bear Summon at slot 14 (top)
        bearPathNodes.put(100006, 23);  // Bear Health at slot 23 (second row)
        bearPathNodes.put(100010, 32);  // Bear Defense at slot 32 (third row)
        bearPathNodes.put(100015, 41);  // Bear Guardian at slot 41 (fourth row)
        bearPathNodes.put(100016, 42);  // Bear Vitality at slot 42 (fourth row)
        bearPathNodes.put(100020, 50);  // Bear Regeneration at slot 50 (fifth row)
        bearPathNodes.put(100021, 51);  // Pack Vitality at slot 51 (fifth row)
        bearPathNodes.put(100024, 32);  // Pack Defense at slot 32 (fifth row)
        bearPathNodes.put(100027, 53);  // Pack Healing at slot 53 (bottom)
        beastmasterBranches.put("Bear Path", bearPathNodes);

        branchNodePositions.put("Beastmaster", beastmasterBranches);

        // Similar structure for Berserker branches
        Map<String, Map<Integer, Integer>> berserkerBranches = new HashMap<>();

        // Rage Path nodes
        Map<Integer, Integer> ragePathNodes = new HashMap<>();
        ragePathNodes.put(200001, 10);  // Unarmored Rage at slot 10 (top)
        ragePathNodes.put(200004, 19);  // Kill Frenzy at slot 19 (second row)
        ragePathNodes.put(200007, 28);  // Glass Cannon at slot 28 (third row)
        ragePathNodes.put(200011, 37);  // Finishing Blow at slot 37 (fourth row)
        ragePathNodes.put(200017, 46);  // Raw Power at slot 46 (fifth row)
        ragePathNodes.put(200027, 49);  // Death Defiance at slot 49 (bottom center)
        berserkerBranches.put("Rage Path", ragePathNodes);

        // Critical Path nodes
        Map<Integer, Integer> criticalPathNodes = new HashMap<>();
        criticalPathNodes.put(200002, 12);  // Berserker's Fury at slot 12 (top)
        criticalPathNodes.put(200005, 21);  // Battle Rage at slot 21 (second row)
        criticalPathNodes.put(200008, 30);  // Reckless Strike at slot 30 (third row)
        criticalPathNodes.put(200009, 31);  // Critical Specialization at slot 31 (third row)
        criticalPathNodes.put(200013, 39);  // Vitality at slot 39 (fourth row)
        criticalPathNodes.put(200014, 40);  // Bleeding Strike at slot 40 (fourth row)
        criticalPathNodes.put(200018, 48);  // Critical Mastery at slot 48 (fifth row)
        criticalPathNodes.put(200020, 50);  // Critical Precision at slot 50 (fifth row)
        berserkerBranches.put("Critical Path", criticalPathNodes);

        // Frenzy Path nodes
        Map<Integer, Integer> frenzyPathNodes = new HashMap<>();
        frenzyPathNodes.put(200003, 14);  // Combat Momentum at slot 14 (top)
        frenzyPathNodes.put(200006, 23);  // Strength Boost at slot 23 (second row)
        frenzyPathNodes.put(200010, 32);  // Attack Speed Frenzy at slot 32 (third row)
        frenzyPathNodes.put(200012, 41);  // Tactical Defense at slot 41 (fourth row)
        frenzyPathNodes.put(200015, 42);  // Agility at slot 42 (fourth row)
        frenzyPathNodes.put(200019, 51);  // Reckless Power at slot 51 (fifth row)
        berserkerBranches.put("Frenzy Path", frenzyPathNodes);

        branchNodePositions.put("Berserker", berserkerBranches);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        // Check if this is an ascendancy skill tree GUI
        if (!title.startsWith(ChatColor.DARK_BLUE + "Ascendancy Skills")) {
            return;
        }

        // Always cancel the event first to prevent double-processing
        event.setCancelled(true);

        // Ignore certain inventory actions that aren't left-clicks
        if (event.getAction() != InventoryAction.PICKUP_ALL &&
                event.getAction() != InventoryAction.PICKUP_HALF) {
            return;
        }

        if (event.getCurrentItem() == null) {
            return;
        }

        String playerClass = plugin.getClassManager().getPlayerClass(player.getUniqueId());
        String ascendancy = plugin.getClassManager().getPlayerAscendancy(player.getUniqueId());

        if ("NoClass".equalsIgnoreCase(playerClass) || ascendancy == null || ascendancy.isEmpty()) {
            return;
        }

        // Parse the branch name from title format: "Ascendancy Skills - [AscendancyName] ([BranchName])"
        String branchName = extractBranchName(title);
        if (branchName == null) {
            if (debuggingFlag == 1) {
                plugin.getLogger().warning("Failed to extract branch name from title: " + title);
            }
            return;
        }

        // Get the current branch index
        List<String> branches = ascendancyBranches.get(ascendancy);
        if (branches == null) {
            return;
        }

        int branchIndex = branches.indexOf(branchName) + 1;

        // Handle navigation buttons
        if (event.getRawSlot() == 45 && branchIndex > 1) {
            // Previous branch button - delay slightly to avoid race conditions
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                ascendancySkillTreeGUI.openAscendancySkillTreeGUI(player, branchIndex - 1);
            }, 1L);
            return;
        } else if (event.getRawSlot() == 53 && branchIndex < branches.size()) {
            // Next branch button - delay slightly to avoid race conditions
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                ascendancySkillTreeGUI.openAscendancySkillTreeGUI(player, branchIndex + 1);
            }, 1L);
            return;
        }

        // Get node ID from slot for this specific branch
        int nodeId = getNodeIdFromSlot(event.getRawSlot(), ascendancy, branchName);

        if (nodeId <= 0) {
            return;
        }

        // Log the attempt for debugging
        if (debuggingFlag == 1) {
            plugin.getLogger().info("CLICK DETECTED: " + player.getName() +
                    " clicked on ascendancy skill " + nodeId +
                    " in branch " + branchName);
        }

        // Hand off to the purchase manager
        purchaseManager.requestAscendancyPurchase(player, nodeId, branchIndex);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();

        // Check if it's our ascendancy skill tree GUI
        if (title.startsWith(ChatColor.DARK_BLUE + "Ascendancy Skills")) {
            purchaseManager.handleInventoryClose(player);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("INVENTORY CLOSED: " + player.getName() +
                        " closed ascendancy skill tree GUI");
            }
        }
    }

    private String extractBranchName(String title) {
        // Format: "Ascendancy Skills - [AscendancyName] ([BranchName])"
        try {
            int start = title.lastIndexOf("(") + 1;
            int end = title.lastIndexOf(")");
            if (start > 0 && end > start) {
                return title.substring(start, end);
            }
        } catch (Exception e) {
            if (debuggingFlag == 1) {
                plugin.getLogger().warning("Failed to extract branch name from title: " + title);
                e.printStackTrace();
            }
        }
        return null;
    }

    private int getNodeIdFromSlot(int slot, String ascendancy, String branchName) {
        // Get the node positions for this branch
        Map<String, Map<Integer, Integer>> ascendancyBranches = branchNodePositions.get(ascendancy);
        if (ascendancyBranches == null) {
            return -1;
        }

        Map<Integer, Integer> slotToNodeMap = ascendancyBranches.get(branchName);
        if (slotToNodeMap == null) {
            return -1;
        }

        // Reverse lookup: find node ID from slot
        for (Map.Entry<Integer, Integer> entry : slotToNodeMap.entrySet()) {
            if (entry.getValue() == slot) {
                return entry.getKey();
            }
        }

        return -1;
    }
}