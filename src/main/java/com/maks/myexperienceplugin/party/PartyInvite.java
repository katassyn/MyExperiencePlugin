package com.maks.myexperienceplugin.party;

import org.bukkit.entity.Player;

public class PartyInvite {
    private final Player inviter;
    private final long expireTime;

    public PartyInvite(Player inviter, long duration) {
        this.inviter = inviter;
        this.expireTime = System.currentTimeMillis() + duration;
    }

    public Player getInviter() {
        return inviter;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expireTime;
    }
}
