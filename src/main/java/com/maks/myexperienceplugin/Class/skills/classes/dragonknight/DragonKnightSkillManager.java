package com.maks.myexperienceplugin.Class.skills.classes.dragonknight;

import com.maks.myexperienceplugin.Class.skills.base.BaseSkillManager;
import com.maks.myexperienceplugin.Class.skills.base.SkillNode;
import com.maks.myexperienceplugin.Class.skills.base.SkillTree;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class DragonKnightSkillManager extends BaseSkillManager {

    public DragonKnightSkillManager(MyExperiencePlugin plugin) {
        super(plugin, "DragonKnight");
    }

    @Override
    protected void initializeSkills() {
        // Create nodes based on DragonKnight.md - FULLY IMPLEMENTED
        SkillNode node1 = new SkillNode(1, "Draconic Defense", "+3% defense", 1,
                Material.SHIELD, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your scales harden!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Draconic Defense skill");
            }
        });

        SkillNode node2 = new SkillNode(2, "Battle Rhythm", "+5% attack speed for 5s after hit", 1,
                Material.IRON_SWORD, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your attacks quicken!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Battle Rhythm skill");
            }
        });

        SkillNode node3 = new SkillNode(3, "Dragon's Might", "+1% damage", 1,
                Material.BLAZE_POWDER, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your strength increases!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Dragon's Might skill");
            }
        });

        SkillNode node4 = new SkillNode(4, "Endurance", "When hp<50% gain +2% def", 2,
                Material.GOLDEN_APPLE, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your endurance increases in dire situations!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Endurance skill");
            }
        });

        SkillNode node5 = new SkillNode(5, "Dragon Speed", "+1% movement speed", 2,
                Material.LEATHER_BOOTS, 2, player -> {
            player.sendMessage(ChatColor.RED + "You move with draconic agility!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Dragon Speed skill");
            }
        });

        SkillNode node6 = new SkillNode(6, "Battle Fury", "For every 100 damage dealt, +1% damage for 5s", 2,
                Material.DIAMOND_SWORD, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your fury builds in battle!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Battle Fury skill");
            }
        });

        SkillNode node7 = new SkillNode(7, "Dragon Heart", "When hp<20% gain 20hp for 5s (1min cd)", 3,
                Material.HEART_OF_THE_SEA, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your dragon's heart beats stronger!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Dragon Heart skill");
            }
        });

        SkillNode node8 = new SkillNode(8, "Draconic Vitality", "+2 HP", 2,
                Material.RED_DYE, 2, player -> {
            player.sendMessage(ChatColor.RED + "Your vitality increases!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Draconic Vitality skill");
            }
        });

        SkillNode node9 = new SkillNode(9, "Dragon's Luck", "+1% luck", 2,
                Material.RABBIT_FOOT, 2, player -> {
            player.sendMessage(ChatColor.RED + "The dragon's fortune favors you!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Dragon's Luck skill");
            }
        });

        SkillNode node10 = new SkillNode(10, "Mighty Blows", "+7 damage", 2,
                Material.NETHERITE_AXE, 2, player -> {
            player.sendMessage(ChatColor.RED + "Your strikes become mightier!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Mighty Blows skill");
            }
        });

        SkillNode node11 = new SkillNode(11, "Dragon's Focus", "+5% dmg, -2% ms", 3,
                Material.DRAGON_HEAD, 1, player -> {
            player.sendMessage(ChatColor.RED + "You focus your draconic energy into pure power!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Dragon's Focus skill");
            }
        });

        SkillNode node12 = new SkillNode(12, "Life Drain", "After killing a mob u heal 2hp", 2,
                Material.REDSTONE, 1, player -> {
            player.sendMessage(ChatColor.RED + "You drain life from your victims!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Life Drain skill");
            }
        });

        SkillNode node13 = new SkillNode(13, "Power Strike", "+10 damage", 2,
                Material.DIAMOND_AXE, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your strikes become devastatingly powerful!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Power Strike skill");
            }
        });

        SkillNode node14 = new SkillNode(14, "Shield Mastery", "+5% shield block chance", 3,
                Material.SHIELD, 1, player -> {
            player.sendMessage(ChatColor.RED + "You master the art of blocking!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Shield Mastery skill");
            }
        });

        // Add nodes to the skill manager
        skillNodes.put(1, node1);
        skillNodes.put(2, node2);
        skillNodes.put(3, node3);
        skillNodes.put(4, node4);
        skillNodes.put(5, node5);
        skillNodes.put(6, node6);
        skillNodes.put(7, node7);
        skillNodes.put(8, node8);
        skillNodes.put(9, node9);
        skillNodes.put(10, node10);
        skillNodes.put(11, node11);
        skillNodes.put(12, node12);
        skillNodes.put(13, node13);
        skillNodes.put(14, node14);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Initialized DragonKnight skill manager with " + skillNodes.size() + " skills");
        }
    }

    @Override
    protected void setupTreeStructure(SkillTree tree) {
        // Define root nodes - these are the starting points of the tree
        tree.addRootNode(1);
        tree.addRootNode(2);
        tree.addRootNode(3);

        // Set up connections based on DragonKnight.md and Drzewko Klas.md
        // Path 1: Draconic Defense branch
        tree.connectNodes(1, 4);
        tree.connectNodes(4, 7);
        tree.connectNodes(7, 12);

        // Path 2: Battle Rhythm branch
        tree.connectNodes(2, 5);
        tree.connectNodes(5, 9);
        tree.connectNodes(9, 13);
        tree.connectNodes(13, 14);

        // Path 3: Dragon's Might branch
        tree.connectNodes(3, 6);
        tree.connectNodes(6, 10);
        tree.connectNodes(6, 11);

        // Connect nodes 4 to 8 as well
        tree.connectNodes(4, 8);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("DragonKnight skill tree structure configured with " +
                    tree.getRootNodeIds().size() + " root nodes and 11 connections");
        }
    }

    @Override
    protected void applySkillStats(Player player, int skillId, int purchaseCount) {
        switch (skillId) {
            case 1: // +3% def
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +3% defense to " + player.getName());
                }
                break;
            case 3: // +1% dmg
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +1% damage to " + player.getName());
                }
                break;
            case 5: // +1% ms (1/2)
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +" + purchaseCount + "% movement speed to " + player.getName() +
                            " (purchase " + purchaseCount + "/2)");
                }
                break;
            case 8: // +2hp (1/2)
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +" + (2 * purchaseCount) + " HP to " + player.getName() +
                            " (purchase " + purchaseCount + "/2)");
                }
                break;
            case 9: // +1% luck (1/2)
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +" + purchaseCount + "% luck to " + player.getName() +
                            " (purchase " + purchaseCount + "/2)");
                }
                break;
            case 10: // +7 dmg (1/2)
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +" + (7 * purchaseCount) + " damage to " + player.getName() +
                            " (purchase " + purchaseCount + "/2)");
                }
                break;
            case 11: // +5% dmg, -2% ms
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +5% damage, -2% movement speed to " + player.getName());
                }
                break;
            case 13: // +10 dmg
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +10 damage to " + player.getName());
                }
                break;
            case 14: // +5% shield block chance
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +5% shield block chance to " + player.getName());
                }
                break;
            default:
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Skill " + skillId + " effect will be handled by event handler");
                }
                break;
        }
    }

    @Override
    public boolean isMultiPurchaseDiscountSkill(int skillId) {
        // DragonKnight skills with (1/X) notation - can be purchased multiple times
        return skillId == 5 || skillId == 8 || skillId == 9 || skillId == 10;
    }
}