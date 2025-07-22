package com.maks.myexperienceplugin.Class.skills.classes.dragonknight.ascendancy;

import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.base.BaseSkillManager;
import com.maks.myexperienceplugin.Class.skills.base.SkillNode;
import com.maks.myexperienceplugin.Class.skills.base.SkillTree;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class BerserkerSkillManager extends BaseSkillManager {

    private static final int ID_OFFSET = 200000;

    public BerserkerSkillManager(MyExperiencePlugin plugin) {
        super(plugin, "Berserker");
    }

    @Override
    protected void initializeSkills() {
        // === ŚCIEŻKA 1: RAGE PATH (Skills 1, 2, 3, 6, 7, 11, 17, 23, 25) ===

        SkillNode node1 = new SkillNode(ID_OFFSET + 1, "Unarmored Rage", "Cannot wear chestplate but u gain +200% dmg", 1,
                Material.IRON_CHESTPLATE, 1, player -> {
            player.sendMessage(ChatColor.DARK_RED + "Your rage grants immense power at the cost of protection!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Unarmored Rage skill");
            }
        });

        SkillNode node2 = new SkillNode(ID_OFFSET + 2, "Berserker's Fury", "Each 10% hp u lose u gain +10% dmg", 1,
                Material.REDSTONE, 1, player -> {
            player.sendMessage(ChatColor.DARK_RED + "Your power grows as your health wanes!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Berserker's Fury skill");
            }
        });

        SkillNode node3 = new SkillNode(ID_OFFSET + 3, "Combat Momentum", "While in combat (10s after attack) every 30s u gain +5% dmg", 1,
                Material.GOLDEN_SWORD, 1, player -> {
            player.sendMessage(ChatColor.DARK_RED + "The longer you fight, the stronger you become!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Combat Momentum skill");
            }
        });

        SkillNode node6 = new SkillNode(ID_OFFSET + 6, "Pure Strength", "+5% dmg", 2,
                Material.IRON_SWORD, 1, player -> {
            player.sendMessage(ChatColor.DARK_RED + "Your strength increases!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Pure Strength skill");
            }
        });

        SkillNode node7 = new SkillNode(ID_OFFSET + 7, "Blood for Power", "-10% hp, +10% dmg", 2,
                Material.WITHER_ROSE, 2, player -> {
            player.sendMessage(ChatColor.DARK_RED + "You sacrifice health for raw power!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Blood for Power skill");
            }
        });

        SkillNode node11 = new SkillNode(ID_OFFSET + 11, "Executioner", "Ur finishing off mobs that are <5% hp", 3,
                Material.DIAMOND_AXE, 1, player -> {
            player.sendMessage(ChatColor.DARK_RED + "You become an efficient executioner!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Executioner skill");
            }
        });

        SkillNode node17 = new SkillNode(ID_OFFSET + 17, "Raw Power", "+50 dmg", 3,
                Material.DIAMOND, 1, player -> {
            player.sendMessage(ChatColor.DARK_RED + "Your raw power increases dramatically!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Raw Power skill");
            }
        });

        SkillNode node23 = new SkillNode(ID_OFFSET + 23, "Desperate Rage", "When hp<50% u gain +25% dmg and +15% crit", 3,
                Material.TOTEM_OF_UNDYING, 1, player -> {
            player.sendMessage(ChatColor.DARK_RED + "Desperation fuels your rage!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Desperate Rage skill");
            }
        });

        SkillNode node25 = new SkillNode(ID_OFFSET + 25, "Barefoot Fury", "U cannot wear boots but u gain +50% dmg and +50% ms", 5,
                Material.LEATHER_BOOTS, 1, player -> {
            player.sendMessage(ChatColor.DARK_RED + "You fight barefoot with primal fury!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Barefoot Fury skill");
            }
        });

        // === ŚCIEŻKA 2: CRITICAL PATH (Skills 8, 9, 14, 16, 18, 20, 26) ===

        SkillNode node8 = new SkillNode(ID_OFFSET + 8, "Glass Cannon", "U gain -5 armor and +10% crit", 2,
                Material.GLASS, 1, player -> {
            player.sendMessage(ChatColor.RED + "You become fragile but deadly!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Glass Cannon skill");
            }
        });

        SkillNode node9 = new SkillNode(ID_OFFSET + 9, "Critical Fury", "Ur crit deals +15% more dmg", 2,
                Material.NETHER_STAR, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your critical hits become devastating!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Critical Fury skill");
            }
        });

        SkillNode node14 = new SkillNode(ID_OFFSET + 14, "Bleeding Strike", "Ur crit now have a 20% chance to cause bleeding that deals 25% ur base dmg per s for 5s", 3,
                Material.REDSTONE_BLOCK, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your critical hits cause bleeding!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Bleeding Strike skill");
            }
        });

        SkillNode node16 = new SkillNode(ID_OFFSET + 16, "Critical Power", "Ur crit deals +5% more dmg", 3,
                Material.EMERALD, 2, player -> {
            player.sendMessage(ChatColor.RED + "Your critical power increases!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Critical Power skill");
            }
        });

        SkillNode node18 = new SkillNode(ID_OFFSET + 18, "Critical Mastery", "U gain +10% crit chance", 3,
                Material.EMERALD_BLOCK, 1, player -> {
            player.sendMessage(ChatColor.RED + "You master the art of critical hits!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Critical Mastery skill");
            }
        });

        SkillNode node20 = new SkillNode(ID_OFFSET + 20, "Rhythmic Strikes", "Ur every 10th hit is crit", 3,
                Material.CLOCK, 1, player -> {
            player.sendMessage(ChatColor.RED + "You develop a deadly rhythm!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Rhythmic Strikes skill");
            }
        });

        SkillNode node26 = new SkillNode(ID_OFFSET + 26, "Trophy Hunter", "After killing mob u have 1% chance to gain his head for 10min that gives u +10% dmg (UNIQUE STACKS NO LIMIT)", 5,
                Material.SKELETON_SKULL, 1, player -> {
            player.sendMessage(ChatColor.RED + "You collect trophies from your victims!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Trophy Hunter skill");
            }
        });

        // === ŚCIEŻKA 3: FRENZY PATH (Skills 4, 5, 10, 12, 13, 15, 19, 21, 22, 24, 27) ===

        SkillNode node4 = new SkillNode(ID_OFFSET + 4, "Kill Frenzy", "For every killed mob u gain +1% ms and +1% dmg for 30s (max 10 stacks)", 2,
                Material.GHAST_TEAR, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "Your kills fuel your battle frenzy!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Kill Frenzy skill");
            }
        });

        SkillNode node5 = new SkillNode(ID_OFFSET + 5, "Battle Rage", "For every hit u gain +1% dmg for 5s (max 5 stacks)", 2,
                Material.BLAZE_POWDER, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "Each strike makes you stronger!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Battle Rage skill");
            }
        });

        SkillNode node10 = new SkillNode(ID_OFFSET + 10, "Attack Speed Frenzy", "For every killed mob u gain +1% as for 30s (max 10 stacks)", 3,
                Material.SUGAR, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "Your attack speed increases with each kill!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Attack Speed Frenzy skill");
            }
        });

        SkillNode node12 = new SkillNode(ID_OFFSET + 12, "Iron Defense", "+5 armor, +5% def", 3,
                Material.IRON_INGOT, 1, player -> {
            player.sendMessage(ChatColor.GRAY + "You gain defensive capabilities!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Iron Defense skill");
            }
        });

        SkillNode node13 = new SkillNode(ID_OFFSET + 13, "Vitality", "U gain +20 hp", 3,
                Material.GOLDEN_APPLE, 1, player -> {
            player.sendMessage(ChatColor.GOLD + "Your vitality increases!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Vitality skill");
            }
        });

        SkillNode node15 = new SkillNode(ID_OFFSET + 15, "Agility", "+5% movement speed", 3,
                Material.LEATHER_BOOTS, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "Your agility improves!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Agility skill");
            }
        });

        SkillNode node19 = new SkillNode(ID_OFFSET + 19, "Reckless Power", "-5% def, +5% dmg", 3,
                Material.REDSTONE_TORCH, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "You sacrifice defense for power!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Reckless Power skill");
            }
        });

        SkillNode node21 = new SkillNode(ID_OFFSET + 21, "Heavy Armor", "+10 armor", 3,
                Material.DIAMOND_CHESTPLATE, 1, player -> {
            player.sendMessage(ChatColor.GRAY + "You gain heavy armor protection!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Heavy Armor skill");
            }
        });

        SkillNode node22 = new SkillNode(ID_OFFSET + 22, "Rampage", "For every 25 killed mob's u gain +30% dmg (max 2 stacks)", 5,
                Material.WITHER_SKELETON_SKULL, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "Your killing spree grants massive power!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Rampage skill");
            }
        });

        SkillNode node24 = new SkillNode(ID_OFFSET + 24, "Focus Target", "Every hit on the target deals +5% dmg (resets on target change, max 5 stacks)", 5,
                Material.TARGET, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "You focus your fury on single targets!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Focus Target skill");
            }
        });

        SkillNode node27 = new SkillNode(ID_OFFSET + 27, "Last Stand", "Once your hp<10% u get fully heald, gain +200% dmg and -50% def for 10s", 5,
                Material.TOTEM_OF_UNDYING, 1, player -> {
            player.sendMessage(ChatColor.DARK_RED + "Your final stand will be legendary!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Last Stand skill");
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
            plugin.getLogger().info("Initialized all 27 Berserker skills with ID offset " + ID_OFFSET);
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
            plugin.getLogger().info("Configured Berserker skill tree with UNIVERSAL structure from Drzewko Podklas.md");
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
            plugin.getLogger().info("Applying Berserker skill " + originalId + 
                    " for player " + player.getName() + " (purchase " + purchaseCount + ")");
        }

        switch (originalId) {
            case 1: // Unarmored Rage - dynamiczny efekt
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Unarmored Rage will be handled dynamically");
                }
                break;

            case 6: // Pure Strength: +5% dmg
                stats.addDamageMultiplier(0.05);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Added 5% damage multiplier");
                }
                break;

            case 7: // Blood for Power: -10% hp, +10% dmg (1/2)
                stats.addMaxHealth(-10 * purchaseCount);
                stats.addDamageMultiplier(0.10 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Added -" + (10 * purchaseCount) + "% HP, +" + 
                            (10 * purchaseCount) + "% damage");
                }
                break;

            case 8: // Glass Cannon: -5 armor, +10% crit
                stats.addDefenseBonus(-5); // Symulacja -5 armor
                stats.addCriticalChance(10);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Added -5 defense (armor), +10% crit");
                }
                break;

            case 12: // Iron Defense: +5 armor, +5% def
                stats.addDefenseBonus(5);
                stats.addShieldBlockChance(5); // Symulacja armor
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Added +5 armor, +5% defense");
                }
                break;

            case 13: // Vitality: +20 hp
                stats.addMaxHealth(20);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Added +20 max HP");
                }
                break;

            case 15: // Agility: +5% ms
                stats.addMovementSpeedBonus(5);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Added +5% movement speed");
                }
                break;

            case 17: // Raw Power: +50 dmg
                stats.addBonusDamage(50);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Added +50 flat damage");
                }
                break;

            case 18: // Critical Mastery: +10% crit chance
                stats.addCriticalChance(10);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Added +10% critical chance");
                }
                break;

            case 19: // Reckless Power: -5% def, +5% dmg
                stats.addDefenseBonus(-5);
                stats.addDamageMultiplier(0.05);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Added -5% defense, +5% damage");
                }
                break;

            case 21: // Heavy Armor: +10 armor
                stats.addShieldBlockChance(10); // Symulacja armor
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Added +10 armor (as shield block chance)");
                }
                break;

            default:
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Skill " + originalId + 
                            " effects will be handled by BerserkerSkillEffectsHandler");
                }
                break;
        }

        // Odśwież statystyki gracza
        plugin.getSkillEffectsHandler().refreshPlayerStats(player);
    }

    @Override
    public boolean isMultiPurchaseDiscountSkill(int skillId) {
        int originalId = skillId - ID_OFFSET;

        // Lista umiejętności z wielokrotnym zakupem
        switch (originalId) {
            case 7:   // Blood for Power (1/2)
            case 16:  // Critical Power (1/2)
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
            plugin.getLogger().info("[BERSERKER DEBUG] Node " + nodeId + " not found in tree!");
            return;
        }

        plugin.getLogger().info("[BERSERKER DEBUG] Node " + nodeId + " (" + node.getName() + ") connections:");

        // Log outgoing connections
        plugin.getLogger().info("[BERSERKER DEBUG] - Outgoing connections: " + node.getConnectedNodes().size());
        for (SkillNode connected : node.getConnectedNodes()) {
            plugin.getLogger().info("[BERSERKER DEBUG]   -> " + connected.getId() + " (" + connected.getName() + ")");
        }

        // Log incoming connections
        plugin.getLogger().info("[BERSERKER DEBUG] - Checking incoming connections...");
        boolean hasIncoming = false;
        for (SkillNode otherNode : tree.getAllNodes()) {
            for (SkillNode connectedNode : otherNode.getConnectedNodes()) {
                if (connectedNode.getId() == nodeId) {
                    plugin.getLogger().info("[BERSERKER DEBUG]   <- " + otherNode.getId() + " (" + otherNode.getName() + ")");
                    hasIncoming = true;
                }
            }
        }

        if (!hasIncoming) {
            plugin.getLogger().info("[BERSERKER DEBUG]   No incoming connections found!");
        }
    }
}
