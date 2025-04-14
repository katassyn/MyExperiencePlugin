package com.maks.myexperienceplugin.Class.skills.classes.ranger.ascendancy;

import com.maks.myexperienceplugin.Class.skills.base.BaseSkillManager;
import com.maks.myexperienceplugin.Class.skills.base.SkillNode;
import com.maks.myexperienceplugin.Class.skills.base.SkillTree;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class BeastmasterSkillManager extends BaseSkillManager {

    public BeastmasterSkillManager(MyExperiencePlugin plugin) {
        super(plugin, "Beastmaster");
    }

    @Override
    protected void initializeSkills() {
        // Create nodes based on Beastmaster.md - FULLY IMPLEMENTED
        SkillNode node1 = new SkillNode(1, "Wolf Summon", "Unlock wolf summon, balanced companion (50% dmg/50hp)", 1,
                Material.BONE, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You can now summon wolves!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Wolf Summon skill");
            }
        });

        SkillNode node2 = new SkillNode(2, "Boar Summon", "Unlock boar summon, high damage companion (80% dmg/20hp)", 1,
                Material.PORKCHOP, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You can now summon boars!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Boar Summon skill");
            }
        });

        SkillNode node3 = new SkillNode(3, "Bear Summon", "Unlock bear summon, tanky companion (20% dmg/80hp)", 1,
                Material.HONEY_BOTTLE, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You can now summon bears!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Bear Summon skill");
            }
        });

        SkillNode node4 = new SkillNode(4, "Wolf Speed", "Wolves gain +5% movement speed", 2,
                Material.FEATHER, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your wolves become faster!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Wolf Speed skill");
            }
        });

        SkillNode node5 = new SkillNode(5, "Boar Damage", "Boars gain +15% damage", 2,
                Material.IRON_AXE, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your boars become stronger!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Boar Damage skill");
            }
        });

        SkillNode node6 = new SkillNode(6, "Bear Health", "Bears gain +10% hp", 2,
                Material.APPLE, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your bears become tougher!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Bear Health skill");
            }
        });

        SkillNode node7 = new SkillNode(7, "Wolf Attack Speed", "Wolves gain +5% attack speed", 2,
                Material.STONE_SWORD, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your wolves attack faster!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Wolf Attack Speed skill");
            }
        });

        SkillNode node8 = new SkillNode(8, "Boar Attack Speed", "Boars gain +10% attack speed", 2,
                Material.IRON_SWORD, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your boars attack faster!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Boar Attack Speed skill");
            }
        });

        SkillNode node9 = new SkillNode(9, "Pack Damage", "All summons gain +5% damage", 2,
                Material.BLAZE_POWDER, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "All your companions become stronger!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Pack Damage skill");
            }
        });

        SkillNode node10 = new SkillNode(10, "Bear Defense", "Bears gain +50% defense", 2,
                Material.SHIELD, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your bears become more defensive!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Bear Defense skill");
            }
        });

        SkillNode node11 = new SkillNode(11, "Wolf Critical", "Wolves gain 10% chance to critical hit", 3,
                Material.DIAMOND_SWORD, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your wolves now have a chance for critical hits!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Wolf Critical skill");
            }
        });

        SkillNode node12 = new SkillNode(12, "Wolf Vitality", "Wolves gain +100hp", 2,
                Material.GOLDEN_APPLE, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your wolves become healthier!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Wolf Vitality skill");
            }
        });

        SkillNode node13 = new SkillNode(13, "Boar Critical", "Boars gain 15% chance to critical hit", 3,
                Material.NETHERITE_AXE, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your boars now have a chance for critical hits!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Boar Critical skill");
            }
        });

        SkillNode node14 = new SkillNode(14, "Pack Damage Plus", "All summons gain +10% damage", 3,
                Material.BLAZE_ROD, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "All your companions become even stronger!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Pack Damage Plus skill");
            }
        });

        SkillNode node15 = new SkillNode(15, "Bear Guardian", "When Bears hp<50% you and all summons gain +10% def", 3,
                Material.NETHERITE_CHESTPLATE, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your bears now protect the pack when injured!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Bear Guardian skill");
            }
        });

        SkillNode node16 = new SkillNode(16, "Bear Vitality", "Bears gain +200hp", 2,
                Material.GOLDEN_CARROT, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your bears become much healthier!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Bear Vitality skill");
            }
        });

        SkillNode node17 = new SkillNode(17, "Wolf Healing", "Wolves heal you for 5% of damage dealt", 3,
                Material.POTION, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your wolves now heal you as they attack!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Wolf Healing skill");
            }
        });

        SkillNode node18 = new SkillNode(18, "Pack Speed", "All summons gain +10% movement speed", 3,
                Material.SUGAR, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "All your companions move faster!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Pack Speed skill");
            }
        });

        SkillNode node19 = new SkillNode(19, "Boar Frenzy", "Boars after killing enemy gain +7% attack speed for 3s", 3,
                Material.REDSTONE, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your boars enter a frenzy after kills!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Boar Frenzy skill");
            }
        });

        SkillNode node20 = new SkillNode(20, "Bear Regeneration", "Bears heal for 10% hp each 10s", 3,
                Material.HONEY_BLOCK, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your bears now regenerate health over time!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Bear Regeneration skill");
            }
        });

        SkillNode node21 = new SkillNode(21, "Pack Vitality", "All summons gain +30% hp", 3,
                Material.TOTEM_OF_UNDYING, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "All your companions become much healthier!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Pack Vitality skill");
            }
        });

        SkillNode node22 = new SkillNode(22, "Wolf Health", "Wolves gain +10% hp", 3,
                Material.MELON_SLICE, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your wolves become tougher!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Wolf Health skill");
            }
        });

        SkillNode node23 = new SkillNode(23, "Boar Speed", "Boars gain +20% movement speed", 3,
                Material.RABBIT_FOOT, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your boars become much faster!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Boar Speed skill");
            }
        });

        SkillNode node24 = new SkillNode(24, "Pack Defense", "All summons gain +25% defense", 3,
                Material.IRON_CHESTPLATE, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "All your companions become more defensive!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Pack Defense skill");
            }
        });

        SkillNode node25 = new SkillNode(25, "Wolf Pack", "You summon 1 more wolf", 5,
                Material.WOLF_SPAWN_EGG, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You can now summon an additional wolf!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Wolf Pack skill");
            }
        });

        SkillNode node26 = new SkillNode(26, "Boar Rage", "Boars gain +15% damage and +15% attack speed", 5,
                Material.PORKCHOP, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "Your boars enter a permanent rage state!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Boar Rage skill");
            }
        });

        SkillNode node27 = new SkillNode(27, "Pack Healing", "Heals your summons for 5% of your damage dealt", 5,
                Material.SPLASH_POTION, 1, player -> {
            player.sendMessage(ChatColor.GREEN + "You now heal your companions as you attack!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Pack Healing skill");
            }
        });

        // Add all nodes to the skill manager
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
        skillNodes.put(15, node15);
        skillNodes.put(16, node16);
        skillNodes.put(17, node17);
        skillNodes.put(18, node18);
        skillNodes.put(19, node19);
        skillNodes.put(20, node20);
        skillNodes.put(21, node21);
        skillNodes.put(22, node22);
        skillNodes.put(23, node23);
        skillNodes.put(24, node24);
        skillNodes.put(25, node25);
        skillNodes.put(26, node26);
        skillNodes.put(27, node27);
    }

    @Override
    protected void setupTreeStructure(SkillTree tree) {
        // Define root nodes
        tree.addRootNode(1);
        tree.addRootNode(2);
        tree.addRootNode(3);

        // Connect nodes based on the ascendancy skill tree structure
        // First level connections
        tree.connectNodes(1, 4);
        tree.connectNodes(2, 5);
        tree.connectNodes(3, 6);

        // Second level connections
        tree.connectNodes(4, 7);
        tree.connectNodes(5, 8);
        tree.connectNodes(5, 9);
        tree.connectNodes(6, 10);

        // Third level connections
        tree.connectNodes(7, 11);
        tree.connectNodes(7, 12);
        tree.connectNodes(8, 13);
        tree.connectNodes(9, 14);
        tree.connectNodes(10, 15);
        tree.connectNodes(10, 16);

        // Fourth level connections
        tree.connectNodes(11, 17);
        tree.connectNodes(12, 18);
        tree.connectNodes(13, 19);
        tree.connectNodes(15, 20);
        tree.connectNodes(16, 21);

        // Fifth level connections
        tree.connectNodes(17, 22);
        tree.connectNodes(18, 22);
        tree.connectNodes(19, 23);
        tree.connectNodes(20, 24);

        // Final connections
        tree.connectNodes(22, 25);
        tree.connectNodes(23, 26);
        tree.connectNodes(24, 27);
    }

    @Override
    protected void applySkillStats(Player player, int skillId, int purchaseCount) {
        // The actual implementation would apply different effects based on skill ID
        // This would interact with a pet/summon system that you'd need to implement

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Applied Beastmaster skill " + skillId +
                    " effect to " + player.getName() +
                    " (purchase " + purchaseCount + ")");
        }
    }

    @Override
    public boolean isMultiPurchaseDiscountSkill(int skillId) {
        // Beastmaster has no multi-purchase discount skills
        return false;
    }
}