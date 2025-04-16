package com.maks.myexperienceplugin.Class.skills;

import com.maks.myexperienceplugin.Class.skills.base.BaseSkillManager;
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
        // Wspólny układ dla wszystkich klas (Ranger, DragonKnight, SpellWeaver)
        Map<Integer, Integer> commonLayout = new HashMap<>();

        // Dokładny układ według podanych slotów
        commonLayout.put(1, 10);  // Skill 1 w slocie 10
        commonLayout.put(2, 13);  // Skill 2 w slocie 13
        commonLayout.put(3, 16);  // Skill 3 w slocie 16

        commonLayout.put(4, 19);  // Skill 4 w slocie 19
        commonLayout.put(5, 22);  // Skill 5 w slocie 22
        commonLayout.put(6, 25);  // Skill 6 w slocie 25

        commonLayout.put(7, 27);  // Skill 7 w slocie 27
        commonLayout.put(8, 29);  // Skill 8 w slocie 29
        commonLayout.put(9, 31);  // Skill 9 w slocie 31
        commonLayout.put(10, 33); // Skill 10 w slocie 33
        commonLayout.put(11, 35); // Skill 11 w slocie 35

        commonLayout.put(12, 36); // Skill 12 w slocie 36
        commonLayout.put(13, 40); // Skill 13 w slocie 40
        commonLayout.put(14, 49); // Skill 14 w slocie 49

        // Stosujemy ten sam układ dla wszystkich klas
        skillNodePositions.put("Ranger", new HashMap<>(commonLayout));
        skillNodePositions.put("DragonKnight", new HashMap<>(commonLayout));
        skillNodePositions.put("SpellWeaver", new HashMap<>(commonLayout)); // na przyszłość

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Zainicjalizowano układ drzewka umiejętności dla wszystkich klas");
            plugin.getLogger().info("Skille umieszczone w slotach: 10,13,16, 19,22,25, 27,29,31,33,35, 36,40,49");
        }
    }    public void openSkillTreeGUI(Player player) {
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

        // Wybór materiału na podstawie statusu zakupu
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

        // Nazwa z odpowiednim kolorem
        String displayName = (isPurchased ? ChatColor.GREEN : (canPurchase ? ChatColor.YELLOW : ChatColor.RED))
                + node.getName();

        // Dodanie licznika zakupów, jeśli umiejętność ma wiele poziomów
        if (node.getMaxPurchases() > 1) {
            displayName += ChatColor.GRAY + " (" + purchaseCount + "/" + node.getMaxPurchases() + ")";
        }

        meta.setDisplayName(displayName);

        // Budowanie opisu (lore)
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + node.getDescription());
        lore.add("");

        // Określamy rzeczywisty koszt umiejętności
        int actualCost = node.getCost();

        // Sprawdzamy, czy to umiejętność z wielokrotnym zakupem (1/2, 1/3)
        // Zamiast używać BaseSkillManager, patrzymy na maxPurchases
        if (node.getMaxPurchases() > 1) {
            actualCost = 1; // Umiejętności z wieloma poziomami kosztują 1 punkt za poziom
        }

        if (isPurchased) {
            if (purchaseCount < node.getMaxPurchases()) {
                // Pokazujemy faktyczny koszt ulepszenia
                lore.add(ChatColor.GOLD + "Cost: " + ChatColor.YELLOW + actualCost + " point" + (actualCost > 1 ? "s" : ""));
                lore.add(ChatColor.GREEN + "Click to upgrade! (" + purchaseCount + "/" + node.getMaxPurchases() + ")");
            } else {
                lore.add(ChatColor.GREEN + "Fully upgraded! (" + purchaseCount + "/" + node.getMaxPurchases() + ")");
            }
        } else if (canPurchase) {
            // Pokazujemy faktyczny koszt zakupu
            lore.add(ChatColor.GOLD + "Cost: " + ChatColor.YELLOW + actualCost + " point" + (actualCost > 1 ? "s" : ""));
            lore.add(ChatColor.GREEN + "Click to purchase!");
        } else {
            lore.add(ChatColor.RED + "Locked - Purchase connected skills first");
            // Pokazujemy faktyczny koszt, nawet gdy umiejętność jest zablokowana
            lore.add(ChatColor.GOLD + "Cost: " + ChatColor.YELLOW + actualCost + " point" + (actualCost > 1 ? "s" : ""));
        }

        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);

        return item;
    }    private void addNodeConnections(Inventory inventory, SkillTree tree, Set<Integer> purchasedSkills) {
        // This would be a more complex implementation to add visual lines between nodes
        // For simplicity, we're omitting this for now
    }
}