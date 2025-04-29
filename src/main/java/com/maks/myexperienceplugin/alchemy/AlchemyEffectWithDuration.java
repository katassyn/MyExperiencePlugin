package com.maks.myexperienceplugin.alchemy;

/**
 * Interface for alchemy effects that have a duration
 */
public interface AlchemyEffectWithDuration {
    /**
     * Get the duration of this effect in milliseconds
     * @return Duration in milliseconds, 0 for instant effects
     */
    long getDuration();
}