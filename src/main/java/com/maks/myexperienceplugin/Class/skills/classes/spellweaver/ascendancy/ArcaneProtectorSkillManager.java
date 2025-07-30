package com.maks.myexperienceplugin.Class.skills.classes.spellweaver.ascendancy;

import com.maks.myexperienceplugin.Class.skills.base.BaseSkillManager;
import com.maks.myexperienceplugin.Class.skills.base.SkillNode;
import com.maks.myexperienceplugin.Class.skills.base.SkillTree;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;


public class ArcaneProtectorSkillManager extends BaseSkillManager {
    private static final int ID_OFFSET = 900000;
    private static final String[] NAMES = {
            "Magic Barrier Chance",
            "Damage Response",
            "Defensive Presence",
            "Weakening Strike",
            "Half-Health Defense",
            "Attack Slow",
            "Slayer's Guard",
            "Periodic Barrier",
            "Ally Guard",
            "Critical Fortitude",
            "Barrier Power",
            "Evasive Shield",
            "Desperate Barrier",
            "Ally Defense Boost",
            "Ally Barrier",
            "Barrier Speed",
            "Area Barrier",
            "Mass Barrier",
            "Empowered Hit",
            "Negative Guard",
            "Barrier Fortitude",
            "Posthumous Shield",
            "Barrier Reflection",
            "Shared Shield",
            "Last Stand",
            "Barrier Spell Boost",
            "Cheat Death"
    };

    private static final String[] DESCRIPTIONS = {
            "Hits have a 10% chance to create a magic barrier reducing the next damage taken by 5%",
            "After taking damage you gain 3% defense for 4s (stacks to 12%)",
            "Your presence increases nearby allies' defense by 2% within 5 blocks",
            "Your attacks have a 10% chance to reduce enemy damage by 5% for 3s",
            "When below 50% HP your defense increases by an additional 5%",
            "Hits reduce enemy attack speed by 5% for 4s (stacks to 15%)",
            "Killing an enemy grants 3% damage reduction for 5s (stacks to 12%)",
            "Every 5s you generate a magic barrier reducing the next damage by 7%",
            "Allies within 5 blocks take 5% less damage",
            "After taking critical damage gain 10% defense for 4s",
            "Each active barrier grants 2% Spell damage for 5s (stacks to 10%)",
            "Dodging damage creates a barrier blocking 10% damage for 4s",
            "When HP drops below 30% you instantly gain a barrier reducing damage by 15% for 5s",
            "Each ally within 5 blocks increases your defense by 1% (max 5%)",
            "Your attacks have 10% chance to place a barrier on the lowest HP ally (5% reduction, 4s)",
            "While a barrier is active you gain 5% movement speed",
            "Barrier on you reduces further area damage by 10%",
            "Killing an enemy grants allies within 8 blocks a 5% barrier for 4s",
            "After activating a barrier your next hit deals 5% extra damage",
            "Barrier also reduces negative effect duration on you by 10%",
            "Every third barrier increases your max HP by 2% for 10s (stacks to 10%)",
            "Upon death you create a shield protecting allies from 20% damage for 5s (5 min cooldown)",
            "Barriers have 15% chance to reflect 10% damage back",
            "While your barrier is active allies receive 3% additional damage reduction",
            "When below 15% HP gain an indestructible barrier for 3s blocking all damage (3 min cooldown)",
            "Each barrier increases your Spell damage by 5% while it lasts",
            "Survive fatal damage once every 5 min with a barrier worth 30% HP for 5s"
    };

    private static final int[] COSTS = {
            1,1,1,2,2,2,2,2,2,2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,5,5,5
    };

    public ArcaneProtectorSkillManager(MyExperiencePlugin plugin) {
        super(plugin, "ArcaneProtector");
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

