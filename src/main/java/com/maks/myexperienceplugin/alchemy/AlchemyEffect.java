package com.maks.myexperienceplugin.alchemy;

import org.bukkit.entity.Player;

public abstract class AlchemyEffect {
    protected final Player player;
    protected final long durationMillis;   // czas trwania efektu w milisekundach (0 = efekt natychmiastowy)
    protected final long cooldownMillis;   // cooldown (czas, przez który nie można ponownie użyć mikstury z tej kategorii)
    protected final String effectName;     // nazwa efektu, np. "Healing Potion", "Tonic Damage", itp.

    public AlchemyEffect(Player player, long durationMillis, long cooldownMillis, String effectName) {
        this.player = player;
        this.durationMillis = durationMillis;
        this.cooldownMillis = cooldownMillis;
        this.effectName = effectName;
    }

    /** Metoda wywoływana przy rozpoczęciu efektu */
    public abstract void apply();

    /** Metoda wywoływana po zakończeniu efektu */
    public abstract void remove();

    public String getEffectName() {
        return effectName;
    }

    public long getCooldownMillis() {
        return cooldownMillis;
    }
}
