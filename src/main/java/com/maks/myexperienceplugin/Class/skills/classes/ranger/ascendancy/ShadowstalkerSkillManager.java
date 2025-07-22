package com.maks.myexperienceplugin.Class.skills.classes.ranger.ascendancy;

import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.SkillIdOffsets;
import com.maks.myexperienceplugin.Class.skills.base.BaseSkillManager;
import com.maks.myexperienceplugin.Class.skills.base.SkillNode;
import com.maks.myexperienceplugin.Class.skills.base.SkillTree;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ShadowstalkerSkillManager extends BaseSkillManager {

    // Use centralized ID offset from SkillIdOffsets class
    private static final int ID_OFFSET = SkillIdOffsets.SHADOWSTALKER_OFFSET;

    public ShadowstalkerSkillManager(MyExperiencePlugin plugin) {
        super(plugin, "Shadowstalker");
    }

    @Override
    protected void initializeSkills() {
        // === ŚCIEŻKA 1: STEALTH PATH (Skille 1, 4, 6, 7, 10, 12, 16, 22, 25) ===
        SkillNode node1 = new SkillNode(ID_OFFSET + 1, "Shadow Speed", "+5% movement speed in shadows and at night", 1,
                Material.LEATHER_BOOTS, 2, player -> {
            player.sendMessage(ChatColor.DARK_GRAY + "You move swiftly through the shadows!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Shadow Speed skill");
            }
        });

        SkillNode node2 = new SkillNode(ID_OFFSET + 2, "Critical Precision", "+2% critical hit chance (CRIT = 200% DMG)", 1,
                Material.ARROW, 3, player -> {
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Critical Precision skill");
            }
        });

        SkillNode node3 = new SkillNode(ID_OFFSET + 3, "First Strike", "+15% damage on first attack against target", 1,
                Material.IRON_SWORD, 2, player -> {
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated First Strike skill");
            }
        });

        SkillNode node4 = new SkillNode(ID_OFFSET + 4, "Sneaking Evasion", "+4% evade chance while sneaking", 2,
                Material.FEATHER, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You become more evasive while sneaking!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Sneaking Evasion skill");
            }
        });

        SkillNode node5 = new SkillNode(ID_OFFSET + 5, "Critical Bleeding", "Critical hits apply 3% of your damage as bleeding for 5 seconds", 2,
                Material.REDSTONE, 2, player -> {
            player.sendMessage(ChatColor.GREEN + "Your critical hits now cause bleeding!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Critical Bleeding skill");
            }
        });

        SkillNode node6 = new SkillNode(ID_OFFSET + 6, "Shadow Evasion", "+3% evade chance in darkness", 2,
                Material.COAL, 2, player -> {
            player.sendMessage(ChatColor.GREEN + "You become more evasive in darkness!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Shadow Evasion skill");
            }
        });

        SkillNode node7 = new SkillNode(ID_OFFSET + 7, "Stealth", "-10% enemy detection range while sneaking", 2,
                Material.GRAY_DYE, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You become harder to detect while sneaking!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Stealth skill");
            }
        });

        SkillNode node8 = new SkillNode(ID_OFFSET + 8, "Backstab", "+10% damage when attacking from behind", 2,
                Material.STONE_SWORD, 2, player -> {
            player.sendMessage(ChatColor.GREEN + "Your attacks from behind deal more damage!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Backstab skill");
            }
        });

        SkillNode node9 = new SkillNode(ID_OFFSET + 9, "Poisoned Blade", "+15% chance for attacks to apply poison for 4 seconds (POISON = 10% DMG/s)", 2,
                Material.SPIDER_EYE, 2, player -> {
            player.sendMessage(ChatColor.GREEN + "Your attacks now have a chance to poison enemies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Poisoned Blade skill");
            }
        });

        SkillNode node10 = new SkillNode(ID_OFFSET + 10, "Kill Rush", "After killing an enemy, gain +10% movement speed for 5 seconds", 2,
                Material.SUGAR, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You gain a burst of speed after kills!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Kill Rush skill");
            }
        });

        SkillNode node11 = new SkillNode(ID_OFFSET + 11, "Critical Power", "+20% critical damage (CRIT = 200% DMG)", 3,
                Material.DIAMOND_SWORD, 2, player -> {
            player.sendMessage(ChatColor.GREEN + "Your critical hits deal more damage!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Critical Power skill");
            }
        });

        SkillNode node12 = new SkillNode(ID_OFFSET + 12, "Desperate Evasion", "When below 30% health, gain +15% evade chance", 3,
                Material.GOLDEN_APPLE, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You become more evasive when injured!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Desperate Evasion skill");
            }
        });

        SkillNode node13 = new SkillNode(ID_OFFSET + 13, "Patient Hunter", "+3% damage per second spent sneaking before attacking (max +15%)", 3,
                Material.CLOCK, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your patience while sneaking is rewarded with increased damage!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Patient Hunter skill");
            }
        });

        SkillNode node14 = new SkillNode(ID_OFFSET + 14, "Toxic Exploitation", "+12% damage against enemies affected by poison or bleeding", 3,
                Material.FERMENTED_SPIDER_EYE, 2, player -> {
            player.sendMessage(ChatColor.GREEN + "You deal more damage to poisoned or bleeding enemies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Toxic Exploitation skill");
            }
        });

        SkillNode node15 = new SkillNode(ID_OFFSET + 15, "Potent Toxins", "+20% poison damage and duration", 3,
                Material.POISONOUS_POTATO, 2, player -> {
            player.sendMessage(ChatColor.GREEN + "Your poisons become more potent!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Potent Toxins skill");
            }
        });

        SkillNode node16 = new SkillNode(ID_OFFSET + 16, "Shadow Step", "+25% movement speed while sneaking", 3,
                Material.NETHERITE_BOOTS, 2, player -> {
            player.sendMessage(ChatColor.GREEN + "You move much faster while sneaking!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Shadow Step skill");
            }
        });

        SkillNode node17 = new SkillNode(ID_OFFSET + 17, "Devastating Critical", "Critical hits have 10% chance to deal +50% additional damage (CRIT = 200% DMG)", 3,
                Material.NETHERITE_SWORD, 2, player -> {
            player.sendMessage(ChatColor.GREEN + "Your critical hits sometimes deal devastating damage!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Devastating Critical skill");
            }
        });

        SkillNode node18 = new SkillNode(ID_OFFSET + 18, "Critical Defense", "Each critical hit reduces damage taken by 3% for 4 seconds (stacks up to 5 times)", 3,
                Material.SHIELD, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your critical hits now provide defensive benefits!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Critical Defense skill");
            }
        });

        SkillNode node19 = new SkillNode(ID_OFFSET + 19, "Ambush", "After 3 seconds of not attacking, your next attack deals +35% damage", 3,
                Material.ENDER_PEARL, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your first strike after waiting deals significant bonus damage!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Ambush skill");
            }
        });

        SkillNode node20 = new SkillNode(ID_OFFSET + 20, "Opening Strike", "Attacks against full-health targets have +15% critical chance", 3,
                Material.GOLDEN_SWORD, 2, player -> {
            player.sendMessage(ChatColor.GREEN + "You're more likely to critically hit full-health targets!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Opening Strike skill");
            }
        });

        SkillNode node21 = new SkillNode(ID_OFFSET + 21, "Toxic Amplification", "Critical hits against poisoned targets have 25% chance to amplify poison effect, dealing double damage (POISON = 10% DMG/s)", 3,
                Material.DRAGON_BREATH, 2, player -> {
            player.sendMessage(ChatColor.GREEN + "Your critical hits can amplify poison effects!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Toxic Amplification skill");
            }
        });

        SkillNode node22 = new SkillNode(ID_OFFSET + 22, "Shadow Protection", "+10% damage reduction in darkness", 3,
                Material.BLACK_STAINED_GLASS, 2, player -> {
            player.sendMessage(ChatColor.GREEN + "You take less damage in darkness!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Shadow Protection skill");
            }
        });

        SkillNode node23 = new SkillNode(ID_OFFSET + 23, "Precision Strike", "Every third attack on the same target is automatically a critical hit (CRIT = 200% DMG)", 5,
                Material.TARGET, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Every third attack on the same target is guaranteed to be a critical hit!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Precision Strike skill");
            }
        });

        SkillNode node24 = new SkillNode(ID_OFFSET + 24, "Wind Mastery", "+1 maximum wind stack and +5% evade chance at max stacks", 5,
                Material.PHANTOM_MEMBRANE, 2, player -> {
            player.sendMessage(ChatColor.GREEN + "Your wind mastery improves your evasion capabilities!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Wind Mastery skill");
            }
        });

        SkillNode node25 = new SkillNode(ID_OFFSET + 25, "Ambush Speed", "After attacking from a sneaking position, gain +40% attack speed for 3 seconds", 5,
                Material.BLAZE_POWDER, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You attack much faster after ambushing enemies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Ambush Speed skill");
            }
        });

        SkillNode node26 = new SkillNode(ID_OFFSET + 26, "Toxic Focus", "After applying poison, gain +5% damage against that target for the duration", 5,
                Material.WITHER_ROSE, 2, player -> {
            player.sendMessage(ChatColor.GREEN + "You deal more damage to targets you've poisoned!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Toxic Focus skill");
            }
        });

        SkillNode node27 = new SkillNode(ID_OFFSET + 27, "Armor Penetration", "While at full health, your attacks ignore 25% of enemy armor", 5,
                Material.NETHERITE_AXE, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your attacks penetrate enemy armor when you're at full health!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Armor Penetration skill");
            }
        });

        // Add all nodes to the skill manager
        skillNodes.put(ID_OFFSET + 1, node1);
        skillNodes.put(ID_OFFSET + 2, node2);
        skillNodes.put(ID_OFFSET + 3, node3);
        skillNodes.put(ID_OFFSET + 4, node4);
        skillNodes.put(ID_OFFSET + 5, node5);
        skillNodes.put(ID_OFFSET + 6, node6);
        skillNodes.put(ID_OFFSET + 7, node7);
        skillNodes.put(ID_OFFSET + 8, node8);
        skillNodes.put(ID_OFFSET + 9, node9);
        skillNodes.put(ID_OFFSET + 10, node10);
        skillNodes.put(ID_OFFSET + 11, node11);
        skillNodes.put(ID_OFFSET + 12, node12);
        skillNodes.put(ID_OFFSET + 13, node13);
        skillNodes.put(ID_OFFSET + 14, node14);
        skillNodes.put(ID_OFFSET + 15, node15);
        skillNodes.put(ID_OFFSET + 16, node16);
        skillNodes.put(ID_OFFSET + 17, node17);
        skillNodes.put(ID_OFFSET + 18, node18);
        skillNodes.put(ID_OFFSET + 19, node19);
        skillNodes.put(ID_OFFSET + 20, node20);
        skillNodes.put(ID_OFFSET + 21, node21);
        skillNodes.put(ID_OFFSET + 22, node22);
        skillNodes.put(ID_OFFSET + 23, node23);
        skillNodes.put(ID_OFFSET + 24, node24);
        skillNodes.put(ID_OFFSET + 25, node25);
        skillNodes.put(ID_OFFSET + 26, node26);
        skillNodes.put(ID_OFFSET + 27, node27);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Initialized all 27 Shadowstalker skills with ID offset " + ID_OFFSET);
        }
    }

    @Override
    protected void setupTreeStructure(SkillTree tree) {
        // Define root nodes
        tree.addRootNode(ID_OFFSET + 1);
        tree.addRootNode(ID_OFFSET + 2);
        tree.addRootNode(ID_OFFSET + 3);

        // === UNIVERSAL STRUCTURE FROM Drzewko Podklas.md ===

        // Poziom 1->2
        tree.connectNodes(ID_OFFSET + 1, ID_OFFSET + 4);
        tree.connectNodes(ID_OFFSET + 2, ID_OFFSET + 5);
        tree.connectNodes(ID_OFFSET + 3, ID_OFFSET + 6);

        // Poziom 2->3
        tree.connectNodes(ID_OFFSET + 4, ID_OFFSET + 7);
        tree.connectNodes(ID_OFFSET + 5, ID_OFFSET + 8);
        tree.connectNodes(ID_OFFSET + 5, ID_OFFSET + 9);  // Node 5 ma DWA wyjścia!
        tree.connectNodes(ID_OFFSET + 6, ID_OFFSET + 10);

        // Poziom 3->4
        tree.connectNodes(ID_OFFSET + 7, ID_OFFSET + 11);
        tree.connectNodes(ID_OFFSET + 7, ID_OFFSET + 12); // Node 7 ma DWA wyjścia!
        tree.connectNodes(ID_OFFSET + 8, ID_OFFSET + 13);
        tree.connectNodes(ID_OFFSET + 9, ID_OFFSET + 14);
        tree.connectNodes(ID_OFFSET + 10, ID_OFFSET + 15);
        tree.connectNodes(ID_OFFSET + 10, ID_OFFSET + 16); // Node 10 ma DWA wyjścia!

        // Poziom 4->5
        tree.connectNodes(ID_OFFSET + 11, ID_OFFSET + 17);
        tree.connectNodes(ID_OFFSET + 12, ID_OFFSET + 18);
        tree.connectNodes(ID_OFFSET + 13, ID_OFFSET + 19);
        tree.connectNodes(ID_OFFSET + 14, ID_OFFSET + 19); // Fixed: Changed from 14->20 to 14->19
        tree.connectNodes(ID_OFFSET + 15, ID_OFFSET + 20);
        tree.connectNodes(ID_OFFSET + 16, ID_OFFSET + 21);

        // Poziom 5->6
        tree.connectNodes(ID_OFFSET + 17, ID_OFFSET + 22);
        tree.connectNodes(ID_OFFSET + 18, ID_OFFSET + 22); // Oba 17 i 18 -> 22!
        tree.connectNodes(ID_OFFSET + 19, ID_OFFSET + 23);
        tree.connectNodes(ID_OFFSET + 20, ID_OFFSET + 24);
        tree.connectNodes(ID_OFFSET + 21, ID_OFFSET + 24); // Też 21 -> 24!

        // Poziom 6->7
        tree.connectNodes(ID_OFFSET + 22, ID_OFFSET + 25);
        tree.connectNodes(ID_OFFSET + 23, ID_OFFSET + 26);
        tree.connectNodes(ID_OFFSET + 24, ID_OFFSET + 27);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Configured Shadowstalker skill tree with UNIVERSAL structure from Drzewko Podklas.md");
            plugin.getLogger().info("Structure: 3 paths merging at nodes 20, 22, and 24");

            // Log all connections to verify
            for (int i = 1; i <= 27; i++) {
                logNodeConnections(tree, ID_OFFSET + i);
            }
        }
    }


    @Override
    protected void applySkillStats(Player player, int skillId, int purchaseCount) {
        int originalId = skillId - ID_OFFSET;
        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Applying Shadowstalker skill " + originalId + 
                    " for player " + player.getName() + " (purchase " + purchaseCount + ")");
        }

        switch (originalId) {
            case 2: // Critical Precision: +2% critical hit chance (1/3)
                stats.addCriticalChance(2 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Added " + (2 * purchaseCount) + 
                            "% critical chance to player stats");
                }
                break;

            case 4: // Sneaking Evasion: +4% evade chance while sneaking
                // To jest warunkowe, obsługiwane w EffectsHandler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Sneaking evasion will be handled dynamically");
                }
                break;

            case 6: // Shadow Evasion: +3% evade chance in darkness (1/2)
                // Warunkowe, ale możemy dodać bazowy evade jako przybliżenie
                stats.addEvadeChance(3 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Added " + (3 * purchaseCount) + 
                            "% base evade chance (full effect in darkness)");
                }
                break;

            case 11: // Critical Power: +20% critical damage (1/2)
                stats.addCriticalDamageBonus(0.2 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Added " + (20 * purchaseCount) + 
                            "% critical damage bonus to player stats");
                }
                break;

            case 13: // Patient Hunter: +4% movement speed (statyczny bonus)
                stats.addMovementSpeedBonus(4);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Added 4% movement speed from Patient Hunter");
                }
                break;

            case 22: // Shadow Protection: +10% damage reduction in darkness (1/2)
                // Warunkowe, ale możemy dodać bazową obronę
                stats.addDefenseBonus(10 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Added " + (10 * purchaseCount) + 
                            "% defense bonus (full effect in darkness)");
                }
                break;

            // Wszystkie pozostałe umiejętności są dynamiczne i obsługiwane w EffectsHandler
            default:
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Skill " + originalId + 
                            " effects will be handled by ShadowstalkerSkillEffectsHandler");
                }
                break;
        }

        // Odśwież statystyki gracza
        plugin.getSkillEffectsHandler().refreshPlayerStats(player);
    }

    @Override
    public boolean isMultiPurchaseDiscountSkill(int skillId) {
        int originalId = skillId - ID_OFFSET;

        // Lista umiejętności z wielokrotnym zakupem (z opisów (1/X))
        switch (originalId) {
            case 1:   // Shadow Speed (1/2)
            case 2:   // Critical Precision (1/3)
            case 3:   // First Strike (1/2)
            case 5:   // Critical Bleeding (1/2)
            case 6:   // Shadow Evasion (1/2)
            case 8:   // Backstab (1/2)
            case 9:   // Poisoned Blade (1/2)
            case 11:  // Critical Power (1/2)
            case 14:  // Toxic Exploitation (1/2)
            case 15:  // Potent Toxins (1/2)
            case 16:  // Shadow Step (1/2)
            case 17:  // Devastating Critical (1/2)
            case 20:  // Opening Strike (1/2)
            case 21:  // Toxic Amplification (1/2)
            case 22:  // Shadow Protection (1/2)
            case 24:  // Wind Mastery (1/2)
            case 26:  // Toxic Focus (1/2)
                return true;
            default:
                return false;
        }
    }

    /**
     * Log detailed information about a node's connections
     */
    private void logNodeConnections(SkillTree tree, int nodeId) {
        SkillNode node = tree.getNode(nodeId);
        if (node == null) {
            plugin.getLogger().info("[SHADOWSTALKER DEBUG] Node " + nodeId + " not found in tree!");
            return;
        }

        plugin.getLogger().info("[SHADOWSTALKER DEBUG] Node " + nodeId + " (" + node.getName() + ") connections:");

        // Log outgoing connections
        plugin.getLogger().info("[SHADOWSTALKER DEBUG] - Outgoing connections: " + node.getConnectedNodes().size());
        for (SkillNode connected : node.getConnectedNodes()) {
            plugin.getLogger().info("[SHADOWSTALKER DEBUG]   -> " + connected.getId() + " (" + connected.getName() + ")");
        }

        // Log incoming connections
        plugin.getLogger().info("[SHADOWSTALKER DEBUG] - Checking incoming connections...");
        boolean hasIncoming = false;
        for (SkillNode otherNode : tree.getAllNodes()) {
            for (SkillNode connectedNode : otherNode.getConnectedNodes()) {
                if (connectedNode.getId() == nodeId) {
                    plugin.getLogger().info("[SHADOWSTALKER DEBUG]   <- " + otherNode.getId() + " (" + otherNode.getName() + ")");
                    hasIncoming = true;
                }
            }
        }

        if (!hasIncoming) {
            plugin.getLogger().info("[SHADOWSTALKER DEBUG]   No incoming connections found!");
        }
    }
}
