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

import java.util.Arrays;
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

        // For FlameWarden - define branches
        List<String> flameWardenBranches = new java.util.ArrayList<>();
        flameWardenBranches.add("Ignite Path");
        flameWardenBranches.add("Burning Damage Path");
        flameWardenBranches.add("Fire Defense Path");
        ascendancyBranches.put("FlameWarden", flameWardenBranches);

        // For ScaleGuardian - define branches
        List<String> scaleGuardianBranches = new java.util.ArrayList<>();
        scaleGuardianBranches.add("Shield Path");
        scaleGuardianBranches.add("Defense Path");
        scaleGuardianBranches.add("Protection Path");
        ascendancyBranches.put("ScaleGuardian", scaleGuardianBranches);

        // For Shadowstalker - define branches
        List<String> shadowstalkerBranches = new java.util.ArrayList<>();
        shadowstalkerBranches.add("Stealth Path");
        shadowstalkerBranches.add("Critical Path");
        shadowstalkerBranches.add("Poison Path");
        ascendancyBranches.put("Shadowstalker", shadowstalkerBranches);

        // For Earthwarden - define branches
        List<String> earthwardenBranches = new java.util.ArrayList<>();
        earthwardenBranches.add("Defense Path");
        earthwardenBranches.add("Healing Path");
        earthwardenBranches.add("Nature Path");
        ascendancyBranches.put("Earthwarden", earthwardenBranches);
    }

    private void initializeNodePositions() {
        // Zamiast przypisywać nową mapę, czyścimy i wypełniamy istniejącą
        branchNodePositions.clear();

        // Tworzymy jeden uniwersalny układ dla wszystkich podklas
        Map<String, Map<Integer, Integer>> universalBranchPositions = new HashMap<>();

        // Branch 1 (Wolf Path / Rage Path / etc.)
        Map<Integer, Integer> branch1Positions = new HashMap<>();
        branch1Positions.put(1, 11);  // Skill 1 w slocie 11
        branch1Positions.put(4, 20);  // Skill 4 w slocie 20
        branch1Positions.put(7, 29);  // Skill 7 w slocie 29
        branch1Positions.put(11, 37); // Skill 11 w slocie 37
        branch1Positions.put(12, 39); // Skill 12 w slocie 39
        branch1Positions.put(17, 46); // Skill 17 w slocie 46
        branch1Positions.put(18, 48); // Skill 18 w slocie 48
        branch1Positions.put(22, 15); // Skill 22 w slocie 15
        branch1Positions.put(25, 24); // Skill 25 w slocie 24
        universalBranchPositions.put("Branch1", branch1Positions);

        // Branch 2 (Boar Path / Critical Path / etc.)
        Map<Integer, Integer> branch2Positions = new HashMap<>();
        branch2Positions.put(2, 11);  // Skill 2 w slocie 11
        branch2Positions.put(5, 20);  // Skill 5 w slocie 20
        branch2Positions.put(8, 28);  // Skill 8 w slocie 28
        branch2Positions.put(9, 30);  // Skill 9 w slocie 30
        branch2Positions.put(13, 37); // Skill 13 w slocie 37
        branch2Positions.put(14, 39); // Skill 14 w slocie 39
        branch2Positions.put(19, 46); // Skill 19 w slocie 46
        branch2Positions.put(23, 14); // Skill 23 w slocie 14
        branch2Positions.put(26, 23); // Skill 26 w slocie 23
        universalBranchPositions.put("Branch2", branch2Positions);

        // Branch 3 (Bear Path / Frenzy Path / etc.)
        Map<Integer, Integer> branch3Positions = new HashMap<>();
        branch3Positions.put(3, 11);  // Skill 3 w slocie 11
        branch3Positions.put(6, 20);  // Skill 6 w slocie 20
        branch3Positions.put(10, 29); // Skill 10 w slocie 29
        branch3Positions.put(15, 37); // Skill 15 w slocie 37
        branch3Positions.put(16, 39); // Skill 16 w slocie 39
        branch3Positions.put(20, 46); // Skill 20 w slocie 46
        branch3Positions.put(21, 48); // Skill 21 w slocie 48
        branch3Positions.put(24, 14); // Skill 24 w slocie 14
        branch3Positions.put(27, 23); // Skill 27 w slocie 23
        universalBranchPositions.put("Branch3", branch3Positions);

        // Definicje nazw gałęzi dla podklas
        Map<String, List<String>> branchNames = new HashMap<>();
        branchNames.put("Beastmaster", Arrays.asList("Wolf Path", "Boar Path", "Bear Path"));
        branchNames.put("Berserker", Arrays.asList("Rage Path", "Critical Path", "Frenzy Path"));
        branchNames.put("FlameWarden", Arrays.asList("Ignite Path", "Burning Damage Path", "Fire Defense Path"));
        branchNames.put("ScaleGuardian", Arrays.asList("Shield Path", "Defense Path", "Protection Path"));
        branchNames.put("Shadowstalker", Arrays.asList("Stealth Path", "Critical Path", "Poison Path"));
        branchNames.put("Earthwarden", Arrays.asList("Defense Path", "Healing Path", "Nature Path"));

        // Utworzenie mapowań dla każdej podklasy
        for (String ascendancy : branchNames.keySet()) {
            Map<String, Map<Integer, Integer>> branches = new HashMap<>();
            List<String> names = branchNames.get(ascendancy);

            for (int i = 0; i < names.size(); i++) {
                String branchName = names.get(i);
                Map<Integer, Integer> positions = new HashMap<>();
                Map<Integer, Integer> universalMap = universalBranchPositions.get("Branch" + (i+1));

                // Zastosuj odpowiedni prefix ID w zależności od podklasy
                int idOffset = 0;
                if (ascendancy.equals("Beastmaster")) idOffset = 100000;
                else if (ascendancy.equals("Berserker")) idOffset = 200000;
                else if (ascendancy.equals("FlameWarden")) idOffset = 300000;
                else if (ascendancy.equals("ScaleGuardian")) idOffset = 400000;
                else if (ascendancy.equals("Shadowstalker")) idOffset = 500000;
                else if (ascendancy.equals("Earthwarden")) idOffset = 600000;

                for (Map.Entry<Integer, Integer> entry : universalMap.entrySet()) {
                    positions.put(entry.getKey() + idOffset, entry.getValue());
                }

                branches.put(branchName, positions);
            }

            // Dodajemy do istniejącej mapy zamiast przypisania
            branchNodePositions.put(ascendancy, branches);
        }

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Zainicjalizowano uniwersalny układ dla podklas w listenerze");
        }
    }    @EventHandler(priority = EventPriority.HIGHEST)
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
