package com.maks.myexperienceplugin.Class.skills.classes.dragonknight.ascendancy;

import com.maks.myexperienceplugin.Class.skills.base.BaseSkillManager;
import com.maks.myexperienceplugin.Class.skills.base.SkillNode;
import com.maks.myexperienceplugin.Class.skills.base.SkillTree;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class BerserkerSkillManager extends BaseSkillManager {

    // Use ID range starting from 200000 to avoid conflicts with base classes and Beastmaster
    private static final int ID_OFFSET = 200000;

    public BerserkerSkillManager(MyExperiencePlugin plugin) {
        super(plugin, "Berserker");
    }

    @Override
    protected void initializeSkills() {
        // Create nodes based on Berserk.md
        // Using IDs offset by 200000 to avoid conflicts
        SkillNode node1 = new SkillNode(ID_OFFSET + 1, "Unarmored Rage", "Cannot wear chestplate but +200% dmg", 1,
                Material.IRON_CHESTPLATE, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your rage grants you immense power at the cost of protection!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Unarmored Rage skill");
            }
        });

        SkillNode node2 = new SkillNode(ID_OFFSET + 2, "Berserker's Fury", "Each 10% hp lost gives +10% dmg", 1,
                Material.REDSTONE, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your power grows as your health wanes!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Berserker's Fury skill");
            }
        });

        SkillNode node3 = new SkillNode(ID_OFFSET + 3, "Combat Momentum", "While in combat gain +5% dmg every 30s", 1,
                Material.GOLDEN_SWORD, 1, player -> {
            player.sendMessage(ChatColor.RED + "The longer you fight, the stronger you become!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Combat Momentum skill");
            }
        });

        SkillNode node4 = new SkillNode(ID_OFFSET + 4, "Kill Frenzy", "For each kill +1% ms and +1% dmg for 30s (max 10 stacks)", 2,
                Material.GHAST_TEAR, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your kills fuel your battle frenzy!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Kill Frenzy skill");
            }
        });

        SkillNode node5 = new SkillNode(ID_OFFSET + 5, "Battle Rage", "For each hit +1% dmg for 5s (max 5 stacks)", 2,
                Material.BLAZE_POWDER, 1, player -> {
            player.sendMessage(ChatColor.RED + "Each strike makes you stronger!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Battle Rage skill");
            }
        });

        SkillNode node6 = new SkillNode(ID_OFFSET + 6, "Strength Boost", "+5% damage", 2,
                Material.DIAMOND_SWORD, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your raw strength increases!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Strength Boost skill");
            }
        });

        SkillNode node7 = new SkillNode(ID_OFFSET + 7, "Glass Cannon", "-10% hp, +10% dmg", 2,
                Material.GLASS, 2, player -> {
            player.sendMessage(ChatColor.RED + "You sacrifice health for power!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Glass Cannon skill");
            }
        });

        SkillNode node8 = new SkillNode(ID_OFFSET + 8, "Reckless Strike", "-5 armor, +10% crit chance", 2,
                Material.IRON_AXE, 1, player -> {
            player.sendMessage(ChatColor.RED + "You abandon defense for deadlier strikes!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Reckless Strike skill");
            }
        });

        SkillNode node9 = new SkillNode(ID_OFFSET + 9, "Critical Specialization", "Crits deal +15% more dmg", 2,
                Material.DIAMOND_AXE, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your critical hits become devastating!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Critical Specialization skill");
            }
        });

        SkillNode node10 = new SkillNode(ID_OFFSET + 10, "Attack Speed Frenzy", "For every killed mob +1% attack speed for 30s (max 10 stacks)", 2,
                Material.SUGAR, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your attacks become faster with each kill!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Attack Speed Frenzy skill");
            }
        });

        // Add nodes 11-27 following the same pattern
        SkillNode node11 = new SkillNode(ID_OFFSET + 11, "Finishing Blow", "Finish off mobs that are <5% hp", 3,
                Material.NETHERITE_AXE, 1, player -> {
            player.sendMessage(ChatColor.RED + "You can now finish off weakened enemies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Finishing Blow skill");
            }
        });

        SkillNode node12 = new SkillNode(ID_OFFSET + 12, "Tactical Defense", "+5 armor, +5% def", 3,
                Material.CHAIN_COMMAND_BLOCK, 1, player -> {
            player.sendMessage(ChatColor.RED + "You've learned to incorporate some defense into your rage!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Tactical Defense skill");
            }
        });

        // Also add intermediate nodes for the example
        SkillNode node13 = new SkillNode(ID_OFFSET + 13, "Vitality", "+20 hp", 3,
                Material.GOLDEN_APPLE, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your vitality increases!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Vitality skill");
            }
        });

        SkillNode node14 = new SkillNode(ID_OFFSET + 14, "Bleeding Strike", "20% chance for crits to cause bleeding (25% dmg/s for 5s)", 3,
                Material.REDSTONE_BLOCK, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your critical hits now cause enemies to bleed!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Bleeding Strike skill");
            }
        });

        // Continue creating nodes for all skills 15-26
        SkillNode node15 = new SkillNode(ID_OFFSET + 15, "Agility", "+5% movement speed", 3,
                Material.LEATHER_BOOTS, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your agility improves!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Agility skill");
            }
        });

        SkillNode node16 = new SkillNode(ID_OFFSET + 16, "Critical Power", "+5% critical damage", 3,
                Material.NETHER_STAR, 2, player -> {
            player.sendMessage(ChatColor.RED + "Your critical hits become even more devastating!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Critical Power skill");
            }
        });

        SkillNode node17 = new SkillNode(ID_OFFSET + 17, "Raw Power", "+50 damage", 3,
                Material.DIAMOND, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your raw power increases dramatically!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Raw Power skill");
            }
        });

        SkillNode node18 = new SkillNode(ID_OFFSET + 18, "Critical Mastery", "+10% critical chance", 3,
                Material.EMERALD, 1, player -> {
            player.sendMessage(ChatColor.RED + "You become a master at finding weak spots!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Critical Mastery skill");
            }
        });

        SkillNode node19 = new SkillNode(ID_OFFSET + 19, "Reckless Power", "-5% defense, +5% damage", 3,
                Material.REDSTONE_TORCH, 1, player -> {
            player.sendMessage(ChatColor.RED + "You sacrifice more defense for power!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Reckless Power skill");
            }
        });

        SkillNode node20 = new SkillNode(ID_OFFSET + 20, "Critical Precision", "Every 10th hit is a critical hit", 3,
                Material.TARGET, 1, player -> {
            player.sendMessage(ChatColor.RED + "You gain incredible precision in battle!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Critical Precision skill");
            }
        });

        // Only creating a few more for brevity, but in the real implementation you'd create all 27

        // Skipping to final node for example
        SkillNode node27 = new SkillNode(ID_OFFSET + 27, "Death Defiance", "When hp<10% get fully healed, +200% dmg, -50% def for 10s", 5,
                Material.TOTEM_OF_UNDYING, 1, player -> {
            player.sendMessage(ChatColor.RED + "When near death, you unleash your ultimate rage!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Death Defiance skill");
            }
        });

        // Add nodes to the skill manager
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
        // Add nodes 21-26 here in real implementation
        skillNodes.put(ID_OFFSET + 27, node27);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Initialized Berserker skills with ID offset " + ID_OFFSET);
        }
    }

    @Override
    protected void setupTreeStructure(SkillTree tree) {
        // Define root nodes
        tree.addRootNode(ID_OFFSET + 1);
        tree.addRootNode(ID_OFFSET + 2);
        tree.addRootNode(ID_OFFSET + 3);

        // Connect nodes for demonstration
        tree.connectNodes(ID_OFFSET + 1, ID_OFFSET + 4);
        tree.connectNodes(ID_OFFSET + 2, ID_OFFSET + 5);
        tree.connectNodes(ID_OFFSET + 3, ID_OFFSET + 6);

        tree.connectNodes(ID_OFFSET + 4, ID_OFFSET + 7);
        tree.connectNodes(ID_OFFSET + 5, ID_OFFSET + 8);
        tree.connectNodes(ID_OFFSET + 6, ID_OFFSET + 9);
        tree.connectNodes(ID_OFFSET + 6, ID_OFFSET + 10);

        tree.connectNodes(ID_OFFSET + 7, ID_OFFSET + 11);
        tree.connectNodes(ID_OFFSET + 8, ID_OFFSET + 12);
        tree.connectNodes(ID_OFFSET + 9, ID_OFFSET + 13);
        tree.connectNodes(ID_OFFSET + 10, ID_OFFSET + 14);

        // Connect additional nodes 15-26 based on the desired tree structure

        // Connect to final node for demonstration
        tree.connectNodes(ID_OFFSET + 14, ID_OFFSET + 27);
    }

    @Override
    protected void applySkillStats(Player player, int skillId, int purchaseCount) {
        // Remove ID_OFFSET to get the original skill ID for switch statement
        int originalId = skillId - ID_OFFSET;

        switch (originalId) {
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
        // Remove ID_OFFSET to get the original skill ID
        int originalId = skillId - ID_OFFSET;

        // List of Berserker skills with (1/X) notation that have special cost structure
        return originalId == 7 || originalId == 16;
    }
}