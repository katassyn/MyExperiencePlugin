package com.maks.myexperienceplugin.Class.skills.base;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.entity.Player;

import java.util.*;

public class SkillTree {
    private final String treeType; // "basic" or "ascendancy"
    private final String className;
    private final Map<Integer, SkillNode> nodes;
    private final Set<Integer> rootNodeIds;
    private final int debuggingFlag = 1; // Debugging flag directly in the class

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
     * 2. At least one of its connected nodes is already purchased, or
     * 3. It is connected to at least one already purchased node
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


        // Check if the target node is connected to any purchased node
        for (SkillNode connectedNode : targetNode.getConnectedNodes()) {
            if (purchasedNodes.contains(connectedNode.getId())) {
                return true;
            }
        }

        // Check if ANY purchased node is connected to our target
        // We need to check inverse connections (find nodes that have the target as a connection)
        for (Map.Entry<Integer, SkillNode> entry : nodes.entrySet()) {
            SkillNode node = entry.getValue();

            // Skip if this node isn't purchased
            if (!purchasedNodes.contains(node.getId())) {
                continue;
            }

            // Check if this purchased node connects to our target
            for (SkillNode connectedNode : node.getConnectedNodes()) {
                if (connectedNode.getId() == nodeId) {
                    return true;
                }
            }
        }

        // If we've checked all purchased nodes and none connect to our target
        return false;
    }
}
