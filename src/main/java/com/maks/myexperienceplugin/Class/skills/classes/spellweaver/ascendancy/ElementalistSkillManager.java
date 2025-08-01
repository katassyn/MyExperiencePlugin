package com.maks.myexperienceplugin.Class.skills.classes.spellweaver.ascendancy;

import com.maks.myexperienceplugin.Class.skills.base.BaseSkillManager;
import com.maks.myexperienceplugin.Class.skills.base.SkillNode;
import com.maks.myexperienceplugin.Class.skills.base.SkillTree;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;



public class ElementalistSkillManager extends BaseSkillManager {
    private static final int ID_OFFSET = 700000;
    private static final String[] NAMES = {
            "Elemental Touch",
            "Lingering Elements",
            "Elemental Power",
            "Lightning Jump",
            "Frost Slow",
            "Stone Burst",
            "Lightning Empower",
            "Frost Attack Slow",
            "Frost Heal",
            "Stone Stun",
            "Lightning Stacks",
            "Lightning Speed",
            "Frozen Weakness",
            "Stone Shield",
            "Elemental Defense",
            "Frost Kill Heal",
            "Shock Spread",
            "Resistance Break",
            "Frost Nova Low HP",
            "Stone Kill Power",
            "Stone Defense",
            "Lightning Overcharge",
            "Frost Slow Stack",
            "Stone Splash",
            "Elemental Explosion",
            "Elemental Surge",
            "Elemental Affinity"
    };

    private static final String[] DESCRIPTIONS = {
            "Your attacks have 10% chance to apply a random element (lightning, frost, stone) for 3s",
            "Elemental effects last 1s longer",
            "Each elemental effect grants 2% Spell damage while active",
            "Lightning jumps to an additional enemy applying shock",
            "Frost additionally slows movement by 10%",
            "Stone deals 3% of your damage as area damage",
            "Lightning hits grant 3% Spell damage for 5s",
            "Frost additionally reduces enemy attack speed by 10%",
            "Hitting a frosted target heals you for 2% max HP",
            "Stone effect has 10% extra chance to stun",
            "Each additional shock increases lightning damage by 5%",
            "Killing a shocked enemy grants 10% movement speed for 4s",
            "Frozen targets take 7% more damage",
            "Enemies with stone effect reduce your damage taken by 3% (max 15%)",
            "Each elemental effect grants 1% damage reduction (max 5%)",
            "Killing a frosted target restores 5% max HP",
            "Lightning hit has 20% chance to shock nearby foes",
            "Lightning hit reduces target resistance by 3% (max 12%)",
            "When HP <30% frost automatically applies to enemies within 4 blocks",
            "Killing a stoned target increases Spell damage by 7% for 5s",
            "Stone hits increase your defense by 5% for 4s",
            "Lightning effects can stack 3 times, each adding 4% damage",
            "Frost can stack, further slowing by 5% per stack (max 20%)",
            "Each stone hit deals an extra 5% of your damage as area damage",
            "On death release an elemental explosion dealing 15% of your damage within 5 blocks",
            "Activating an element increases Spell damage by 10% for 10s",
            "All elemental effects have an extra 15% chance to apply"
    };

    private static final int[] COSTS = {
            1,1,1,2,2,2,2,2,2,2,3,2,3,3,3,2,3,3,3,3,3,3,3,3,5,5,5
    };

    public ElementalistSkillManager(MyExperiencePlugin plugin) {
        super(plugin, "Elementalist");
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

