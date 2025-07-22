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

        // For FlameWarden - define branches based on the theme
        List<String> flameWardenBranches = new ArrayList<>();
        flameWardenBranches.add("Ignite Path");
        flameWardenBranches.add("Burning Damage Path");
        flameWardenBranches.add("Fire Defense Path");
        ascendancyBranches.put("FlameWarden", flameWardenBranches);

        // For ScaleGuardian - define branches based on the theme
        List<String> scaleGuardianBranches = new ArrayList<>();
        scaleGuardianBranches.add("Shield Path");
        scaleGuardianBranches.add("Defense Path");
        scaleGuardianBranches.add("Protection Path");
        ascendancyBranches.put("ScaleGuardian", scaleGuardianBranches);

        // For Shadowstalker - define branches based on the theme
        List<String> shadowstalkerBranches = new ArrayList<>();
        shadowstalkerBranches.add("Stealth Path");
        shadowstalkerBranches.add("Critical Path");
        shadowstalkerBranches.add("Poison Path");
        ascendancyBranches.put("Shadowstalker", shadowstalkerBranches);

        // For Earthwarden - define branches based on the theme
        List<String> earthwardenBranches = new ArrayList<>();
        earthwardenBranches.add("Defense Path");
        earthwardenBranches.add("Healing Path");
        earthwardenBranches.add("Nature Path");
        ascendancyBranches.put("Earthwarden", earthwardenBranches);

        // Add more ascendancies as needed
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

        // Nazwy gałęzi dla każdej podklasy
        Map<String, List<String>> branchNames = new HashMap<>();

        // Beastmaster
        List<String> beastmasterBranches = Arrays.asList("Wolf Path", "Boar Path", "Bear Path");
        branchNames.put("Beastmaster", beastmasterBranches);

        // Berserker
        List<String> berserkerBranches = Arrays.asList("Rage Path", "Critical Path", "Frenzy Path");
        branchNames.put("Berserker", berserkerBranches);

        // FlameWarden
        List<String> flameWardenBranches = Arrays.asList("Ignite Path", "Burning Damage Path", "Fire Defense Path");
        branchNames.put("FlameWarden", flameWardenBranches);

        // ScaleGuardian
        List<String> scaleGuardianBranches = Arrays.asList("Shield Path", "Defense Path", "Protection Path");
        branchNames.put("ScaleGuardian", scaleGuardianBranches);

        // Shadowstalker
        List<String> shadowstalkerBranches = Arrays.asList("Stealth Path", "Critical Path", "Poison Path");
        branchNames.put("Shadowstalker", shadowstalkerBranches);

        // Earthwarden
        List<String> earthwardenBranches = Arrays.asList("Defense Path", "Healing Path", "Nature Path");
        branchNames.put("Earthwarden", earthwardenBranches);

        // Inicjalizacja układów dla wszystkich podklas
        for (String ascendancy : branchNames.keySet()) {
            Map<String, Map<Integer, Integer>> branches = new HashMap<>();
            List<String> names = branchNames.get(ascendancy);

            for (int i = 0; i < names.size(); i++) {
                String branchName = names.get(i);
                Map<Integer, Integer> universalPositions = universalBranchPositions.get("Branch" + (i+1));
                Map<Integer, Integer> branchPositions = new HashMap<>();

                // Przekształcenie ID skilli w zależności od podklasy
                int idOffset = 0;
                if (ascendancy.equals("Beastmaster")) idOffset = 100000;
                else if (ascendancy.equals("Berserker")) idOffset = 200000;
                else if (ascendancy.equals("FlameWarden")) idOffset = 300000;
                else if (ascendancy.equals("ScaleGuardian")) idOffset = 400000;
                else if (ascendancy.equals("Shadowstalker")) idOffset = 500000;
                else if (ascendancy.equals("Earthwarden")) idOffset = 600000;

                for (Map.Entry<Integer, Integer> entry : universalPositions.entrySet()) {
                    branchPositions.put(entry.getKey() + idOffset, entry.getValue());
                }

                branches.put(branchName, branchPositions);
            }

            // Dodajemy do istniejącej mapy zamiast przypisania
            branchNodePositions.put(ascendancy, branches);
        }

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Zainicjalizowano uniwersalny układ drzewka podklas");
            for (String ascendancy : branchNodePositions.keySet()) {
                plugin.getLogger().info("Podklasa: " + ascendancy + " - gałęzie: " +
                        branchNodePositions.get(ascendancy).keySet());
            }
        }
    }    private void initializeConnectionSlots() {
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

        // FlameWarden connections
        Map<String, List<Integer>> flameWardenConnections = new HashMap<>();

        List<Integer> ignitePath = new ArrayList<>();
        ignitePath.add(10+9);  // Connection between node 1 and node 4
        ignitePath.add(19+9);  // Connection between node 4 and node 7
        ignitePath.add(28+9);  // Connection between node 7 and node 12
        ignitePath.add(37+9);  // Connection between node 12 and node 17
        ignitePath.add(46+3);  // Connection between node 17 and node 21
        ignitePath.add(49+3);  // Connection between node 21 and node 25
        flameWardenConnections.put("Ignite Path", ignitePath);

        List<Integer> burningDamagePath = new ArrayList<>();
        burningDamagePath.add(12+9);  // Connection between node 2 and node 5
        burningDamagePath.add(21+9);  // Connection between node 5 and node 8/9
        burningDamagePath.add(30+9);  // Connection between node 8 and node 13
        burningDamagePath.add(31+9);  // Connection between node 9 and node 14
        burningDamagePath.add(39+9);  // Connection between node 13 and node 19
        burningDamagePath.add(40+9);  // Connection between node 14 and node 20
        burningDamagePath.add(48+3);  // Connection between node 19 and node 24
        burningDamagePath.add(49+3);  // Connection between node 20 and node 26
        burningDamagePath.add(51+3);  // Connection between node 24 and node 26
        flameWardenConnections.put("Burning Damage Path", burningDamagePath);

        List<Integer> fireDefensePath = new ArrayList<>();
        fireDefensePath.add(14+9);  // Connection between node 3 and node 6
        fireDefensePath.add(23+9);  // Connection between node 6 and node 10/11
        fireDefensePath.add(32+9);  // Connection between node 10 and node 15
        fireDefensePath.add(33+9);  // Connection between node 11 and node 16
        fireDefensePath.add(41+9);  // Connection between node 15 and node 18
        fireDefensePath.add(42+9);  // Connection between node 16 and node 22
        fireDefensePath.add(50+3);  // Connection between node 18 and node 23
        fireDefensePath.add(51+3);  // Connection between node 22 and node 23
        fireDefensePath.add(52+3);  // Connection between node 23 and node 27
        flameWardenConnections.put("Fire Defense Path", fireDefensePath);

        branchConnectionSlots.put("FlameWarden", flameWardenConnections);

        // ScaleGuardian connections
        Map<String, List<Integer>> scaleGuardianConnections = new HashMap<>();

        List<Integer> shieldPath = new ArrayList<>();
        shieldPath.add(10+9);  // Connection between node 1 and node 4
        shieldPath.add(19+9);  // Connection between node 4 and node 9
        shieldPath.add(28+9);  // Connection between node 9 and node 13/18
        shieldPath.add(37+9);  // Connection between node 13 and node 17
        shieldPath.add(46+3);  // Connection between node 17 and node 24
        shieldPath.add(47+3);  // Connection between node 18 and node 24
        shieldPath.add(51+3);  // Connection between node 24 and node 25
        scaleGuardianConnections.put("Shield Path", shieldPath);

        List<Integer> defensePath = new ArrayList<>();
        defensePath.add(12+9);  // Connection between node 2 and node 5
        defensePath.add(21+9);  // Connection between node 5 and node 8
        defensePath.add(29+9);  // Connection between node 8 and node 11/12
        defensePath.add(37+9);  // Connection between node 11 and node 15
        defensePath.add(38+9);  // Connection between node 12 and node 20
        defensePath.add(46+3);  // Connection between node 15 and node 21
        defensePath.add(47+3);  // Connection between node 20 and node 23
        defensePath.add(50+3);  // Connection between node 21 and node 23
        defensePath.add(51+3);  // Connection between node 23 and node 26
        scaleGuardianConnections.put("Defense Path", defensePath);

        List<Integer> protectionPath = new ArrayList<>();
        protectionPath.add(14+9);  // Connection between node 3 and node 6/7
        protectionPath.add(23+9);  // Connection between node 6 and node 10
        protectionPath.add(24+9);  // Connection between node 7 and node 14
        protectionPath.add(32+9);  // Connection between node 10 and node 16
        protectionPath.add(33+9);  // Connection between node 14 and node 19
        protectionPath.add(41+9);  // Connection between node 16 and node 22
        protectionPath.add(42+9);  // Connection between node 19 and node 22
        protectionPath.add(50+3);  // Connection between node 22 and node 27
        scaleGuardianConnections.put("Protection Path", protectionPath);

        branchConnectionSlots.put("ScaleGuardian", scaleGuardianConnections);

        // Shadowstalker connections
        Map<String, List<Integer>> shadowstalkerConnections = new HashMap<>();

        List<Integer> stealthPath = new ArrayList<>();
        stealthPath.add(10+9);  // Connection between node 1 and node 4
        stealthPath.add(19+9);  // Connection between node 4 and node 6/7
        stealthPath.add(28+9);  // Connection between node 6 and node 12
        stealthPath.add(29+9);  // Connection between node 7 and node 10
        stealthPath.add(37+9);  // Connection between node 12 and node 16
        stealthPath.add(38+9);  // Connection between node 10 and node 22
        stealthPath.add(46+3);  // Connection between node 16 and node 25
        stealthPath.add(47+3);  // Connection between node 22 and node 25
        shadowstalkerConnections.put("Stealth Path", stealthPath);

        List<Integer> criticalPath = new ArrayList<>();
        criticalPath.add(12+9);  // Connection between node 2 and node 5
        criticalPath.add(21+9);  // Connection between node 5 and node 8
        criticalPath.add(30+9);  // Connection between node 8 and node 11
        criticalPath.add(39+9);  // Connection between node 11 and node 17
        criticalPath.add(48+3);  // Connection between node 17 and node 20
        criticalPath.add(51+3);  // Connection between node 20 and node 26
        shadowstalkerConnections.put("Critical Path", criticalPath);

        List<Integer> poisonPath = new ArrayList<>();
        poisonPath.add(14+9);  // Connection between node 3 and node 9
        poisonPath.add(23+9);  // Connection between node 9 and node 13/14/15
        poisonPath.add(32+9);  // Connection between node 13 and node 18
        poisonPath.add(33+9);  // Connection between node 14 and node 19
        poisonPath.add(34+9);  // Connection between node 15 and node 21
        poisonPath.add(41+9);  // Connection between node 18 and node 23
        poisonPath.add(42+9);  // Connection between node 19 and node 24
        poisonPath.add(43+9);  // Connection between node 21 and node 24
        poisonPath.add(50+3);  // Connection between node 23 and node 27
        poisonPath.add(51+3);  // Connection between node 24 and node 27
        shadowstalkerConnections.put("Poison Path", poisonPath);

        branchConnectionSlots.put("Shadowstalker", shadowstalkerConnections);

        // Earthwarden connections
        Map<String, List<Integer>> earthwardenConnections = new HashMap<>();

        List<Integer> earthDefensePath = new ArrayList<>();
        earthDefensePath.add(10+9);  // Connection between node 1 and node 4/5
        earthDefensePath.add(19+9);  // Connection between node 4 and node 7
        earthDefensePath.add(20+9);  // Connection between node 5 and node 10
        earthDefensePath.add(28+9);  // Connection between node 7 and node 11/12
        earthDefensePath.add(29+9);  // Connection between node 10 and node 15
        earthDefensePath.add(37+9);  // Connection between node 11 and node 16
        earthDefensePath.add(38+9);  // Connection between node 12 and node 18
        earthDefensePath.add(39+9);  // Connection between node 15 and node 23
        earthDefensePath.add(46+3);  // Connection between node 16 and node 25
        earthDefensePath.add(47+3);  // Connection between node 18 and node 25
        earthDefensePath.add(48+3);  // Connection between node 23 and node 25
        earthwardenConnections.put("Defense Path", earthDefensePath);

        List<Integer> healingPath = new ArrayList<>();
        healingPath.add(12+9);  // Connection between node 2 and node 6/9
        healingPath.add(21+9);  // Connection between node 6 and node 13
        healingPath.add(22+9);  // Connection between node 9 and node 17
        healingPath.add(30+9);  // Connection between node 13 and node 19
        healingPath.add(31+9);  // Connection between node 17 and node 21
        healingPath.add(39+9);  // Connection between node 19 and node 24
        healingPath.add(40+9);  // Connection between node 21 and node 24
        healingPath.add(49+3);  // Connection between node 24 and node 26
        earthwardenConnections.put("Healing Path", healingPath);

        List<Integer> naturePath = new ArrayList<>();
        naturePath.add(14+9);  // Connection between node 3 and node 8
        naturePath.add(23+9);  // Connection between node 8 and node 14
        naturePath.add(32+9);  // Connection between node 14 and node 20
        naturePath.add(41+9);  // Connection between node 20 and node 22
        naturePath.add(50+3);  // Connection between node 22 and node 27
        earthwardenConnections.put("Nature Path", naturePath);

        branchConnectionSlots.put("Earthwarden", earthwardenConnections);
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
        } else if ("FlameWarden".equals(ascendancy)) {
            if ("Ignite Path".equals(branchName)) {
                material = Material.FLINT_AND_STEEL;
                lore.add(ChatColor.GRAY + "Focus on igniting enemies");
                lore.add(ChatColor.GRAY + "• Increased chance to ignite enemies");
                lore.add(ChatColor.GRAY + "• Burning aura and momentum");
                lore.add(ChatColor.GRAY + "• Damage scaling with burning enemies");
            } else if ("Burning Damage Path".equals(branchName)) {
                material = Material.BLAZE_POWDER;
                lore.add(ChatColor.GRAY + "Focus on damage against burning enemies");
                lore.add(ChatColor.GRAY + "• Bonus damage against burning enemies");
                lore.add(ChatColor.GRAY + "• Critical hits cause additional burning");
                lore.add(ChatColor.GRAY + "• Spreading flames between enemies");
            } else {
                material = Material.MAGMA_CREAM;
                lore.add(ChatColor.GRAY + "Focus on fire-based defense");
                lore.add(ChatColor.GRAY + "• Fire resistance and healing");
                lore.add(ChatColor.GRAY + "• Fire nova and protection");
                lore.add(ChatColor.GRAY + "• Defensive bonuses from burning enemies");
            }
        } else if ("ScaleGuardian".equals(ascendancy)) {
            if ("Shield Path".equals(branchName)) {
                material = Material.SHIELD;
                lore.add(ChatColor.GRAY + "Focus on shield blocking and reflection");
                lore.add(ChatColor.GRAY + "• Increased shield block chance");
                lore.add(ChatColor.GRAY + "• Damage reflection when blocking");
                lore.add(ChatColor.GRAY + "• Health restoration from successful blocks");
            } else if ("Defense Path".equals(branchName)) {
                material = Material.IRON_CHESTPLATE;
                lore.add(ChatColor.GRAY + "Focus on defensive capabilities");
                lore.add(ChatColor.GRAY + "• Increased defense and armor");
                lore.add(ChatColor.GRAY + "• Resistance to knockback effects");
                lore.add(ChatColor.GRAY + "• Defensive bonuses when stationary");
            } else {
                material = Material.TOTEM_OF_UNDYING;
                lore.add(ChatColor.GRAY + "Focus on protecting allies");
                lore.add(ChatColor.GRAY + "• Defensive auras for nearby allies");
                lore.add(ChatColor.GRAY + "• Taunting enemies to focus on you");
                lore.add(ChatColor.GRAY + "• Last resort survival abilities");
            }
        } else if ("Shadowstalker".equals(ascendancy)) {
            if ("Stealth Path".equals(branchName)) {
                material = Material.GRAY_DYE;
                lore.add(ChatColor.GRAY + "Focus on stealth and evasion");
                lore.add(ChatColor.GRAY + "• Increased movement speed in shadows");
                lore.add(ChatColor.GRAY + "• Reduced enemy detection range");
                lore.add(ChatColor.GRAY + "• Evasion bonuses while sneaking");
            } else if ("Critical Path".equals(branchName)) {
                material = Material.DIAMOND_SWORD;
                lore.add(ChatColor.GRAY + "Focus on critical hits and precision");
                lore.add(ChatColor.GRAY + "• Increased critical hit chance");
                lore.add(ChatColor.GRAY + "• Enhanced critical damage");
                lore.add(ChatColor.GRAY + "• Special effects on critical hits");
            } else {
                material = Material.SPIDER_EYE;
                lore.add(ChatColor.GRAY + "Focus on poison and debuffs");
                lore.add(ChatColor.GRAY + "• Poisoned attacks and bleeding");
                lore.add(ChatColor.GRAY + "• Enhanced damage against poisoned enemies");
                lore.add(ChatColor.GRAY + "• Toxic amplification abilities");
            }
        } else if ("Earthwarden".equals(ascendancy)) {
            if ("Defense Path".equals(branchName)) {
                material = Material.SHIELD;
                lore.add(ChatColor.GRAY + "Focus on defensive capabilities");
                lore.add(ChatColor.GRAY + "• Increased defense in natural areas");
                lore.add(ChatColor.GRAY + "• Defensive bonuses when stationary");
                lore.add(ChatColor.GRAY + "• Survival instincts when wounded");
            } else if ("Healing Path".equals(branchName)) {
                material = Material.GOLDEN_APPLE;
                lore.add(ChatColor.GRAY + "Focus on healing and regeneration");
                lore.add(ChatColor.GRAY + "• Enhanced healing from all sources");
                lore.add(ChatColor.GRAY + "• Health restoration abilities");
                lore.add(ChatColor.GRAY + "• Healing bonuses for allies");
            } else {
                material = Material.GRASS_BLOCK;
                lore.add(ChatColor.GRAY + "Focus on nature-based abilities");
                lore.add(ChatColor.GRAY + "• Environmental resistance");
                lore.add(ChatColor.GRAY + "• Entangling strikes against enemies");
                lore.add(ChatColor.GRAY + "• Nature's blessing abilities");
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
        // Całkowicie usunięte dodawanie wskaźników połączeń
        // Ta metoda pozostaje pusta, ale jest nadal wywoływana z głównej metody,
        // aby nie wprowadzać dodatkowych zmian w strukturze kodu

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Pomijanie dodawania wskaźników połączeń w drzewku " +
                    ascendancy + ", gałąź: " + branchName);
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

    private ItemStack createNodeItem(SkillNode node, boolean isPurchased, boolean canPurchase, int purchaseCount) {
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

        // Use the actual cost from the skill node
        int actualCost = node.getCost();

        if (isPurchased) {
            if (purchaseCount < node.getMaxPurchases()) {
                lore.add(ChatColor.GOLD + "Cost: " + ChatColor.YELLOW + actualCost + 
                        (actualCost == 1 ? " point" : " points"));
                lore.add(ChatColor.GREEN + "Click to upgrade! (" + purchaseCount + "/" +
                        node.getMaxPurchases() + ")");
            } else {
                lore.add(ChatColor.GREEN + "Fully upgraded! (" + purchaseCount + "/" +
                        node.getMaxPurchases() + ")");
            }
        } else if (canPurchase) {
            lore.add(ChatColor.GOLD + "Cost: " + ChatColor.YELLOW + actualCost + 
                    (actualCost == 1 ? " point" : " points"));
            lore.add(ChatColor.GREEN + "Click to purchase!");
        } else {
            lore.add(ChatColor.RED + "Locked - Purchase connected skills first");
            lore.add(ChatColor.GOLD + "Cost: " + ChatColor.YELLOW + actualCost + 
                    (actualCost == 1 ? " point" : " points"));
        }

        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);

        return item;
    }
}
