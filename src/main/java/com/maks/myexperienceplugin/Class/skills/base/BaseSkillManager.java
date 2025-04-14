package com.maks.myexperienceplugin.Class.skills.base;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseSkillManager {
    protected final MyExperiencePlugin plugin;
    protected final int debuggingFlag = 1;
    protected final String className;

    // Map of skill ID to SkillNode for this class
    protected final Map<Integer, SkillNode> skillNodes = new HashMap<>();

    public BaseSkillManager(MyExperiencePlugin plugin, String className) {
        this.plugin = plugin;
        this.className = className;

        // Initialize skills
        initializeSkills();

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Initialized " + className + " skill manager with " +
                    skillNodes.size() + " skills");
        }
    }

    /**
     * Initialize all skills for this class
     */
    protected abstract void initializeSkills();

    /**
     * Create a skill tree for this class
     */
    public SkillTree createSkillTree(String treeType) {
        SkillTree tree = new SkillTree(treeType, className);

        // Add all skills to the tree
        for (SkillNode node : skillNodes.values()) {
            tree.addNode(node);
        }

        // Set up root nodes and connections
        setupTreeStructure(tree);

        return tree;
    }

    /**
     * Set up the tree structure (root nodes and connections)
     */
    protected abstract void setupTreeStructure(SkillTree tree);

    /**
     * Handle the effects of a skill when it's purchased
     */
    public void applySkillEffects(Player player, int skillId, int purchaseCount) {
        SkillNode node = skillNodes.get(skillId);
        if (node != null) {
            // Apply effect through the node
            node.applyEffect(player);

            // Apply specific stat bonuses for this skill
            applySkillStats(player, skillId, purchaseCount);
        }
    }

    /**
     * Apply stat bonuses for a specific skill
     */
    protected abstract void applySkillStats(Player player, int skillId, int purchaseCount);

    /**
     * Check if this skill has the special (1/X) cost structure
     */
    public abstract boolean isMultiPurchaseDiscountSkill(int skillId);

    /**
     * Get all skill nodes for this class
     */
    public Map<Integer, SkillNode> getSkillNodes() {
        return skillNodes;
    }

    /**
     * Get a specific skill node by ID
     */
    public SkillNode getSkillNode(int skillId) {
        return skillNodes.get(skillId);
    }

    /**
     * Get the class name
     */
    public String getClassName() {
        return className;
    }
}