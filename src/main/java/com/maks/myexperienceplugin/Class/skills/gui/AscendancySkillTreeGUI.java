package com.maks.myexperienceplugin.Class.skills.gui;

import com.maks.myexperienceplugin.Class.skills.SkillTreeManager;
import com.maks.myexperienceplugin.Class.skills.base.SkillNode;
import com.maks.myexperienceplugin.Class.skills.base.SkillTree;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class AscendancySkillTreeGUI {
    private final MyExperiencePlugin plugin;
    private final SkillTreeManager skillTreeManager;

    // Debugging flag
    private final int debuggingFlag = 1;

    // Constants for GUI layout
    private static final int INVENTORY_SIZE = 54; // 6 rows
    private static final String GUI_TITLE_PREFIX = ChatColor.DARK_BLUE + "Ascendancy Skills";

    // Map to store the branch names for each ascendancy class
    private final Map<String, List<String>> ascendancyBranches;

    // Map to store the skill node positions for each branch of each ascendancy
    private final Map<String, Map<String, Map<Integer, Integer>>> branchNodePositions;

    // Map to store connection indicators (visual items that show connections between nodes)
    private final Map<String, Map<String, List<Integer>>> branchConnectionSlots;

    public AscendancySkillTreeGUI(MyExperiencePlugin plugin, SkillTreeManager skillTreeManager) {
        this.plugin = plugin;
        this.skillTreeManager = skillTreeManager;
        this.ascendancyBranches = new HashMap<>();
        this.branchNodePositions = new HashMap<>();
        this.branchConnectionSlots = new HashMap<>();

        // Initialize branch names and node positions
        initializeBranchNames();
        initializeNodePositions();
        initializeConnectionSlots();
    }

    private void initializeBranchNames() {
        // For Beastmaster - define the three branches based on wolves, boars, and bears
        List<String> beastmasterBranches = new ArrayList<>();
        beastmasterBranches.add("Wolf Path");
        beastmasterBranches.add("Boar Path");
        beastmasterBranches.add("Bear Path");
        ascendancyBranches.put("Beastmaster", beastmasterBranches);

        // For Berserker - define branches based on the theme
        List<String> berserkerBranches = new ArrayList<>();
        berserkerBranches.add("Rage Path");
        berserkerBranches.add("Critical Path");
        berserkerBranches.add("Frenzy Path");
        ascendancyBranches.put("Berserker", berserkerBranches);

        // Add more ascendancies as needed
    }

    private void initializeNodePositions() {
        // Beastmaster - Wolf Path (showing vertical progression)
        Map<String, Map<Integer, Integer>> beastmasterBranches = new HashMap<>();

        // Wolf Path nodes - vertical layout
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

        // Boar Path nodes - vertical layout
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

        // Bear Path nodes - vertical layout
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

        // Similar structure for Berserker with its three branches
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

        // Add more ascendancies as needed
    }

    private void initializeConnectionSlots() {
        // Define connections for Beastmaster Wolf Path
        Map<String, List<Integer>> beastmasterConnections = new HashMap<>();

        List<Integer> wolfConnections = new ArrayList<>();
        // Vertical connections between nodes
        wolfConnections.add(10+9);  // Connection between Wolf Summon and Wolf Speed
        wolfConnections.add(19+9);  // Connection between Wolf Speed and Wolf Attack Speed
        wolfConnections.add(28+9);  // Connection between Wolf Attack Speed and Wolf Critical/Vitality
        wolfConnections.add(37+9);  // Connection between Wolf Critical and Wolf Healing
        wolfConnections.add(38+9);  // Connection between Wolf Vitality and Wolf Health
        wolfConnections.add(46+1);  // Connection between Wolf Healing and Wolf Pack
        wolfConnections.add(47+2);  // Connection between Wolf Health and Wolf Pack
        beastmasterConnections.put("Wolf Path", wolfConnections);

        // Similar for other branches
        List<Integer> boarConnections = new ArrayList<>();
        boarConnections.add(12+9);  // Connection between Boar Summon and Boar Damage
        boarConnections.add(21+9);  // Connection between Boar Damage and Boar Attack Speed/Pack Damage
        boarConnections.add(30+9);  // Connection between Boar Attack Speed and Boar Critical
        boarConnections.add(31+9);  // Connection between Pack Damage and Pack Damage Plus
        boarConnections.add(39+9);  // Connection between Boar Critical and Boar Frenzy
        boarConnections.add(40+9);  // Connection between Pack Damage Plus and Boar Speed
        boarConnections.add(48+3);  // Connection between Boar Frenzy and Boar Rage
        beastmasterConnections.put("Boar Path", boarConnections);

        List<Integer> bearConnections = new ArrayList<>();
        bearConnections.add(14+9);  // Connection between Bear Summon and Bear Health
        bearConnections.add(23+9);  // Connection between Bear Health and Bear Defense
        bearConnections.add(32+9);  // Connection between Bear Defense and Bear Guardian/Vitality
        bearConnections.add(41+9);  // Connection between Bear Guardian and Bear Regeneration
        bearConnections.add(42+9);  // Connection between Bear Vitality and Pack Vitality
        bearConnections.add(50+3);  // Connection between Bear Regeneration and Pack Defense
        bearConnections.add(51+2);  // Connection between Pack Vitality and Pack Healing
        bearConnections.add(32+12); // Additional connection
        beastmasterConnections.put("Bear Path", bearConnections);

        branchConnectionSlots.put("Beastmaster", beastmasterConnections);

        // Similar structure for Berserker connections
        Map<String, List<Integer>> berserkerConnections = new HashMap<>();

        List<Integer> rageConnections = new ArrayList<>();
        rageConnections.add(10+9);  // Connection between Unarmored Rage and Kill Frenzy
        rageConnections.add(19+9);  // Connection between Kill Frenzy and Glass Cannon
        rageConnections.add(28+9);  // Connection between Glass Cannon and Finishing Blow
        rageConnections.add(37+9);  // Connection between Finishing Blow and Raw Power
        rageConnections.add(46+3);  // Connection between Raw Power and Death Defiance
        berserkerConnections.put("Rage Path", rageConnections);

        // Add other Berserker connections
        berserkerConnections.put("Critical Path", new ArrayList<>());
        berserkerConnections.put("Frenzy Path", new ArrayList<>());

        branchConnectionSlots.put("Berserker", berserkerConnections);
    }

    public void openAscendancySkillTreeGUI(Player player, int branchIndex) {
        UUID uuid = player.getUniqueId();
        String playerClass = plugin.getClassManager().getPlayerClass(uuid);
        String ascendancy = plugin.getClassManager().getPlayerAscendancy(uuid);

        if ("NoClass".equalsIgnoreCase(playerClass) || ascendancy.isEmpty()) {
            player.sendMessage(ChatColor.RED + "You need to choose a class and ascendancy first!");
            return;
        }

        // Get the skill tree for this ascendancy
        SkillTree tree = skillTreeManager.getSkillTree(playerClass, "ascendancy", ascendancy);
        if (tree == null) {
            player.sendMessage(ChatColor.RED + "Skill tree not found for your ascendancy: " + ascendancy);
            return;
        }

        // Get branches for this ascendancy
        List<String> branches = ascendancyBranches.get(ascendancy);
        if (branches == null || branches.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Branch configuration not found for: " + ascendancy);
            return;
        }

        // Make sure branchIndex is valid
        if (branchIndex < 1 || branchIndex > branches.size()) {
            branchIndex = 1;
        }

        // Get the current branch name
        String currentBranch = branches.get(branchIndex - 1);

        // Create inventory with branch name
        String title = GUI_TITLE_PREFIX + " - " + ascendancy + " (" + currentBranch + ")";
        Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE, title);

        // Get purchased skills
        Set<Integer> purchasedSkills = skillTreeManager.getPurchasedSkills(uuid);

        // Fill the inventory with background
        fillBackground(inventory);

        // Add skill points info
        addSkillPointsInfo(inventory, player);

        // Add navigation buttons
        addNavigationButtons(inventory, branchIndex, branches.size(), ascendancy);

        // Add branch info
        addBranchInfo(inventory, ascendancy, branchIndex, branches);

        // Add connection indicators
        addConnectionIndicators(inventory, ascendancy, currentBranch);

        // Add skill nodes for this branch
        addAscendancySkillNodes(inventory, player, tree, purchasedSkills, ascendancy, currentBranch);

        // Open the inventory
        player.openInventory(inventory);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Opened ascendancy skill tree GUI for " + player.getName() +
                    ", ascendancy: " + ascendancy + ", branch: " + currentBranch);
        }
    }

    private void fillBackground(Inventory inventory) {
        ItemStack background = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = background.getItemMeta();
        meta.setDisplayName(" ");
        background.setItemMeta(meta);

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, background);
        }
    }

    private void addSkillPointsInfo(Inventory inventory, Player player) {
        UUID uuid = player.getUniqueId();
        int unusedPoints = skillTreeManager.getUnusedAscendancySkillPoints(uuid);

        ItemStack pointsItem = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = pointsItem.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Ascendancy Points: " + ChatColor.GREEN + unusedPoints);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Click on skills to spend your points");
        lore.add(ChatColor.GRAY + "You have " + ChatColor.YELLOW + unusedPoints +
                ChatColor.GRAY + " ascendancy points");

        meta.setLore(lore);
        pointsItem.setItemMeta(meta);

        inventory.setItem(4, pointsItem);
    }

    private void addNavigationButtons(Inventory inventory, int currentBranchIndex, int totalBranches, String ascendancy) {
        // Previous branch button (if not on first branch)
        if (currentBranchIndex > 1) {
            ItemStack prevButton = new ItemStack(Material.ARROW);
            ItemMeta meta = prevButton.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "Previous Branch");
            prevButton.setItemMeta(meta);
            inventory.setItem(45, prevButton);
        }

        // Next branch button (if not on last branch)
        if (currentBranchIndex < totalBranches) {
            ItemStack nextButton = new ItemStack(Material.ARROW);
            ItemMeta meta = nextButton.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "Next Branch");
            nextButton.setItemMeta(meta);
            inventory.setItem(53, nextButton);
        }

        // Branch indicator in the middle bottom
        ItemStack branchIndicator = new ItemStack(Material.PAPER);
        ItemMeta meta = branchIndicator.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Branch " + currentBranchIndex + " of " + totalBranches);

        // Add description of what each branch focuses on
        List<String> lore = new ArrayList<>();
        if ("Beastmaster".equals(ascendancy)) {
            if (currentBranchIndex == 1) {
                lore.add(ChatColor.GRAY + "Wolf Path: Focus on wolves and their abilities");
            } else if (currentBranchIndex == 2) {
                lore.add(ChatColor.GRAY + "Boar Path: Focus on boars and damage");
            } else if (currentBranchIndex == 3) {
                lore.add(ChatColor.GRAY + "Bear Path: Focus on bears and defensive abilities");
            }
        } else if ("Berserker".equals(ascendancy)) {
            if (currentBranchIndex == 1) {
                lore.add(ChatColor.GRAY + "Rage Path: Focus on damage and power");
            } else if (currentBranchIndex == 2) {
                lore.add(ChatColor.GRAY + "Critical Path: Focus on critical hits");
            } else if (currentBranchIndex == 3) {
                lore.add(ChatColor.GRAY + "Frenzy Path: Focus on attack speed and combat bonuses");
            }
        }
        meta.setLore(lore);

        branchIndicator.setItemMeta(meta);
        inventory.setItem(49, branchIndicator);
    }

    private void addBranchInfo(Inventory inventory, String ascendancy, int branchIndex, List<String> branches) {
        String branchName = branches.get(branchIndex - 1);

        ItemStack branchInfoItem = createBranchInfoItem(ascendancy, branchName);
        inventory.setItem(0, branchInfoItem);
    }

    private ItemStack createBranchInfoItem(String ascendancy, String branchName) {
        Material material;
        List<String> lore = new ArrayList<>();

        // Choose appropriate material and lore based on ascendancy and branch
        if ("Beastmaster".equals(ascendancy)) {
            if ("Wolf Path".equals(branchName)) {
                material = Material.BONE;
                lore.add(ChatColor.GRAY + "Focus on wolves and their abilities");
                lore.add(ChatColor.GRAY + "• Wolf companions and synergies");
                lore.add(ChatColor.GRAY + "• Healing and bonuses from wolves");
                lore.add(ChatColor.GRAY + "• Pack tactics with multiple wolves");
            } else if ("Boar Path".equals(branchName)) {
                material = Material.PORKCHOP;
                lore.add(ChatColor.GRAY + "Focus on boars and offensive power");
                lore.add(ChatColor.GRAY + "• High damage boar companions");
                lore.add(ChatColor.GRAY + "• Critical strikes and frenzy");
                lore.add(ChatColor.GRAY + "• Pack damage improvements");
            } else {
                material = Material.HONEY_BOTTLE;
                lore.add(ChatColor.GRAY + "Focus on bears and defensive abilities");
                lore.add(ChatColor.GRAY + "• Tanky bear companions");
                lore.add(ChatColor.GRAY + "• Defensive bonuses for your party");
                lore.add(ChatColor.GRAY + "• Health regeneration and vitality");
            }
        } else if ("Berserker".equals(ascendancy)) {
            if ("Rage Path".equals(branchName)) {
                material = Material.REDSTONE;
                lore.add(ChatColor.GRAY + "Focus on rage and raw power");
                lore.add(ChatColor.GRAY + "• Sacrificing defense for damage");
                lore.add(ChatColor.GRAY + "• Building power through combat");
                lore.add(ChatColor.GRAY + "• Finishing moves and bonuses at low health");
            } else if ("Critical Path".equals(branchName)) {
                material = Material.DIAMOND_AXE;
                lore.add(ChatColor.GRAY + "Focus on critical hits and precision");
                lore.add(ChatColor.GRAY + "• Increased critical chance and damage");
                lore.add(ChatColor.GRAY + "• Bleeding effects from critical hits");
                lore.add(ChatColor.GRAY + "• Critical hit mastery abilities");
            } else {
                material = Material.SUGAR;
                lore.add(ChatColor.GRAY + "Focus on attack speed and combat momentum");
                lore.add(ChatColor.GRAY + "• Attack speed bonuses");
                lore.add(ChatColor.GRAY + "• Movement and agility improvements");
                lore.add(ChatColor.GRAY + "• Gaining power through continuous combat");
            }
        } else {
            material = Material.PAPER;
            lore.add(ChatColor.GRAY + "Branch information not available");
        }

        ItemStack infoItem = new ItemStack(material);
        ItemMeta meta = infoItem.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + branchName + " Information");
        meta.setLore(lore);
        infoItem.setItemMeta(meta);

        return infoItem;
    }

    private void addConnectionIndicators(Inventory inventory, String ascendancy, String branchName) {
        Map<String, List<Integer>> ascendancyConnections = branchConnectionSlots.get(ascendancy);
        if (ascendancyConnections == null) {
            return;
        }

        List<Integer> connections = ascendancyConnections.get(branchName);
        if (connections == null) {
            return;
        }

        ItemStack connectionItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = connectionItem.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GRAY + "↓");
        connectionItem.setItemMeta(meta);

        for (int slot : connections) {
            inventory.setItem(slot, connectionItem);
        }
    }

    private void addAscendancySkillNodes(Inventory inventory, Player player, SkillTree tree,
                                         Set<Integer> purchasedSkills, String ascendancy, String branchName) {
        UUID uuid = player.getUniqueId();

        Map<String, Map<Integer, Integer>> ascendancyBranches = branchNodePositions.get(ascendancy);
        if (ascendancyBranches == null) {
            if (debuggingFlag == 1) {
                plugin.getLogger().warning("No node positions defined for ascendancy: " + ascendancy);
            }
            return;
        }

        Map<Integer, Integer> positions = ascendancyBranches.get(branchName);
        if (positions == null) {
            if (debuggingFlag == 1) {
                plugin.getLogger().warning("No node positions defined for branch " + branchName +
                        " of ascendancy: " + ascendancy);
            }
            return;
        }

        for (Map.Entry<Integer, Integer> entry : positions.entrySet()) {
            int nodeId = entry.getKey();
            int slot = entry.getValue();

            SkillNode node = tree.getNode(nodeId);
            if (node == null) {
                if (debuggingFlag == 1) {
                    plugin.getLogger().warning("Node " + nodeId + " not found in tree for " +
                            ascendancy);
                }
                continue;
            }

            boolean isPurchased = purchasedSkills.contains(nodeId);
            boolean canPurchase = !isPurchased && skillTreeManager.canPurchaseAscendancySkill(player, nodeId);
            int purchaseCount = skillTreeManager.getSkillPurchaseCount(uuid, nodeId);

            ItemStack nodeItem = createNodeItem(node, isPurchased, canPurchase, purchaseCount);
            inventory.setItem(slot, nodeItem);
        }
    }

    private ItemStack createNodeItem(SkillNode node, boolean isPurchased, boolean canPurchase,
                                     int purchaseCount) {
        Material material = node.getIcon();
        if (isPurchased) {
            // Use a different material for purchased nodes
            if (purchaseCount >= node.getMaxPurchases()) {
                material = Material.EMERALD_BLOCK; // Fully purchased
            } else {
                material = Material.EMERALD; // Partially purchased
            }
        } else if (canPurchase) {
            // Use a different material for available nodes
            material = Material.LIME_CONCRETE;
        } else {
            // Use a different material for locked nodes
            material = Material.RED_CONCRETE;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String displayName = (isPurchased ? ChatColor.GREEN : (canPurchase ? ChatColor.YELLOW : ChatColor.RED))
                + node.getName();
        if (node.getMaxPurchases() > 1 && isPurchased) {
            displayName += ChatColor.GRAY + " (" + purchaseCount + "/" + node.getMaxPurchases() + ")";
        }

        meta.setDisplayName(displayName);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + node.getDescription());
        lore.add("");

        if (isPurchased) {
            if (purchaseCount < node.getMaxPurchases()) {
                lore.add(ChatColor.GOLD + "Cost: " + ChatColor.YELLOW + "1 point");
                lore.add(ChatColor.GREEN + "Click to upgrade! (" + purchaseCount + "/" +
                        node.getMaxPurchases() + ")");
            } else {
                lore.add(ChatColor.GREEN + "Fully upgraded! (" + purchaseCount + "/" +
                        node.getMaxPurchases() + ")");
            }
        } else if (canPurchase) {
            lore.add(ChatColor.GOLD + "Cost: " + ChatColor.YELLOW + "1 point");
            lore.add(ChatColor.GREEN + "Click to purchase!");
        } else {
            lore.add(ChatColor.RED + "Locked - Purchase connected skills first");
            lore.add(ChatColor.GOLD + "Cost: " + ChatColor.YELLOW + "1 point");
        }

        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);

        return item;
    }
}