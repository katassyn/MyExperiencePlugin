package com.maks.myexperienceplugin;

import com.maks.myexperienceplugin.Class.*;
import com.maks.myexperienceplugin.Class.skills.*;
import com.maks.myexperienceplugin.Class.skills.AscendancySkillEffectIntegrator;
import com.maks.myexperienceplugin.Class.skills.gui.AscendancySkillTreeGUIListener;
import com.maks.myexperienceplugin.alchemy.*;
import com.maks.myexperienceplugin.exp.*;
import com.maks.myexperienceplugin.listener.*;
import com.maks.myexperienceplugin.party.PartyAPI;
import com.maks.myexperienceplugin.party.PartyCommand;
import com.maks.myexperienceplugin.party.PartyManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import com.maks.myexperienceplugin.Class.skills.gui.AscendancySkillTreeGUI;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.user.UserDataRecalculateEvent;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class MyExperiencePlugin extends JavaPlugin implements Listener {

    private DatabaseManager databaseManager;
    private Economy economy;
    public MoneyRewardHandler moneyRewardHandler;
    public final HashMap<UUID, Integer> playerLevels = new HashMap<>();
    private final HashMap<UUID, Double> playerRequiredXP = new HashMap<>();
    public final HashMap<UUID, Double> playerCurrentXP = new HashMap<>();
    private final HashMap<Integer, Double> xpPerLevel = new HashMap<>();
    private final HashMap<UUID, ExpBoost> playerExpBoosts = new HashMap<>();
    private File expTableFile;
    private FileConfiguration expTableConfig;
    private final Map<String, Double> xpPerMob = new HashMap<>();
    private static MyExperiencePlugin instance;
    private PartyManager partyManager;
    private PlayerLevelDisplayHandler playerLevelDisplayHandler;
    private final int maxLevel = 100;
    private AlchemyLevelConfig alchemyLevelConfig;
    // Class system references
    private ClassManager classManager;
    private ClassGUI classGUI;
    private SkillTreeManager skillTreeManager;
    private SkillTreeGUI skillTreeGUI;
    private SkillEffectsHandler skillEffectsHandler;
    private AscendancySkillEffectIntegrator ascendancySkillEffectIntegrator;
    public ClassManager getClassManager() {
        return classManager;
    }
    public ClassGUI getClassGUI() {
        return classGUI;
    }
    private AscendancySkillTreeGUI ascendancySkillTreeGUI; // Add this field
    private SkillPurchaseManager skillPurchaseManager;
    private LuckPerms luckPerms;
    private com.maks.myexperienceplugin.Class.skills.systems.CriticalStrikeSystem criticalStrikeSystem;
    public static MyExperiencePlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        // Database initialization
        String host = getConfig().getString("database.host");
        String port = getConfig().getString("database.port");
        String database = getConfig().getString("database.name");
        String username = getConfig().getString("database.user");
        String password = getConfig().getString("database.password");

        try {
            databaseManager = new DatabaseManager(this, host, port, database, username, password);
        } catch (Exception e) {
            getLogger().severe("Failed to initialize database connection pool: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Economy setup
        if (!setupEconomy()) {
            getLogger().severe("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getLogger().info("MyExperiencePlugin has been enabled!");

        // LuckPerms setup
        RegisteredServiceProvider<LuckPerms> luckPermsProvider = getServer().getServicesManager().getRegistration(LuckPerms.class);
        if (luckPermsProvider != null) {
            luckPerms = luckPermsProvider.getProvider();
            getLogger().info("LuckPerms integration successful!");
        } else {
            getLogger().severe("LuckPerms not found! Premium and Deluxe packages will not work.");
            luckPerms = null;
        }

// Register custom item handler
        if (economy != null) {
            getServer().getPluginManager().registerEvents(new CustomItemHandler(this, economy, luckPerms), this);
            getLogger().info("CustomItemHandler registered successfully!");
        } else {
            getLogger().severe("Economy not found! Gold coin items will not work.");
        }

        // Initialize core managers and handlers
        partyManager = new PartyManager(this);

        // Inicjalizacja Party API dla integracji z innymi pluginami
        PartyAPI.initialize(this);

        moneyRewardHandler = new MoneyRewardHandler(economy, this);
        playerLevelDisplayHandler = new PlayerLevelDisplayHandler(this);
        alchemyLevelConfig = new AlchemyLevelConfig(this);
        AlchemyManager.getInstance().initialize(this);

        // Register the player join alchemy listener
        getServer().getPluginManager().registerEvents(new PlayerJoinAlchemyListener(this), this);
        classManager = new ClassManager(this);
        classGUI = new ClassGUI(this);

        // Initialize XP system
        initializeXPLevels();
        loadExpTable();

        // Initialize skill system
        skillTreeManager = new SkillTreeManager(this);
        skillEffectsHandler = new SkillEffectsHandler(this, skillTreeManager);
        skillEffectsHandler.initializePeriodicTasks();

        // Initialize ascendancy skill effect integrator
        ascendancySkillEffectIntegrator = new AscendancySkillEffectIntegrator(this, skillEffectsHandler);
        getServer().getPluginManager().registerEvents(ascendancySkillEffectIntegrator, this);

        skillTreeGUI = new SkillTreeGUI(this, skillTreeManager);
        ascendancySkillTreeGUI = new AscendancySkillTreeGUI(this, skillTreeManager);

        // Initialize the skill purchase manager
        skillPurchaseManager = new SkillPurchaseManager(this, skillTreeManager, skillTreeGUI, ascendancySkillTreeGUI);

        // Register this plugin as a listener
        getServer().getPluginManager().registerEvents(this, this);

        // Register event listeners - XP, Chat, Player
        getServer().getPluginManager().registerEvents(new MythicMobXPHandler(this), this);
        getServer().getPluginManager().registerEvents(new ChatLevelHandler(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDisconnectListener(partyManager), this);
        getServer().getPluginManager().registerEvents(playerLevelDisplayHandler, this);

        if (luckPerms != null) {
            luckPerms.getEventBus().subscribe(this, UserDataRecalculateEvent.class, event -> {
                Player player = Bukkit.getPlayer(event.getUser().getUniqueId());
                if (player != null) {
                    Bukkit.getScheduler().runTask(this, () -> playerLevelDisplayHandler.updatePlayerTab(player));
                }
            });
        }
        
        // Register party damage prevention listener
        getServer().getPluginManager().registerEvents(new PartyDamagePreventionListener(this), this);

        // Register alchemy listeners
        getServer().getPluginManager().registerEvents(new TotemEffectListener(), this);
        getServer().getPluginManager().registerEvents(new LifestealListener(), this);
        getServer().getPluginManager().registerEvents(new ImmunityListener(), this);
        getServer().getPluginManager().registerEvents(new AlchemyItemListener(this, alchemyLevelConfig), this);
        getServer().getPluginManager().registerEvents(new PhysisExpListener(this), this);

        // Register class system listeners
        getServer().getPluginManager().registerEvents(new ClassGUIListener(this), this);
        getServer().getPluginManager().registerEvents(new ClassResetItemListener(this), this);

        // Initialize critical strike system
        criticalStrikeSystem = new com.maks.myexperienceplugin.Class.skills.systems.CriticalStrikeSystem(this);

        // Register skill system listeners
        getServer().getPluginManager().registerEvents(skillEffectsHandler, this);
        getServer().getPluginManager().registerEvents(new SkillTreeGUIListener(this, skillTreeManager, skillTreeGUI, skillPurchaseManager), this);
        getServer().getPluginManager().registerEvents(new AscendancySkillTreeGUIListener(this, skillTreeManager, ascendancySkillTreeGUI, skillPurchaseManager), this);
        getServer().getPluginManager().registerEvents(new com.maks.myexperienceplugin.Class.skills.listeners.BerserkerEquipmentListener(this), this);

        // Register commands - Party
        PartyCommand partyCommand = new PartyCommand(this, partyManager);
        getCommand("party").setExecutor(partyCommand);
        getCommand("party").setTabCompleter(partyCommand);

        // Register commands - XP
        getCommand("exp").setExecutor(this);
        getCommand("exp_table").setExecutor(new ExpTableCommand(this));
        getCommand("exp_table").setTabCompleter(new ExpTableCommand(this));
        getCommand("exp_money").setExecutor(moneyRewardHandler);
        getCommand("exp_money").setTabCompleter(moneyRewardHandler);
        getCommand("reload_bonus").setExecutor(new ReloadBonusCommand(this));
        getCommand("top").setExecutor(new TopCommand(this));
        getCommand("bonus_exp").setExecutor(new BonusExpCommand(this));

        // Register commands - Experience
        ExperienceCommandHandler experienceCommandHandler = new ExperienceCommandHandler(this);
        getCommand("get_lvl").setExecutor(experienceCommandHandler);
        getCommand("exp_give").setExecutor(experienceCommandHandler);
        getCommand("exp_give_p").setExecutor(experienceCommandHandler);

        // Register commands - Class system
        getCommand("class").setExecutor(new ClassCommand(this));
        getCommand("chose_class").setExecutor(new ChoseClassCommand(this));
        getCommand("chose_ascendancy").setExecutor(new ChoseAscendancyCommand(this));
        getCommand("alchemy_reset").setExecutor(new AlchemyResetCommand());
        getCommand("alchemy_cd").setExecutor(new AlchemyCooldownCommand());
        
        // Register ranking update command
        getCommand("updaterankings").setExecutor(new com.maks.myexperienceplugin.commands.UpdateRankingsCommand(this));

        // Register commands - Skill system
        getCommand("skilltree").setExecutor(new SkillTreeCommand(this, skillTreeGUI));
        getCommand("skillstats").setExecutor(new SkillStatsCommand(this, skillEffectsHandler));
        getCommand("skilltree2").setExecutor(new AscendancySkillTreeCommand(this, ascendancySkillTreeGUI));
        getCommand("playerattributes").setExecutor(new PlayerAttributesCommand(this, skillEffectsHandler));


        // Register commands - Reset attributes
        ResetAttributesCommand resetAttributesCommand = new ResetAttributesCommand(this, skillEffectsHandler);
        getCommand("resetattributes").setExecutor(resetAttributesCommand);
        getCommand("resetattributes").setTabCompleter(resetAttributesCommand);

        // Register command - Reset ranks
        if (luckPerms != null) {
            ResetRanksCommand resetRanksCommand = new ResetRanksCommand(this, luckPerms);
            getCommand("resetranks").setExecutor(resetRanksCommand);
            getCommand("resetranks").setTabCompleter(resetRanksCommand);
            getLogger().info("ResetRanksCommand registered successfully!");
        } else {
            getLogger().severe("LuckPerms not found! ResetRanksCommand will not work.");
        }

        ForceUpdateSkillPointsCommand forceUpdateSkillPointsCommand = new ForceUpdateSkillPointsCommand(
                this,
                skillTreeManager,
                skillTreeGUI  // Pass the SkillTreeGUI directly
        );
        getCommand("updateskillpoints").setExecutor(forceUpdateSkillPointsCommand);
        getCommand("updateskillpoints").setTabCompleter(forceUpdateSkillPointsCommand);
        // Start periodic tasks
        new PeriodicClassReminder(this).runTaskTimer(this, 20L, 1200L); // 20L = 1s, 1200L = 60s
        new PeriodicSkillPointReminder(this).runTaskTimer(this, 20L, 6000L); // remind every 5 minutes
        PlayerSkillEffectsListener playerSkillEffectsListener = new PlayerSkillEffectsListener(
                this,
                skillTreeManager,
                skillEffectsHandler
        );
        getServer().getPluginManager().registerEvents(playerSkillEffectsListener, this);
        getCommand("applyskills").setExecutor((sender, cmd, label, args) -> {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                playerSkillEffectsListener.applyAllSkillEffects(player);
                player.sendMessage("§aAll your skill effects have been applied!");
                return true;
            } else {
                sender.sendMessage("§cThis command can only be run by a player.");
                return true;
            }
        });
        // Let's add a command to manually refresh skill effects for debugging
        getCommand("refreshskills").setExecutor((sender, cmd, label, args) -> {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                skillEffectsHandler.refreshPlayerStats(player);
                player.sendMessage("§aYour skill effects have been refreshed!");

                if (args.length > 0 && args[0].equalsIgnoreCase("debug")) {
                    // Use the fully qualified class name for PlayerSkillStats
                    SkillEffectsHandler.PlayerSkillStats stats = skillEffectsHandler.getPlayerStats(player);
                    player.sendMessage("§bCurrent stats:");
                    player.sendMessage("§7 - HP Bonus: §f" + stats.getMaxHealthBonus());
                    player.sendMessage("§7 - Damage Bonus: §f" + stats.getBonusDamage());
                    player.sendMessage("§7 - Damage Multiplier: §f" + String.format("%.2f", stats.getDamageMultiplier()) + "x");
                    player.sendMessage("§7 - Evade Chance: §f" + stats.getEvadeChance() + "%");
                    player.sendMessage("§7 - Shield Block: §f" + stats.getShieldBlockChance() + "%");
                }
                return true;
            } else {
                sender.sendMessage("§cThis command can only be run by a player.");
                return true;
            }
        });
// Register the player join alchemy listener

// Add this line to make sure the ActionBarUtils class is loaded

        // Uruchom zadanie czyszczenia wygasłych exp boostów co 5 minut
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::cleanupExpiredBoosts, 6000L, 6000L); // 5 min = 6000 ticks

        // Dodaj komendę do sprawdzania statusu exp boost
        getCommand("expboost").setExecutor((sender, cmd, label, args) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cThis command can only be used by players.");
                return true;
            }

            Player player = (Player) sender;
            showExpBoostStatus(player);
            return true;
        });

        // Opcjonalnie - dodaj też komendę admin do nadawania boostów:
        getCommand("giveexpboost").setExecutor((sender, cmd, label, args) -> {
            if (!sender.hasPermission("myplugin.giveexpboost")) {
                sender.sendMessage("§cYou don't have permission to use this command!");
                return true;
            }

            if (args.length < 3) {
                sender.sendMessage("§cUsage: /giveexpboost <player> <percent> <hours>");
                return true;
            }

            try {
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found.");
                    return true;
                }

                double percent = Double.parseDouble(args[1]);
                int hours = Integer.parseInt(args[2]);

                if (percent <= 0 || percent > 1000) {
                    sender.sendMessage("§cPercent must be between 1 and 1000.");
                    return true;
                }

                if (hours <= 0 || hours > 168) { // Max 7 days
                    sender.sendMessage("§cHours must be between 1 and 168.");
                    return true;
                }

                addExpBoost(target, percent, hours);
                sender.sendMessage("§aGave " + target.getName() + " a +" + (int)percent + "% EXP boost for " + hours + " hours!");

            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid number format!");
            }

            return true;
        });
        
        // Komenda do sprawdzania statusu adminów
        getCommand("admincheck").setExecutor((sender, cmd, label, args) -> {
            if (!sender.hasPermission("myplugin.admincheck")) {
                sender.sendMessage("§cNo permission!");
                return true;
            }
            
            sender.sendMessage("§eChecking admin status in database...");
            
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                try (Connection connection = getDatabaseManager().getConnection()) {
                    List<String> results = new ArrayList<>();
                    results.add("§6=== Admin Status Check ===");
                    
                    // First query: Get all admin players
                    String adminSql = "SELECT name, level, is_admin FROM players WHERE is_admin = TRUE";
                    try (PreparedStatement adminStmt = connection.prepareStatement(adminSql)) {
                        ResultSet adminRs = adminStmt.executeQuery();
                        while (adminRs.next()) {
                            String name = adminRs.getString("name");
                            int level = adminRs.getInt("level");
                            boolean isAdmin = adminRs.getBoolean("is_admin");
                            String status = isAdmin ? "§aADMIN" : "§7NORMAL";
                            results.add(String.format("§f%s §7(Level %d) - %s", name, level, status));
                        }
                    }
                    
                    // Second query: Get top 5 players by level
                    String topPlayersSql = "SELECT name, level, is_admin FROM players WHERE is_admin = FALSE ORDER BY level DESC LIMIT 5";
                    try (PreparedStatement topStmt = connection.prepareStatement(topPlayersSql)) {
                        ResultSet topRs = topStmt.executeQuery();
                        while (topRs.next()) {
                            String name = topRs.getString("name");
                            int level = topRs.getInt("level");
                            boolean isAdmin = topRs.getBoolean("is_admin");
                            String status = isAdmin ? "§aADMIN" : "§7NORMAL";
                            results.add(String.format("§f%s §7(Level %d) - %s", name, level, status));
                        }
                    }
                    
                    Bukkit.getScheduler().runTask(this, () -> {
                        for (String result : results) {
                            sender.sendMessage(result);
                        }
                    });
                } catch (SQLException e) {
                    getLogger().severe("Error checking admin status: " + e.getMessage());
                    Bukkit.getScheduler().runTask(this, () -> 
                        sender.sendMessage("§cError checking database!")
                    );
                }
            });
            
            return true;
        });

        // Komenda do naprawienia statusów adminów
        getCommand("fixadmins").setExecutor((sender, cmd, label, args) -> {
            if (!sender.hasPermission("myplugin.fixadmins")) {
                sender.sendMessage("§cNo permission!");
                return true;
            }
            
            sender.sendMessage("§eFixing admin statuses...");
            
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                int fixed = 0;
                
                try (Connection connection = getDatabaseManager().getConnection()) {
                    // Update all online players first
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        String sql = "UPDATE players SET is_admin = ? WHERE uuid = ?";
                        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                            stmt.setBoolean(1, player.isOp());
                            stmt.setString(2, player.getUniqueId().toString());
                            int updated = stmt.executeUpdate();
                            if (updated > 0) fixed++;
                        }
                    }
                    
                    // Force update rankings
                    getDatabaseManager().forceUpdatePlayerRankings();
                    
                } catch (SQLException e) {
                    getLogger().severe("Error fixing admin status: " + e.getMessage());
                    Bukkit.getScheduler().runTask(this, () -> 
                        sender.sendMessage("§cError updating database!")
                    );
                    return;
                }
                
                int finalFixed = fixed;
                Bukkit.getScheduler().runTask(this, () -> {
                    sender.sendMessage("§aFixed admin status for " + finalFixed + " players!");
                    sender.sendMessage("§aRankings have been updated!");
                });
            });
            
            return true;
        });
    }
    @Override
    public void onDisable() {
        if (skillPurchaseManager != null) {
            skillPurchaseManager.cleanup();
        }

        if (databaseManager != null) {
            try {
                databaseManager.shutdown();
            } catch (Exception e) {
                getLogger().severe("Error while shutting down database connection pool: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // In onDisable(), add before other shutdown code:
            getLogger().info("Saving alchemy data before shutdown");

        // Save any remaining alchemy data
        AlchemyManager.getInstance().saveData();

        // Wyczyść exp boosts
        playerExpBoosts.clear();

        Bukkit.getLogger().info("MyExperiencePlugin has been disabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    private void initializeXPLevels() {
        double previousXP = 100;
        for (int level = 1; level <= 100; level++) {
            previousXP = Math.round(Math.pow(previousXP + 100, 1.013) * 100.0) / 100.0;
            xpPerLevel.put(level, previousXP);
        }
    }

    public void loadExpTable() {
        expTableFile = new File(getDataFolder(), "exp_table.yml");
        if (!expTableFile.exists()) {
            expTableFile.getParentFile().mkdirs();
            saveResource("exp_table.yml", false);
        }
        expTableConfig = YamlConfiguration.loadConfiguration(expTableFile);
        xpPerMob.clear();
        if (expTableConfig.getConfigurationSection("xp_per_mob") != null) {
            for (String mobName : expTableConfig.getConfigurationSection("xp_per_mob").getKeys(false)) {
                double xp = expTableConfig.getDouble("xp_per_mob." + mobName);
                xpPerMob.put(mobName, xp);
            }
        }
    }

    public double getXpForMob(String mobName) {
        return xpPerMob.getOrDefault(mobName, 0.0);
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (label.equalsIgnoreCase("exp")) {
                UUID playerId = player.getUniqueId();
                int level = playerLevels.getOrDefault(playerId, 1);
                double currentXP = playerCurrentXP.getOrDefault(playerId, 0.0);
                
                // Safely get required XP with null check
                Double xpForLevel = xpPerLevel.get(level);
                if (xpForLevel == null) {
                    getLogger().warning("No XP requirement found for level " + level + " for player " + player.getName());
                    xpForLevel = Math.pow(level * 100 + 100, 1.013);
                }
                double requiredXP = playerRequiredXP.getOrDefault(playerId, xpForLevel);

                double progress = (level >= maxLevel) ? 100.0 : (currentXP / requiredXP) * 100;
                if (level >= maxLevel) {
                    currentXP = requiredXP;
                }

                String formattedCurrentXP = formatNumber(currentXP);
                String formattedRequiredXP = formatNumber(requiredXP);
                String formattedProgress = String.format("%.2f%%", progress);

                player.sendMessage("§e===== §6[§aXP Info§6] §e=====");
                player.sendMessage("§6Level: §a" + level);
                player.sendMessage("§6XP: §a" + formattedCurrentXP + "§6 / §a" + formattedRequiredXP);
                player.sendMessage("§6Progress: §a" + formattedProgress);
                player.sendMessage("§e======================");
                return true;
            }
        }
        return false;
    }

    // Add XP
    public void addXP(Player player, double xp) {
        UUID playerId = player.getUniqueId();

        // Zastosuj player exp boost jeśli aktywny
        double playerBoostMultiplier = 1.0 + (getPlayerExpBoost(player) / 100.0);

        // Oblicz finalne XP z player boost
        double finalXP = xp * playerBoostMultiplier;

        // Dodaj XP
        double currentXP = playerCurrentXP.getOrDefault(playerId, 0.0);
        currentXP += finalXP;
        playerCurrentXP.put(playerId, currentXP);

        // Debug info jeśli boost jest aktywny
        if (playerBoostMultiplier > 1.0) {
            getLogger().info("[EXP BOOST] " + player.getName() + " received " + 
                    String.format("%.2f", finalXP) + " XP (base: " + String.format("%.2f", xp) + 
                    " + " + (int)getPlayerExpBoost(player) + "% boost)");
        }

        checkLevelUp(player);
        updatePlayerXPBar(player);
    }

    // Check level up
    public void checkLevelUp(Player player) {
        UUID playerId = player.getUniqueId();
        int currentLevel = playerLevels.getOrDefault(playerId, 1);
        double currentXP = playerCurrentXP.getOrDefault(playerId, 0.0);
        double requiredXP = xpPerLevel.getOrDefault(currentLevel, Math.pow(currentLevel * 100 + 100, 1.013));

        while (currentXP >= requiredXP && currentLevel < maxLevel) {
            currentLevel++;
            playerLevels.put(playerId, currentLevel);
            currentXP -= requiredXP;
            // Ensure we have a valid XP requirement for the new level
            Double nextRequiredXP = xpPerLevel.get(currentLevel);
            if (nextRequiredXP == null) {
                getLogger().warning("No XP requirement found for level " + currentLevel + ", using calculated value");
                requiredXP = Math.pow(currentLevel * 100 + 100, 1.013);
            } else {
                requiredXP = nextRequiredXP;
            }

            // Play level up effects
            playLevelUpEffects(player, currentLevel);
            
            broadcastLevelUpMessage(player, currentLevel);
            moneyRewardHandler.onLevelUp(player, currentLevel);

            // Add 1 skill point on every level up
            classManager.addSkillPoints(player, 1);
            skillTreeManager.updateSkillPoints(player);
            databaseManager.savePlayerData(player, currentLevel, currentXP);
            updatePlayerDisplay(player);
        }

        playerCurrentXP.put(playerId, currentXP);
        playerRequiredXP.put(playerId, requiredXP);
        updatePlayerXPBar(player);
        databaseManager.savePlayerData(player, currentLevel, currentXP);
    }

    public void updatePlayerXPBar(Player player) {
        UUID playerId = player.getUniqueId();
        double currentXP = playerCurrentXP.getOrDefault(playerId, 0.0);
        int level = playerLevels.getOrDefault(playerId, 1);
        
        // Safely get required XP with null check
        Double xpForLevel = xpPerLevel.get(level);
        if (xpForLevel == null) {
            getLogger().warning("No XP requirement found for level " + level + " when updating XP bar");
            xpForLevel = Math.pow(level * 100 + 100, 1.013);
        }
        double requiredXP = playerRequiredXP.getOrDefault(playerId, xpForLevel);

        player.setLevel(level);
        if (level >= maxLevel) {
            player.setExp(0.0f);
        } else {
            float progress = (float) (currentXP / requiredXP);
            player.setExp(Math.min(1.0f, Math.max(0.0f, progress))); // Ensure progress is between 0 and 1
        }
    }

    public void broadcastLevelUpMessage(Player player, int level) {
        List<Integer> specialLevels = Arrays.asList(
                20, 30, 40, 50, 60, 70, 75, 80, 85, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99
        );
        if (specialLevels.contains(level)) {
            String message = String.format("§7[§6INFO§7] §6%s §7has reached level §6%d§7!", player.getName(), level);
            Bukkit.broadcastMessage(message);
        }
        if (level == 100) {
            String maxLevelMessage = String.format("§7[§4LEGENDARY§7] §6%s §7has reached the §4MAX LEVEL§7!", player.getName());
            Bukkit.broadcastMessage(maxLevelMessage);
        }
    }

    public int getPlayerLevel(Player player) {
        return playerLevels.getOrDefault(player.getUniqueId(), 1);
    }

    public void updatePlayerDisplay(Player player) {
        if (playerLevelDisplayHandler != null) {
            playerLevelDisplayHandler.updatePlayerTab(player);
        }
    }
    
    /**
     * Displays sound and visual effects when a player levels up
     * @param player Player who leveled up
     * @param level New level of the player
     */
    public void playLevelUpEffects(Player player, int level) {
        if (player == null || !player.isOnline()) return;
        
        Location location = player.getLocation().add(0, 1, 0);
        
        // Yellow particles
        player.getWorld().spawnParticle(
                Particle.REDSTONE,
                location,
                30, // amount
                0.7, 0.7, 0.7, // spread
                1, // speed
                new Particle.DustOptions(Color.YELLOW, 1.5f)
        );
        
        // Green particles
        player.getWorld().spawnParticle(
                Particle.REDSTONE,
                location,
                30, // amount
                0.7, 0.7, 0.7, // spread
                1, // speed
                new Particle.DustOptions(Color.GREEN, 1.5f)
        );
        
        // Explosion effect
        player.getWorld().spawnParticle(
                Particle.EXPLOSION_NORMAL,
                location,
                3, // amount
                0.5, 0.5, 0.5, // spread
                0.1 // speed
        );
        
        // Star effect
        player.getWorld().spawnParticle(
                Particle.FIREWORKS_SPARK,
                location,
                50, // amount
                1.0, 1.0, 1.0, // spread
                0.2 // speed
        );
        
        // Sound effects
        // Level up sound
        player.getWorld().playSound(
                location,
                Sound.ENTITY_PLAYER_LEVELUP,
                0.8f, // volume (reduced from 1.0f to make it not too loud)
                1.0f  // pitch
        );
        
        // Firework sound
        player.getWorld().playSound(
                location,
                Sound.ENTITY_FIREWORK_ROCKET_BLAST,
                0.7f, // volume (reduced from 1.0f to make it not too loud)
                1.0f  // pitch
        );
        
        // Special effects for milestone levels (every 10 levels or 90+)
        if (level % 10 == 0 || level >= 90) {
            // Additional explosion effect
            player.getWorld().spawnParticle(
                    Particle.EXPLOSION_LARGE,
                    location,
                    2, // amount
                    0.5, 0.5, 0.5, // spread
                    0.1 // speed
            );
            
            // Additional sound
            player.getWorld().playSound(
                    location,
                    Sound.ENTITY_GENERIC_EXPLODE,
                    0.7f, // volume (reduced from 1.0f to make it not too loud)
                    1.0f  // pitch
            );
        }
    }

    public static String formatNumber(double number) {
        if (number >= 1_000_000_000) {
            return String.format("%.2fB", number / 1_000_000_000);
        } else if (number >= 1_000_000) {
            return String.format("%.2fM", number / 1_000_000);
        } else if (number >= 1_000) {
            return String.format("%.2fK", number / 1_000);
        } else {
            return String.format("%.2f", number);
        }
    }

    public int getMaxLevel() {
        return maxLevel;
    }
    public AlchemyLevelConfig getAlchemyLevelConfig() {
        return alchemyLevelConfig;
    }
    public HashMap<UUID, Integer> getPlayerLevels() {
        return playerLevels;
    }

    public HashMap<UUID, Double> getPlayerCurrentXP() {
        return playerCurrentXP;
    }

    public HashMap<Integer, Double> getXpPerLevel() {
        return xpPerLevel;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public PartyManager getPartyManager() {
        return partyManager;
    }

    public boolean isBonusExpEnabled() {
        return getConfig().getBoolean("Bonus_exp.Enabled", false);
    }

    public double getBonusExpValue() {
        return getConfig().getDouble("Bonus_exp.Value", 100.0);
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Clear physis exp
        PhysisExpManager.getInstance().clearPlayerBonus(uuid);
        
        // Clear EXP boost to prevent memory leak
        playerExpBoosts.remove(uuid);

        // Clean up skill purchase manager resources
        if (skillPurchaseManager != null) {
            skillPurchaseManager.cleanup(uuid);
        }

        // AlchemyManager will now handle this instead of clearing effects
        AlchemyManager.getInstance().handlePlayerDisconnect(player);

        // Remove all summons for Beastmaster players
        String ascendancy = classManager.getPlayerAscendancy(uuid);
        if ("Beastmaster".equalsIgnoreCase(ascendancy) && ascendancySkillEffectIntegrator != null) {
            com.maks.myexperienceplugin.Class.skills.effects.ascendancy.BeastmasterSkillEffectsHandler handler = 
                (com.maks.myexperienceplugin.Class.skills.effects.ascendancy.BeastmasterSkillEffectsHandler) 
                ascendancySkillEffectIntegrator.getHandler("Beastmaster");

            if (handler != null) {
                handler.removeAllSummons(player);
                getLogger().info("[BEASTMASTER] Removed all summons for " + player.getName() + " on logout");
            }
        }

        // Clean up FlameWarden player data
        if ("FlameWarden".equals(ascendancy) && ascendancySkillEffectIntegrator != null) {
            com.maks.myexperienceplugin.Class.skills.effects.ascendancy.FlameWardenSkillEffectsHandler handler = 
                (com.maks.myexperienceplugin.Class.skills.effects.ascendancy.FlameWardenSkillEffectsHandler) 
                ascendancySkillEffectIntegrator.getHandler("FlameWarden");

            if (handler != null) {
                handler.clearPlayerData(uuid);
                getLogger().info("[FLAMEWARDEN] Cleared all data for " + player.getName() + " on logout");
            }
        }

        // Clean up ScaleGuardian player data
        if ("ScaleGuardian".equals(ascendancy) && ascendancySkillEffectIntegrator != null) {
            com.maks.myexperienceplugin.Class.skills.effects.ascendancy.ScaleGuardianSkillEffectsHandler handler = 
                (com.maks.myexperienceplugin.Class.skills.effects.ascendancy.ScaleGuardianSkillEffectsHandler) 
                ascendancySkillEffectIntegrator.getHandler("ScaleGuardian");

            if (handler != null) {
                handler.clearPlayerData(uuid);
                getLogger().info("[SCALEGUARDIAN] Cleared all data for " + player.getName() + " on logout");
            }
        }

        // Clean up Shadowstalker player data
        if ("Shadowstalker".equals(ascendancy) && ascendancySkillEffectIntegrator != null) {
            com.maks.myexperienceplugin.Class.skills.effects.ascendancy.ShadowstalkerSkillEffectsHandler handler = 
                (com.maks.myexperienceplugin.Class.skills.effects.ascendancy.ShadowstalkerSkillEffectsHandler) 
                ascendancySkillEffectIntegrator.getHandler("Shadowstalker");

            if (handler != null) {
                handler.clearPlayerData(uuid);
                getLogger().info("[SHADOWSTALKER] Cleared all data for " + player.getName() + " on logout");
            }
        }

        // Clean up Earthwarden player data
        if ("Earthwarden".equals(ascendancy) && ascendancySkillEffectIntegrator != null) {
            com.maks.myexperienceplugin.Class.skills.effects.ascendancy.EarthwardenSkillEffectsHandler handler = 
                (com.maks.myexperienceplugin.Class.skills.effects.ascendancy.EarthwardenSkillEffectsHandler) 
                ascendancySkillEffectIntegrator.getHandler("Earthwarden");

            if (handler != null) {
                handler.clearPlayerData(uuid);
                getLogger().info("[EARTHWARDEN] Cleared all data for " + player.getName() + " on logout");
            }
        }

        // Clean up Berserker player data
        if ("Berserker".equals(ascendancy) && ascendancySkillEffectIntegrator != null) {
            com.maks.myexperienceplugin.Class.skills.effects.ascendancy.BerserkerSkillEffectsHandler handler =
                (com.maks.myexperienceplugin.Class.skills.effects.ascendancy.BerserkerSkillEffectsHandler)
                ascendancySkillEffectIntegrator.getHandler("Berserker");

            if (handler != null) {
                handler.clearPlayerData(uuid);
                getLogger().info("[BERSERKER] Cleared all data for " + player.getName() + " on logout");
            }
        }

        // Clean up Elementalist player data
        if ("Elementalist".equals(ascendancy) && ascendancySkillEffectIntegrator != null) {
            com.maks.myexperienceplugin.Class.skills.effects.ascendancy.ElementalistSkillEffectsHandler handler =
                (com.maks.myexperienceplugin.Class.skills.effects.ascendancy.ElementalistSkillEffectsHandler)
                ascendancySkillEffectIntegrator.getHandler("Elementalist");

            if (handler != null) {
                handler.clearPlayerData(uuid);
                getLogger().info("[ELEMENTALIST] Cleared all data for " + player.getName() + " on logout");
            }
        }

        // Clean up Chronomancer player data
        if ("Chronomancer".equals(ascendancy) && ascendancySkillEffectIntegrator != null) {
            com.maks.myexperienceplugin.Class.skills.effects.ascendancy.ChronomancerSkillEffectsHandler handler =
                (com.maks.myexperienceplugin.Class.skills.effects.ascendancy.ChronomancerSkillEffectsHandler)
                ascendancySkillEffectIntegrator.getHandler("Chronomancer");

            if (handler != null) {
                handler.clearPlayerData(uuid);
                getLogger().info("[CHRONOMANCER] Cleared all data for " + player.getName() + " on logout");
            }
        }

        // Clean up ArcaneProtector player data
        if ("ArcaneProtector".equals(ascendancy) && ascendancySkillEffectIntegrator != null) {
            com.maks.myexperienceplugin.Class.skills.effects.ascendancy.ArcaneProtectorSkillEffectsHandler handler =
                (com.maks.myexperienceplugin.Class.skills.effects.ascendancy.ArcaneProtectorSkillEffectsHandler)
                ascendancySkillEffectIntegrator.getHandler("ArcaneProtector");

            if (handler != null) {
                handler.clearPlayerData(uuid);
                getLogger().info("[ARCANEPROTECTOR] Cleared all data for " + player.getName() + " on logout");
            }
        }

        // Clean up CriticalStrikeSystem data
        if (criticalStrikeSystem != null) {
            criticalStrikeSystem.clearPlayerData(uuid);
            getLogger().info("[CRITICAL] Cleared critical strike data for " + player.getName() + " on logout");
        }
    }
    public SkillTreeManager getSkillTreeManager() {
        return skillTreeManager;
    }
    public SkillEffectsHandler getSkillEffectsHandler() {
        return skillEffectsHandler;
    }
    public SkillPurchaseManager getSkillPurchaseManager() {
        return skillPurchaseManager;
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Delay to ensure all data is loaded
        Bukkit.getScheduler().runTaskLater(this, () -> {
            // Force recalculation of skill points
            skillTreeManager.updateSkillPoints(player);

            // Use a local debugging flag if not defined at class level
            final int DEBUG_FLAG = 0;
            if (DEBUG_FLAG == 1) {
                getLogger().info("Recalculated skill points for " + player.getName() +
                        " upon login (level " + getPlayerLevel(player) + ")");
            }

            // Respawn summons for Beastmaster players
            UUID uuid = player.getUniqueId();
            String ascendancy = classManager.getPlayerAscendancy(uuid);
            if ("Beastmaster".equalsIgnoreCase(ascendancy) && ascendancySkillEffectIntegrator != null) {
                com.maks.myexperienceplugin.Class.skills.effects.ascendancy.BeastmasterSkillEffectsHandler handler = 
                    (com.maks.myexperienceplugin.Class.skills.effects.ascendancy.BeastmasterSkillEffectsHandler) 
                    ascendancySkillEffectIntegrator.getHandler("Beastmaster");

                if (handler != null) {
                    // Add a slight delay to ensure all player data is loaded
                    Bukkit.getScheduler().runTaskLater(this, () -> {
                        handler.checkAndSummonCreatures(player);
                        getLogger().info("[BEASTMASTER] Checked and respawned summons for " + player.getName() + " on login");
                    }, 20L); // 1 second delay
                }
            }

            // Apply effects for FlameWarden players
            if ("FlameWarden".equals(ascendancy) && ascendancySkillEffectIntegrator != null) {
                com.maks.myexperienceplugin.Class.skills.effects.ascendancy.FlameWardenSkillEffectsHandler handler = 
                    (com.maks.myexperienceplugin.Class.skills.effects.ascendancy.FlameWardenSkillEffectsHandler) 
                    ascendancySkillEffectIntegrator.getHandler("FlameWarden");

                if (handler != null) {
                    // Add a slight delay to ensure all player data is loaded
                    Bukkit.getScheduler().runTaskLater(this, () -> {
                        // Check if player has the Fire Resistance skill (ID 3)
                        if (skillTreeManager.getPurchasedSkills(uuid).contains(300003)) {
                            // Apply fire resistance effect
                            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                                org.bukkit.potion.PotionEffectType.FIRE_RESISTANCE, 
                                Integer.MAX_VALUE, 0, false, false, true));
                            getLogger().info("[FLAMEWARDEN] Applied infinite fire resistance to " + player.getName() + " on login");
                        }
                    }, 20L); // 1 second delay
                }
            }
        }, 40L); // 2 second delay
    }

    // Add this method to your MyExperiencePlugin class:

    /**
     * Get the SkillTreeGUI instance
     * @return The SkillTreeGUI instance
     */
    public SkillTreeGUI getSkillTreeGUI() {
        return skillTreeGUI;
    }

    public AscendancySkillEffectIntegrator getAscendancySkillEffectIntegrator() {
        return ascendancySkillEffectIntegrator;
    }

    public com.maks.myexperienceplugin.Class.skills.systems.CriticalStrikeSystem getCriticalStrikeSystem() {
        return criticalStrikeSystem;
    }

    // Dodaj tę klasę wewnętrzną do MyExperiencePlugin:
    public static class ExpBoost {
        private final double multiplier;
        private final long expiryTime;

        public ExpBoost(double multiplier, long expiryTime) {
            this.multiplier = multiplier;
            this.expiryTime = expiryTime;
        }

        public double getMultiplier() {
            return multiplier;
        }

        public long getExpiryTime() {
            return expiryTime;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }

        public long getRemainingMinutes() {
            long remaining = expiryTime - System.currentTimeMillis();
            return Math.max(0, remaining / (1000 * 60));
        }
    }

    /**
     * Pobiera aktualny exp boost gracza
     */
    public double getPlayerExpBoost(Player player) {
        ExpBoost boost = playerExpBoosts.get(player.getUniqueId());
        if (boost == null || boost.isExpired()) {
            return 0.0; // Brak boost
        }
        return boost.getMultiplier();
    }

    /**
     * Usuwa wygasły boost gracza
     */
    public void removeExpiredBoost(UUID playerId) {
        ExpBoost boost = playerExpBoosts.get(playerId);
        if (boost != null && boost.isExpired()) {
            playerExpBoosts.remove(playerId);
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.sendMessage("§eYour EXP boost has expired!");
            }
        }
    }

    /**
     * Dodaje exp boost dla gracza
     */
    public void addExpBoost(Player player, double multiplierPercent, int hours) {
        UUID playerId = player.getUniqueId();
        long expiryTime = System.currentTimeMillis() + (hours * 60L * 60L * 1000L);

        ExpBoost existingBoost = playerExpBoosts.get(playerId);
        if (existingBoost != null && !existingBoost.isExpired()) {
            // Jeśli gracz ma już aktywny boost
            if (existingBoost.getMultiplier() >= multiplierPercent) {
                player.sendMessage("§cYou already have a better or equal EXP boost active! " +
                        "Current: §6+" + (int)existingBoost.getMultiplier() + "%§c, remaining: §6" + 
                        existingBoost.getRemainingMinutes() + " minutes");
                return;
            } else {
                player.sendMessage("§aYour previous EXP boost has been replaced with a better one!");
            }
        }

        ExpBoost boost = new ExpBoost(multiplierPercent, expiryTime);
        playerExpBoosts.put(playerId, boost);

        player.sendMessage("§aYou have activated §6+" + (int)multiplierPercent + "% EXP boost§a for §6" + hours + " hours§a!");

        // Poinformuj gracza o statusie boost
        showExpBoostStatus(player);
    }

    /**
     * Pokazuje status exp boost gracza
     */
    public void showExpBoostStatus(Player player) {
        ExpBoost boost = playerExpBoosts.get(player.getUniqueId());
        if (boost == null || boost.isExpired()) {
            player.sendMessage("§7You don't have any active EXP boost.");
        } else {
            long remainingMinutes = boost.getRemainingMinutes();
            long hours = remainingMinutes / 60;
            long minutes = remainingMinutes % 60;

            player.sendMessage("§aActive EXP Boost: §6+" + (int)boost.getMultiplier() + "%");
            player.sendMessage("§aTime remaining: §6" + hours + "h " + minutes + "m");
        }
    }

    /**
     * Czyści wszystkie wygasłe boost
     */
    public void cleanupExpiredBoosts() {
        playerExpBoosts.entrySet().removeIf(entry -> {
            if (entry.getValue().isExpired()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null) {
                    player.sendMessage("§eYour EXP boost has expired!");
                }
                return true;
            }
            return false;
        });
    }
}
