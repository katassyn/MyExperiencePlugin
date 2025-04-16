package com.maks.myexperienceplugin.Class.skills;

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

public class SkillTreeGUI {
    private final MyExperiencePlugin plugin;
    private final SkillTreeManager skillTreeManager;
    private final int debuggingFlag = 1; // Add debugging flag directly in the class

    // Constants for GUI layout
    private static final int INVENTORY_SIZE = 54; // 6 rows
    private static final String GUI_TITLE = ChatColor.DARK_GREEN + "Skill Tree";

    // Map of slot positions for skill nodes
    private final Map<String, Map<Integer, Integer>> skillNodePositions;

    public SkillTreeGUI(MyExperiencePlugin plugin, SkillTreeManager skillTreeManager) {
        this.plugin = plugin;
        this.skillTreeManager = skillTreeManager;
        this.skillNodePositions = new HashMap<>();

        // Initialize predefined positions for skill nodes in the GUI
        initializeNodePositions();
    }

    private void initializeNodePositions() {
        // Define positions for Ranger skill tree
        Map<Integer, Integer> rangerPositions = new HashMap<>();
        rangerPositions.put(1, 10); // Node 1 at slot 10
        rangerPositions.put(2, 12); // Node 2 at slot 12
        rangerPositions.put(3, 14); // Node 3 at slot 14
        rangerPositions.put(4, 19); // Node 4 at slot 19
        rangerPositions.put(5, 21); // Node 5 at slot 21
        rangerPositions.put(6, 23); // Node 6 at slot 23
        rangerPositions.put(7, 28); // Node 7 at slot 28
        rangerPositions.put(8, 30); // Node 8 at slot 30
        rangerPositions.put(9, 31); // Node 9 at slot 31
        rangerPositions.put(10, 32); // Node 10 at slot 32
        rangerPositions.put(11, 37); // Node 11 at slot 37
        rangerPositions.put(12, 38); // Node 12 at slot 38
        rangerPositions.put(13, 39); // Node 13 at slot 39
        rangerPositions.put(14, 40); // Node 14 at slot 40

        skillNodePositions.put("Ranger", rangerPositions);

        // Define positions for DragonKnight skill tree
        Map<Integer, Integer> dragonKnightPositions = new HashMap<>();
        dragonKnightPositions.put(1, 10);
        dragonKnightPositions.put(2, 12);
        dragonKnightPositions.put(3, 14);
        dragonKnightPositions.put(4, 19);
        dragonKnightPositions.put(5, 21);
        dragonKnightPositions.put(6, 23);
        dragonKnightPositions.put(7, 28);
        dragonKnightPositions.put(8, 30);
        dragonKnightPositions.put(9, 31);
        dragonKnightPositions.put(10, 32);
        dragonKnightPositions.put(11, 37);
        dragonKnightPositions.put(12, 38);
        dragonKnightPositions.put(13, 39);
        dragonKnightPositions.put(14, 40);

        skillNodePositions.put("DragonKnight", dragonKnightPositions);

        // Add more skill tree positions as needed
    }

    public void openSkillTreeGUI(Player player) {
        UUID uuid = player.getUniqueId();
        String playerClass = plugin.getClassManager().getPlayerClass(uuid);

        if ("NoClass".equalsIgnoreCase(playerClass)) {
            player.sendMessage(ChatColor.RED + "You need to choose a class before accessing the skill tree!");
            return;
        }

        // Create inventory
        Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE, GUI_TITLE);

        // Get player's skill tree
        SkillTree tree = skillTreeManager.getSkillTree(playerClass, "basic");
        if (tree == null) {
            player.sendMessage(ChatColor.RED + "Skill tree not found for your class!");
            return;
        }

        // Get purchased skills
        Set<Integer> purchasedSkills = skillTreeManager.getPurchasedSkills(uuid);

        // Fill the inventory with background
        fillBackground(inventory);

        // Add skill points info
        addSkillPointsInfo(inventory, player);

        // Add skill nodes
        addSkillNodes(inventory, player, tree, purchasedSkills);

        // Add connections between nodes (visual lines)
        addNodeConnections(inventory, tree, purchasedSkills);

        // Open the inventory
        player.openInventory(inventory);
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

        // Force recalculation of skill points
        int unusedPoints = skillTreeManager.getUnusedBasicSkillPoints(uuid);

