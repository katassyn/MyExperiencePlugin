package com.maks.myexperienceplugin.Class.skills.classes.spellweaver.ascendancy;

import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.base.BaseSkillManager;
import com.maks.myexperienceplugin.Class.skills.base.SkillNode;
import com.maks.myexperienceplugin.Class.skills.base.SkillTree;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ArcaneProtectorSkillManager extends BaseSkillManager {

    // Use ID range starting from 900000 to avoid conflicts with base class skills
    private static final int ID_OFFSET = 900000;

    public ArcaneProtectorSkillManager(MyExperiencePlugin plugin) {
        super(plugin, "ArcaneProtector");
    }

    @Override
    protected void initializeSkills() {
        // Create nodes for ArcaneProtector - Shield Path, Warding Path, Aura Path

        // Shield Path
        SkillNode node1 = new SkillNode(ID_OFFSET + 1, "Arcane Shield", "Increase shield strength by 5% per level.", 1,
                Material.SHIELD, 3, player -> {
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Arcane Shield skill");
            }
        });

        SkillNode node4 = new SkillNode(ID_OFFSET + 4, "Mana Barrier", "Convert 10% of damage taken to mana cost instead.", 2,
                Material.LIGHT_BLUE_STAINED_GLASS, 1, player -> {
            player.sendMessage(ChatColor.BLUE + "You can now convert damage to mana cost!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Mana Barrier skill");
            }
        });

        SkillNode node7 = new SkillNode(ID_OFFSET + 7, "Arcane Reflection", "Your shields reflect 15% of damage back to attackers.", 2,
                Material.GLASS, 1, player -> {
            player.sendMessage(ChatColor.BLUE + "Your shields now reflect damage!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Arcane Reflection skill");
            }
        });

        SkillNode node11 = new SkillNode(ID_OFFSET + 11, "Spell Absorption", "When hit by spells, 20% chance to gain 5% of the damage as mana.", 3,
                Material.LAPIS_BLOCK, 1, player -> {
            player.sendMessage(ChatColor.BLUE + "You can now absorb spell damage as mana!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Spell Absorption skill");
            }
        });

        SkillNode node12 = new SkillNode(ID_OFFSET + 12, "Fortified Barrier", "Your shields have 25% more health and regenerate 5% per second.", 2,
                Material.IRON_BLOCK, 1, player -> {
            player.sendMessage(ChatColor.BLUE + "Your barriers are now fortified!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Fortified Barrier skill");
            }
        });

        SkillNode node17 = new SkillNode(ID_OFFSET + 17, "Spell Ward", "Reduce spell damage taken by 20%.", 3,
                Material.ENCHANTED_BOOK, 1, player -> {
            player.sendMessage(ChatColor.BLUE + "You are now warded against spells!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Spell Ward skill");
            }
        });

        SkillNode node18 = new SkillNode(ID_OFFSET + 18, "Arcane Aegis", "Create a powerful shield that absorbs 50% of damage for 5s. 30s cooldown.", 3,
                Material.DIAMOND_BLOCK, 1, player -> {
            player.sendMessage(ChatColor.BLUE + "You can now create an arcane aegis!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Arcane Aegis skill");
            }
        });

        SkillNode node22 = new SkillNode(ID_OFFSET + 22, "Spell Deflection", "Your shields have 15% chance to completely negate spell damage.", 3,
                Material.NETHER_STAR, 1, player -> {
            player.sendMessage(ChatColor.BLUE + "Your shields can now deflect spells!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Spell Deflection skill");
            }
        });

        SkillNode node25 = new SkillNode(ID_OFFSET + 25, "Arcane Fortress", "Unlock the Arcane Fortress spell, creating an impenetrable barrier around you for 8s.", 5,
                Material.BEACON, 1, player -> {
            player.sendMessage(ChatColor.BLUE + "You have unlocked the Arcane Fortress spell!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Arcane Fortress skill");
            }
        });

        // Warding Path
        SkillNode node2 = new SkillNode(ID_OFFSET + 2, "Arcane Warding", "Increase magic resistance by 3% per level.", 1,
                Material.BOOK, 3, player -> {
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Arcane Warding skill");
            }
        });

        SkillNode node5 = new SkillNode(ID_OFFSET + 5, "Protective Runes", "Place runes that reduce damage taken by 15% for allies who stand on them.", 2,
                Material.GLOW_ITEM_FRAME, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "You can now place protective runes!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Protective Runes skill");
            }
        });

        SkillNode node8 = new SkillNode(ID_OFFSET + 8, "Arcane Armor", "Convert 15% of your mana into additional defense.", 2,
                Material.NETHERITE_CHESTPLATE, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "Your mana now reinforces your armor!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Arcane Armor skill");
            }
        });

        SkillNode node9 = new SkillNode(ID_OFFSET + 9, "Dispel Magic", "Remove negative effects from yourself and nearby allies. 20s cooldown.", 2,
                Material.MILK_BUCKET, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "You can now dispel negative magic effects!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Dispel Magic skill");
            }
        });

        SkillNode node13 = new SkillNode(ID_OFFSET + 13, "Arcane Resistance", "Gain 25% resistance to all magical damage.", 3,
                Material.AMETHYST_BLOCK, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "You now have increased resistance to magic!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Arcane Resistance skill");
            }
        });

        SkillNode node14 = new SkillNode(ID_OFFSET + 14, "Cleansing Aura", "Allies near you recover from negative effects 30% faster.", 3,
                Material.BEACON, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "You now emanate a cleansing aura!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Cleansing Aura skill");
            }
        });

        SkillNode node19 = new SkillNode(ID_OFFSET + 19, "Spell Barrier", "Create a wall that blocks all spells for 5s. 25s cooldown.", 3,
                Material.COBBLESTONE_WALL, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "You can now create spell barriers!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Spell Barrier skill");
            }
        });

        SkillNode node23 = new SkillNode(ID_OFFSET + 23, "Arcane Immunity", "Gain complete immunity to magic damage for 3s. 60s cooldown.", 3,
                Material.TOTEM_OF_UNDYING, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "You can now become immune to magic!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Arcane Immunity skill");
            }
        });

        SkillNode node26 = new SkillNode(ID_OFFSET + 26, "Nullification Field", "Unlock the Nullification Field spell, creating an area where no magic works for 10s.", 5,
                Material.END_CRYSTAL, 1, player -> {
            player.sendMessage(ChatColor.AQUA + "You have unlocked the Nullification Field spell!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Nullification Field skill");
            }
        });

        // Aura Path
        SkillNode node3 = new SkillNode(ID_OFFSET + 3, "Protective Aura", "Allies within 5 blocks gain 3% damage reduction per level.", 1,
                Material.GLOWSTONE, 3, player -> {
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Protective Aura skill");
            }
        });

        SkillNode node6 = new SkillNode(ID_OFFSET + 6, "Mana Link", "Share 10% of your mana with nearby allies.", 2,
                Material.SOUL_LANTERN, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "You now share mana with your allies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Mana Link skill");
            }
        });

        SkillNode node10 = new SkillNode(ID_OFFSET + 10, "Healing Circle", "Create a circle that heals allies for 2% of their max health per second.", 2,
                Material.GLISTERING_MELON_SLICE, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "You can now create healing circles!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Healing Circle skill");
            }
        });

        SkillNode node15 = new SkillNode(ID_OFFSET + 15, "Arcane Resonance", "Your spells have 20% increased effect on allies.", 3,
                Material.AMETHYST_SHARD, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "Your spells now resonate with your allies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Arcane Resonance skill");
            }
        });

        SkillNode node16 = new SkillNode(ID_OFFSET + 16, "Mana Shield", "Convert 20% of damage taken by nearby allies to your mana cost instead.", 2,
                Material.BLUE_STAINED_GLASS, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "You now shield your allies with your mana!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Mana Shield skill");
            }
        });

        SkillNode node20 = new SkillNode(ID_OFFSET + 20, "Arcane Bond", "Link with an ally, sharing 30% of damage taken. 15s duration, 30s cooldown.", 3,
                Material.LEAD, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "You can now form arcane bonds with allies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Arcane Bond skill");
            }
        });

        SkillNode node21 = new SkillNode(ID_OFFSET + 21, "Protective Ward", "Place a ward that absorbs 50% of damage for allies inside it. 20s cooldown.", 3,
                Material.LODESTONE, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "You can now create protective wards!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Protective Ward skill");
            }
        });

        SkillNode node24 = new SkillNode(ID_OFFSET + 24, "Mass Protection", "Cast all your protective spells on all nearby allies at once. 60s cooldown.", 3,
                Material.GOLDEN_APPLE, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "You can now protect all your allies at once!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Mass Protection skill");
            }
        });

        SkillNode node27 = new SkillNode(ID_OFFSET + 27, "Sanctuary", "Unlock the Sanctuary spell, creating a zone where allies cannot be damaged for 5s.", 5,
                Material.CONDUIT, 1, player -> {
            player.sendMessage(ChatColor.YELLOW + "You have unlocked the Sanctuary spell!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Sanctuary skill");
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
            plugin.getLogger().info("Initialized all 27 ArcaneProtector skills with ID offset " + ID_OFFSET);
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
            plugin.getLogger().info("Configured ArcaneProtector skill tree with UNIVERSAL structure from Drzewko Podklas.md");
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
            case 1: // Arcane Shield
                stats.addDefenseBonus(5 * purchaseCount);
                break;
            case 2: // Arcane Warding
                // Magic resistance handled by effects handler
                break;
            case 3: // Protective Aura
                // Aura effects handled by effects handler
                break;
            case 11: // Spell Absorption
                // Handled by effects handler
                break;
            case 13: // Arcane Resistance
                // Handled by effects handler
                break;
            case 17: // Spell Ward
                // Handled by effects handler
                break;
            case 22: // Spell Deflection
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
