package com.maks.myexperienceplugin.Class.skills.classes.dragonknight.ascendancy;

import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.base.BaseSkillManager;
import com.maks.myexperienceplugin.Class.skills.base.SkillNode;
import com.maks.myexperienceplugin.Class.skills.base.SkillTree;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ScaleGuardianSkillManager extends BaseSkillManager {

    // Use ID range starting from 400000 to avoid conflicts with base classes, Beastmaster, Berserker, and FlameWarden
    private static final int ID_OFFSET = 400000;

    public ScaleGuardianSkillManager(MyExperiencePlugin plugin) {
        super(plugin, "ScaleGuardian");
    }

    @Override
    protected void initializeSkills() {
        // Create nodes based on Specific_plugin_description.md
        SkillNode node1 = new SkillNode(ID_OFFSET + 1, "Shield Block Chance", "+5% shield block chance", 1,
                Material.SHIELD, 2, player -> {
            player.sendMessage(ChatColor.AQUA + "Your shield blocking improves!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Shield Block Chance skill");
            }
        });

        SkillNode node2 = new SkillNode(ID_OFFSET + 2, "Damage Reflection", "+10% damage reflection when blocking", 1,
                Material.IRON_BLOCK, 2, player -> {
            player.sendMessage(ChatColor.AQUA + "Your shield now reflects damage!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Damage Reflection skill");
            }
        });

        SkillNode node3 = new SkillNode(ID_OFFSET + 3, "Proximity Defense", "+3% defense for each nearby enemy (max +15%)", 1,
                Material.CHAIN_COMMAND_BLOCK, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "You gain strength from being surrounded!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Proximity Defense skill");
            }
        });

        SkillNode node4 = new SkillNode(ID_OFFSET + 4, "Block Healing", "Successful blocks restore 5% of maximum health", 2,
                Material.GOLDEN_APPLE, 2, player -> {
            player.sendMessage(ChatColor.AQUA + "Your blocks now restore health!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Block Healing skill");
            }
        });

        SkillNode node5 = new SkillNode(ID_OFFSET + 5, "Knockback Resistance", "+20% resistance to knockback effects", 2,
                Material.ANVIL, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "You stand your ground more firmly!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Knockback Resistance skill");
            }
        });

        SkillNode node6 = new SkillNode(ID_OFFSET + 6, "Low Health Defense", "When hp<50%, your defense is increased by 15%", 2,
                Material.IRON_CHESTPLATE, 2, player -> {
            player.sendMessage(ChatColor.AQUA + "Your defenses strengthen when wounded!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Low Health Defense skill");
            }
        });

        SkillNode node7 = new SkillNode(ID_OFFSET + 7, "Taunt", "Being hit by a melee attack has 25% chance to taunt the attacker (they focus on you for 5 seconds)", 2,
                Material.DRAGON_HEAD, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "You can now taunt enemies to focus on you!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Taunt skill");
            }
        });

        SkillNode node8 = new SkillNode(ID_OFFSET + 8, "Stationary Defense", "+10% defense when not moving for 2 seconds", 2,
                Material.OBSIDIAN, 2, player -> {
            player.sendMessage(ChatColor.AQUA + "Standing your ground increases your defense!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Stationary Defense skill");
            }
        });

        SkillNode node9 = new SkillNode(ID_OFFSET + 9, "Weakening Block", "Blocking an attack reduces damage from that enemy by 5% for 3 seconds (stacks up to 4 times)", 2,
                Material.COBWEB, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "Your blocks now weaken enemies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Weakening Block skill");
            }
        });

        SkillNode node10 = new SkillNode(ID_OFFSET + 10, "Last Stand", "When below 30% hp, gain +25% shield block chance for 8 seconds (30 second cooldown)", 3,
                Material.TOTEM_OF_UNDYING, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "Your shield becomes more effective when near death!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Last Stand skill");
            }
        });

        SkillNode node11 = new SkillNode(ID_OFFSET + 11, "Weakening Strike", "Your attacks have 15% chance to reduce enemy's damage by 10% for 5 seconds", 3,
                Material.IRON_SWORD, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "Your attacks now weaken enemies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Weakening Strike skill");
            }
        });

        SkillNode node12 = new SkillNode(ID_OFFSET + 12, "Heavy Armor Mastery", "+3 armor for each piece of heavy armor worn", 3,
                Material.NETHERITE_CHESTPLATE, 2, player -> {
            player.sendMessage(ChatColor.AQUA + "Your heavy armor becomes more effective!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Heavy Armor Mastery skill");
            }
        });

        SkillNode node13 = new SkillNode(ID_OFFSET + 13, "Critical Block", "Blocking a critical hit reflects 40% of the damage back to attacker", 3,
                Material.DIAMOND_BLOCK, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "You can now reflect critical hits!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Critical Block skill");
            }
        });

        SkillNode node14 = new SkillNode(ID_OFFSET + 14, "Protective Aura", "Nearby allies (within 8 blocks) gain +10% defense", 3,
                Material.BEACON, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "Your presence now protects allies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Protective Aura skill");
            }
        });

        SkillNode node15 = new SkillNode(ID_OFFSET + 15, "Reactive Defense", "Taking more than 20% of your max health in a single hit grants +30% defense for 5 seconds", 3,
                Material.NETHERITE_BLOCK, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "You react defensively to heavy blows!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Reactive Defense skill");
            }
        });

        SkillNode node16 = new SkillNode(ID_OFFSET + 16, "Surrounded Healing", "When surrounded by 3+ enemies, gain +5% healing from all sources per enemy", 3,
                Material.GOLDEN_CARROT, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "Being surrounded increases your healing!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Surrounded Healing skill");
            }
        });

        SkillNode node17 = new SkillNode(ID_OFFSET + 17, "Counter Attack", "After blocking, your next attack deals +30% damage", 3,
                Material.DIAMOND_SWORD, 2, player -> {
            player.sendMessage(ChatColor.AQUA + "Your blocks now empower your next attack!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Counter Attack skill");
            }
        });

        SkillNode node18 = new SkillNode(ID_OFFSET + 18, "Block Momentum", "Every 3 successful blocks increases your damage by 15% for 5 seconds (does not stack)", 3,
                Material.SHIELD, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "Successful blocks now build offensive momentum!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Block Momentum skill");
            }
        });

        SkillNode node19 = new SkillNode(ID_OFFSET + 19, "Guardian Angel", "When an ally within 10 blocks falls below 30% hp, gain +30% movement speed and heal them for 50% hp (30s cooldown)", 3,
                Material.FEATHER, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "You can now rush to save wounded allies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Guardian Angel skill");
            }
        });

        SkillNode node20 = new SkillNode(ID_OFFSET + 20, "Potion Master", "+20% duration of all positive potion effects", 3,
                Material.BREWING_STAND, 2, player -> {
            player.sendMessage(ChatColor.AQUA + "Your potions last longer!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Potion Master skill");
            }
        });

        SkillNode node21 = new SkillNode(ID_OFFSET + 21, "Slowing Defense", "Enemies attacking you have a 10% chance to be slowed by 20% for 3 seconds", 3,
                Material.SOUL_SAND, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "Your defenses now slow attackers!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Slowing Defense skill");
            }
        });

        SkillNode node22 = new SkillNode(ID_OFFSET + 22, "Healthy Protection", "While above 80% hp, nearby allies take 15% reduced damage", 3,
                Material.GOLDEN_APPLE, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "Your vitality now protects allies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Healthy Protection skill");
            }
        });

        SkillNode node23 = new SkillNode(ID_OFFSET + 23, "Critical Immunity", "After taking damage exceeding 15% of your max health, gain immunity to critical hits for 5 seconds", 3,
                Material.TURTLE_HELMET, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "Heavy blows now grant you critical hit immunity!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Critical Immunity skill");
            }
        });

        SkillNode node24 = new SkillNode(ID_OFFSET + 24, "Shield Bash", "Shield blocks have 25% chance to stun the attacker for 1 second (10 second cooldown)", 3,
                Material.SHIELD, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "Your shield blocks can now stun enemies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Shield Bash skill");
            }
        });

        SkillNode node25 = new SkillNode(ID_OFFSET + 25, "Desperate Defense", "Damage taken is reduced by 1% for each 1% of hp you're missing (max 30%)", 5,
                Material.NETHERITE_CHESTPLATE, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "Your defenses strengthen as your health decreases!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Desperate Defense skill");
            }
        });

        SkillNode node26 = new SkillNode(ID_OFFSET + 26, "Ally Protection", "Allies within 12 blocks gain +10% shield block chance and +5% damage", 5,
                Material.BEACON, 2, player -> {
            player.sendMessage(ChatColor.AQUA + "Your presence now empowers allies' shields and attacks!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Ally Protection skill");
            }
        });

        SkillNode node27 = new SkillNode(ID_OFFSET + 27, "Last Resort", "When hp<10%, gain 70% damage reduction for 5 seconds (3 minute cooldown)", 5,
                Material.TOTEM_OF_UNDYING, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "Near death, you gain incredible damage reduction!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Last Resort skill");
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
            plugin.getLogger().info("Initialized all 27 Scale Guardian skills with ID offset " + ID_OFFSET);
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
        tree.connectNodes(ID_OFFSET + 14, ID_OFFSET + 20); // NIE 14->19!
        tree.connectNodes(ID_OFFSET + 15, ID_OFFSET + 20); // Też prowadzi do 20!
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
            plugin.getLogger().info("Configured Scale Guardian skill tree with UNIVERSAL structure from Drzewko Podklas.md");
            plugin.getLogger().info("Structure: 3 paths merging at nodes 20, 22, and 24");

            // Log all connections to verify
            for (int i = 1; i <= 27; i++) {
                logNodeConnections(tree, ID_OFFSET + i);
            }
        }
    }

    @Override
    protected void applySkillStats(Player player, int skillId, int purchaseCount) {
        // Remove ID_OFFSET to get the original skill ID for switch statement
        int originalId = skillId - ID_OFFSET;
        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);

        switch (originalId) {
            case 1: // +5% shield block chance (1/2)
                stats.addShieldBlockChance(5 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +" + (5 * purchaseCount) + "% shield block chance to " + player.getName());
                }
                break;

            case 2: // +10% damage reflection when blocking (1/2)
                // This is handled dynamically in ScaleGuardianSkillEffectsHandler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied damage reflection logic to " + player.getName());
                }
                break;

            case 3: // +3% defense for each nearby enemy (max +15%)
                // This is handled dynamically in ScaleGuardianSkillEffectsHandler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied proximity defense logic to " + player.getName());
                }
                break;

            case 5: // +20% resistance to knockback effects
                // This is handled dynamically in ScaleGuardianSkillEffectsHandler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied knockback resistance to " + player.getName());
                }
                break;

            case 6: // When hp<50%, your defense is increased by 15% (1/2)
                // This is handled dynamically in ScaleGuardianSkillEffectsHandler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied low health defense logic to " + player.getName());
                }
                break;

            case 8: // +10% defense when not moving for 2 seconds (1/2)
                // This is handled dynamically in ScaleGuardianSkillEffectsHandler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied stationary defense logic to " + player.getName());
                }
                break;

            case 12: // +3 armor for each piece of heavy armor worn (1/2)
                // This is handled dynamically in ScaleGuardianSkillEffectsHandler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied heavy armor mastery logic to " + player.getName());
                }
                break;

            case 14: // Nearby allies (within 8 blocks) gain +10% defense
                // This is handled dynamically in ScaleGuardianSkillEffectsHandler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied protective aura logic to " + player.getName());
                }
                break;

            case 20: // +20% duration of all positive potion effects (1/2)
                // This is handled dynamically in ScaleGuardianSkillEffectsHandler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied potion master logic to " + player.getName());
                }
                break;

            case 26: // Allies within 12 blocks gain +10% shield block chance and +5% damage (1/2)
                // This is handled dynamically in ScaleGuardianSkillEffectsHandler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied ally protection logic to " + player.getName());
                }
                break;

            // All other skills are handled dynamically in ScaleGuardianSkillEffectsHandler
            default:
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Skill " + originalId + " will be handled by ScaleGuardianSkillEffectsHandler");
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

        // Skills with (1/X) notation in Specific_plugin_description.md
        return originalId == 1 || originalId == 2 || originalId == 4 || originalId == 6 || 
               originalId == 8 || originalId == 12 || originalId == 17 || originalId == 20 || 
               originalId == 26;
    }

    /**
     * Log detailed information about a node's connections
     */
    private void logNodeConnections(SkillTree tree, int nodeId) {
        SkillNode node = tree.getNode(nodeId);
        if (node == null) {
            plugin.getLogger().info("[SCALE GUARDIAN DEBUG] Node " + nodeId + " not found in tree!");
            return;
        }

        plugin.getLogger().info("[SCALE GUARDIAN DEBUG] Node " + nodeId + " (" + node.getName() + ") connections:");

        // Log outgoing connections
        plugin.getLogger().info("[SCALE GUARDIAN DEBUG] - Outgoing connections: " + node.getConnectedNodes().size());
        for (SkillNode connected : node.getConnectedNodes()) {
            plugin.getLogger().info("[SCALE GUARDIAN DEBUG]   -> " + connected.getId() + " (" + connected.getName() + ")");
        }

        // Log incoming connections
        plugin.getLogger().info("[SCALE GUARDIAN DEBUG] - Checking incoming connections...");
        boolean hasIncoming = false;
        for (SkillNode otherNode : tree.getAllNodes()) {
            for (SkillNode connectedNode : otherNode.getConnectedNodes()) {
                if (connectedNode.getId() == nodeId) {
                    plugin.getLogger().info("[SCALE GUARDIAN DEBUG]   <- " + otherNode.getId() + " (" + otherNode.getName() + ")");
                    hasIncoming = true;
                }
            }
        }

        if (!hasIncoming) {
            plugin.getLogger().info("[SCALE GUARDIAN DEBUG]   No incoming connections found!");
        }
    }
}
