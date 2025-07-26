package com.maks.myexperienceplugin.Class.skills.classes.ranger;

import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.base.BaseSkillManager;
import com.maks.myexperienceplugin.Class.skills.base.SkillNode;
import com.maks.myexperienceplugin.Class.skills.base.SkillTree;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class RangerSkillManager extends BaseSkillManager {

    public RangerSkillManager(MyExperiencePlugin plugin) {
        super(plugin, "Ranger");
    }

    @Override
    protected void initializeSkills() {
        // Create nodes based on Ranger.md - FULLY IMPLEMENTED
        SkillNode node1 = new SkillNode(1, "Swift Movement", "+1% movement speed", 1,
                Material.LEATHER_BOOTS, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You feel swifter!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Swift Movement skill");
            }
        });

        SkillNode node2 = new SkillNode(2, "Nature's Recovery", "Gain Regeneration I", 1,
                Material.GOLDEN_APPLE, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You feel the healing power of nature!");
            // In actual implementation, would apply regeneration effect
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Nature's Recovery skill");
            }
        });

        SkillNode node3 = new SkillNode(3, "Hunter's Precision", "+5 damage", 1,
                Material.ARROW, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your attacks become more precise!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Hunter's Precision skill");
            }
        });

        SkillNode node4 = new SkillNode(4, "Evasion Training", "+2% evade chance (EVADE = 100% damage avoided)", 2,
                Material.FEATHER, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You become more evasive!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Evasion Training skill");
            }
        });

        SkillNode node5 = new SkillNode(5, "Vitality", "+1 HP", 2,
                Material.APPLE, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your vitality increases!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Vitality skill");
            }
        });

        SkillNode node6 = new SkillNode(6, "Trophy Hunter", "+3$ per killed mob", 2,
                Material.GOLD_NUGGET, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You'll earn more from your hunts!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Trophy Hunter skill");
            }
        });

