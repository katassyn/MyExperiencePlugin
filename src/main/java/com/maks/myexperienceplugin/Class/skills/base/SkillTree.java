package com.maks.myexperienceplugin.Class.skills.base;

import org.bukkit.entity.Player;

import java.util.*;

public class SkillTree {
    private final String treeType; // "basic" or "ascendancy"
    private final String className;
    private final Map<Integer, SkillNode> nodes;
    private final Set<Integer> rootNodeIds;

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
     * 2. At least one of its connected nodes is already purchased
     */
    public boolean canPurchaseNode(Player player, int nodeId, Set<Integer> purchasedNodes) {
        // Root nodes can always be purchased
        if (rootNodeIds.contains(nodeId)) {
            return true;
        }

        // Check if this node is connected to any purchased node
        for (SkillNode node : nodes.values()) {
            if (purchasedNodes.contains(node.getId())) {
                for (SkillNode connectedNode : node.getConnectedNodes()) {
                    if (connectedNode.getId() == nodeId) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}