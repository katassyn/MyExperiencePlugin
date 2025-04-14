package com.maks.myexperienceplugin.Class.skills;

import com.maks.myexperienceplugin.Class.skills.base.SkillNode;
import com.maks.myexperienceplugin.Class.skills.base.SkillTree;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkillTreeGUIListener implements Listener {
    private final MyExperiencePlugin plugin;
    private final SkillTreeManager skillTreeManager;
    private final SkillTreeGUI skillTreeGUI;

    // Map to track node IDs in the GUI
    private final Map<String, Map<Integer, Integer>> nodeSlotMap;

    public SkillTreeGUIListener(MyExperiencePlugin plugin, SkillTreeManager skillTreeManager, SkillTreeGUI skillTreeGUI) {
        this.plugin = plugin;
        this.skillTreeManager = skillTreeManager;
        this.skillTreeGUI = skillTreeGUI;
        this.nodeSlotMap = new HashMap<>();

        // Initialize the reverse mapping of slots to node IDs
        initializeNodeSlotMap();
    }

    private void initializeNodeSlotMap() {
        // Ranger node slots to node IDs
        Map<Integer, Integer> rangerSlotMap = new HashMap<>();
        rangerSlotMap.put(10, 1);  // Node 1 at slot 10
        rangerSlotMap.put(12, 2);  // Node 2 at slot 12
        rangerSlotMap.put(14, 3);  // Node 3 at slot 14
        rangerSlotMap.put(19, 4);  // Node 4 at slot 19
        rangerSlotMap.put(21, 5);  // Node 5 at slot 21
        rangerSlotMap.put(23, 6);  // Node 6 at slot 23
        rangerSlotMap.put(28, 7);  // Node 7 at slot 28
        rangerSlotMap.put(30, 8);  // Node 8 at slot 30
        rangerSlotMap.put(31, 9);  // Node 9 at slot 31
        rangerSlotMap.put(32, 10); // Node 10 at slot 32
        rangerSlotMap.put(37, 11); // Node 11 at slot 37
        rangerSlotMap.put(38, 12); // Node 12 at slot 38
        rangerSlotMap.put(39, 13); // Node 13 at slot 39
        rangerSlotMap.put(40, 14); // Node 14 at slot 40

        nodeSlotMap.put("Ranger", rangerSlotMap);

        // DragonKnight node slots to node IDs
        Map<Integer, Integer> dragonKnightSlotMap = new HashMap<>();
        dragonKnightSlotMap.put(10, 1);  // Node 1 at slot 10
        dragonKnightSlotMap.put(12, 2);  // Node 2 at slot 12
        dragonKnightSlotMap.put(14, 3);  // Node 3 at slot 14
        dragonKnightSlotMap.put(19, 4);  // Node 4 at slot 19
        dragonKnightSlotMap.put(21, 5);  // Node 5 at slot 21
        dragonKnightSlotMap.put(23, 6);  // Node 6 at slot 23
        dragonKnightSlotMap.put(28, 7);  // Node 7 at slot 28
        dragonKnightSlotMap.put(30, 8);  // Node 8 at slot 30
        dragonKnightSlotMap.put(31, 9);  // Node 9 at slot 31
        dragonKnightSlotMap.put(32, 10); // Node 10 at slot 32
        dragonKnightSlotMap.put(37, 11); // Node 11 at slot 37
        dragonKnightSlotMap.put(38, 12); // Node 12 at slot 38
        dragonKnightSlotMap.put(39, 13); // Node 13 at slot 39
        dragonKnightSlotMap.put(40, 14); // Node 14 at slot 40

        nodeSlotMap.put("DragonKnight", dragonKnightSlotMap);

        // Add more slot mappings for other classes as needed
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (title.equals(ChatColor.DARK_GREEN + "Skill Tree")) {
            event.setCancelled(true);

            UUID playerUUID = player.getUniqueId();
            String playerClass = plugin.getClassManager().getPlayerClass(playerUUID);

            if ("NoClass".equalsIgnoreCase(playerClass)) {
                return;
            }

            Map<Integer, Integer> slotMap = nodeSlotMap.get(playerClass);
            if (slotMap == null) {
                return;
            }

            int slot = event.getRawSlot();
            if (slotMap.containsKey(slot)) {
                int nodeId = slotMap.get(slot);

                // Try to purchase the skill
                if (skillTreeManager.canPurchaseSkill(player, nodeId)) {
                    boolean success = skillTreeManager.purchaseSkill(player, nodeId);

                    if (success) {
                        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                        player.sendMessage(ChatColor.GREEN + "Skill purchased successfully!");

                        // Refresh the GUI
                        skillTreeGUI.openSkillTreeGUI(player);
                    } else {
                        player.sendMessage(ChatColor.RED + "Failed to purchase skill!");
                    }
                } else {
                    // Check if the player has already purchased this skill
                    if (skillTreeManager.getPurchasedSkills(playerUUID).contains(nodeId)) {
                        SkillTree tree = skillTreeManager.getSkillTree(playerClass, "basic");
                        SkillNode node = tree.getNode(nodeId);

                        int purchaseCount = skillTreeManager.getSkillPurchaseCount(playerUUID, nodeId);

                        // If the skill can be upgraded and the player has enough points
                        if (purchaseCount < node.getMaxPurchases() && skillTreeManager.getUnusedBasicSkillPoints(playerUUID) >= node.getCost()) {
                            boolean success = skillTreeManager.purchaseSkill(player, nodeId);

                            if (success) {
                                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                                player.sendMessage(ChatColor.GREEN + "Skill upgraded successfully!");

                                // Refresh the GUI
                                skillTreeGUI.openSkillTreeGUI(player);
                            } else {
                                player.sendMessage(ChatColor.RED + "Failed to upgrade skill!");
                            }
                        } else if (purchaseCount >= node.getMaxPurchases()) {
                            player.sendMessage(ChatColor.RED + "This skill is already fully upgraded!");
                        } else {
                            player.sendMessage(ChatColor.RED + "You don't have enough skill points!");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You need to unlock connected skills first!");
                    }
                }
            }
        }
    }
}