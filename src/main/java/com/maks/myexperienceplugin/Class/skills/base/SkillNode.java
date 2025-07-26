package com.maks.myexperienceplugin.Class.skills.base;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SkillNode {
    private final int id;
    private final String name;
    private final String description;
    private final int cost;
    private final Material icon;
    private final int maxPurchases;
    private final List<SkillNode> connectedNodes;
    private final Consumer<Player> effectApplier;

    public SkillNode(int id, String name, String description, int cost, Material icon, int maxPurchases, Consumer<Player> effectApplier) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.icon = icon;
        this.maxPurchases = maxPurchases;
        this.connectedNodes = new ArrayList<>();
        this.effectApplier = effectApplier;
    }

    public void addConnection(SkillNode node) {
        if (!connectedNodes.contains(node)) {
            connectedNodes.add(node);
        }
    }

    public void applyEffect(Player player) {
        if (effectApplier != null) {
            effectApplier.accept(player);
        }
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getCost() {
        return cost;
    }

    public Material getIcon() {
        return icon;
    }

    public int getMaxPurchases() {
        return maxPurchases;
    }

    public List<SkillNode> getConnectedNodes() {
        return connectedNodes;
    }
}
