package com.maks.myexperienceplugin.Class.skills.effects;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

/** Base class for all skill effect handlers. Time unit: ticks (20 ticks = 1s). */
public abstract class BaseSkillEffectsHandler {
    protected final MyExperiencePlugin plugin;
    protected int debuggingFlag = 0;

    protected BaseSkillEffectsHandler(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    /** Add passive bonuses from purchased nodes into stats (called during stats rebuild). */
    public abstract void applySkillEffects(
            SkillEffectsHandler.PlayerSkillStats stats,
            int skillId,
            int purchaseCount,
            Player player
    );

    /** Called when player receives damage. */
    public abstract void handleEntityDamage(
            EntityDamageEvent event,
            Player player,
            SkillEffectsHandler.PlayerSkillStats stats
    );

    /** Called when player deals damage. */
    public abstract void handleEntityDamageByEntity(
            EntityDamageByEntityEvent event,
            Player player,
            SkillEffectsHandler.PlayerSkillStats stats
    );

    /** Called when an entity dies (for on-kill mechanics). */
    public abstract void handleEntityDeath(
            EntityDeathEvent event,
            Player player,
            SkillEffectsHandler.PlayerSkillStats stats
    );
}
