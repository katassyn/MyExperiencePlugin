package com.maks.myexperienceplugin.Class.skills.classes.ranger.ascendancy;

import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.base.BaseSkillManager;
import com.maks.myexperienceplugin.Class.skills.base.SkillNode;
import com.maks.myexperienceplugin.Class.skills.base.SkillTree;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class EarthwardenSkillManager extends BaseSkillManager {

    // Use ID range starting from 600000 to avoid conflicts with base class skills, Beastmaster (100000), Berserker (200000), FlameWarden (300000), ScaleGuardian (400000), and Shadowstalker (500000)
    private static final int ID_OFFSET = 600000;

    public EarthwardenSkillManager(MyExperiencePlugin plugin) {
        super(plugin, "Earthwarden");
    }

    @Override
    protected void initializeSkills() {
        // Create nodes based on Specific_plugin_description.md
        SkillNode node1 = new SkillNode(ID_OFFSET + 1, "+3% Defense in Grassy Areas", "Gain +3% defense bonus when in grassy areas (DEFENSE = -X% DMG TAKEN).", 1,
                Material.GRASS_BLOCK, 2, player -> {
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated +3% Defense in Grassy Areas skill");
            }
        });

        SkillNode node2 = new SkillNode(ID_OFFSET + 2, "Kill Heal", "Heal 1 hp after killing an enemy.", 1,
                Material.REDSTONE, 2, player -> {
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Kill Heal skill");
            }
        });

        SkillNode node3 = new SkillNode(ID_OFFSET + 3, "Environmental Resistance", "+5% resistance to environmental damage (fall, fire, lava).", 1,
                Material.NETHERITE_BOOTS, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You feel more resistant to environmental hazards!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Environmental Resistance skill");
            }
        });

        SkillNode node4 = new SkillNode(ID_OFFSET + 4, "HP Per Level", "+2 hp per level.", 2,
                Material.GOLDEN_APPLE, 3, player -> {
            player.sendMessage(ChatColor.GREEN + "Your vitality increases with your experience!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated HP Per Level skill");
            }
        });

        SkillNode node5 = new SkillNode(ID_OFFSET + 5, "Steadfast Defense", "After standing still for 3 seconds, gain +10% defense (DEFENSE = -X% DMG TAKEN).", 2,
                Material.SHIELD, 2, player -> {
            player.sendMessage(ChatColor.GREEN + "You feel more protected when standing still!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Steadfast Defense skill");
            }
        });

        SkillNode node6 = new SkillNode(ID_OFFSET + 6, "Desperate Luck", "+5% luck when below 50% hp.", 2,
                Material.RABBIT_FOOT, 2, player -> {
            player.sendMessage(ChatColor.GREEN + "Your luck increases when wounded!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Desperate Luck skill");
            }
        });

        SkillNode node7 = new SkillNode(ID_OFFSET + 7, "Survival Instinct", "When hp<50%, gain +5% defense for 3 seconds (DEFENSE = -X% DMG TAKEN).", 2,
                Material.IRON_CHESTPLATE, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your survival instincts sharpen when wounded!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Survival Instinct skill");
            }
        });

        SkillNode node8 = new SkillNode(ID_OFFSET + 8, "Entangling Strike", "Every 10 seconds, your next attack roots the enemy, reducing their movement speed by 30% for 3 seconds.", 2,
                Material.VINE, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your attacks can now root enemies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Entangling Strike skill");
            }
        });

        SkillNode node9 = new SkillNode(ID_OFFSET + 9, "Enhanced Regeneration", "+10% hp regen from all sources.", 2,
                Material.GLISTERING_MELON_SLICE, 2, player -> {
            player.sendMessage(ChatColor.GREEN + "Your healing abilities are enhanced!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Enhanced Regeneration skill");
            }
        });

        SkillNode node10 = new SkillNode(ID_OFFSET + 10, "Reactive Defense", "When taking damage, 15% chance to gain +15% defense for 5 seconds (DEFENSE = -X% DMG TAKEN).", 2,
                Material.CHAINMAIL_CHESTPLATE, 2, player -> {
            player.sendMessage(ChatColor.GREEN + "Your body reacts defensively to attacks!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Reactive Defense skill");
            }
        });

        SkillNode node11 = new SkillNode(ID_OFFSET + 11, "Strength in Numbers", "+1% defense for each nearby ally within 10 blocks (max +5%).", 3,
                Material.PLAYER_HEAD, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You feel stronger with allies nearby!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Strength in Numbers skill");
            }
        });

        SkillNode node12 = new SkillNode(ID_OFFSET + 12, "Counterattack", "After blocking attack, gain +20% movement speed for 3 seconds.", 3,
                Material.SHIELD, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You can now counterattack after blocking!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Counterattack skill");
            }
        });

        SkillNode node13 = new SkillNode(ID_OFFSET + 13, "Vengeance", "+8% damage against enemies that have attacked you.", 3,
                Material.IRON_SWORD, 2, player -> {
            player.sendMessage(ChatColor.GREEN + "Your vengeance grows stronger!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Vengeance skill");
            }
        });

        SkillNode node14 = new SkillNode(ID_OFFSET + 14, "Lifesteal", "Attacks have 10% chance to heal you for 5% of damage dealt.", 3,
                Material.GHAST_TEAR, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your attacks can now steal life!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Lifesteal skill");
            }
        });

        SkillNode node15 = new SkillNode(ID_OFFSET + 15, "Last Stand", "When below 30% hp, gain +20% defense and +10% damage (DEFENSE = -X% DMG TAKEN).", 3,
                Material.TOTEM_OF_UNDYING, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your last stand is formidable!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Last Stand skill");
            }
        });

        SkillNode node16 = new SkillNode(ID_OFFSET + 16, "Divine Protection", "Taking damage has 5% chance to grant immunity to all damage for 2 seconds (30 sec cooldown).", 3,
                Material.BEACON, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You occasionally receive divine protection!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Divine Protection skill");
            }
        });

        SkillNode node17 = new SkillNode(ID_OFFSET + 17, "Surrounded Defense", "+3% defense for each enemy within 8 blocks (max +15%).", 3,
                Material.COBWEB, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You become more defensive when surrounded!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Surrounded Defense skill");
            }
        });

        SkillNode node18 = new SkillNode(ID_OFFSET + 18, "Immovable", "Reduce knockback taken by 50% and gain +20% knockback dealt.", 3,
                Material.ANVIL, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You stand firm against attacks!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Immovable skill");
            }
        });

        SkillNode node19 = new SkillNode(ID_OFFSET + 19, "Desperate Strength", "Gain +5% damage for every 30% of health you're missing.", 3,
                Material.BLAZE_POWDER, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your strength grows as your health diminishes!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Desperate Strength skill");
            }
        });

        SkillNode node20 = new SkillNode(ID_OFFSET + 20, "Healthy Speed", "While above 80% hp, gain +15% attack speed.", 3,
                Material.SUGAR, 2, player -> {
            player.sendMessage(ChatColor.GREEN + "You attack faster when healthy!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Healthy Speed skill");
            }
        });

        SkillNode node21 = new SkillNode(ID_OFFSET + 21, "Protective Instinct", "When an ally within 15 blocks takes damage, gain +10% movement speed and +5% damage for 5 seconds.", 3,
                Material.FEATHER, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You react quickly to protect your allies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Protective Instinct skill");
            }
        });

        SkillNode node22 = new SkillNode(ID_OFFSET + 22, "Healing Strikes", "Attacks have 25% chance to restore 2% of your maximum health.", 3,
                Material.GOLDEN_CARROT, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your strikes can now heal you!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Healing Strikes skill");
            }
        });

        SkillNode node23 = new SkillNode(ID_OFFSET + 23, "Surrounded Strength", "+15% defense when surrounded by 3+ enemies.", 3,
                Material.DIAMOND_CHESTPLATE, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You become much stronger when surrounded!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Surrounded Strength skill");
            }
        });

        SkillNode node24 = new SkillNode(ID_OFFSET + 24, "Ambush", "After not taking damage for 5 seconds, your next attack deals +30% damage.", 3,
                Material.ENDER_PEARL, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You can now ambush your enemies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Ambush skill");
            }
        });

        SkillNode node25 = new SkillNode(ID_OFFSET + 25, "Desperate Escape", "When hp<20%, gain +25% evade chance and +15% movement speed for 5 seconds.", 3,
                Material.PHANTOM_MEMBRANE, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You can escape when near death!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Desperate Escape skill");
            }
        });

        SkillNode node26 = new SkillNode(ID_OFFSET + 26, "Allied Strength", "+2% defense and +1% damage for each nearby ally (max 5 allies).", 3,
                Material.EMERALD, 2, player -> {
            player.sendMessage(ChatColor.GREEN + "Your allies make you stronger!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Allied Strength skill");
            }
        });

        SkillNode node27 = new SkillNode(ID_OFFSET + 27, "Second Chance", "When killed, you survive with 10% hp and gain immunity for 2 seconds (5 min cooldown).", 5,
                Material.TOTEM_OF_UNDYING, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You've gained a second chance at life!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Second Chance skill");
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
            plugin.getLogger().info("Initialized all 27 Earthwarden skills with ID offset " + ID_OFFSET);
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
            plugin.getLogger().info("Configured Earthwarden skill tree with UNIVERSAL structure from Drzewko Podklas.md");
            plugin.getLogger().info("Structure: 3 paths merging at nodes 20, 22, and 24");

            // Log all connections to verify
            for (int i = 1; i <= 27; i++) {
                logNodeConnections(tree, ID_OFFSET + i);
            }
        }
    }

    /**
     * Log detailed information about a node's connections
     */
    private void logNodeConnections(SkillTree tree, int nodeId) {
        SkillNode node = tree.getNode(nodeId);
        if (node == null) {
            plugin.getLogger().info("[EARTHWARDEN DEBUG] Node " + nodeId + " not found in tree!");
            return;
        }

        plugin.getLogger().info("[EARTHWARDEN DEBUG] Node " + nodeId + " (" + node.getName() + ") connections:");

        // Log outgoing connections
        plugin.getLogger().info("[EARTHWARDEN DEBUG] - Outgoing connections: " + node.getConnectedNodes().size());
        for (SkillNode connected : node.getConnectedNodes()) {
            plugin.getLogger().info("[EARTHWARDEN DEBUG]   -> " + connected.getId() + " (" + connected.getName() + ")");
        }

        // Log incoming connections
        plugin.getLogger().info("[EARTHWARDEN DEBUG] - Checking incoming connections...");
        boolean hasIncoming = false;
        for (SkillNode otherNode : tree.getAllNodes()) {
            for (SkillNode connectedNode : otherNode.getConnectedNodes()) {
                if (connectedNode.getId() == nodeId) {
                    plugin.getLogger().info("[EARTHWARDEN DEBUG]   <- " + otherNode.getId() + " (" + otherNode.getName() + ")");
                    hasIncoming = true;
                }
            }
        }

        if (!hasIncoming) {
            plugin.getLogger().info("[EARTHWARDEN DEBUG]   No incoming connections found!");
        }
    }

    @Override
    protected void applySkillStats(Player player, int skillId, int purchaseCount) {
        // Most Earthwarden skills apply effects that are handled in the effects handler
        // This method is primarily for any direct effects on the player

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Earthwarden skill " + (skillId - ID_OFFSET) +
                    " for player " + player.getName() + " will be handled by EarthwardenSkillEffectsHandler");
        }

        // Force refresh player stats to ensure any relevant effects are applied
        plugin.getSkillEffectsHandler().refreshPlayerStats(player);
    }

    @Override
    public boolean isMultiPurchaseDiscountSkill(int skillId) {
        int originalId = skillId - ID_OFFSET;

        // Skills that can be purchased multiple times (based on skill descriptions)
        switch (originalId) {
            case 1:  // +3% defense in grassy areas (1/2)
            case 2:  // Heal 1 hp after killing (1/2)  
            case 4:  // +2 hp per level (1/3)
            case 5:  // Steadfast Defense (1/2)
            case 6:  // Desperate Luck (1/2)
            case 8:  // Entangling Strike (1/2)
            case 9:  // Enhanced Regeneration (1/2)
            case 10: // Reactive Defense (1/2)
            case 13: // Vengeance (1/2)
            case 20: // Healthy Speed (1/2)
            case 26: // Allied Strength (1/2)
                return true;
            default:
                return false;
        }
    }
}
