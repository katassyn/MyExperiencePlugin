package com.maks.myexperienceplugin.Class.skills.gui;

import com.maks.myexperienceplugin.Class.skills.SkillTreeManager;
import com.maks.myexperienceplugin.Class.skills.base.SkillNode;
import com.maks.myexperienceplugin.Class.skills.base.SkillTree;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class AscendancySkillTreeGUIListener implements Listener {
    private final MyExperiencePlugin plugin;
    private final SkillTreeManager skillTreeManager;
    private final AscendancySkillTreeGUI ascendancySkillTreeGUI;

    // Debugging flag
    private final int debuggingFlag = 1;

    public AscendancySkillTreeGUIListener(MyExperiencePlugin plugin,
                                          SkillTreeManager skillTreeManager,
                                          AscendancySkillTreeGUI ascendancySkillTreeGUI) {
        this.plugin = plugin;
        this.skillTreeManager = skillTreeManager;
        this.ascendancySkillTreeGUI = ascendancySkillTreeGUI;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        // Check if this is an ascendancy skill tree GUI
        if (title.startsWith(ChatColor.DARK_BLUE + "Ascendancy Skills")) {
            event.setCancelled(true);

            // Extract the page number from the title
            int page = extractPageNumber(title);

            // Handle navigation buttons
            if (event.getRawSlot() == 45 && page > 1) {
                // Previous page button
                ascendancySkillTreeGUI.openAscendancySkillTreeGUI(player, page - 1);
                return;
            } else if (event.getRawSlot() == 53 && page < 3) {
                // Next page button
                ascendancySkillTreeGUI.openAscendancySkillTreeGUI(player, page + 1);
                return;
            }

            // Handle skill node clicks - this would need to map slots to node IDs
            // based on the current page and the player's ascendancy
            String playerClass = plugin.getClassManager().getPlayerClass(player.getUniqueId());
            String ascendancy = plugin.getClassManager().getPlayerAscendancy(player.getUniqueId());
            int nodeId = getNodeIdFromSlot(event.getRawSlot(), ascendancy, page);

            if (nodeId > 0) {
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Player clicked node " + nodeId + " in ascendancy " +
                            ascendancy + " on page " + page);
                }

                // Try to purchase the skill
                if (skillTreeManager.canPurchaseAscendancySkill(player, nodeId)) {
                    boolean success = skillTreeManager.purchaseAscendancySkill(player, nodeId);

                    if (success) {
                        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP,
                                1.0f, 1.0f);
                        player.sendMessage(ChatColor.GREEN + "Ascendancy skill purchased successfully!");

                        // Refresh the GUI
                        ascendancySkillTreeGUI.openAscendancySkillTreeGUI(player, page);
                    } else {
                        player.sendMessage(ChatColor.RED + "Failed to purchase ascendancy skill!");
                    }
                } else {
                    // Check if the player has already purchased this skill
                    if (skillTreeManager.getPurchasedSkills(player.getUniqueId()).contains(nodeId)) {
                        int purchaseCount = skillTreeManager.getSkillPurchaseCount(player.getUniqueId(), nodeId);
                        SkillTree tree = skillTreeManager.getSkillTree(
                                playerClass,
                                "ascendancy",
                                ascendancy
                        );

                        if (tree != null) {
                            SkillNode node = tree.getNode(nodeId);

                            if (node != null && purchaseCount < node.getMaxPurchases()) {
                                // Try to upgrade
                                boolean success = skillTreeManager.purchaseAscendancySkill(player, nodeId);

                                if (success) {
                                    player.playSound(player.getLocation(),
                                            org.bukkit.Sound.ENTITY_PLAYER_LEVELUP,
                                            1.0f, 1.0f);
                                    player.sendMessage(ChatColor.GREEN +
                                            "Ascendancy skill upgraded successfully!");

                                    // Refresh the GUI
                                    ascendancySkillTreeGUI.openAscendancySkillTreeGUI(player, page);
                                } else {
                                    player.sendMessage(ChatColor.RED +
                                            "Failed to upgrade ascendancy skill!");
                                }
                            } else {
                                player.sendMessage(ChatColor.RED +
                                        "This skill is already fully upgraded!");
                            }
                        }
                    } else {
                        player.sendMessage(ChatColor.RED +
                                "You need to unlock connected skills first!");
                    }
                }
            }
        }
    }

    private int extractPageNumber(String title) {
        // Format: "Ascendancy Skills - [AscendancyName] (Page X)"
        try {
            int start = title.lastIndexOf("Page ") + 5;
            int end = title.lastIndexOf(")");
            String pageStr = title.substring(start, end);
            return Integer.parseInt(pageStr);
        } catch (Exception e) {
            if (debuggingFlag == 1) {
                plugin.getLogger().warning("Failed to extract page number from title: " + title);
                e.printStackTrace();
            }
            return 1; // Default to page 1
        }
    }

    private int getNodeIdFromSlot(int slot, String ascendancy, int page) {
        // This is the mapping of slots to node IDs for each ascendancy and page

        // For Beastmaster
        if ("Beastmaster".equals(ascendancy)) {
            if (page == 1) {
                switch (slot) {
                    case 10: return 1;
                    case 12: return 2;
                    case 14: return 3;
                    case 19: return 4;
                    case 21: return 5;
                    case 23: return 6;
                    case 28: return 7;
                    case 30: return 8;
                    case 32: return 9;
                }
            } else if (page == 2) {
                switch (slot) {
                    case 10: return 10;
                    case 12: return 11;
                    case 14: return 12;
                    case 19: return 13;
                    case 21: return 14;
                    case 23: return 15;
                    case 28: return 16;
                    case 30: return 17;
                    case 32: return 18;
                }
            } else if (page == 3) {
                switch (slot) {
                    case 10: return 19;
                    case 12: return 20;
                    case 14: return 21;
                    case 19: return 22;
                    case 21: return 23;
                    case 23: return 24;
                    case 28: return 25;
                    case 30: return 26;
                    case 32: return 27;
                }
            }
        }

        // For Berserker
        else if ("Berserker".equals(ascendancy)) {
            if (page == 1) {
                switch (slot) {
                    case 10: return 1;
                    case 12: return 2;
                    case 14: return 3;
                    case 19: return 4;
                    case 21: return 5;
                    case 23: return 6;
                    case 28: return 7;
                    case 30: return 8;
                    case 32: return 9;
                }
            } else if (page == 2) {
                switch (slot) {
                    case 10: return 10;
                    case 12: return 11;
                    case 14: return 12;
                    case 19: return 13;
                    case 21: return 14;
                    case 23: return 15;
                    case 28: return 16;
                    case 30: return 17;
                    case 32: return 18;
                }
            } else if (page == 3) {
                switch (slot) {
                    case 10: return 19;
                    case 12: return 20;
                    case 14: return 21;
                    case 19: return 22;
                    case 21: return 23;
                    case 23: return 24;
                    case 28: return 25;
                    case 30: return 26;
                    case 32: return 27;
                }
            }
        }

        // Add more mappings for other ascendancies

        return -1; // No node at this slot
    }
}