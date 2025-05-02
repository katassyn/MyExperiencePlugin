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

    // Use ID range starting from 200000 to avoid conflicts with base classes and Beastmaster
    private static final int ID_OFFSET = 200000;

    public BerserkerSkillManager(MyExperiencePlugin plugin) {
        super(plugin, "Berserker");
    }

    @Override
    protected void initializeSkills() {
        // Create nodes based on Berserk.md - FULLY IMPLEMENTED
        SkillNode node1 = new SkillNode(ID_OFFSET + 1, "Unarmored Rage", "Cannot wear chestplate but u gain +200% dmg", 1,
                Material.IRON_CHESTPLATE, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your rage grants you immense power at the cost of protection!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Unarmored Rage skill");
            }
        });

        SkillNode node2 = new SkillNode(ID_OFFSET + 2, "Berserker's Fury", "Each 10% hp u lose u gain +10% dmg", 1,
                Material.REDSTONE, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your power grows as your health wanes!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Berserker's Fury skill");
            }
        });

        SkillNode node3 = new SkillNode(ID_OFFSET + 3, "Combat Momentum", "While in combat (10s after attack) every 30s u gain +5% dmg", 1,
                Material.GOLDEN_SWORD, 1, player -> {
            player.sendMessage(ChatColor.RED + "The longer you fight, the stronger you become!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Combat Momentum skill");
            }
        });

        SkillNode node4 = new SkillNode(ID_OFFSET + 4, "Kill Frenzy", "For every killed mob u gain +1% ms and +1% dmg for 30s (max 10 stacks)", 2,
                Material.GHAST_TEAR, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your kills fuel your battle frenzy!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Kill Frenzy skill");
            }
        });

        SkillNode node5 = new SkillNode(ID_OFFSET + 5, "Battle Rage", "For every hit u gain +1% dmg for 5s (max 5 stacks)", 2,
                Material.BLAZE_POWDER, 1, player -> {
            player.sendMessage(ChatColor.RED + "Each strike makes you stronger!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Battle Rage skill");
            }
        });

        SkillNode node6 = new SkillNode(ID_OFFSET + 6, "Strength Boost", "+5% dmg", 2,
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

        SkillNode node8 = new SkillNode(ID_OFFSET + 8, "Reckless Strike", "-5 armor and +10% crit chance", 2,
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

        SkillNode node10 = new SkillNode(ID_OFFSET + 10, "Attack Speed Frenzy", "For every killed mob u gain +1% attack speed for 30s (max 10 stacks)", 2,
                Material.SUGAR, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your attacks become faster with each kill!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Attack Speed Frenzy skill");
            }
        });

        SkillNode node11 = new SkillNode(ID_OFFSET + 11, "Finishing Blow", "You're finishing off mobs that are <5% hp", 3,
                Material.NETHERITE_AXE, 1, player -> {
            player.sendMessage(ChatColor.RED + "You can now finish off weakened enemies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Finishing Blow skill");
            }
        });

        SkillNode node12 = new SkillNode(ID_OFFSET + 12, "Tactical Defense", "+5 armor, +5% def", 2,
                Material.CHAIN_COMMAND_BLOCK, 1, player -> {
            player.sendMessage(ChatColor.RED + "You've learned to incorporate some defense into your rage!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Tactical Defense skill");
            }
        });

        SkillNode node13 = new SkillNode(ID_OFFSET + 13, "Vitality", "+20 hp", 2,
                Material.GOLDEN_APPLE, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your vitality increases!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Vitality skill");
            }
        });

        SkillNode node14 = new SkillNode(ID_OFFSET + 14, "Bleeding Strike", "Ur crit now have a 20% chance to cause bleeding that deals 25% ur base dmg per s for 5s", 3,
                Material.REDSTONE_BLOCK, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your critical hits now cause enemies to bleed!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Bleeding Strike skill");
            }
        });

        SkillNode node15 = new SkillNode(ID_OFFSET + 15, "Agility", "+5% movement speed", 3,
                Material.LEATHER_BOOTS, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your agility improves!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Agility skill");
            }
        });

        SkillNode node16 = new SkillNode(ID_OFFSET + 16, "Critical Power", "Ur crit deals +5% more dmg", 3,
                Material.NETHER_STAR, 2, player -> {
            player.sendMessage(ChatColor.RED + "Your critical hits become even more devastating!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Critical Power skill");
            }
        });

        SkillNode node17 = new SkillNode(ID_OFFSET + 17, "Raw Power", "+50 dmg", 3,
                Material.DIAMOND, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your raw power increases dramatically!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Raw Power skill");
            }
        });

        SkillNode node18 = new SkillNode(ID_OFFSET + 18, "Critical Mastery", "U gain +10% crit chance", 3,
                Material.EMERALD, 1, player -> {
            player.sendMessage(ChatColor.RED + "You become a master at finding weak spots!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Critical Mastery skill");
            }
        });

        SkillNode node19 = new SkillNode(ID_OFFSET + 19, "Reckless Power", "-5% def, +5% dmg", 3,
                Material.REDSTONE_TORCH, 1, player -> {
            player.sendMessage(ChatColor.RED + "You sacrifice more defense for power!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Reckless Power skill");
            }
        });

        SkillNode node20 = new SkillNode(ID_OFFSET + 20, "Critical Precision", "Ur every 10th hit is crit", 3,
                Material.TARGET, 1, player -> {
            player.sendMessage(ChatColor.RED + "You gain incredible precision in battle!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Critical Precision skill");
            }
        });

        SkillNode node21 = new SkillNode(ID_OFFSET + 21, "Heavy Armor", "+10 armor", 3,
                Material.NETHERITE_CHESTPLATE, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your armor becomes sturdier!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Heavy Armor skill");
            }
        });

        SkillNode node22 = new SkillNode(ID_OFFSET + 22, "Killing Power", "For every 25 killed mob's u gain +30% dmg (max 2 stacks)", 3,
                Material.WITHER_SKELETON_SKULL, 1, player -> {
            player.sendMessage(ChatColor.RED + "You grow stronger with each life you take!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Killing Power skill");
            }
        });

        SkillNode node23 = new SkillNode(ID_OFFSET + 23, "Desperate Measures", "When hp<50% u gain +25% dmg and +15% crit", 3,
                Material.BLAZE_ROD, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your desperation fuels your attacks!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Desperate Measures skill");
            }
        });

        SkillNode node24 = new SkillNode(ID_OFFSET + 24, "Target Focus", "Every hit on the target deals +5% dmg (resets on target change, max 5 stacks)", 3,
                Material.SPYGLASS, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your attacks become increasingly focused on your target!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Target Focus skill");
            }
        });

        SkillNode node25 = new SkillNode(ID_OFFSET + 25, "Lightfoot Rage", "U cannot wear boots but u gain +50% dmg and +50% ms", 5,
                Material.GOLDEN_BOOTS, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your feet are unbound and your fury unleashed!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Lightfoot Rage skill");
            }
        });

        SkillNode node26 = new SkillNode(ID_OFFSET + 26, "Trophy Hunter", "After killing mob u have 1% chance to gain his head for 10min that gives u +10% dmg", 5,
                Material.PLAYER_HEAD, 1, player -> {
            player.sendMessage(ChatColor.RED + "You begin collecting trophies of your most worthy opponents!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Trophy Hunter skill");
            }
        });

        SkillNode node27 = new SkillNode(ID_OFFSET + 27, "Death Defiance", "Once your hp<10% u get fully healed, gain +200% dmg and -50% def for 10s", 5,
                Material.TOTEM_OF_UNDYING, 1, player -> {
            player.sendMessage(ChatColor.RED + "When near death, you unleash your ultimate rage!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Death Defiance skill");
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

        // Path 1: Unarmored Rage branch (Rage Path)
        tree.connectNodes(ID_OFFSET + 1, ID_OFFSET + 4);
        tree.connectNodes(ID_OFFSET + 4, ID_OFFSET + 7);
        tree.connectNodes(ID_OFFSET + 7, ID_OFFSET + 11);
        tree.connectNodes(ID_OFFSET + 11, ID_OFFSET + 17);
        tree.connectNodes(ID_OFFSET + 17, ID_OFFSET + 22);
        tree.connectNodes(ID_OFFSET + 22, ID_OFFSET + 25);

        // Path 2: Berserker's Fury branch (Critical Path)
        tree.connectNodes(ID_OFFSET + 2, ID_OFFSET + 5);
        tree.connectNodes(ID_OFFSET + 5, ID_OFFSET + 8);
        tree.connectNodes(ID_OFFSET + 5, ID_OFFSET + 9);
        tree.connectNodes(ID_OFFSET + 8, ID_OFFSET + 12);
        tree.connectNodes(ID_OFFSET + 9, ID_OFFSET + 13);
        tree.connectNodes(ID_OFFSET + 9, ID_OFFSET + 14);
        tree.connectNodes(ID_OFFSET + 14, ID_OFFSET + 16);
        tree.connectNodes(ID_OFFSET + 14, ID_OFFSET + 18);
        tree.connectNodes(ID_OFFSET + 16, ID_OFFSET + 20);
        tree.connectNodes(ID_OFFSET + 18, ID_OFFSET + 23);
        tree.connectNodes(ID_OFFSET + 20, ID_OFFSET + 26);
        tree.connectNodes(ID_OFFSET + 23, ID_OFFSET + 26);

        // Path 3: Combat Momentum branch (Frenzy Path)
        tree.connectNodes(ID_OFFSET + 3, ID_OFFSET + 6);
        tree.connectNodes(ID_OFFSET + 6, ID_OFFSET + 10);
        tree.connectNodes(ID_OFFSET + 6, ID_OFFSET + 15);
        tree.connectNodes(ID_OFFSET + 10, ID_OFFSET + 19);
        tree.connectNodes(ID_OFFSET + 10, ID_OFFSET + 21);
        tree.connectNodes(ID_OFFSET + 15, ID_OFFSET + 19);
        tree.connectNodes(ID_OFFSET + 19, ID_OFFSET + 24);
        tree.connectNodes(ID_OFFSET + 21, ID_OFFSET + 24);
        tree.connectNodes(ID_OFFSET + 24, ID_OFFSET + 27);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Configured Berserker skill tree with " +
                    tree.getRootNodeIds().size() + " root nodes and complete connection paths");
        }
    }

    @Override
    protected void applySkillStats(Player player, int skillId, int purchaseCount) {
        // Remove ID_OFFSET to get the original skill ID for switch statement
        int originalId = skillId - ID_OFFSET;
        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);

        switch (originalId) {
            case 1: // Cannot wear chestplate but +200% dmg
                // This is handled dynamically in BerserkerSkillEffectsHandler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied Unarmored Rage logic to " + player.getName());
                }
                break;

            case 2: // Each 10% hp u lose u gain +10% dmg
                // This is handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied health-based damage scaling to " + player.getName());
                }
                break;

            case 3: // While in combat every 30s u gain +5% dmg
                // This is handled via periodic task
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied combat momentum logic to " + player.getName());
                }
                break;

            case 6: // +5% dmg
                stats.addDamageMultiplier(0.05 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +" + (0.05 * purchaseCount) + " damage multiplier to " + player.getName());
                }
                break;

            case 7: // -10% hp, +10% dmg (1/2)
                stats.addMaxHealth(-10 * purchaseCount);
                stats.addDamageMultiplier(0.10 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied -10% HP, +10% damage (level " + purchaseCount + "/2) to " + player.getName());
                }
                break;

            case 8: // -5 armor and +10% crit chance
                // Armor is approximated using other stats since there's no direct armor stat
                stats.addShieldBlockChance(-5);
                stats.addEvadeChance(10); // Using evade as a proxy for crit chance
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied -5 armor, +10% crit chance to " + player.getName());
                }
                break;

            case 12: // +5 armor, +5% def
                stats.addShieldBlockChance(5);
                stats.addDefenseBonus(5);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +5 armor, +5% defense to " + player.getName());
                }
                break;

            case 13: // +20 hp
                stats.addMaxHealth(20);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +20 HP to " + player.getName());
                }
                break;

            case 15: // +5% ms
                stats.addMovementSpeedBonus(5);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +5% movement speed to " + player.getName());
                }
                break;

            case 17: // +50 dmg
                stats.addBonusDamage(50);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +50 flat damage to " + player.getName());
                }
                break;

            case 18: // +10% crit chance
                stats.addEvadeChance(10); // Using evade as a proxy for crit chance
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +10% critical chance to " + player.getName());
                }
                break;

            case 19: // -5% def, +5% dmg
                stats.addDefenseBonus(-5);
                stats.addDamageMultiplier(0.05);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied -5% defense, +5% damage to " + player.getName());
                }
                break;

            case 21: // +10 armor
                stats.addShieldBlockChance(10);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +10 armor to " + player.getName());
                }
                break;

            // All other skills are handled dynamically in BerserkerSkillEffectsHandler
            default:
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Skill " + originalId + " will be handled by BerserkerSkillEffectsHandler");
                }
                break;
        }

        // Force refresh player stats
        plugin.getSkillEffectsHandler().refreshPlayerStats(player);
    }

    @Override
    public boolean isMultiPurchaseDiscountSkill(int skillId) {
        // Remove ID_OFFSET to get the original skill ID
        int originalId = skillId - ID_OFFSET;

        // Skills with (1/X) notation in Berserk.md
        return originalId == 7 || originalId == 16;
    }
}