package com.maks.myexperienceplugin.Class.skills.classes.ranger.ascendancy;

import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.base.BaseSkillManager;
import com.maks.myexperienceplugin.Class.skills.base.SkillNode;
import com.maks.myexperienceplugin.Class.skills.base.SkillTree;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class BeastmasterSkillManager extends BaseSkillManager {

    // Use ID range starting from 100000 to avoid conflicts with base class skills
    private static final int ID_OFFSET = 100000;

    public BeastmasterSkillManager(MyExperiencePlugin plugin) {
        super(plugin, "Beastmaster");
    }

    @Override
    protected void initializeSkills() {
        // Create nodes based on Beastmaster.md - FULLY IMPLEMENTED
        SkillNode node1 = new SkillNode(ID_OFFSET + 1, "Wolf Summon", "Unlock wolf summon, balanced companion (50% dmg/50hp).\nNote: You can only have 2 types of summons at once.", 1,
                Material.BONE, 1, player -> {
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Wolf Summon skill");
            }
        });

        SkillNode node2 = new SkillNode(ID_OFFSET + 2, "Boar Summon", "Unlock boar summon, high damage companion (80% dmg/20hp).\nNote: You can only have 2 types of summons at once.", 1,
                Material.PORKCHOP, 1, player -> {
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Boar Summon skill");
            }
        });

        SkillNode node3 = new SkillNode(ID_OFFSET + 3, "Bear Summon", "Unlock bear summon, tanky companion (20 dmg/80hp).\nNote: You can only have 2 types of summons at once.", 1,
                Material.HONEY_BOTTLE, 1, player -> {
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Bear Summon skill");
            }
        });

        SkillNode node4 = new SkillNode(ID_OFFSET + 4, "Wolf Speed", "Wolves gain +5% movement speed", 2,
                Material.FEATHER, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your wolves become faster!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Wolf Speed skill");
            }
        });

        SkillNode node5 = new SkillNode(ID_OFFSET + 5, "Boar Damage", "Boars gain +15% damage", 2,
                Material.IRON_AXE, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your boars become stronger!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Boar Damage skill");
            }
        });

        SkillNode node6 = new SkillNode(ID_OFFSET + 6, "Bear Health", "Bears gain +10% hp", 2,
                Material.APPLE, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your bears become tougher!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Bear Health skill");
            }
        });

        SkillNode node7 = new SkillNode(ID_OFFSET + 7, "Wolf Attack Speed", "Wolves gain +5% attack speed", 2,
                Material.STONE_SWORD, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your wolves attack faster!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Wolf Attack Speed skill");
            }
        });

        SkillNode node8 = new SkillNode(ID_OFFSET + 8, "Boar Attack Speed", "Boars gain +10% attack speed", 2,
                Material.IRON_SWORD, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your boars attack faster!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Boar Attack Speed skill");
            }
        });

        SkillNode node9 = new SkillNode(ID_OFFSET + 9, "Pack Damage", "All summons gain +5% damage", 2,
                Material.BLAZE_POWDER, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "All your companions become stronger!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Pack Damage skill");
            }
        });

        SkillNode node10 = new SkillNode(ID_OFFSET + 10, "Bear Defense", "Bears gain +50% defense", 2,
                Material.SHIELD, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your bears become more defensive!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Bear Defense skill");
            }
        });

        SkillNode node11 = new SkillNode(ID_OFFSET + 11, "Wolf Critical", "Wolves gain 10% chance to critical hit", 3,
                Material.DIAMOND_SWORD, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your wolves now have a chance for critical hits!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Wolf Critical skill");
            }
        });

        SkillNode node12 = new SkillNode(ID_OFFSET + 12, "Wolf Vitality", "Wolves gain +100hp", 2,
                Material.GOLDEN_APPLE, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your wolves become healthier!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Wolf Vitality skill");
            }
        });

        SkillNode node13 = new SkillNode(ID_OFFSET + 13, "Boar Critical", "Boars gain 15% chance to critical hit", 3,
                Material.NETHERITE_AXE, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your boars now have a chance for critical hits!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Boar Critical skill");
            }
        });

        SkillNode node14 = new SkillNode(ID_OFFSET + 14, "Pack Damage Plus", "All summons gain +10% damage", 3,
                Material.BLAZE_ROD, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "All your companions become even stronger!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Pack Damage Plus skill");
            }
        });

        SkillNode node15 = new SkillNode(ID_OFFSET + 15, "Bear Guardian", "When Bears hp<50% u and all summons gain +10% def", 3,
                Material.NETHERITE_CHESTPLATE, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your bears now protect the pack when injured!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Bear Guardian skill");
            }
        });

        SkillNode node16 = new SkillNode(ID_OFFSET + 16, "Bear Vitality", "Bears gain +200hp", 2,
                Material.GOLDEN_CARROT, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your bears become much healthier!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Bear Vitality skill");
            }
        });

        SkillNode node17 = new SkillNode(ID_OFFSET + 17, "Wolf Healing", "Wolves heal you for 5% of damage dealt", 3,
                Material.POTION, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your wolves now heal you as they attack!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Wolf Healing skill");
            }
        });

        SkillNode node18 = new SkillNode(ID_OFFSET + 18, "Pack Speed", "All summons gain +10% movement speed", 3,
                Material.SUGAR, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "All your companions move faster!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Pack Speed skill");
            }
        });

        SkillNode node19 = new SkillNode(ID_OFFSET + 19, "Boar Frenzy", "Boars after killing enemy gain +7% attack speed for 3s", 3,
                Material.REDSTONE, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your boars enter a frenzy after kills!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Boar Frenzy skill");
            }
        });

        SkillNode node20 = new SkillNode(ID_OFFSET + 20, "Bear Regeneration", "Bears heal for 10% hp each 10s", 3,
                Material.HONEY_BLOCK, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your bears now regenerate health over time!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Bear Regeneration skill");
            }
        });

        SkillNode node21 = new SkillNode(ID_OFFSET + 21, "Pack Vitality", "All summons gain +30% hp", 3,
                Material.TOTEM_OF_UNDYING, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "All your companions become much healthier!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Pack Vitality skill");
            }
        });

        SkillNode node22 = new SkillNode(ID_OFFSET + 22, "Wolf Health", "Wolves gain +10% hp", 3,
                Material.MELON_SLICE, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your wolves become tougher!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Wolf Health skill");
            }
        });

        SkillNode node23 = new SkillNode(ID_OFFSET + 23, "Boar Speed", "Boars gain +20% movement speed", 3,
                Material.RABBIT_FOOT, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your boars become much faster!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Boar Speed skill");
            }
        });

        SkillNode node24 = new SkillNode(ID_OFFSET + 24, "Pack Defense", "All summons gain +25% defense", 3,
                Material.IRON_CHESTPLATE, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "All your companions become more defensive!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Pack Defense skill");
            }
        });

        SkillNode node25 = new SkillNode(ID_OFFSET + 25, "Wolf Pack", "You summon 1 more wolf", 5,
                Material.WOLF_SPAWN_EGG, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You can now summon an additional wolf!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Wolf Pack skill");
            }
        });

        SkillNode node26 = new SkillNode(ID_OFFSET + 26, "Boar Rage", "Boars gain +15% damage and +15% attack speed", 5,
                Material.PORKCHOP, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your boars enter a permanent rage state!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Boar Rage skill");
            }
        });

        SkillNode node27 = new SkillNode(ID_OFFSET + 27, "Pack Healing", "Heals your summons for 5% of your damage dealt", 5,
                Material.SPLASH_POTION, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You now heal your companions as you attack!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Pack Healing skill");
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
            plugin.getLogger().info("Initialized all 27 Beastmaster skills with ID offset " + ID_OFFSET);
        }
    }

    @Override
    protected void setupTreeStructure(SkillTree tree) {
        // Define root nodes
        tree.addRootNode(ID_OFFSET + 1);
        tree.addRootNode(ID_OFFSET + 2);
        tree.addRootNode(ID_OFFSET + 3);

        plugin.getLogger().info("[BEASTMASTER DEBUG] Added root nodes: " + 
                (ID_OFFSET + 1) + " (Wolf Summon), " + 
                (ID_OFFSET + 2) + " (Boar Summon), " + 
                (ID_OFFSET + 3) + " (Bear Summon)");

        // Path 1: Wolf Path
        tree.connectNodes(ID_OFFSET + 1, ID_OFFSET + 4);
        plugin.getLogger().info("[BEASTMASTER DEBUG] Connected: " + (ID_OFFSET + 1) + " (Wolf Summon) -> " + (ID_OFFSET + 4) + " (Wolf Speed)");

        tree.connectNodes(ID_OFFSET + 4, ID_OFFSET + 7);
        plugin.getLogger().info("[BEASTMASTER DEBUG] Connected: " + (ID_OFFSET + 4) + " (Wolf Speed) -> " + (ID_OFFSET + 7) + " (Wolf Attack Speed)");

        tree.connectNodes(ID_OFFSET + 7, ID_OFFSET + 11);
        plugin.getLogger().info("[BEASTMASTER DEBUG] Connected: " + (ID_OFFSET + 7) + " (Wolf Attack Speed) -> " + (ID_OFFSET + 11) + " (Wolf Critical)");

        tree.connectNodes(ID_OFFSET + 7, ID_OFFSET + 12);
        plugin.getLogger().info("[BEASTMASTER DEBUG] Connected: " + (ID_OFFSET + 7) + " (Wolf Attack Speed) -> " + (ID_OFFSET + 12) + " (Wolf Vitality)");

        tree.connectNodes(ID_OFFSET + 11, ID_OFFSET + 17);
        plugin.getLogger().info("[BEASTMASTER DEBUG] Connected: " + (ID_OFFSET + 11) + " (Wolf Critical) -> " + (ID_OFFSET + 17) + " (Wolf Healing)");

        tree.connectNodes(ID_OFFSET + 12, ID_OFFSET + 18);
        plugin.getLogger().info("[BEASTMASTER DEBUG] Connected: " + (ID_OFFSET + 12) + " (Wolf Vitality) -> " + (ID_OFFSET + 18) + " (Pack Speed)");

        tree.connectNodes(ID_OFFSET + 17, ID_OFFSET + 22);
        plugin.getLogger().info("[BEASTMASTER DEBUG] Connected: " + (ID_OFFSET + 17) + " (Wolf Healing) -> " + (ID_OFFSET + 22) + " (Wolf Health)");

        tree.connectNodes(ID_OFFSET + 18, ID_OFFSET + 22);
        plugin.getLogger().info("[BEASTMASTER DEBUG] Connected: " + (ID_OFFSET + 18) + " (Pack Speed) -> " + (ID_OFFSET + 22) + " (Wolf Health)");

        tree.connectNodes(ID_OFFSET + 22, ID_OFFSET + 25);
        plugin.getLogger().info("[BEASTMASTER DEBUG] Connected: " + (ID_OFFSET + 22) + " (Wolf Health) -> " + (ID_OFFSET + 25) + " (Wolf Pack)");

        // Path 2: Boar Path
        tree.connectNodes(ID_OFFSET + 2, ID_OFFSET + 5);
        plugin.getLogger().info("[BEASTMASTER DEBUG] Connected: " + (ID_OFFSET + 2) + " (Boar Summon) -> " + (ID_OFFSET + 5) + " (Boar Damage)");

        tree.connectNodes(ID_OFFSET + 5, ID_OFFSET + 8);
        plugin.getLogger().info("[BEASTMASTER DEBUG] Connected: " + (ID_OFFSET + 5) + " (Boar Damage) -> " + (ID_OFFSET + 8) + " (Boar Attack Speed)");

        tree.connectNodes(ID_OFFSET + 5, ID_OFFSET + 9);
        plugin.getLogger().info("[BEASTMASTER DEBUG] Connected: " + (ID_OFFSET + 5) + " (Boar Damage) -> " + (ID_OFFSET + 9) + " (Pack Damage)");

        tree.connectNodes(ID_OFFSET + 8, ID_OFFSET + 13);
        plugin.getLogger().info("[BEASTMASTER DEBUG] Connected: " + (ID_OFFSET + 8) + " (Boar Attack Speed) -> " + (ID_OFFSET + 13) + " (Boar Critical)");

        tree.connectNodes(ID_OFFSET + 9, ID_OFFSET + 14);
        plugin.getLogger().info("[BEASTMASTER DEBUG] Connected: " + (ID_OFFSET + 9) + " (Pack Damage) -> " + (ID_OFFSET + 14) + " (Pack Damage Plus)");

        tree.connectNodes(ID_OFFSET + 13, ID_OFFSET + 19);
        plugin.getLogger().info("[BEASTMASTER DEBUG] Connected: " + (ID_OFFSET + 13) + " (Boar Critical) -> " + (ID_OFFSET + 19) + " (Boar Frenzy)");

        // REMOVED: tree.connectNodes(ID_OFFSET + 14, ID_OFFSET + 19);
        // This connection shouldn't exist

        tree.connectNodes(ID_OFFSET + 19, ID_OFFSET + 23);
        plugin.getLogger().info("[BEASTMASTER DEBUG] Connected: " + (ID_OFFSET + 19) + " (Boar Frenzy) -> " + (ID_OFFSET + 23) + " (Boar Speed)");

        tree.connectNodes(ID_OFFSET + 23, ID_OFFSET + 26);
        plugin.getLogger().info("[BEASTMASTER DEBUG] Connected: " + (ID_OFFSET + 23) + " (Boar Speed) -> " + (ID_OFFSET + 26) + " (Boar Rage)");

        // Path 3: Bear Path
        tree.connectNodes(ID_OFFSET + 3, ID_OFFSET + 6);
        plugin.getLogger().info("[BEASTMASTER DEBUG] Connected: " + (ID_OFFSET + 3) + " (Bear Summon) -> " + (ID_OFFSET + 6) + " (Bear Health)");

        tree.connectNodes(ID_OFFSET + 6, ID_OFFSET + 10);
        plugin.getLogger().info("[BEASTMASTER DEBUG] Connected: " + (ID_OFFSET + 6) + " (Bear Health) -> " + (ID_OFFSET + 10) + " (Bear Defense)");

        tree.connectNodes(ID_OFFSET + 10, ID_OFFSET + 15);
        plugin.getLogger().info("[BEASTMASTER DEBUG] Connected: " + (ID_OFFSET + 10) + " (Bear Defense) -> " + (ID_OFFSET + 15) + " (Bear Guardian)");

        tree.connectNodes(ID_OFFSET + 10, ID_OFFSET + 16);
        plugin.getLogger().info("[BEASTMASTER DEBUG] Connected: " + (ID_OFFSET + 10) + " (Bear Defense) -> " + (ID_OFFSET + 16) + " (Bear Vitality)");

        tree.connectNodes(ID_OFFSET + 15, ID_OFFSET + 20);
        plugin.getLogger().info("[BEASTMASTER DEBUG] Connected: " + (ID_OFFSET + 15) + " (Bear Guardian) -> " + (ID_OFFSET + 20) + " (Bear Regeneration)");

        tree.connectNodes(ID_OFFSET + 16, ID_OFFSET + 21);
        plugin.getLogger().info("[BEASTMASTER DEBUG] Connected: " + (ID_OFFSET + 16) + " (Bear Vitality) -> " + (ID_OFFSET + 21) + " (Pack Vitality)");

        tree.connectNodes(ID_OFFSET + 20, ID_OFFSET + 24);
        plugin.getLogger().info("[BEASTMASTER DEBUG] Connected: " + (ID_OFFSET + 20) + " (Bear Regeneration) -> " + (ID_OFFSET + 24) + " (Pack Defense)");

        tree.connectNodes(ID_OFFSET + 21, ID_OFFSET + 24);
        plugin.getLogger().info("[BEASTMASTER DEBUG] Connected: " + (ID_OFFSET + 21) + " (Pack Vitality) -> " + (ID_OFFSET + 24) + " (Pack Defense)");

        tree.connectNodes(ID_OFFSET + 24, ID_OFFSET + 27);
        plugin.getLogger().info("[BEASTMASTER DEBUG] Connected: " + (ID_OFFSET + 24) + " (Pack Defense) -> " + (ID_OFFSET + 27) + " (Pack Healing)");

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Configured Beastmaster skill tree with " +
                    tree.getRootNodeIds().size() + " root nodes and complete connection paths");

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
            plugin.getLogger().info("[BEASTMASTER DEBUG] Node " + nodeId + " not found in tree!");
            return;
        }

        plugin.getLogger().info("[BEASTMASTER DEBUG] Node " + nodeId + " (" + node.getName() + ") connections:");

        // Log outgoing connections
        plugin.getLogger().info("[BEASTMASTER DEBUG] - Outgoing connections: " + node.getConnectedNodes().size());
        for (SkillNode connected : node.getConnectedNodes()) {
            plugin.getLogger().info("[BEASTMASTER DEBUG]   -> " + connected.getId() + " (" + connected.getName() + ")");
        }

        // Log incoming connections
        plugin.getLogger().info("[BEASTMASTER DEBUG] - Checking incoming connections...");
        boolean hasIncoming = false;
        for (SkillNode otherNode : tree.getAllNodes()) {
            for (SkillNode connectedNode : otherNode.getConnectedNodes()) {
                if (connectedNode.getId() == nodeId) {
                    plugin.getLogger().info("[BEASTMASTER DEBUG]   <- " + otherNode.getId() + " (" + otherNode.getName() + ")");
                    hasIncoming = true;
                }
            }
        }

        if (!hasIncoming) {
            plugin.getLogger().info("[BEASTMASTER DEBUG]   No incoming connections found!");
        }
    }

    @Override
    protected void applySkillStats(Player player, int skillId, int purchaseCount) {
        // Most Beastmaster skills apply to summons which are handled in the effects handler
        // This method is primarily for any direct effects on the player

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Beastmaster skill " + (skillId - ID_OFFSET) +
                    " for player " + player.getName() + " will be handled by BeastmasterSkillEffectsHandler");
        }

        // Force refresh player stats to ensure any relevant effects are applied
        plugin.getSkillEffectsHandler().refreshPlayerStats(player);
    }

    @Override
    public boolean isMultiPurchaseDiscountSkill(int skillId) {
        // Beastmaster doesn't have any multi-purchase discount skills
        return false;
    }
}
