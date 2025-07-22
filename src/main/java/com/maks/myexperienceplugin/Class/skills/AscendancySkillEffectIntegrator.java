package com.maks.myexperienceplugin.Class.skills;

import com.maks.myexperienceplugin.Class.skills.effects.BaseSkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.effects.ascendancy.*;
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
    private int shadowstalkerPeriodicTaskId = -1;
    private int earthwardenPeriodicTaskId = -1;
    private int flamewardenPeriodicTaskId = -1;
    private int scaleguardianPeriodicTaskId = -1;

    // Counter for periodic tasks to manage check frequency
    private int schedulerCounter = 0;

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

        // Create Shadowstalker handler
        ShadowstalkerSkillEffectsHandler shadowstalkerHandler = new ShadowstalkerSkillEffectsHandler(plugin);
        ascendancyHandlers.put("Shadowstalker", shadowstalkerHandler);
        plugin.getLogger().info("[ASCENDANCY DEBUG] Created ShadowstalkerSkillEffectsHandler and added to ascendancyHandlers map");

        // Create Earthwarden handler
        EarthwardenSkillEffectsHandler earthwardenHandler = new EarthwardenSkillEffectsHandler(plugin);
        ascendancyHandlers.put("Earthwarden", earthwardenHandler);
        plugin.getLogger().info("[ASCENDANCY DEBUG] Created EarthwardenSkillEffectsHandler and added to ascendancyHandlers map");

        // Create FlameWarden handler
        FlameWardenSkillEffectsHandler flameWardenHandler = new FlameWardenSkillEffectsHandler(plugin);
        ascendancyHandlers.put("FlameWarden", flameWardenHandler);
        plugin.getLogger().info("[ASCENDANCY DEBUG] Created FlameWardenSkillEffectsHandler and added to ascendancyHandlers map");

        // Create ScaleGuardian handler
        ScaleGuardianSkillEffectsHandler scaleGuardianHandler = new ScaleGuardianSkillEffectsHandler(plugin);
        ascendancyHandlers.put("ScaleGuardian", scaleGuardianHandler);
        plugin.getLogger().info("[ASCENDANCY DEBUG] Created ScaleGuardianSkillEffectsHandler and added to ascendancyHandlers map");

        // Create Elementalist handler
        ElementalistSkillEffectsHandler elementalistHandler = new ElementalistSkillEffectsHandler(plugin);
        ascendancyHandlers.put("Elementalist", elementalistHandler);
        plugin.getLogger().info("[ASCENDANCY DEBUG] Created ElementalistSkillEffectsHandler and added to ascendancyHandlers map");

        // Create Chronomancer handler
        ChromomancerSkillEffectsHandler chronomancerHandler = new ChromomancerSkillEffectsHandler(plugin);
        ascendancyHandlers.put("Chronomancer", chronomancerHandler);
        plugin.getLogger().info("[ASCENDANCY DEBUG] Created ChromomancerSkillEffectsHandler and added to ascendancyHandlers map");

        // Create ArcaneProtector handler
        ArcaneProtectorSkillEffectsHandler arcaneProtectorHandler = new ArcaneProtectorSkillEffectsHandler(plugin);
        ascendancyHandlers.put("ArcaneProtector", arcaneProtectorHandler);
        plugin.getLogger().info("[ASCENDANCY DEBUG] Created ArcaneProtectorSkillEffectsHandler and added to ascendancyHandlers map");

        // Register with main SkillEffectsHandler
        skillEffectsHandler.registerClassHandler("Beastmaster", beastmasterHandler);
        plugin.getLogger().info("[ASCENDANCY DEBUG] Registered BeastmasterSkillEffectsHandler with main SkillEffectsHandler");

        skillEffectsHandler.registerClassHandler("Berserker", berserkerHandler);
        plugin.getLogger().info("[ASCENDANCY DEBUG] Registered BerserkerSkillEffectsHandler with main SkillEffectsHandler");

        skillEffectsHandler.registerClassHandler("Shadowstalker", shadowstalkerHandler);
        plugin.getLogger().info("[ASCENDANCY DEBUG] Registered ShadowstalkerSkillEffectsHandler with main SkillEffectsHandler");

        skillEffectsHandler.registerClassHandler("Earthwarden", earthwardenHandler);
        plugin.getLogger().info("[ASCENDANCY DEBUG] Registered EarthwardenSkillEffectsHandler with main SkillEffectsHandler");

        skillEffectsHandler.registerClassHandler("FlameWarden", flameWardenHandler);
        plugin.getLogger().info("[ASCENDANCY DEBUG] Registered FlameWardenSkillEffectsHandler with main SkillEffectsHandler");

        skillEffectsHandler.registerClassHandler("ScaleGuardian", scaleGuardianHandler);
        plugin.getLogger().info("[ASCENDANCY DEBUG] Registered ScaleGuardianSkillEffectsHandler with main SkillEffectsHandler");

        skillEffectsHandler.registerClassHandler("Elementalist", elementalistHandler);
        plugin.getLogger().info("[ASCENDANCY DEBUG] Registered ElementalistSkillEffectsHandler with main SkillEffectsHandler");

        skillEffectsHandler.registerClassHandler("Chronomancer", chronomancerHandler);
        plugin.getLogger().info("[ASCENDANCY DEBUG] Registered ChromomancerSkillEffectsHandler with main SkillEffectsHandler");

        skillEffectsHandler.registerClassHandler("ArcaneProtector", arcaneProtectorHandler);
        plugin.getLogger().info("[ASCENDANCY DEBUG] Registered ArcaneProtectorSkillEffectsHandler with main SkillEffectsHandler");

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Registered all 9 ascendancy skill effect handlers: Beastmaster, Berserker, Shadowstalker, Earthwarden, FlameWarden, ScaleGuardian, Elementalist, Chronomancer, ArcaneProtector");
        }
    }

    /**
     * Start periodic tasks for all ascendancy handlers
     */
    private void startPeriodicTasks() {
        // Start Beastmaster periodic task (every 60 seconds for creature checks)
        beastmasterPeriodicTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (ascendancyHandlers.containsKey("Beastmaster")) {
                BeastmasterSkillEffectsHandler handler = (BeastmasterSkillEffectsHandler) ascendancyHandlers.get("Beastmaster");

                // Apply periodic effects (heal bears, update name tags, etc.) - still every 10 seconds
                handler.applyPeriodicEffects();

                // Only check summons every 6 cycles (60 seconds)
                if (schedulerCounter % 6 == 0) {
//                    if (debuggingFlag == 1) {
//                        plugin.getLogger().info("[BEASTMASTER] Running summon check for all Beastmaster players");
//                    }

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        String ascendancy = plugin.getClassManager().getPlayerAscendancy(player.getUniqueId());
                        if ("Beastmaster".equals(ascendancy)) {
                            handler.checkAndSummonCreatures(player);
                        }
                    }
                }

                // Increment counter
                schedulerCounter++;
            }
        }, 200L, 200L); // Initial delay 10s, repeat every 10s (200 ticks)

        // Start Berserker periodic task (every 5 seconds)
        berserkerPeriodicTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (ascendancyHandlers.containsKey("Berserker")) {
                BerserkerSkillEffectsHandler handler = (BerserkerSkillEffectsHandler) ascendancyHandlers.get("Berserker");
                handler.checkCombatMomentum();
            }
        }, 100L, 100L); // Initial delay 5s, repeat every 5s (100 ticks)

        // Start Shadowstalker periodic task (every 5 seconds)
        shadowstalkerPeriodicTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (ascendancyHandlers.containsKey("Shadowstalker")) {
                ShadowstalkerSkillEffectsHandler handler = (ShadowstalkerSkillEffectsHandler) ascendancyHandlers.get("Shadowstalker");
                handler.applyPeriodicEffects();
            }
        }, 100L, 100L); // Initial delay 5s, repeat every 5s (100 ticks)

        // Start Earthwarden periodic task (every 5 seconds)
        earthwardenPeriodicTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (ascendancyHandlers.containsKey("Earthwarden")) {
                EarthwardenSkillEffectsHandler handler = (EarthwardenSkillEffectsHandler) ascendancyHandlers.get("Earthwarden");
                handler.applyPeriodicEffects();
            }
        }, 100L, 100L); // Initial delay 5s, repeat every 5s (100 ticks)

        // Start FlameWarden periodic task (every 5 seconds)
        flamewardenPeriodicTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (ascendancyHandlers.containsKey("FlameWarden")) {
                FlameWardenSkillEffectsHandler handler = (FlameWardenSkillEffectsHandler) ascendancyHandlers.get("FlameWarden");
                handler.applyPeriodicEffects();
                handler.checkFireHealing();
            }
        }, 100L, 100L); // Initial delay 5s, repeat every 5s (100 ticks)

        // Start ScaleGuardian periodic task (every 5 seconds)
        scaleguardianPeriodicTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (ascendancyHandlers.containsKey("ScaleGuardian")) {
                ScaleGuardianSkillEffectsHandler handler = (ScaleGuardianSkillEffectsHandler) ascendancyHandlers.get("ScaleGuardian");

                // Apply periodic effects for each player with ScaleGuardian ascendancy
                for (Player player : Bukkit.getOnlinePlayers()) {
                    String ascendancy = plugin.getClassManager().getPlayerAscendancy(player.getUniqueId());
                    if ("ScaleGuardian".equals(ascendancy)) {
                        // Removed checkProximityDefense call to prevent duplicate calls with SkillEffectsHandler
                        // which already calls this method every second
                        handler.checkSurroundedHealing(player);
                        handler.checkAllyEffects(player);
                        handler.checkHeavyArmorMastery(player);
                    }
                }
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

        if (shadowstalkerPeriodicTaskId != -1) {
            Bukkit.getScheduler().cancelTask(shadowstalkerPeriodicTaskId);
            shadowstalkerPeriodicTaskId = -1;
        }

        if (earthwardenPeriodicTaskId != -1) {
            Bukkit.getScheduler().cancelTask(earthwardenPeriodicTaskId);
            earthwardenPeriodicTaskId = -1;
        }

        if (flamewardenPeriodicTaskId != -1) {
            Bukkit.getScheduler().cancelTask(flamewardenPeriodicTaskId);
            flamewardenPeriodicTaskId = -1;
        }

        if (scaleguardianPeriodicTaskId != -1) {
            Bukkit.getScheduler().cancelTask(scaleguardianPeriodicTaskId);
            scaleguardianPeriodicTaskId = -1;
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

                // REMOVED auto-summon code from here since it will be handled by MyExperiencePlugin.java
                // This prevents duplicate calls
            }
        }, 40L); // 2 second delay
    }

    /**
     * Process commands for summoning creatures and testing ascendancy skills
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

        // Handle test commands for Shadowstalker
        if ("Shadowstalker".equals(ascendancy) && label.equalsIgnoreCase("shadowtest")) {
            if (args.length < 1) {
                player.sendMessage("§6=== Shadowstalker Test Commands ===");
                player.sendMessage("§e/shadowtest sneak §7- Test sneaking effects");
                player.sendMessage("§e/shadowtest poison §7- Test poison application");
                player.sendMessage("§e/shadowtest crit §7- Test critical hit effects");
                player.sendMessage("§e/shadowtest speed §7- Test movement speed buffs");
                player.sendMessage("§e/shadowtest info §7- Show current effect status");
                return true;
            }

            ShadowstalkerSkillEffectsHandler handler = (ShadowstalkerSkillEffectsHandler) ascendancyHandlers.get("Shadowstalker");
            if (handler == null) {
                player.sendMessage("§cError: Shadowstalker handler not found.");
                return true;
            }

            String testType = args[0].toLowerCase();
            switch (testType) {
                case "sneak":
                    player.sendMessage("§6[SHADOWSTALKER TEST] §eSneaking test:");
                    player.sendMessage("§7- Current sneaking: §e" + player.isSneaking());
                    player.sendMessage("§7- In darkness: §e" + (player.getLocation().getBlock().getLightLevel() <= 7));
                    player.sendMessage("§7- Night time: §e" + (player.getWorld().getTime() >= 13000 && player.getWorld().getTime() <= 23000));
                    player.sendMessage("§7Try sneaking or moving to shadows to test movement speed bonuses!");
                    break;

                case "poison":
                    player.sendMessage("§6[SHADOWSTALKER TEST] §ePoison test:");
                    player.sendMessage("§7Attack enemies to test poison application with Skill 9!");
                    player.sendMessage("§7Poison chance: §e" + (isPurchased(player, 500009) ? "15%" : "0% (need Skill 9)"));
                    break;

                case "crit":
                    player.sendMessage("§6[SHADOWSTALKER TEST] §eCritical hit test:");
                    player.sendMessage("§7Base crit chance: §e" + plugin.getSkillEffectsHandler().getPlayerStats(player).getCriticalChance() + "%");
                    player.sendMessage("§7Skill 2 bonus: §e" + (isPurchased(player, 500002) ? "+2%" : "0% (need Skill 2)"));
                    player.sendMessage("§7Attack enemies to test critical hits and their effects!");
                    break;

                case "speed":
                    player.sendMessage("§6[SHADOWSTALKER TEST] §eMovement speed test:");
                    player.sendMessage("§7Current walk speed: §e" + player.getWalkSpeed());
                    player.sendMessage("§7Try killing enemies to trigger Assassin's Haste (Skill 10)!");
                    player.sendMessage("§7Or sneak with Skill 16 for sneaking speed bonus!");
                    break;

                case "info":
                    player.sendMessage("§6[SHADOWSTALKER STATUS] §eCurrent effects:");
                    player.sendMessage("§7Health: §e" + String.format("%.1f", player.getHealth()) + "/" + String.format("%.1f", player.getMaxHealth()));
                    player.sendMessage("§7Light level: §e" + player.getLocation().getBlock().getLightLevel());
                    player.sendMessage("§7Sneaking: §e" + player.isSneaking());
                    player.sendMessage("§7Walk speed: §e" + player.getWalkSpeed());
                    break;

                default:
                    player.sendMessage("§cInvalid test type. Use: sneak, poison, crit, speed, info");
                    break;
            }

            return true;
        }

        // Handle test commands for Earthwarden
        if ("Earthwarden".equals(ascendancy) && label.equalsIgnoreCase("earthtest")) {
            if (args.length < 1) {
                player.sendMessage("§a=== Earthwarden Test Commands ===");
                player.sendMessage("§e/earthtest defense §7- Test defense bonuses");
                player.sendMessage("§e/earthtest heal §7- Test healing effects");
                player.sendMessage("§e/earthtest allies §7- Test ally-based effects");
                player.sendMessage("§e/earthtest environment §7- Test environmental effects");
                player.sendMessage("§e/earthtest info §7- Show current effect status");
                return true;
            }

            EarthwardenSkillEffectsHandler handler = (EarthwardenSkillEffectsHandler) ascendancyHandlers.get("Earthwarden");
            if (handler == null) {
                player.sendMessage("§cError: Earthwarden handler not found.");
                return true;
            }

            String testType = args[0].toLowerCase();
            switch (testType) {
                case "defense":
                    player.sendMessage("§a[EARTHWARDEN TEST] §eDefense test:");
                    double healthPercent = (player.getHealth() / player.getMaxHealth()) * 100;
                    player.sendMessage("§7Current health: §e" + String.format("%.1f", healthPercent) + "%");
                    player.sendMessage("§7Standing still for 3s gives +10% defense (Skill 5)");
                    player.sendMessage("§7Below 50% HP gives +5% defense for 3s (Skill 7)");
                    player.sendMessage("§7Below 30% HP gives +20% defense + 10% damage (Skill 15)");
                    break;

                case "heal":
                    player.sendMessage("§a[EARTHWARDEN TEST] §eHealing test:");
                    player.sendMessage("§7Kill enemies to heal 1 HP (Skill 2)");
                    player.sendMessage("§7Attack enemies for 10% chance to heal 5% damage dealt (Skill 14)");
                    player.sendMessage("§7Attack enemies for 25% chance to heal 2% max HP (Skill 22)");
                    break;

                case "allies":
                    int nearbyPlayers = 0;
                    for (Player other : player.getWorld().getPlayers()) {
                        if (!other.equals(player) && other.getLocation().distance(player.getLocation()) <= 10) {
                            nearbyPlayers++;
                        }
                    }
                    player.sendMessage("§a[EARTHWARDEN TEST] §eAlly effects test:");
                    player.sendMessage("§7Nearby allies (10 blocks): §e" + nearbyPlayers);
                    player.sendMessage("§7Skill 11: +1% defense per ally (max 5%)");
                    player.sendMessage("§7Skill 26: +2% defense +1% damage per ally (max 5 allies)");
                    break;

                case "environment":
                    String blockBelow = player.getLocation().getBlock().getRelative(org.bukkit.block.BlockFace.DOWN).getType().toString();
                    player.sendMessage("§a[EARTHWARDEN TEST] §eEnvironment test:");
                    player.sendMessage("§7Block below: §e" + blockBelow);
                    player.sendMessage("§7Grassy areas give +3% defense (Skill 1)");
                    player.sendMessage("§7Fall/fire/lava damage reduced by 5% (Skill 3)");
                    break;

                case "info":
                    player.sendMessage("§a[EARTHWARDEN STATUS] §eCurrent effects:");
                    player.sendMessage("§7Health: §e" + String.format("%.1f", player.getHealth()) + "/" + String.format("%.1f", player.getMaxHealth()));
                    player.sendMessage("§7Defense bonus: §e" + plugin.getSkillEffectsHandler().getPlayerStats(player).getDefenseBonus() + "%");
                    player.sendMessage("§7Movement speed: §e" + player.getWalkSpeed());
                    break;

                default:
                    player.sendMessage("§cInvalid test type. Use: defense, heal, allies, environment, info");
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
