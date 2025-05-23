package com.maks.myexperienceplugin.Class.skills.classes.dragonknight.ascendancy;

import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.base.BaseSkillManager;
import com.maks.myexperienceplugin.Class.skills.base.SkillNode;
import com.maks.myexperienceplugin.Class.skills.base.SkillTree;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class FlameWardenSkillManager extends BaseSkillManager {

    // Use ID range starting from 300000 to avoid conflicts with base classes, Beastmaster (100000), and Berserker (200000)
    private static final int ID_OFFSET = 300000;

    public FlameWardenSkillManager(MyExperiencePlugin plugin) {
        super(plugin, "FlameWarden");
    }

    @Override
    protected void initializeSkills() {
        // Create nodes based on Specific_plugin_description.md
        SkillNode node1 = new SkillNode(ID_OFFSET + 1, "Ignite Chance", "Attacks have 15% chance to ignite enemies for 3 seconds", 1,
                Material.FLINT_AND_STEEL, 2, player -> {
            player.sendMessage(ChatColor.GOLD + "Your attacks now have a chance to ignite enemies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Ignite Chance skill");
            }
        });

        SkillNode node2 = new SkillNode(ID_OFFSET + 2, "Burning Damage", "+10% damage against burning enemies", 1,
                Material.BLAZE_POWDER, 2, player -> {
            player.sendMessage(ChatColor.GOLD + "You deal more damage to burning enemies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Burning Damage skill");
            }
        });

        SkillNode node3 = new SkillNode(ID_OFFSET + 3, "Fire Resistance", "Gain fire resistance potion effect infinity", 1,
                Material.MAGMA_CREAM, 1, player -> {
            player.sendMessage(ChatColor.GOLD + "You are now resistant to fire!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Fire Resistance skill");
            }
        });

        SkillNode node4 = new SkillNode(ID_OFFSET + 4, "Desperate Ignition", "When hp<50%, your attacks have +10% chance to ignite enemies", 2,
                Material.FIRE_CHARGE, 1, player -> {
            player.sendMessage(ChatColor.GOLD + "Your desperation fuels your flames!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Desperate Ignition skill");
            }
        });

        SkillNode node5 = new SkillNode(ID_OFFSET + 5, "Flame Shield", "Burning enemies deal -15% damage to you", 2,
                Material.SHIELD, 2, player -> {
            player.sendMessage(ChatColor.GOLD + "Burning enemies deal less damage to you!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Flame Shield skill");
            }
        });

        SkillNode node6 = new SkillNode(ID_OFFSET + 6, "Fire Nova", "Taking damage has 20% chance to trigger a fire nova dealing 30 damage to nearby enemies and ignite them", 2,
                Material.FIRE_CHARGE, 1, player -> {
            player.sendMessage(ChatColor.GOLD + "You can now release a nova of fire when taking damage!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Fire Nova skill");
            }
        });

        SkillNode node7 = new SkillNode(ID_OFFSET + 7, "Burning Aura", "+5% damage for each burning enemy within 10 blocks (max +15%)", 2,
                Material.BLAZE_ROD, 1, player -> {
            player.sendMessage(ChatColor.GOLD + "Nearby burning enemies fuel your power!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Burning Aura skill");
            }
        });

        SkillNode node8 = new SkillNode(ID_OFFSET + 8, "Spreading Flames", "Ignited enemies spread burn to nearby enemies within 3 blocks (30% chance)", 2,
                Material.CAMPFIRE, 1, player -> {
            player.sendMessage(ChatColor.GOLD + "Your flames now spread between enemies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Spreading Flames skill");
            }
        });

        SkillNode node9 = new SkillNode(ID_OFFSET + 9, "Critical Burn", "Critical hits deal additional 5 fire damage over 3 seconds", 2,
                Material.BLAZE_POWDER, 2, player -> {
            player.sendMessage(ChatColor.GOLD + "Your critical hits now cause additional burning!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Critical Burn skill");
            }
        });

        SkillNode node10 = new SkillNode(ID_OFFSET + 10, "Surrounded Defense", "When surrounded by 3+ enemies, gain +15% defense", 2,
                Material.IRON_BARS, 1, player -> {
            player.sendMessage(ChatColor.GOLD + "You gain defensive power when surrounded!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Surrounded Defense skill");
            }
        });

        SkillNode node11 = new SkillNode(ID_OFFSET + 11, "Splash Damage", "Attacks deal splash damage (20% of damage) to enemies within 2 blocks of target", 2,
                Material.WATER_BUCKET, 2, player -> {
            player.sendMessage(ChatColor.GOLD + "Your attacks now splash to nearby enemies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Splash Damage skill");
            }
        });

        SkillNode node12 = new SkillNode(ID_OFFSET + 12, "Burning Momentum", "After killing a burning enemy, gain +5% damage and movement speed for 5 seconds (stacks up to 3 times)", 3,
                Material.SOUL_CAMPFIRE, 1, player -> {
            player.sendMessage(ChatColor.GOLD + "Killing burning enemies fuels your momentum!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Burning Momentum skill");
            }
        });

        SkillNode node13 = new SkillNode(ID_OFFSET + 13, "Fire Healing", "Standing in fire heals you instead of dealing damage", 3,
                Material.GOLDEN_APPLE, 1, player -> {
            player.sendMessage(ChatColor.GOLD + "Fire now heals you instead of harming you!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Fire Healing skill");
            }
        });

        SkillNode node14 = new SkillNode(ID_OFFSET + 14, "Third Strike", "Every third attack on the same enemy deals +40% damage as fire damage", 3,
                Material.NETHERITE_SWORD, 1, player -> {
            player.sendMessage(ChatColor.GOLD + "Your third strike on an enemy is empowered with fire!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Third Strike skill");
            }
        });

        SkillNode node15 = new SkillNode(ID_OFFSET + 15, "Desperate Nova", "When hp<30%, ignite all enemies within 5 blocks (30 second cooldown)", 3,
                Material.NETHER_STAR, 1, player -> {
            player.sendMessage(ChatColor.GOLD + "When near death, you release a massive fire nova!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Desperate Nova skill");
            }
        });

        SkillNode node16 = new SkillNode(ID_OFFSET + 16, "Extended Burn", "+15% chance for burning duration to extend by 2 seconds whenever the enemy takes damage", 3,
                Material.CLOCK, 1, player -> {
            player.sendMessage(ChatColor.GOLD + "Your burns have a chance to last longer!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Extended Burn skill");
            }
        });

        SkillNode node17 = new SkillNode(ID_OFFSET + 17, "Burning Presence", "While you have 3+ burning enemies nearby, gain +20% damage and +10% defense", 3,
                Material.BEACON, 1, player -> {
            player.sendMessage(ChatColor.GOLD + "Multiple burning enemies empower you!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Burning Presence skill");
            }
        });

        SkillNode node18 = new SkillNode(ID_OFFSET + 18, "Flame Reflection", "When blocking an attack, reflect 25% of damage as fire damage", 3,
                Material.SHIELD, 2, player -> {
            player.sendMessage(ChatColor.GOLD + "Your blocks now reflect fire damage!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Flame Reflection skill");
            }
        });

        SkillNode node19 = new SkillNode(ID_OFFSET + 19, "Hotter Flames", "Your fires burn 30% hotter (enemies take +30% burning damage)", 3,
                Material.LAVA_BUCKET, 1, player -> {
            player.sendMessage(ChatColor.GOLD + "Your flames burn hotter than before!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Hotter Flames skill");
            }
        });

        SkillNode node20 = new SkillNode(ID_OFFSET + 20, "Critical Explosion", "Critical hits create a fire explosion dealing 100 area damage", 3,
                Material.TNT, 2, player -> {
            player.sendMessage(ChatColor.GOLD + "Your critical hits now cause fire explosions!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Critical Explosion skill");
            }
        });

        SkillNode node21 = new SkillNode(ID_OFFSET + 21, "Burn Damage Scaling", "+1% damage for each second an enemy burns (max +10% per enemy)", 3,
                Material.REDSTONE_TORCH, 1, player -> {
            player.sendMessage(ChatColor.GOLD + "Your damage increases the longer enemies burn!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Burn Damage Scaling skill");
            }
        });

        SkillNode node22 = new SkillNode(ID_OFFSET + 22, "Burning Protection", "Each burning enemy reduces your damage taken by 3% (max 15%)", 3,
                Material.NETHERITE_CHESTPLATE, 1, player -> {
            player.sendMessage(ChatColor.GOLD + "Burning enemies reduce damage you take!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Burning Protection skill");
            }
        });

        SkillNode node23 = new SkillNode(ID_OFFSET + 23, "Last Stand", "After taking a hit that reduces you below 40% hp, emit a fire nova dealing 100 damage to all nearby enemies and burn them", 3,
                Material.TOTEM_OF_UNDYING, 1, player -> {
            player.sendMessage(ChatColor.GOLD + "When gravely wounded, you release a powerful fire nova!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Last Stand skill");
            }
        });

        SkillNode node24 = new SkillNode(ID_OFFSET + 24, "Burning Retaliation", "Burning enemies have 20% chance to spread fire to their attackers", 3,
                Material.FIRE_CORAL, 1, player -> {
            player.sendMessage(ChatColor.GOLD + "Burning enemies may spread fire to those who attack them!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Burning Retaliation skill");
            }
        });

        SkillNode node25 = new SkillNode(ID_OFFSET + 25, "Embrace the Flame", "You deal +25% damage while burning yourself after you burn enemy", 5,
                Material.BLAZE_SPAWN_EGG, 1, player -> {
            player.sendMessage(ChatColor.GOLD + "You embrace the flame, gaining power while burning!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Embrace the Flame skill");
            }
        });

        SkillNode node26 = new SkillNode(ID_OFFSET + 26, "Opening Strike", "Attacks against enemies above 80% hp have +25% chance to critically hit", 5,
                Material.DIAMOND_SWORD, 2, player -> {
            player.sendMessage(ChatColor.GOLD + "Your opening strikes are more likely to critically hit!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Opening Strike skill");
            }
        });

        SkillNode node27 = new SkillNode(ID_OFFSET + 27, "Phoenix Rebirth", "When killed, explode in flames dealing massive damage 5000% to nearby enemies and igniting them (5 min cooldown)", 5,
                Material.TOTEM_OF_UNDYING, 1, player -> {
            player.sendMessage(ChatColor.GOLD + "Upon death, you will be reborn in a massive explosion of flame!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Phoenix Rebirth skill");
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
            plugin.getLogger().info("Initialized all 27 FlameWarden skills with ID offset " + ID_OFFSET);
        }
    }

    @Override
    protected void setupTreeStructure(SkillTree tree) {
        // Define root nodes
        tree.addRootNode(ID_OFFSET + 1);
        tree.addRootNode(ID_OFFSET + 2);
        tree.addRootNode(ID_OFFSET + 3);

        // Path 1: Ignite Path
        tree.connectNodes(ID_OFFSET + 1, ID_OFFSET + 4);
        tree.connectNodes(ID_OFFSET + 4, ID_OFFSET + 7);
        tree.connectNodes(ID_OFFSET + 7, ID_OFFSET + 12);
        tree.connectNodes(ID_OFFSET + 12, ID_OFFSET + 17);
        tree.connectNodes(ID_OFFSET + 17, ID_OFFSET + 21);
        tree.connectNodes(ID_OFFSET + 21, ID_OFFSET + 25);

        // Path 2: Burning Damage Path
        tree.connectNodes(ID_OFFSET + 2, ID_OFFSET + 5);
        tree.connectNodes(ID_OFFSET + 5, ID_OFFSET + 8);
        tree.connectNodes(ID_OFFSET + 5, ID_OFFSET + 9);
        tree.connectNodes(ID_OFFSET + 8, ID_OFFSET + 13);
        tree.connectNodes(ID_OFFSET + 9, ID_OFFSET + 14);
        tree.connectNodes(ID_OFFSET + 13, ID_OFFSET + 19);
        tree.connectNodes(ID_OFFSET + 14, ID_OFFSET + 20);
        tree.connectNodes(ID_OFFSET + 19, ID_OFFSET + 24);
        tree.connectNodes(ID_OFFSET + 20, ID_OFFSET + 26);
        tree.connectNodes(ID_OFFSET + 24, ID_OFFSET + 26);

        // Path 3: Fire Defense Path
        tree.connectNodes(ID_OFFSET + 3, ID_OFFSET + 6);
        tree.connectNodes(ID_OFFSET + 6, ID_OFFSET + 10);
        tree.connectNodes(ID_OFFSET + 6, ID_OFFSET + 11);
        tree.connectNodes(ID_OFFSET + 10, ID_OFFSET + 15);
        tree.connectNodes(ID_OFFSET + 11, ID_OFFSET + 16);
        tree.connectNodes(ID_OFFSET + 15, ID_OFFSET + 18);
        tree.connectNodes(ID_OFFSET + 16, ID_OFFSET + 22);
        tree.connectNodes(ID_OFFSET + 18, ID_OFFSET + 23);
        tree.connectNodes(ID_OFFSET + 22, ID_OFFSET + 23);
        tree.connectNodes(ID_OFFSET + 23, ID_OFFSET + 27);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Configured FlameWarden skill tree with " +
                    tree.getRootNodeIds().size() + " root nodes and complete connection paths");
        }
    }

    @Override
    protected void applySkillStats(Player player, int skillId, int purchaseCount) {
        // Remove ID_OFFSET to get the original skill ID for switch statement
        int originalId = skillId - ID_OFFSET;
        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);

        switch (originalId) {
            case 2: // +10% damage against burning enemies (1/2)
                // This is handled dynamically in FlameWardenSkillEffectsHandler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied burning damage bonus logic to " + player.getName());
                }
                break;

            case 3: // Gain fire resistance potion effect infinity
                // This is handled in FlameWardenSkillEffectsHandler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied fire resistance effect to " + player.getName());
                }
                break;

            case 5: // Burning enemies deal -15% damage to you (1/2)
                // This is handled dynamically in FlameWardenSkillEffectsHandler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied damage reduction from burning enemies to " + player.getName());
                }
                break;

            case 9: // Critical hits deal additional 5 fire damage over 3 seconds (1/2)
                // This is handled dynamically in FlameWardenSkillEffectsHandler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied critical burn effect to " + player.getName());
                }
                break;

            case 11: // Attacks deal splash damage (20% of damage) to enemies within 2 blocks of target (1/2)
                // This is handled dynamically in FlameWardenSkillEffectsHandler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied splash damage effect to " + player.getName());
                }
                break;

            case 18: // When blocking an attack, reflect 25% of damage as fire damage (1/2)
                // This is handled dynamically in FlameWardenSkillEffectsHandler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied flame reflection effect to " + player.getName());
                }
                break;

            case 20: // Critical hits create a fire explosion dealing 100 area damage (1/2)
                // This is handled dynamically in FlameWardenSkillEffectsHandler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied critical explosion effect to " + player.getName());
                }
                break;

            case 26: // Attacks against enemies above 80% hp have +25% chance to critically hit (1/2)
                // This is handled dynamically in FlameWardenSkillEffectsHandler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied opening strike effect to " + player.getName());
                }
                break;

            // All other skills are handled dynamically in FlameWardenSkillEffectsHandler
            default:
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Skill " + originalId + " will be handled by FlameWardenSkillEffectsHandler");
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
        return originalId == 1 || originalId == 2 || originalId == 5 || originalId == 9 || 
               originalId == 11 || originalId == 18 || originalId == 20 || originalId == 26;
    }
}
