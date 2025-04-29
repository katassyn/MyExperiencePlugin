package com.maks.myexperienceplugin.alchemy;

import org.bukkit.entity.Player;

public abstract class AlchemyEffect implements AlchemyManager.AlchemyEffectWithDuration {
    protected final Player player;
    protected final long durationMillis;   // duration in milliseconds (0 = instant effect)
    protected final long cooldownMillis;   // cooldown (time after which you can't reuse a potion in this category)
    protected final String effectName;     // effect name, e.g. "Healing Potion", "Tonic Damage", etc.

    public AlchemyEffect(Player player, long durationMillis, long cooldownMillis, String effectName) {
        this.player = player;
        this.durationMillis = durationMillis;
        this.cooldownMillis = cooldownMillis;
        this.effectName = effectName;
    }

    /** Method called at the start of effect */
    public abstract void apply();

    /** Method called when effect ends */
    public abstract void remove();

    public String getEffectName() {
        return effectName;
    }

    public long getCooldownMillis() {
        return cooldownMillis;
    }

    @Override
    public long getDuration() {
        return durationMillis;
    }
}