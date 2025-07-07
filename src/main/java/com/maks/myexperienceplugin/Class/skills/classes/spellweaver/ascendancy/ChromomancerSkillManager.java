package com.maks.myexperienceplugin.Class.skills.classes.spellweaver.ascendancy;

import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.base.BaseSkillManager;
import com.maks.myexperienceplugin.Class.skills.base.SkillNode;
import com.maks.myexperienceplugin.Class.skills.base.SkillTree;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ChromomancerSkillManager extends BaseSkillManager {

    // Use ID range starting from 800000 to avoid conflicts with base class skills
    private static final int ID_OFFSET = 800000;

    public ChromomancerSkillManager(MyExperiencePlugin plugin) {
        super(plugin, "Chronomancer");
    }

    @Override
    protected void initializeSkills() {
        // Create nodes for Chronomancer - Time Manipulation Path, Haste Path, Temporal Control Path

        // Time Manipulation Path
        SkillNode node1 = new SkillNode(ID_OFFSET + 1, "Time Mastery", "Increase time manipulation spell damage by 5% per level.", 1,
                Material.CLOCK, 3, player -> {
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Time Mastery skill");
            }
        });

        SkillNode node4 = new SkillNode(ID_OFFSET + 4, "Time Slow", "Your spells have 10% chance to slow enemies by 20% for 3s.", 2,
                Material.SOUL_SAND, 1, player -> {
            player.sendMessage(ChatColor.LIGHT_PURPLE + "Your spells can now slow enemies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Time Slow skill");
            }
        });

        SkillNode node7 = new SkillNode(ID_OFFSET + 7, "Temporal Shift", "When hit, 15% chance to teleport 5 blocks away and gain immunity for 1s.", 2,
                Material.ENDER_PEARL, 1, player -> {
            player.sendMessage(ChatColor.LIGHT_PURPLE + "You can now shift through time when hit!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Temporal Shift skill");
            }
        });

        SkillNode node11 = new SkillNode(ID_OFFSET + 11, "Time Bubble", "Create a bubble that slows all enemies inside by 30% for 5s.", 3,
                Material.LIGHT_BLUE_STAINED_GLASS, 1, player -> {
            player.sendMessage(ChatColor.LIGHT_PURPLE + "You can now create time bubbles!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Time Bubble skill");
            }
        });

        SkillNode node12 = new SkillNode(ID_OFFSET + 12, "Rewind", "When below 20% health, 25% chance to restore 15% health and gain immunity for 2s.", 2,
                Material.COMPASS, 1, player -> {
            player.sendMessage(ChatColor.LIGHT_PURPLE + "You can now rewind your injuries!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Rewind skill");
            }
        });

        SkillNode node17 = new SkillNode(ID_OFFSET + 17, "Time Stop", "Freeze all enemies in a 5-block radius for 3s. 60s cooldown.", 3,
                Material.NETHER_STAR, 1, player -> {
            player.sendMessage(ChatColor.LIGHT_PURPLE + "You can now stop time around you!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Time Stop skill");
            }
        });

        SkillNode node18 = new SkillNode(ID_OFFSET + 18, "Echo", "Your spells have 20% chance to cast a second time at 50% effectiveness.", 3,
                Material.ECHO_SHARD, 1, player -> {
            player.sendMessage(ChatColor.LIGHT_PURPLE + "Your spells can now echo through time!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Echo skill");
            }
        });

        SkillNode node22 = new SkillNode(ID_OFFSET + 22, "Temporal Mastery", "Reduce all cooldowns by 15%.", 3,
                Material.CLOCK, 1, player -> {
            player.sendMessage(ChatColor.LIGHT_PURPLE + "You have mastered temporal manipulation!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Temporal Mastery skill");
            }
        });

        SkillNode node25 = new SkillNode(ID_OFFSET + 25, "Chronobreak", "Unlock the Chronobreak spell, freezing time in a large area for 5s.", 5,
                Material.END_CRYSTAL, 1, player -> {
            player.sendMessage(ChatColor.LIGHT_PURPLE + "You have unlocked the Chronobreak spell!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Chronobreak skill");
            }
        });

        // Haste Path
        SkillNode node2 = new SkillNode(ID_OFFSET + 2, "Haste Mastery", "Increase movement and attack speed by 3% per level.", 1,
                Material.SUGAR, 3, player -> {
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Haste Mastery skill");
            }
        });

        SkillNode node5 = new SkillNode(ID_OFFSET + 5, "Swift Casting", "Reduce spell casting time by 10%.", 2,
                Material.FEATHER, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "Your spells cast faster!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Swift Casting skill");
            }
        });

        SkillNode node8 = new SkillNode(ID_OFFSET + 8, "Accelerate", "Gain 20% movement speed for 3s after casting a spell.", 2,
                Material.RABBIT_FOOT, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "Casting spells now accelerates you!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Accelerate skill");
            }
        });

        SkillNode node9 = new SkillNode(ID_OFFSET + 9, "Time Flux", "Your spells deal 15% more damage to slowed enemies.", 2,
                Material.PRISMARINE_CRYSTALS, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "Your spells now deal more damage to slowed enemies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Time Flux skill");
            }
        });

        SkillNode node13 = new SkillNode(ID_OFFSET + 13, "Quickstep", "Teleport 10 blocks forward. 15s cooldown.", 3,
                Material.ENDER_EYE, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "You can now quickstep through space!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Quickstep skill");
            }
        });

        SkillNode node14 = new SkillNode(ID_OFFSET + 14, "Time Dilation", "Increase attack and casting speed by 25% for 5s after killing an enemy.", 3,
                Material.GLOWSTONE_DUST, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "Killing enemies now dilates time for you!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Time Dilation skill");
            }
        });

        SkillNode node19 = new SkillNode(ID_OFFSET + 19, "Temporal Rush", "Dash forward, dealing damage to all enemies in your path. 20s cooldown.", 3,
                Material.BLAZE_ROD, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "You can now rush through time!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Temporal Rush skill");
            }
        });

        SkillNode node23 = new SkillNode(ID_OFFSET + 23, "Hastened Mind", "Reduce mana costs by 20% and increase spell damage by 10%.", 3,
                Material.DIAMOND, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "Your mind now works faster than time itself!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Hastened Mind skill");
            }
        });

        SkillNode node26 = new SkillNode(ID_OFFSET + 26, "Time Warp", "Unlock the Time Warp spell, greatly increasing your speed and attack rate for 8s.", 5,
                Material.BEACON, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "You have unlocked the Time Warp spell!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Time Warp skill");
            }
        });

        // Temporal Control Path
        SkillNode node3 = new SkillNode(ID_OFFSET + 3, "Temporal Control", "Increase duration of your time effects by 5% per level.", 1,
                Material.DAYLIGHT_DETECTOR, 3, player -> {
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Temporal Control skill");
            }
        });

        SkillNode node6 = new SkillNode(ID_OFFSET + 6, "Precognition", "15% chance to dodge attacks and spells.", 2,
                Material.ENDER_EYE, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "You can now see attacks before they happen!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Precognition skill");
            }
        });

        SkillNode node10 = new SkillNode(ID_OFFSET + 10, "Time Loop", "When you would die, 20% chance to restore 30% health instead. 120s cooldown.", 2,
                Material.TOTEM_OF_UNDYING, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "You can now loop back in time to avoid death!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Time Loop skill");
            }
        });

        SkillNode node15 = new SkillNode(ID_OFFSET + 15, "Temporal Anchor", "Place an anchor that you can teleport back to within 10s. 30s cooldown.", 3,
                Material.LODESTONE, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "You can now place temporal anchors!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Temporal Anchor skill");
            }
        });

        SkillNode node16 = new SkillNode(ID_OFFSET + 16, "Paradox", "Your spells have 10% chance to hit the target twice.", 2,
                Material.AMETHYST_SHARD, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "Your spells can now create paradoxes!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Paradox skill");
            }
        });

        SkillNode node20 = new SkillNode(ID_OFFSET + 20, "Temporal Chains", "Your spells apply chains that slow enemies by 5% per stack, max 5 stacks.", 3,
                Material.CHAIN, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "Your spells now apply temporal chains!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Temporal Chains skill");
            }
        });

        SkillNode node21 = new SkillNode(ID_OFFSET + 21, "Fate Sealing", "Enemies affected by your time effects take 25% more damage from all sources.", 3,
                Material.BOOK, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "You can now seal the fate of your enemies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Fate Sealing skill");
            }
        });

        SkillNode node24 = new SkillNode(ID_OFFSET + 24, "Time Fracture", "Your critical hits create fractures in time, dealing 20% of the damage again after 2s.", 3,
                Material.AMETHYST_CLUSTER, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "Your critical hits now fracture time itself!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Time Fracture skill");
            }
        });

        SkillNode node27 = new SkillNode(ID_OFFSET + 27, "Temporal Singularity", "Unlock the Temporal Singularity spell, creating a vortex that pulls and damages enemies.", 5,
                Material.DRAGON_EGG, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "You have unlocked the Temporal Singularity spell!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Temporal Singularity skill");
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
            plugin.getLogger().info("Initialized all 27 Chronomancer skills with ID offset " + ID_OFFSET);
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
            plugin.getLogger().info("Configured Chronomancer skill tree with UNIVERSAL structure from Drzewko Podklas.md");
            plugin.getLogger().info("Structure: 3 paths merging at nodes 20, 22, and 24");

            // Log all connections to verify
            for (int i = 1; i <= 27; i++) {
                logNodeConnections(tree, ID_OFFSET + i);
            }
        }
    }

    @Override
    protected void applySkillStats(Player player, int skillId, int purchaseCount) {
        // Get player stats
        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);

        // Apply stats based on skill ID
        int originalId = skillId - ID_OFFSET;

        switch (originalId) {
            case 1: // Time Mastery
                stats.addSpellDamageBonus(5 * purchaseCount);
                break;
            case 2: // Haste Mastery
                stats.addMovementSpeedBonus(3 * purchaseCount);
                break;
            case 3: // Temporal Control
                // Effect handled by handler - increases duration of time effects
                break;
            case 5: // Swift Casting
                // Handled by effects handler - reduces casting time
                break;
            case 6: // Precognition
                stats.addEvadeChance(15);
                break;
            case 9: // Time Flux
                // Handled by effects handler - more damage to slowed enemies
                break;
            case 22: // Temporal Mastery
                // Handled by effects handler - cooldown reduction
                break;
            case 23: // Hastened Mind
                stats.addSpellDamageBonus(10);
                // Mana cost reduction handled by effects handler
                break;
            default:
                // Other skills are handled by the effects handler
                break;
        }

        // Force refresh player stats to ensure any relevant effects are applied
        plugin.getSkillEffectsHandler().refreshPlayerStats(player);
    }

    @Override
    public boolean isMultiPurchaseDiscountSkill(int skillId) {
        int originalId = skillId - ID_OFFSET;

        // Only mastery skills can be purchased multiple times
        return originalId == 1 || originalId == 2 || originalId == 3;
    }

    /**
     * Log detailed information about a node's connections
     */
    private void logNodeConnections(SkillTree tree, int nodeId) {
        SkillNode node = tree.getNode(nodeId);
        if (node == null) {
            plugin.getLogger().info("[CHRONOMANCER DEBUG] Node " + nodeId + " not found in tree!");
            return;
        }

        plugin.getLogger().info("[CHRONOMANCER DEBUG] Node " + nodeId + " (" + node.getName() + ") connections:");

        // Log outgoing connections
        plugin.getLogger().info("[CHRONOMANCER DEBUG] - Outgoing connections: " + node.getConnectedNodes().size());
        for (SkillNode connected : node.getConnectedNodes()) {
            plugin.getLogger().info("[CHRONOMANCER DEBUG]   -> " + connected.getId() + " (" + connected.getName() + ")");
        }

        // Log incoming connections
        plugin.getLogger().info("[CHRONOMANCER DEBUG] - Checking incoming connections...");
        boolean hasIncoming = false;
        for (SkillNode otherNode : tree.getAllNodes()) {
            for (SkillNode connectedNode : otherNode.getConnectedNodes()) {
                if (connectedNode.getId() == nodeId) {
                    plugin.getLogger().info("[CHRONOMANCER DEBUG]   <- " + otherNode.getId() + " (" + otherNode.getName() + ")");
                    hasIncoming = true;
                }
            }
        }

        if (!hasIncoming) {
            plugin.getLogger().info("[CHRONOMANCER DEBUG]   No incoming connections found!");
        }
    }
}
