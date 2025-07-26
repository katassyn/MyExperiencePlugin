package com.maks.myexperienceplugin.Class.skills.base;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.entity.Player;

import java.util.*;

public class SkillTree {
    private final String treeType; // "basic" or "ascendancy"
    private final String className;
    private final Map<Integer, SkillNode> nodes;
    private final Set<Integer> rootNodeIds;
    private final int debuggingFlag = 0; // Debugging flag directly in the class

    public SkillTree(String treeType, String className) {
        this.treeType = treeType;
        this.className = className;
        this.nodes = new HashMap<>();
        this.rootNodeIds = new HashSet<>();
    }

    public void addNode(SkillNode node) {
        nodes.put(node.getId(), node);
    }

    public void addRootNode(int nodeId) {
        if (nodes.containsKey(nodeId)) {
            rootNodeIds.add(nodeId);
        }
    }

    public void connectNodes(int fromId, int toId) {
        SkillNode from = nodes.get(fromId);
        SkillNode to = nodes.get(toId);

        if (from != null && to != null) {
            from.addConnection(to);
        }
    }

    public SkillNode getNode(int id) {
        return nodes.get(id);
    }

    public Collection<SkillNode> getAllNodes() {
        return nodes.values();
    }

    public Set<Integer> getRootNodeIds() {
        return rootNodeIds;
    }

    public String getTreeType() {
        return treeType;
    }

    public String getClassName() {
        return className;
    }

    /**
     * Check if a node can be purchased by the player
     * A node can be purchased if:
     * 1. It's a root node, or
     * 2. The player has purchased at least one of its parent nodes
     */
    public boolean canPurchaseNode(Player player, int nodeId, Set<Integer> purchasedNodes) {
        // Root nodes can always be purchased
        if (rootNodeIds.contains(nodeId)) {
            return true;
        }

        // Get the node we're checking
        SkillNode targetNode = nodes.get(nodeId);
        if (targetNode == null) {
            return false;
        }

        // Find nodes that have the target node as a connection
        // These are the parent nodes that must be purchased first
        boolean hasValidParent = false;

        for (Map.Entry<Integer, SkillNode> entry : nodes.entrySet()) {
            int parentId = entry.getKey();
            SkillNode parentNode = entry.getValue();

            // Skip if this node doesn't connect to our target
            boolean isParent = false;
            for (SkillNode connectedNode : parentNode.getConnectedNodes()) {
                if (connectedNode.getId() == nodeId) {
                    isParent = true;
                    break;
                }
            }

            if (!isParent) {
                continue;
            }

            // If this is a parent and it's purchased, the node can be purchased
            if (purchasedNodes.contains(parentId)) {
                hasValidParent = true;

                if (debuggingFlag == 1) {
                    MyExperiencePlugin.getInstance().getLogger().info("Node " + nodeId + " can be purchased because parent node " + 
                            parentId + " is purchased");
                }

                break;
            }
        }

        return hasValidParent;
    }
}