        // Make sure we never show negative points
        if (unusedPoints < 0) {
            if (debuggingFlag == 1) {
                plugin.getLogger().warning("Negative skill points detected for " + player.getName() +
                        ": " + unusedPoints + ", setting to 0");
            }
            unusedPoints = 0;
        }

        // Create the item
        ItemStack pointsItem = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = pointsItem.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Skill Points: " + ChatColor.GREEN + unusedPoints);

        // Add lore with explanation
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Click on skills to spend your points");
        lore.add(ChatColor.GRAY + "You have " + ChatColor.YELLOW + unusedPoints +
                ChatColor.GRAY + " points to spend");

        // Add additional info about total and used points for debugging
        if (debuggingFlag == 1) {
            int totalPoints = skillTreeManager.getBasicSkillPoints(uuid);
            int usedPoints = totalPoints - unusedPoints;
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "Total: " + totalPoints);
            lore.add(ChatColor.DARK_GRAY + "Used: " + usedPoints);
        }

        meta.setLore(lore);
        pointsItem.setItemMeta(meta);

        // Place in inventory
        inventory.setItem(4, pointsItem);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Displayed skill points for " + player.getName() +
                    ": " + unusedPoints + " (unused)");
        }
    }

    private void addSkillNodes(Inventory inventory, Player player, SkillTree tree, Set<Integer> purchasedSkills) {
        UUID uuid = player.getUniqueId();
        String playerClass = plugin.getClassManager().getPlayerClass(uuid);
        Map<Integer, Integer> positions = skillNodePositions.get(playerClass);

        if (positions == null) {
            return;
        }

        for (SkillNode node : tree.getAllNodes()) {
            int nodeId = node.getId();
            Integer slot = positions.get(nodeId);

            if (slot == null) {
                continue;
            }

            boolean isPurchased = purchasedSkills.contains(nodeId);
            boolean canPurchase = !isPurchased && skillTreeManager.canPurchaseSkill(player, nodeId);
            int purchaseCount = skillTreeManager.getSkillPurchaseCount(uuid, nodeId);

            ItemStack nodeItem = createNodeItem(node, isPurchased, canPurchase, purchaseCount);
            inventory.setItem(slot, nodeItem);
        }
    }

    private ItemStack createNodeItem(SkillNode node, boolean isPurchased, boolean canPurchase, int purchaseCount) {
        Material material;

        // Choose material based on purchase status
        if (isPurchased) {
            if (purchaseCount >= node.getMaxPurchases()) {
                material = Material.EMERALD_BLOCK; // Fully purchased
            } else {
                material = Material.EMERALD; // Partially purchased
            }
        } else if (canPurchase) {
            material = Material.LIME_CONCRETE; // Available to purchase
        } else {
            material = Material.RED_CONCRETE; // Locked
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        // Set display name with proper coloring based on status
        String displayName = (isPurchased ? ChatColor.GREEN : (canPurchase ? ChatColor.YELLOW : ChatColor.RED))
                + node.getName();

        // Add purchase count if applicable
        if (node.getMaxPurchases() > 1) {
            displayName += ChatColor.GRAY + " (" + purchaseCount + "/" + node.getMaxPurchases() + ")";
        }

        meta.setDisplayName(displayName);

        // Build lore (description)
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + node.getDescription());
        lore.add("");

        if (isPurchased) {
            if (purchaseCount < node.getMaxPurchases()) {
                lore.add(ChatColor.GOLD + "Cost: " + ChatColor.YELLOW + node.getCost() + " points");
                lore.add(ChatColor.GREEN + "Click to upgrade! (" + purchaseCount + "/" + node.getMaxPurchases() + ")");
            } else {
                lore.add(ChatColor.GREEN + "Fully upgraded! (" + purchaseCount + "/" + node.getMaxPurchases() + ")");
            }
        } else if (canPurchase) {
            lore.add(ChatColor.GOLD + "Cost: " + ChatColor.YELLOW + node.getCost() + " points");
            lore.add(ChatColor.GREEN + "Click to purchase!");
        } else {
            lore.add(ChatColor.RED + "Locked - Purchase connected skills first");
            lore.add(ChatColor.GOLD + "Cost: " + ChatColor.YELLOW + node.getCost() + " points");
        }

        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);

        return item;
    }

    private void addNodeConnections(Inventory inventory, SkillTree tree, Set<Integer> purchasedSkills) {
        // This would be a more complex implementation to add visual lines between nodes
        // For simplicity, we're omitting this for now
    }
}