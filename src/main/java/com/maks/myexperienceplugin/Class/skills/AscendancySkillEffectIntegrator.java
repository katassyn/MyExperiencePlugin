package com.maks.myexperienceplugin.Class.skills;

import com.maks.myexperienceplugin.Class.skills.effects.BaseSkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.effects.ascendancy.BeastmasterSkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.effects.ascendancy.BerserkerSkillEffectsHandler;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Integrates ascendancy-specific skill effect handlers into the main plugin
 * and initializes their periodic tasks
 */
public class AscendancySkillEffectIntegrator implements Listener {
    private final MyExperiencePlugin plugin;
    private final SkillEffectsHandler skillEffectsHandler;
    private final int debuggingFlag = 1;

    // Store handlers by ascendancy name
    private final Map<String, BaseSkillEffectsHandler> ascendancyHandlers = new HashMap<>();

    // Task IDs for periodic effects
    private int beastmasterPeriodicTaskId = -1;
    private int berserkerPeriodicTaskId = -1;

    public AscendancySkillEffectIntegrator(MyExperiencePlugin plugin, SkillEffectsHandler skillEffectsHandler) {
        this.plugin = plugin;
        this.skillEffectsHandler = skillEffectsHandler;

        // Initialize handlers
        initializeHandlers();

        // Start periodic tasks
        startPeriodicTasks();

        if (debuggingFlag == 1) {
            plugin.getLogger().info("AscendancySkillEffectIntegrator initialized with " +
                    ascendancyHandlers.size() + " ascendancy handlers");
        }
    }

