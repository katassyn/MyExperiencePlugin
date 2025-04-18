package com.maks.myexperienceplugin.Class.skills;

import com.maks.myexperienceplugin.Class.skills.effects.DragonKnightSkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.effects.RangerSkillEffectsHandler;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.Bukkit;

/**
 * Integrates all skill system components
 */
public class SkillSystemIntegrator {
    private final MyExperiencePlugin plugin;
    private final SkillTreeManager skillTreeManager;
    private final SkillEffectsHandler skillEffectsHandler;
    private final SkillTreeGUI skillTreeGUI;
    private final int debuggingFlag = 1;

    public SkillSystemIntegrator(MyExperiencePlugin plugin) {
        this.plugin = plugin;
        this.skillTreeManager = plugin.getSkillTreeManager();
        this.skillEffectsHandler = plugin.getSkillEffectsHandler();
        this.skillTreeGUI = plugin.getSkillTreeGUI();
    }

    /**
     * Initialize all components of the skill system
     */
    public void initialize() {
        // Register skill effect handlers
        registerSkillEffectHandlers();

        // Initialize periodic tasks
        skillEffectsHandler.initializePeriodicTasks();

        // Apply debug delay for first time effects
        if (debuggingFlag == 1) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                plugin.getLogger().info("SkillSystemIntegrator: Applying initial skill effects to all online players");
                Bukkit.getOnlinePlayers().forEach(skillEffectsHandler::refreshPlayerStats);
            }, 100L); // 5 second delay
        }
    }

    /**
     * Register all class-specific skill effect handlers
     */
    private void registerSkillEffectHandlers() {
        // Create instances
        RangerSkillEffectsHandler rangerHandler = new RangerSkillEffectsHandler(plugin);
        DragonKnightSkillEffectsHandler dragonKnightHandler = new DragonKnightSkillEffectsHandler(plugin);

        // Register with main handler
        skillEffectsHandler.registerClassHandler("Ranger", rangerHandler);
        skillEffectsHandler.registerClassHandler("DragonKnight", dragonKnightHandler);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("SkillSystemIntegrator: Registered skill effect handlers for Ranger and DragonKnight");
        }
    }
}