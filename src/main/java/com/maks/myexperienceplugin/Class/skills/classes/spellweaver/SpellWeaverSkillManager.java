package com.maks.myexperienceplugin.Class.skills.classes.spellweaver;

import com.maks.myexperienceplugin.Class.skills.base.BaseSkillManager;
import com.maks.myexperienceplugin.Class.skills.base.SkillNode;
import com.maks.myexperienceplugin.Class.skills.base.SkillTree;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SpellWeaverSkillManager extends BaseSkillManager {

    public SpellWeaverSkillManager(MyExperiencePlugin plugin) {
        super(plugin, "SpellWeaver");
    }

    @Override
    protected void initializeSkills() {
        // Create nodes based on SpellWeaver.md
        SkillNode node1 = new SkillNode(1, "Arcane Strike", "Every 10s, next attack deals +5 dmg and ignores 10% armor", 1,
                Material.ENCHANTED_BOOK, 1, player -> {
            player.sendMessage(ChatColor.BLUE + "Your hands crackle with arcane energy!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Arcane Strike skill");
            }
        });

        SkillNode node2 = new SkillNode(2, "Mana Shield", "10% chance attacks reduce next hit by 25% (10s cd)", 1,
                Material.LIGHT_BLUE_STAINED_GLASS, 1, player -> {
            player.sendMessage(ChatColor.BLUE + "A protective aura surrounds you!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Mana Shield skill");
            }
        });

        SkillNode node3 = new SkillNode(3, "Spell Mastery", "+1% spell damage", 1,
                Material.BLAZE_ROD, 1, player -> {
            player.sendMessage(ChatColor.BLUE + "Your spells grow stronger!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Spell Mastery skill");
            }
        });

        SkillNode node4 = new SkillNode(4, "Molten Shield", "When hp<50%, gain absorption shield and reflect 5 dmg (30s cd)", 2,
                Material.MAGMA_BLOCK, 1, player -> {
            player.sendMessage(ChatColor.BLUE + "You summon a protective shield of molten energy!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Molten Shield skill");
            }
        });

        SkillNode node5 = new SkillNode(5, "Arcane Momentum", "+3% movement speed after 5s without damage", 2,
                Material.ENDER_PEARL, 1, player -> {
            player.sendMessage(ChatColor.BLUE + "You flow with arcane energy!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Arcane Momentum skill");
            }
        });

        SkillNode node6 = new SkillNode(6, "Frost Bolt", "Every 15s, next attack applies frost for 7 dmg and 10% slow", 2,
                Material.BLUE_ICE, 1, player -> {
            player.sendMessage(ChatColor.BLUE + "Your hands glow with frost energy!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Frost Bolt skill");
            }
        });

        SkillNode node7 = new SkillNode(7, "Fire Resistance", "Gain permanent Fire Resistance effect", 3,
                Material.FIRE_CHARGE, 1, player -> {
            player.sendMessage(ChatColor.BLUE + "The flames no longer harm you!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Fire Resistance skill");
            }
        });

        SkillNode node8 = new SkillNode(8, "Spell Critical", "+1% chance to double spell damage", 2,
                Material.NETHER_STAR, 1, player -> {
            player.sendMessage(ChatColor.BLUE + "Your spells may strike with devastating force!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Spell Critical skill");
            }
        });

        SkillNode node9 = new SkillNode(9, "Frost Nova", "Every third hit slows enemy by 15% for 2s", 2,
                Material.SNOW_BLOCK, 1, player -> {
            player.sendMessage(ChatColor.BLUE + "Your attacks chill your enemies!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Frost Nova skill");
            }
        });

        SkillNode node10 = new SkillNode(10, "Arcane Bolt", "+2 spell damage", 2,
                Material.AMETHYST_SHARD, 1, player -> {
            player.sendMessage(ChatColor.BLUE + "Your arcane bolts strengthen!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Arcane Bolt skill");
            }
        });

        SkillNode node11 = new SkillNode(11, "Advanced Spellcasting", "+1% spell damage", 3,
                Material.END_CRYSTAL, 2, player -> {
            player.sendMessage(ChatColor.BLUE + "Your mastery of spells deepens!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Advanced Spellcasting skill");
            }
        });

        SkillNode node12 = new SkillNode(12, "Spell Focus", "Each hit on same target deals 2% more damage", 2,
                Material.DRAGON_BREATH, 1, player -> {
            player.sendMessage(ChatColor.BLUE + "Your focus on a single target intensifies your spells!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Spell Focus skill");
            }
        });

        SkillNode node13 = new SkillNode(13, "Arcane Surge", "+5 spell damage", 2,
                Material.SPECTRAL_ARROW, 1, player -> {
            player.sendMessage(ChatColor.BLUE + "Your arcane power surges!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Arcane Surge skill");
            }
        });

        SkillNode node14 = new SkillNode(14, "Arcane Explosion", "After 5 consecutive hits, cause 10 dmg explosion (15s cd)", 3,
                Material.FIREWORK_ROCKET, 1, player -> {
            player.sendMessage(ChatColor.BLUE + "You harness explosive arcane energy!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Arcane Explosion skill");
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

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Initialized SpellWeaver skill manager with " + skillNodes.size() + " skills");
        }
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
        switch (skillId) {
            case 3: // +1% spell dmg
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +1% spell damage to " + player.getName());
                }
                break;
            case 10: // +2 spell dmg
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +2 spell damage to " + player.getName());
                }
                break;
            case 11: // +1% spell dmg (1/2)
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +" + purchaseCount + "% spell damage to " + player.getName() +
                            " (purchase " + purchaseCount + "/2)");
                }
                break;
            case 13: // +5 spell dmg
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied +5 spell damage to " + player.getName());
                }
                break;
            default:
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Skill " + skillId + " effect will be handled by event handler");
                }
                break;
        }
    }

    @Override
    public boolean isMultiPurchaseDiscountSkill(int skillId) {
        // Only Advanced Spellcasting (11) has the (1/2) notation
        return skillId == 11;
    }
}