    /**
     * Initialize all ascendancy handlers
     */
    private void initializeHandlers() {
        // Create Beastmaster handler
        BeastmasterSkillEffectsHandler beastmasterHandler = new BeastmasterSkillEffectsHandler(plugin);
        ascendancyHandlers.put("Beastmaster", beastmasterHandler);
        plugin.getLogger().info("[ASCENDANCY DEBUG] Created BeastmasterSkillEffectsHandler and added to ascendancyHandlers map");

        // Create Berserker handler
        BerserkerSkillEffectsHandler berserkerHandler = new BerserkerSkillEffectsHandler(plugin);
        ascendancyHandlers.put("Berserker", berserkerHandler);
        plugin.getLogger().info("[ASCENDANCY DEBUG] Created BerserkerSkillEffectsHandler and added to ascendancyHandlers map");

        // Register with main SkillEffectsHandler
        skillEffectsHandler.registerClassHandler("Beastmaster", beastmasterHandler);
        plugin.getLogger().info("[ASCENDANCY DEBUG] Registered BeastmasterSkillEffectsHandler with main SkillEffectsHandler");

        skillEffectsHandler.registerClassHandler("Berserker", berserkerHandler);
        plugin.getLogger().info("[ASCENDANCY DEBUG] Registered BerserkerSkillEffectsHandler with main SkillEffectsHandler");

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Registered Beastmaster and Berserker skill effect handlers");
        }
    }

    /**
     * Start periodic tasks for all ascendancy handlers
     */
    private void startPeriodicTasks() {
        // Start Beastmaster periodic task (every 10 seconds)
        beastmasterPeriodicTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (ascendancyHandlers.containsKey("Beastmaster")) {
                BeastmasterSkillEffectsHandler handler = (BeastmasterSkillEffectsHandler) ascendancyHandlers.get("Beastmaster");

                // Apply periodic effects (heal bears, update name tags, etc.)
                handler.applyPeriodicEffects();

                // Check for all online Beastmasters and ensure they have their summons
                for (Player player : Bukkit.getOnlinePlayers()) {
                    String ascendancy = plugin.getClassManager().getPlayerAscendancy(player.getUniqueId());
                    if ("Beastmaster".equals(ascendancy)) {
                        handler.checkAndSummonCreatures(player);
                    }
                }
            }
        }, 200L, 200L); // Initial delay 10s, repeat every 10s (200 ticks)

        // Start Berserker periodic task (every 5 seconds)
        berserkerPeriodicTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (ascendancyHandlers.containsKey("Berserker")) {
                BerserkerSkillEffectsHandler handler = (BerserkerSkillEffectsHandler) ascendancyHandlers.get("Berserker");
                handler.checkCombatMomentum();
            }
        }, 100L, 100L); // Initial delay 5s, repeat every 5s (100 ticks)

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Started periodic tasks for ascendancy skill effects");
        }
    }

    /**
     * Stop all periodic tasks
     */
    public void stopPeriodicTasks() {
        if (beastmasterPeriodicTaskId != -1) {
            Bukkit.getScheduler().cancelTask(beastmasterPeriodicTaskId);
            beastmasterPeriodicTaskId = -1;
        }

        if (berserkerPeriodicTaskId != -1) {
            Bukkit.getScheduler().cancelTask(berserkerPeriodicTaskId);
            berserkerPeriodicTaskId = -1;
        }

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Stopped all ascendancy skill effect periodic tasks");
        }
    }

    /**
     * Get a handler for a specific ascendancy
     */
    public BaseSkillEffectsHandler getHandler(String ascendancy) {
        return ascendancyHandlers.get(ascendancy);
    }

    /**
     * Handle joining players to apply ascendancy effects
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        // Delay to ensure all data is loaded
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            String ascendancy = plugin.getClassManager().getPlayerAscendancy(player.getUniqueId());
            if (ascendancy != null && !ascendancy.isEmpty() && ascendancyHandlers.containsKey(ascendancy)) {
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Initializing ascendancy effects for " + player.getName() +
                            " with ascendancy " + ascendancy);
                }

                // Auto-summon creatures for Beastmasters
                if ("Beastmaster".equals(ascendancy)) {
                    BeastmasterSkillEffectsHandler handler = 
                        (BeastmasterSkillEffectsHandler) ascendancyHandlers.get("Beastmaster");
                    if (handler != null) {
                        handler.checkAndSummonCreatures(player);
                    }
                }
            }
        }, 40L); // 2 second delay
    }

    /**
     * Process commands for summoning creatures
     */
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        String ascendancy = plugin.getClassManager().getPlayerAscendancy(player.getUniqueId());

        // Handle summon commands for Beastmaster
        if ("Beastmaster".equals(ascendancy) && label.equalsIgnoreCase("summon")) {
            if (args.length < 1) {
                player.sendMessage("§cUsage: /summon <wolf|boar|bear>");
                return true;
            }

            BeastmasterSkillEffectsHandler handler = (BeastmasterSkillEffectsHandler) ascendancyHandlers.get("Beastmaster");
            if (handler == null) {
                player.sendMessage("§cError: Beastmaster handler not found.");
                return true;
            }

            String summonType = args[0].toLowerCase();
            switch (summonType) {
                case "wolf":
                    if (isPurchased(player, 100001)) { // Wolf summon skill
                        handler.summonWolves(player);
                    } else {
                        player.sendMessage("§cYou haven't learned to summon wolves yet!");
                    }
                    break;

                case "boar":
                    if (isPurchased(player, 100002)) { // Boar summon skill
                        handler.summonBoars(player);
                    } else {
                        player.sendMessage("§cYou haven't learned to summon boars yet!");
                    }
                    break;

                case "bear":
                    if (isPurchased(player, 100003)) { // Bear summon skill
                        handler.summonBears(player);
                    } else {
                        player.sendMessage("§cYou haven't learned to summon bears yet!");
                    }
                    break;

                default:
                    player.sendMessage("§cInvalid summon type. Use wolf, boar, or bear.");
                    break;
            }

            return true;
        }

        return false;
    }

    /**
     * Check if player has purchased a specific skill
     */
    private boolean isPurchased(Player player, int skillId) {
        return plugin.getSkillTreeManager().getPurchasedSkills(player.getUniqueId()).contains(skillId);
    }
}