// W RangerSkillManager.initializeSkills()
        SkillNode node7 = new SkillNode(7, "Wind Stacks", "After killing a mob gain wind stacks, each +1% ms and evade (max 3 stacks)", 3,
                Material.PHANTOM_MEMBRANE, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You harness the power of the wind!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Wind Stacks skill");
            }
        });

        SkillNode node8 = new SkillNode(8, "Enhanced Evasion", "+1% evade chance (EVADE = 100% damage avoided)", 2,
                Material.WHITE_WOOL, 2, player -> {
            player.sendMessage(ChatColor.GREEN + "Your evasive movements improve!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Enhanced Evasion skill");
            }
        });

        SkillNode node9 = new SkillNode(9, "Hunter's Luck", "+1% luck", 2,
                Material.RABBIT_FOOT, 2, player -> {
            player.sendMessage(ChatColor.GREEN + "Your hunter's instinct sharpens!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Hunter's Luck skill");
            }
        });

        SkillNode node10 = new SkillNode(10, "Triple Strike", "Each 3 hits deals +10 dmg", 2,
                Material.IRON_SWORD, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your attack pattern becomes more deadly!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Triple Strike skill");
            }
        });

        SkillNode node11 = new SkillNode(11, "Empowered Shots", "+1% damage", 3,
                Material.SPECTRAL_ARROW, 3, player -> {
            player.sendMessage(ChatColor.GREEN + "Your attacks grow stronger!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Empowered Shots skill");
            }
        });

        SkillNode node12 = new SkillNode(12, "Wind Mastery", "+2 max stacks of wind", 2,
                Material.FEATHER, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You master the power of the wind!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Wind Mastery skill");
            }
        });

        SkillNode node13 = new SkillNode(13, "Natural Defense", "+1% defense", 2,
                Material.OAK_LEAVES, 2, player -> {
            player.sendMessage(ChatColor.GREEN + "Nature protects you!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Natural Defense skill");
            }
        });

        SkillNode node14 = new SkillNode(14, "Shadow Step", "+4% evade chance(EVADE = 100% damage avoided), -2% dmg ", 3,
                Material.BLACK_WOOL, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You learn to move like a shadow!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Shadow Step skill");
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
    }

    @Override
    protected void setupTreeStructure(SkillTree tree) {
        // Definiujemy węzły startowe
        tree.addRootNode(1);
        tree.addRootNode(2);
        tree.addRootNode(3);

        // Łączymy węzły zgodnie z diagramem Drzewko Klas.md
        tree.connectNodes(1, 4);
        tree.connectNodes(4, 7);
        tree.connectNodes(4, 8);
        tree.connectNodes(7, 12);

        tree.connectNodes(2, 5);
        tree.connectNodes(5, 9);
        tree.connectNodes(9, 13);
        tree.connectNodes(13, 14);  // FIXED: Connect 13 to 14 as per diagram

        tree.connectNodes(3, 6);
        tree.connectNodes(6, 10);
        tree.connectNodes(6, 11);
        // tree.connectNodes(11, 14);  // REMOVED: This incorrect connection

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Skonfigurowano strukturę drzewka umiejętności dla klasy " + className);
        }
    }

    @Override
    protected void applySkillStats(Player player, int skillId, int purchaseCount) {
        // Get the player's current stats
        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);

        switch (skillId) {
            case 1: // +1% movement speed
                stats.addMovementSpeedBonus(1 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +" + (1 * purchaseCount) + "% movement speed to " + player.getName());
                }
                break;
            case 3: // +5 damage
                stats.addBonusDamage(5);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +5 damage to " + player.getName());
                }
                break;
            case 4: // +2% evade chance
                stats.addEvadeChance(2 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +" + (2 * purchaseCount) + "% evade chance to " + player.getName());
                }
                break;
            case 5: // +1 HP
                stats.addMaxHealth(1 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +" + (1 * purchaseCount) + " HP to " + player.getName());
                }
                break;
            case 6: // +3$ per killed mob
                stats.addGoldPerKill(3);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +3$ per kill to " + player.getName());
                }
                break;
            case 8: // +1% evade chance (1/2)
                stats.addEvadeChance(1 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +" + (1 * purchaseCount) + "% evade chance to " + player.getName() +
                            " (purchase " + purchaseCount + "/2)");
                }
                break;
            case 9: // +1% luck (1/2)
                stats.addLuckBonus(1 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +" + (1 * purchaseCount) + "% luck to " + player.getName() +
                            " (purchase " + purchaseCount + "/2)");
                }
                break;
            case 11: // +1% dmg (1/3)
                stats.addDamageMultiplier(0.01 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +" + (0.01 * purchaseCount) + " damage multiplier to " + player.getName() +
                            " (purchase " + purchaseCount + "/3)");
                }
                break;
            // W metodzie applySkillStats w RangerSkillManager
            case 12: // Wind Mastery: +2 max stacks of wind
                // Zwiększ maksymalną liczbę stacków
                stats.setMaxWindStacks(5); // Bazowe 3 + 2 z umiejętności Wind Mastery
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied Wind Mastery: +2 max stacks of wind to " + player.getName() +
                            ". Max stacks: " + stats.getMaxWindStacks());
                }
                break;
            case 13: // +1% def (1/2)
                stats.addDefenseBonus(1 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +" + (1 * purchaseCount) + "% defense to " + player.getName() +
                            " (purchase " + purchaseCount + "/2)");
                }
                break;
            case 14: // +4% evade chance, -2% dmg
                stats.addEvadeChance(4 * purchaseCount);
                stats.multiplyDamageMultiplier(1.0 - (0.02 * purchaseCount));
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +4% evade chance and -2% damage to " + player.getName());
                }
                break;
        }

        // Force a refresh of player stats to apply changes
        plugin.getSkillEffectsHandler().refreshPlayerStats(player);
    }

    @Override
    public boolean isMultiPurchaseDiscountSkill(int skillId) {
        // List of Ranger skills with (1/X) notation that have special cost structure
        return skillId == 8 || skillId == 9 || skillId == 11 || skillId == 13;
    }
}
