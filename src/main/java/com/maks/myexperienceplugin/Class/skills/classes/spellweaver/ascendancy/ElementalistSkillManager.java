package com.maks.myexperienceplugin.Class.skills.classes.spellweaver.ascendancy;

import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.base.BaseSkillManager;
import com.maks.myexperienceplugin.Class.skills.base.SkillNode;
import com.maks.myexperienceplugin.Class.skills.base.SkillTree;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ElementalistSkillManager extends BaseSkillManager {

    // Use ID range starting from 700000 to avoid conflicts with base class skills
    private static final int ID_OFFSET = 700000;

    public ElementalistSkillManager(MyExperiencePlugin plugin) {
        super(plugin, "Elementalist");
    }

    @Override
    protected void initializeSkills() {
        // Create nodes for Elementalist - Fire Path, Ice Path, Lightning Path

        // Fire Path
        SkillNode node1 = new SkillNode(ID_OFFSET + 1, "Fire Mastery", "Increase fire damage by 5% per level.", 1,
                Material.BLAZE_POWDER, 3, player -> {
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Fire Mastery skill");
            }
        });

        SkillNode node4 = new SkillNode(ID_OFFSET + 4, "Flame Burst", "Your fire spells have 10% chance to create a small explosion.", 2,
                Material.FIRE_CHARGE, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your fire spells can now create explosions!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Flame Burst skill");
            }
        });

        SkillNode node7 = new SkillNode(ID_OFFSET + 7, "Heat Wave", "Fire spells apply a burning effect that deals 2% damage per second for 3s.", 2,
                Material.MAGMA_CREAM, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your fire spells now apply a burning effect!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Heat Wave skill");
            }
        });

        SkillNode node11 = new SkillNode(ID_OFFSET + 11, "Fire Shield", "When hit, 15% chance to create a fire shield that reduces damage by 20% for 3s.", 3,
                Material.SHIELD, 1, player -> {
            player.sendMessage(ChatColor.RED + "You can now create a fire shield when hit!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Fire Shield skill");
            }
        });

        SkillNode node12 = new SkillNode(ID_OFFSET + 12, "Inferno", "Fire spells deal 15% more damage to burning enemies.", 2,
                Material.LAVA_BUCKET, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your fire spells now deal more damage to burning enemies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Inferno skill");
            }
        });

        SkillNode node17 = new SkillNode(ID_OFFSET + 17, "Phoenix Form", "When below 20% health, gain 50% fire resistance and heal 2% per second for 5s.", 3,
                Material.FEATHER, 1, player -> {
            player.sendMessage(ChatColor.RED + "You can now transform into a phoenix when near death!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Phoenix Form skill");
            }
        });

        SkillNode node18 = new SkillNode(ID_OFFSET + 18, "Fire Nova", "Every 5th fire spell creates a nova that deals 30% spell damage to all nearby enemies.", 3,
                Material.FIRE_CHARGE, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your fire spells can now create a nova effect!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Fire Nova skill");
            }
        });

        SkillNode node22 = new SkillNode(ID_OFFSET + 22, "Combustion", "Critical hits with fire spells cause enemies to explode for 40% of the damage.", 3,
                Material.TNT, 1, player -> {
            player.sendMessage(ChatColor.RED + "Your critical fire spells now cause enemies to explode!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Combustion skill");
            }
        });

        SkillNode node25 = new SkillNode(ID_OFFSET + 25, "Meteor Strike", "Unlock the Meteor Strike spell, dealing massive fire damage in an area.", 5,
                Material.FIRE_CHARGE, 1, player -> {
            player.sendMessage(ChatColor.RED + "You have unlocked the Meteor Strike spell!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Meteor Strike skill");
            }
        });

        // Ice Path
        SkillNode node2 = new SkillNode(ID_OFFSET + 2, "Ice Mastery", "Increase ice damage by 5% per level.", 1,
                Material.ICE, 3, player -> {
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Ice Mastery skill");
            }
        });

        SkillNode node5 = new SkillNode(ID_OFFSET + 5, "Frost Nova", "Your ice spells have 10% chance to freeze nearby enemies for 1s.", 2,
                Material.PACKED_ICE, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "Your ice spells can now freeze nearby enemies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Frost Nova skill");
            }
        });

        SkillNode node8 = new SkillNode(ID_OFFSET + 8, "Chilling Touch", "Ice spells slow enemies by 20% for 3s.", 2,
                Material.BLUE_ICE, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "Your ice spells now slow enemies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Chilling Touch skill");
            }
        });

        SkillNode node9 = new SkillNode(ID_OFFSET + 9, "Ice Barrier", "Create an ice barrier that absorbs 30% of damage for 5s.", 2,
                Material.GLASS, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "You can now create an ice barrier!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Ice Barrier skill");
            }
        });

        SkillNode node13 = new SkillNode(ID_OFFSET + 13, "Deep Freeze", "Frozen enemies take 25% more damage from all sources.", 3,
                Material.BLUE_ICE, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "Frozen enemies now take more damage!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Deep Freeze skill");
            }
        });

        SkillNode node14 = new SkillNode(ID_OFFSET + 14, "Ice Reflection", "Your ice barrier reflects 20% of damage back to attackers.", 3,
                Material.LIGHT_BLUE_STAINED_GLASS, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "Your ice barrier now reflects damage!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Ice Reflection skill");
            }
        });

        SkillNode node19 = new SkillNode(ID_OFFSET + 19, "Absolute Zero", "Ice spells have 15% chance to instantly freeze enemies for 2s.", 3,
                Material.DIAMOND_BLOCK, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "Your ice spells can now instantly freeze enemies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Absolute Zero skill");
            }
        });

        SkillNode node23 = new SkillNode(ID_OFFSET + 23, "Winter's Embrace", "While below 30% health, gain 50% damage reduction and immunity to slowing effects.", 3,
                Material.SNOW_BLOCK, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "You now gain protection when near death!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Winter's Embrace skill");
            }
        });

        SkillNode node26 = new SkillNode(ID_OFFSET + 26, "Blizzard", "Unlock the Blizzard spell, creating a storm of ice that damages and slows all enemies in an area.", 5,
                Material.SNOWBALL, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "You have unlocked the Blizzard spell!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Blizzard skill");
            }
        });

        // Lightning Path
        SkillNode node3 = new SkillNode(ID_OFFSET + 3, "Lightning Mastery", "Increase lightning damage by 5% per level.", 1,
                Material.GLOWSTONE_DUST, 3, player -> {
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Lightning Mastery skill");
            }
        });

        SkillNode node6 = new SkillNode(ID_OFFSET + 6, "Chain Lightning", "Lightning spells have 15% chance to chain to another target for 50% damage.", 2,
                Material.LIGHTNING_ROD, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "Your lightning spells can now chain to additional targets!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Chain Lightning skill");
            }
        });

        SkillNode node10 = new SkillNode(ID_OFFSET + 10, "Static Charge", "Enemies hit by lightning spells are charged for 5s, taking 10% more lightning damage.", 2,
                Material.REDSTONE, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "Your lightning spells now apply a static charge!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Static Charge skill");
            }
        });

        SkillNode node15 = new SkillNode(ID_OFFSET + 15, "Thunderstrike", "Lightning spells have 10% chance to call down a thunderbolt for 40% additional damage.", 3,
                Material.NETHER_STAR, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "Your lightning spells can now call down thunderbolts!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Thunderstrike skill");
            }
        });

        SkillNode node16 = new SkillNode(ID_OFFSET + 16, "Electrified", "When hit, 20% chance to release a shock wave that damages nearby enemies for 15% of your spell damage.", 2,
                Material.LIGHT_BLUE_DYE, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "You now release shock waves when hit!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Electrified skill");
            }
        });

        SkillNode node20 = new SkillNode(ID_OFFSET + 20, "Overcharge", "Lightning spells have 20% chance to critically strike, dealing 50% more damage.", 3,
                Material.GOLD_BLOCK, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "Your lightning spells now have increased critical chance!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Overcharge skill");
            }
        });

        SkillNode node21 = new SkillNode(ID_OFFSET + 21, "Conduction", "Enemies affected by Static Charge spread 30% of lightning damage to nearby enemies.", 3,
                Material.IRON_BARS, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "Your static charge now spreads damage to nearby enemies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Conduction skill");
            }
        });

        SkillNode node24 = new SkillNode(ID_OFFSET + 24, "Storm Shield", "When below 25% health, gain 30% movement speed and 20% spell damage for 5s.", 3,
                Material.SHIELD, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "You now gain speed and power when near death!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Storm Shield skill");
            }
        });

        SkillNode node27 = new SkillNode(ID_OFFSET + 27, "Thunderstorm", "Unlock the Thunderstorm spell, creating a persistent storm that strikes random enemies.", 5,
                Material.TRIDENT, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "You have unlocked the Thunderstorm spell!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Thunderstorm skill");
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
            plugin.getLogger().info("Initialized all 27 Elementalist skills with ID offset " + ID_OFFSET);
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
            plugin.getLogger().info("Configured Elementalist skill tree with UNIVERSAL structure from Drzewko Podklas.md");
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
            plugin.getLogger().info("[" + getClass().getSimpleName().replace("SkillManager", "").toUpperCase() + " DEBUG] Node " + nodeId + " not found in tree!");
            return;
        }

        plugin.getLogger().info("[" + getClass().getSimpleName().replace("SkillManager", "").toUpperCase() + " DEBUG] Node " + nodeId + " (" + node.getName() + ") connections:");

        // Log outgoing connections
        plugin.getLogger().info("[" + getClass().getSimpleName().replace("SkillManager", "").toUpperCase() + " DEBUG] - Outgoing connections: " + node.getConnectedNodes().size());
        for (SkillNode connected : node.getConnectedNodes()) {
            plugin.getLogger().info("[" + getClass().getSimpleName().replace("SkillManager", "").toUpperCase() + " DEBUG]   -> " + connected.getId() + " (" + connected.getName() + ")");
        }

        // Log incoming connections
        plugin.getLogger().info("[" + getClass().getSimpleName().replace("SkillManager", "").toUpperCase() + " DEBUG] - Checking incoming connections...");
        boolean hasIncoming = false;
        for (SkillNode otherNode : tree.getAllNodes()) {
            for (SkillNode connectedNode : otherNode.getConnectedNodes()) {
                if (connectedNode.getId() == nodeId) {
                    plugin.getLogger().info("[" + getClass().getSimpleName().replace("SkillManager", "").toUpperCase() + " DEBUG]   <- " + otherNode.getId() + " (" + otherNode.getName() + ")");
                    hasIncoming = true;
                }
            }
        }

        if (!hasIncoming) {
            plugin.getLogger().info("[" + getClass().getSimpleName().replace("SkillManager", "").toUpperCase() + " DEBUG]   No incoming connections found!");
        }
    }

    @Override
    protected void applySkillStats(Player player, int skillId, int purchaseCount) {
        // Get player stats
        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);

        // Apply stats based on skill ID
        int originalId = skillId - ID_OFFSET;

        switch (originalId) {
            case 1: // Fire Mastery
                stats.addSpellDamageBonus(5 * purchaseCount);
                break;
            case 2: // Ice Mastery
                stats.addSpellDamageBonus(5 * purchaseCount);
                break;
            case 3: // Lightning Mastery
                stats.addSpellDamageBonus(5 * purchaseCount);
                break;
            case 11: // Fire Shield
                // Handled by effects handler
                break;
            case 12: // Inferno
                // Handled by effects handler
                break;
            case 13: // Deep Freeze
                // Handled by effects handler
                break;
            case 14: // Ice Reflection
                // Handled by effects handler
                break;
            case 17: // Phoenix Form
                // Handled by effects handler
                break;
            case 18: // Fire Nova
                // Handled by effects handler
                break;
            case 19: // Absolute Zero
                // Handled by effects handler
                break;
            case 20: // Overcharge
                // Handled by effects handler
                break;
            case 22: // Combustion
                // Handled by effects handler
                break;
            case 23: // Winter's Embrace
                // Handled by effects handler
                break;
            case 24: // Storm Shield
                // Handled by effects handler
                break;
            case 25: // Meteor Strike
                // Handled by effects handler
                break;
            case 26: // Blizzard
                // Handled by effects handler
                break;
            case 27: // Thunderstorm
                // Handled by effects handler
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
}
