package com.maks.myexperienceplugin.Class.skills.classes.dragonknight.ascendancy;

import com.maks.myexperienceplugin.Class.skills.base.BaseSkillManager;
import com.maks.myexperienceplugin.Class.skills.base.SkillNode;
import com.maks.myexperienceplugin.Class.skills.base.SkillTree;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class BerserkerSkillManager extends BaseSkillManager {

    public BerserkerSkillManager(MyExperiencePlugin plugin) {
        super(plugin, "Berserker");
    }

    @Override
    protected void initializeSkills() {
        // Create nodes based on Berserk.md
        SkillNode node1 = new SkillNode(1, "Unarmored Rage", "Cannot wear chestplate but +200% dmg", 1,
                Material.IRON_CHESTPLATE, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your rage grants you immense power at the cost of protection!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Unarmored Rage skill");
            }
        });

        SkillNode node2 = new SkillNode(2, "Berserker's Fury", "Each 10% hp lost gives +10% dmg", 1,
                Material.REDSTONE, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your power grows as your health wanes!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Berserker's Fury skill");
            }
        });

        SkillNode node3 = new SkillNode(3, "Combat Momentum", "While in combat gain +5% dmg every 30s", 1,
                Material.GOLDEN_SWORD, 1, player -> {
            player.sendMessage(ChatColor.RED + "The longer you fight, the stronger you become!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Combat Momentum skill");
            }
        });

        SkillNode node4 = new SkillNode(4, "Kill Frenzy", "For each kill +1% ms and +1% dmg for 30s (max 10 stacks)", 2,
                Material.GHAST_TEAR, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your kills fuel your battle frenzy!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Kill Frenzy skill");
            }
        });

        SkillNode node5 = new SkillNode(5, "Battle Rage", "For each hit +1% dmg for 5s (max 5 stacks)", 2,
                Material.BLAZE_POWDER, 1, player -> {
            player.sendMessage(ChatColor.RED + "Each strike makes you stronger!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Battle Rage skill");
            }
        });

        SkillNode node6 = new SkillNode(6, "Strength Boost", "+5% damage", 2,
                Material.DIAMOND_SWORD, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your raw strength increases!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Strength Boost skill");
            }
        });

        SkillNode node7 = new SkillNode(7, "Glass Cannon", "-10% hp, +10% dmg", 2,
                Material.GLASS, 2, player -> {
            player.sendMessage(ChatColor.RED + "You sacrifice health for power!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Glass Cannon skill");
            }
        });

        SkillNode node8 = new SkillNode(8, "Reckless Strike", "-5 armor, +10% crit chance", 2,
                Material.IRON_AXE, 1, player -> {
            player.sendMessage(ChatColor.RED + "You abandon defense for deadlier strikes!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Reckless Strike skill");
            }
        });

        SkillNode node9 = new SkillNode(9, "Critical Specialization", "Crits deal +15% more dmg", 2,
                Material.DIAMOND_AXE, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your critical hits become devastating!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Critical Specialization skill");
            }
        });

        SkillNode node10 = new SkillNode(10, "Attack Speed Frenzy", "For every killed mob +1% attack speed for 30s (max 10 stacks)", 2,
                Material.SUGAR, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your attacks become faster with each kill!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Attack Speed Frenzy skill");
            }
        });

        // Add nodes 11-27 following the same pattern
        SkillNode node11 = new SkillNode(11, "Finishing Blow", "Finish off mobs that are <5% hp", 3,
                Material.NETHERITE_AXE, 1, player -> {
            player.sendMessage(ChatColor.RED + "You can now finish off weakened enemies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Finishing Blow skill");
            }
        });

        SkillNode node12 = new SkillNode(12, "Tactical Defense", "+5 armor, +5% def", 3,
                Material.CHAIN_COMMAND_BLOCK, 1, player -> {
            player.sendMessage(ChatColor.RED + "You've learned to incorporate some defense into your rage!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Tactical Defense skill");
            }
        });

        // Add more nodes to reach node27

        // Also add intermediate nodes for the example
        SkillNode node13 = new SkillNode(13, "Vitality", "+20 hp", 3,
                Material.GOLDEN_APPLE, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your vitality increases!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Vitality skill");
            }
        });

        SkillNode node14 = new SkillNode(14, "Bleeding Strike", "20% chance for crits to cause bleeding (25% dmg/s for 5s)", 3,
                Material.REDSTONE_BLOCK, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your critical hits now cause enemies to bleed!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Bleeding Strike skill");
            }
        });

        // Skipping to final node for example
        SkillNode node27 = new SkillNode(27, "Death Defiance", "When hp<10% get fully healed, +200% dmg, -50% def for 10s", 5,
                Material.TOTEM_OF_UNDYING, 1, player -> {
            player.sendMessage(ChatColor.RED + "When near death, you unleash your ultimate rage!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Death Defiance skill");
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
        // Add rest of nodes here
        skillNodes.put(27, node27);
    }

    @Override
    protected void setupTreeStructure(SkillTree tree) {
        // Define root nodes
        tree.addRootNode(1);
        tree.addRootNode(2);
        tree.addRootNode(3);

        // Connect nodes for demonstration
        tree.connectNodes(1, 4);
        tree.connectNodes(2, 5);
        tree.connectNodes(3, 6);

        tree.connectNodes(4, 7);
        tree.connectNodes(5, 8);
        tree.connectNodes(6, 9);
        tree.connectNodes(6, 10);

        tree.connectNodes(7, 11);
        tree.connectNodes(8, 12);
        tree.connectNodes(9, 13);
        tree.connectNodes(10, 14);

        // Connect to final node for demonstration
        tree.connectNodes(14, 27);
    }

    @Override
    protected void applySkillStats(Player player, int skillId, int purchaseCount) {
        switch (skillId) {
            case 1: // Cannot wear chestplate but u gain +200% dmg
                // Note: In actual implementation, need to check if player is wearing chestplate
                // and implement damage multiplier based on that
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +200% damage when not wearing chestplate to " + player.getName());
                }
                break;
            case 2: // Each 10% hp u lose u gain +10% dmg
                // This would be handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied health-based damage scaling to " + player.getName());
                }
                break;
            case 6: // +5% dmg
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +5% damage to " + player.getName());
                }
                break;
            case 7: // -10% hp, +10% dmg
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied -10% HP, +10% damage to " + player.getName() +
                            " (purchase " + purchaseCount + "/2)");
                }
                break;
            // Implement other skills as needed
        }
    }

    @Override
    public boolean isMultiPurchaseDiscountSkill(int skillId) {
        // List of Berserker skills with (1/X) notation that have special cost structure
        return skillId == 7;
    }
}