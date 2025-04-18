package com.maks.myexperienceplugin;

import com.maks.myexperienceplugin.Class.*;
import com.maks.myexperienceplugin.Class.skills.*;
import com.maks.myexperienceplugin.Class.skills.gui.AscendancySkillTreeGUIListener;
import com.maks.myexperienceplugin.alchemy.AlchemyItemListener;
import com.maks.myexperienceplugin.alchemy.AlchemyLevelConfig;
import com.maks.myexperienceplugin.alchemy.AlchemyResetCommand;
import com.maks.myexperienceplugin.alchemy.PhysisExpManager;
import com.maks.myexperienceplugin.exp.*;
import com.maks.myexperienceplugin.listener.*;
import com.maks.myexperienceplugin.party.PartyCommand;
import com.maks.myexperienceplugin.party.PartyManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
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

import java.io.File;
import java.util.*;

public class MyExperiencePlugin extends JavaPlugin implements Listener {

    private DatabaseManager databaseManager;
    private Economy economy;
    public MoneyRewardHandler moneyRewardHandler;
    public final HashMap<UUID, Integer> playerLevels = new HashMap<>();
    private final HashMap<UUID, Double> playerRequiredXP = new HashMap<>();
    public final HashMap<UUID, Double> playerCurrentXP = new HashMap<>();
    private final HashMap<Integer, Double> xpPerLevel = new HashMap<>();
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
    public ClassManager getClassManager() {
        return classManager;
    }
    public ClassGUI getClassGUI() {
        return classGUI;
    }
    private AscendancySkillTreeGUI ascendancySkillTreeGUI; // Add this field
    private SkillPurchaseManager skillPurchaseManager;

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

        // Initialize core managers and handlers
        partyManager = new PartyManager(this);
        moneyRewardHandler = new MoneyRewardHandler(economy, this);
        playerLevelDisplayHandler = new PlayerLevelDisplayHandler(this);
        alchemyLevelConfig = new AlchemyLevelConfig(this);
        classManager = new ClassManager(this);
        classGUI = new ClassGUI(this);

        // Initialize XP system
        initializeXPLevels();
        loadExpTable();

        // Initialize skill system
        skillTreeManager = new SkillTreeManager(this);
        skillEffectsHandler = new SkillEffectsHandler(this, skillTreeManager);
        skillEffectsHandler.initializePeriodicTasks();
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

        // Register alchemy listeners
        getServer().getPluginManager().registerEvents(new TotemEffectListener(), this);
        getServer().getPluginManager().registerEvents(new LifestealListener(), this);
        getServer().getPluginManager().registerEvents(new ImmunityListener(), this);
        getServer().getPluginManager().registerEvents(new AlchemyItemListener(this, alchemyLevelConfig), this);
        getServer().getPluginManager().registerEvents(new PhysisExpListener(this), this);

        // Register class system listeners
        getServer().getPluginManager().registerEvents(new ClassGUIListener(this), this);
        getServer().getPluginManager().registerEvents(new ClassResetItemListener(this), this);

        // Register skill system listeners
        getServer().getPluginManager().registerEvents(skillEffectsHandler, this);
        getServer().getPluginManager().registerEvents(new SkillTreeGUIListener(this, skillTreeManager, skillTreeGUI, skillPurchaseManager), this);
        getServer().getPluginManager().registerEvents(new AscendancySkillTreeGUIListener(this, skillTreeManager, ascendancySkillTreeGUI, skillPurchaseManager), this);

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
        getCommand("chose_class").setExecutor(new ChoseClassCommand(this));
        getCommand("chose_ascendancy").setExecutor(new ChoseAscendancyCommand(this));
        getCommand("alchemy_reset").setExecutor(new AlchemyResetCommand());

        // Register commands - Skill system
        getCommand("skilltree").setExecutor(new SkillTreeCommand(this, skillTreeGUI));
        getCommand("skillstats").setExecutor(new SkillStatsCommand(this, skillEffectsHandler));
        getCommand("skilltree2").setExecutor(new AscendancySkillTreeCommand(this, ascendancySkillTreeGUI));
        getCommand("playerattributes").setExecutor(new PlayerAttributesCommand(this, skillEffectsHandler));

        // Register commands - Reset attributes
        ResetAttributesCommand resetAttributesCommand = new ResetAttributesCommand(this, skillEffectsHandler);
        getCommand("resetattributes").setExecutor(resetAttributesCommand);
        getCommand("resetattributes").setTabCompleter(resetAttributesCommand);

        ForceUpdateSkillPointsCommand forceUpdateSkillPointsCommand = new ForceUpdateSkillPointsCommand(
                this,
                skillTreeManager,
                skillTreeGUI  // Pass the SkillTreeGUI directly
        );
        getCommand("updateskillpoints").setExecutor(forceUpdateSkillPointsCommand);
        getCommand("updateskillpoints").setTabCompleter(forceUpdateSkillPointsCommand);
        // Start periodic tasks
        new PeriodicClassReminder(this).runTaskTimer(this, 20L, 1200L); // 20L = 1s, 1200L = 60s
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
                double requiredXP = playerRequiredXP.getOrDefault(playerId, xpPerLevel.get(level));

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
        double currentXP = playerCurrentXP.getOrDefault(playerId, 0.0);
        currentXP += xp;
        playerCurrentXP.put(playerId, currentXP);

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
            requiredXP = xpPerLevel.getOrDefault(currentLevel, Math.pow(currentLevel * 100 + 100, 1.013));

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
        double requiredXP = playerRequiredXP.getOrDefault(
                playerId,
                xpPerLevel.get(playerLevels.getOrDefault(playerId, 1))
        );
        int level = playerLevels.getOrDefault(playerId, 1);

        player.setLevel(level);
        if (level >= maxLevel) {
            player.setExp(0.0f);
        } else {
            float progress = (float) (currentXP / requiredXP);
            player.setExp(progress);
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
        int level = playerLevels.getOrDefault(player.getUniqueId(), 1);
        player.setPlayerListName(String.format("§b[ %d ] §r%s", level, player.getName()));
        player.setCustomName(String.format("§b[ %d ] §r%s", level, player.getName()));
        player.setCustomNameVisible(true);
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
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Clear physis exp
        PhysisExpManager.getInstance().clearPlayerBonus(uuid);

        // Clean up skill purchase manager resources
        if (skillPurchaseManager != null) {
            skillPurchaseManager.cleanup(uuid);
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
            final int DEBUG_FLAG = 1;
            if (DEBUG_FLAG == 1) {
                getLogger().info("Recalculated skill points for " + player.getName() +
                        " upon login (level " + getPlayerLevel(player) + ")");
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
}
