package com.maks.myexperienceplugin.Class.skills.effects;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Base class for all skill effect handlers
 */
public abstract class BaseSkillEffectsHandler {
    protected final MyExperiencePlugin plugin;
    protected final int debuggingFlag = 1;

    public BaseSkillEffectsHandler(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Apply class-specific skill effects to player stats
     */
    public abstract void applySkillEffects(SkillEffectsHandler.PlayerSkillStats stats, int skillId, int purchaseCount);

    /**
     * Handle entity damage event for class-specific effects (e.g., evade, block)
     */
    public abstract void handleEntityDamage(EntityDamageEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats);

    /**
     * Handle entity damage by entity event for class-specific effects (e.g., bonus damage)
     */
    public abstract void handleEntityDamageByEntity(EntityDamageByEntityEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats);

    /**
     * Handle entity death event for class-specific effects (e.g., heal on kill)
     */
    public abstract void handleEntityDeath(EntityDeathEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats);
}