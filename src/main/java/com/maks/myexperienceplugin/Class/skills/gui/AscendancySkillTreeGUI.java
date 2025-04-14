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

    // Map to store node positions for each page of each ascendancy
    private final Map<String, Map<Integer, Map<Integer, Integer>>> ascendancyNodePositions;

    public AscendancySkillTreeGUI(MyExperiencePlugin plugin, SkillTreeManager skillTreeManager) {
        this.plugin = plugin;
        this.skillTreeManager = skillTreeManager;
        this.ascendancyNodePositions = new HashMap<>();

        // Initialize predefined positions for ascendancy skill nodes
        initializeNodePositions();
    }

    private void initializeNodePositions() {
        // Example for Beastmaster (3 pages)
        Map<Integer, Map<Integer, Integer>> beastmasterPages = new HashMap<>();

        // Page 1 (nodes 1-9)
        Map<Integer, Integer> page1 = new HashMap<>();
        page1.put(1, 10); // Node 1 at slot 10
        page1.put(2, 12); // Node 2 at slot 12
        page1.put(3, 14); // Node 3 at slot 14
        page1.put(4, 19); // Node 4 at slot 19
        page1.put(5, 21); // Node 5 at slot 21
        page1.put(6, 23); // Node 6 at slot 23
        page1.put(7, 28); // Node 7 at slot 28
        page1.put(8, 30); // Node 8 at slot 30
        page1.put(9, 32); // Node 9 at slot 32

        // Page 2 (nodes 10-18)
        Map<Integer, Integer> page2 = new HashMap<>();
        page2.put(10, 10); // Node 10 at slot 10
        page2.put(11, 12); // Node 11 at slot 12
        page2.put(12, 14); // Node 12 at slot 14
        page2.put(13, 19); // Node 13 at slot 19
        page2.put(14, 21); // Node 14 at slot 21
        page2.put(15, 23); // Node 15 at slot 23
        page2.put(16, 28); // Node 16 at slot 28
        page2.put(17, 30); // Node 17 at slot 30
        page2.put(18, 32); // Node 18 at slot 32

        // Page 3 (nodes 19-27)
        Map<Integer, Integer> page3 = new HashMap<>();
        page3.put(19, 10); // Node 19 at slot 10
        page3.put(20, 12); // Node 20 at slot 12
        page3.put(21, 14); // Node 21 at slot 14
        page3.put(22, 19); // Node 22 at slot 19
        page3.put(23, 21); // Node 23 at slot 21
        page3.put(24, 23); // Node 24 at slot 23
        page3.put(25, 28); // Node 25 at slot 28
        page3.put(26, 30); // Node 26 at slot 30
        page3.put(27, 32); // Node 27 at slot 32

        beastmasterPages.put(1, page1);
        beastmasterPages.put(2, page2);
        beastmasterPages.put(3, page3);

        ascendancyNodePositions.put("Beastmaster", beastmasterPages);

        // Example for Berserker (3 pages)
        Map<Integer, Map<Integer, Integer>> berserkerPages = new HashMap<>();

        // Page 1 (nodes 1-9)
        Map<Integer, Integer> berserkPage1 = new HashMap<>();
        berserkPage1.put(1, 10);
        berserkPage1.put(2, 12);
        berserkPage1.put(3, 14);
        berserkPage1.put(4, 19);
        berserkPage1.put(5, 21);
        berserkPage1.put(6, 23);
        berserkPage1.put(7, 28);
        berserkPage1.put(8, 30);
        berserkPage1.put(9, 32);

        // Page 2 (nodes 10-18)
        Map<Integer, Integer> berserkPage2 = new HashMap<>();
        berserkPage2.put(10, 10);
        berserkPage2.put(11, 12);
        berserkPage2.put(12, 14);
        berserkPage2.put(13, 19);
        berserkPage2.put(14, 21);
        berserkPage2.put(15, 23);
        berserkPage2.put(16, 28);
        berserkPage2.put(17, 30);
        berserkPage2.put(18, 32);

        // Page 3 (nodes 19-27)
        Map<Integer, Integer> berserkPage3 = new HashMap<>();
        berserkPage3.put(19, 10);
        berserkPage3.put(20, 12);
        berserkPage3.put(21, 14);
        berserkPage3.put(22, 19);
        berserkPage3.put(23, 21);
        berserkPage3.put(24, 23);
        berserkPage3.put(25, 28);
        berserkPage3.put(26, 30);
        berserkPage3.put(27, 32);

        berserkerPages.put(1, berserkPage1);
        berserkerPages.put(2, berserkPage2);
        berserkerPages.put(3, berserkPage3);

        ascendancyNodePositions.put("Berserker", berserkerPages);

        // Add positions for other ascendancies as needed
    }

    public void openAscendancySkillTreeGUI(Player player, int page) {
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

        // Create inventory with page number
        String title = GUI_TITLE_PREFIX + " - " + ascendancy + " (Page " + page + ")";
        Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE, title);

        // Get purchased skills
        Set<Integer> purchasedSkills = skillTreeManager.getPurchasedSkills(uuid);

        // Fill the inventory with background
        fillBackground(inventory);

        // Add skill points info
        addSkillPointsInfo(inventory, player);

        // Add navigation buttons
        addNavigationButtons(inventory, page, ascendancy);

        // Add skill nodes for this page
        addAscendancySkillNodes(inventory, player, tree, purchasedSkills, ascendancy, page);

        // Open the inventory
        player.openInventory(inventory);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Opened ascendancy skill tree GUI for " + player.getName() +
                    ", ascendancy: " + ascendancy + ", page: " + page);
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

    private void addNavigationButtons(Inventory inventory, int currentPage, String ascendancy) {
        // Previous page button (if not on first page)
        if (currentPage > 1) {
            ItemStack prevButton = new ItemStack(Material.ARROW);
            ItemMeta meta = prevButton.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "Previous Page");
            prevButton.setItemMeta(meta);
            inventory.setItem(45, prevButton);
        }

        // Next page button (if not on last page)
        // Assuming 3 pages for now - could be made dynamic
        if (currentPage < 3) {
            ItemStack nextButton = new ItemStack(Material.ARROW);
            ItemMeta meta = nextButton.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "Next Page");
            nextButton.setItemMeta(meta);
            inventory.setItem(53, nextButton);
        }

        // Page indicator in the middle
        ItemStack pageIndicator = new ItemStack(Material.PAPER);
        ItemMeta meta = pageIndicator.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Page " + currentPage + " of 3");
        pageIndicator.setItemMeta(meta);
        inventory.setItem(49, pageIndicator);
    }

    private void addAscendancySkillNodes(Inventory inventory, Player player, SkillTree tree,
                                         Set<Integer> purchasedSkills, String ascendancy, int page) {
        UUID uuid = player.getUniqueId();

        Map<Integer, Map<Integer, Integer>> ascendancyPages = ascendancyNodePositions.get(ascendancy);
        if (ascendancyPages == null) {
            if (debuggingFlag == 1) {
                plugin.getLogger().warning("No node positions defined for ascendancy: " + ascendancy);
            }
            return;
        }

        Map<Integer, Integer> positions = ascendancyPages.get(page);
        if (positions == null) {
            if (debuggingFlag == 1) {
                plugin.getLogger().warning("No node positions defined for page " + page +
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