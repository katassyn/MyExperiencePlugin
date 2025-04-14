package com.maks.myexperienceplugin.Class.skills;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SkillPurchasedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final int skillId;

    public SkillPurchasedEvent(Player player, int skillId) {
        this.player = player;
        this.skillId = skillId;
    }

    public Player getPlayer() {
        return player;
    }

    public int getSkillId() {
        return skillId;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}