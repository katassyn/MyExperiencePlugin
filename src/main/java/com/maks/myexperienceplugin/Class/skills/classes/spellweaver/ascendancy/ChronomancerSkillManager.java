package com.maks.myexperienceplugin.Class.skills.classes.spellweaver.ascendancy;

import com.maks.myexperienceplugin.Class.skills.base.BaseSkillManager;
import com.maks.myexperienceplugin.Class.skills.base.SkillNode;
import com.maks.myexperienceplugin.Class.skills.base.SkillTree;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;



public class ChronomancerSkillManager extends BaseSkillManager {
    private static final int ID_OFFSET = 800000;
    private static final String[] NAMES = {
            "Slow Strike",
            "Speed Surge",
            "Evasive Power",
            "Attack Hinder",
            "Triple Slow",
            "Kill Heal",
            "Focused Assault",
            "Survival Dampening",
            "Dodge Defense",
            "Calm Recovery",
            "Slowed Heal",
            "Rapid Strikes",
            "Weakening Slow",
            "Kill Purge",
            "Panic Slow",
            "Dodge Power",
            "Momentum Build",
            "Dodge Explosion",
            "Hit Evasion Boost",
            "Slow Damage Boost",
            "Rewind Death",
            "Cooldown Reset",
            "Dodge Mitigation",
            "Extended Slow",
            "Temporal Barrier",
            "Time Freeze",
            "Post-Dodge Power"
    };

    private static final String[] DESCRIPTIONS = {
            "Your attacks have 10% chance to slow the enemy by 10% for 3s",
            "Each hit grants 2% movement speed for 3s (up to 10%)",
            "Dodging damage makes your next hit deal 5% more damage",
            "Hits reduce enemy attack speed by 5% for 4s (stack to 15%)",
            "Every third hit adds another 5% slow (max 20%)",
            "Killing an enemy restores 3% HP over 4s",
            "Repeated attacks on the same target grant 2% Spell damage (up to 10%)",
            "When below 50% HP enemy damage is reduced by 7%",
            "Dodging increases your defense by 5% for 5s (stack to 15%)",
            "After 4s without damage you heal 5% HP",
            "Hitting slowed enemies restores 1% HP (max 5% per 5s)",
            "Hits grant 3% attack speed for 3s (stack to 12%)",
            "Slowed enemies deal 5% less damage to you",
            "Killing an enemy shortens negative effects on you by 1s",
            "When HP <30% you slow all enemies within 5 blocks by 20% for 5s",
            "Each dodge grants 3% Spell damage for 5s (stack to 15%)",
            "Hits grant 1% Spell damage for 5s (stack to 10%)",
            "After dodging, your next hit deals area damage equal to 7% of your damage",
            "Being hit increases your dodge chance by 10% for 3s",
            "Slowed enemies take 5% additional damage",
            "On death you rewind 3s restoring 20% HP (5 min cooldown)",
            "Hits have 10% chance to reset cooldowns of other Chronomancer effects",
            "Each dodge reduces next damage by 5% for 4s (stack to 15%)",
            "Hitting slowed targets extends the slow by 1s",
            "At HP <15% activate Temporal Barrier reducing all damage by 50% for 5s",
            "Your hits have 15% chance to freeze time around the enemy for 1s",
            "After dodging gain 15% Spell damage for 5s"
    };

    private static final int[] COSTS = {
            1,1,1,2,2,2,2,2,2,2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,5,5,5
    };

    public ChronomancerSkillManager(MyExperiencePlugin plugin) {
        super(plugin, "Chronomancer");
    }

    @Override
    protected void initializeSkills() {
        for (int i = 0; i < DESCRIPTIONS.length; i++) {
            SkillNode node = new SkillNode(
                    ID_OFFSET + i + 1,
                    NAMES[i],
                    DESCRIPTIONS[i],
                    COSTS[i],
                    Material.BOOK,
                    1,
                    player -> {}
            );
            skillNodes.put(ID_OFFSET + i + 1, node);
        }
    }

    @Override
    protected void setupTreeStructure(SkillTree tree) {
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
        tree.connectNodes(ID_OFFSET + 5, ID_OFFSET + 9);
        tree.connectNodes(ID_OFFSET + 6, ID_OFFSET + 10);

        // Poziom 3->4
        tree.connectNodes(ID_OFFSET + 7, ID_OFFSET + 11);
        tree.connectNodes(ID_OFFSET + 7, ID_OFFSET + 12);
        tree.connectNodes(ID_OFFSET + 8, ID_OFFSET + 13);
        tree.connectNodes(ID_OFFSET + 9, ID_OFFSET + 14);
        tree.connectNodes(ID_OFFSET + 10, ID_OFFSET + 15);
        tree.connectNodes(ID_OFFSET + 10, ID_OFFSET + 16);

        // Poziom 4->5
        tree.connectNodes(ID_OFFSET + 11, ID_OFFSET + 17);
        tree.connectNodes(ID_OFFSET + 12, ID_OFFSET + 18);
        tree.connectNodes(ID_OFFSET + 13, ID_OFFSET + 19);
        tree.connectNodes(ID_OFFSET + 14, ID_OFFSET + 19);
        tree.connectNodes(ID_OFFSET + 15, ID_OFFSET + 20);
        tree.connectNodes(ID_OFFSET + 16, ID_OFFSET + 21);

        // Poziom 5->6
        tree.connectNodes(ID_OFFSET + 17, ID_OFFSET + 22);
        tree.connectNodes(ID_OFFSET + 18, ID_OFFSET + 22);
        tree.connectNodes(ID_OFFSET + 19, ID_OFFSET + 23);
        tree.connectNodes(ID_OFFSET + 20, ID_OFFSET + 24);
        tree.connectNodes(ID_OFFSET + 21, ID_OFFSET + 24);

        // Poziom 6->7
        tree.connectNodes(ID_OFFSET + 22, ID_OFFSET + 25);
        tree.connectNodes(ID_OFFSET + 23, ID_OFFSET + 26);
        tree.connectNodes(ID_OFFSET + 24, ID_OFFSET + 27);
    }

    @Override
    protected void applySkillStats(Player player, int skillId, int purchaseCount) {
        plugin.getSkillEffectsHandler().refreshPlayerStats(player);
    }

    @Override
    public boolean isMultiPurchaseDiscountSkill(int skillId) {
        return false;
    }

    // No dynamic skill loading needed
}

