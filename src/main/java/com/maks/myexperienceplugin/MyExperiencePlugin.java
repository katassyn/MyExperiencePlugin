package com.maks.myexperienceplugin;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class MyExperiencePlugin extends JavaPlugin implements Listener {

    private DatabaseManager databaseManager;
    private Economy economy;
    private MoneyRewardHandler moneyRewardHandler;
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

    // Class system references
    private ClassManager classManager;
    private ClassGUI classGUI;

    public ClassManager getClassManager() {
        return classManager;
    }
    public ClassGUI getClassGUI() {
        return classGUI;
    }

    public static MyExperiencePlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        String host = getConfig().getString("database.host");
        String port = getConfig().getString("database.port");
        String database = getConfig().getString("database.name");
        String username = getConfig().getString("database.user");
        String password = getConfig().getString("database.password");

        databaseManager = new DatabaseManager(host, port, database, username, password);
        partyManager = new PartyManager(this);

        if (!setupEconomy()) {
            getLogger().severe("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        moneyRewardHandler = new MoneyRewardHandler(economy, this);

        Bukkit.getLogger().info("MyExperiencePlugin has been enabled!");
        getServer().getPluginManager().registerEvents(this, this);

        initializeXPLevels();
        loadExpTable();

        // Register event listeners
        getServer().getPluginManager().registerEvents(new MythicMobXPHandler(this), this);
        getServer().getPluginManager().registerEvents(new ChatLevelHandler(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDisconnectListener(partyManager), this);

        // Class system
        classManager = new ClassManager(this);
        classGUI = new ClassGUI(this);
        getServer().getPluginManager().registerEvents(new ClassGUIListener(this), this);
        getServer().getPluginManager().registerEvents(new ClassResetItemListener(this), this);

        // Commands
        PartyCommand partyCommand = new PartyCommand(this, partyManager);
        getCommand("party").setExecutor(partyCommand);
        getCommand("party").setTabCompleter(partyCommand);

        getCommand("exp_table").setExecutor(new ExpTableCommand(this));
        getCommand("exp_table").setTabCompleter(new ExpTableCommand(this));

        getCommand("exp_money").setExecutor(moneyRewardHandler);
        getCommand("exp_money").setTabCompleter(moneyRewardHandler);

        getCommand("exp").setExecutor(this);
        getCommand("reload_bonus").setExecutor(new ReloadBonusCommand(this));
        getCommand("top").setExecutor(new TopCommand(this));

        ExperienceCommandHandler experienceCommandHandler = new ExperienceCommandHandler(this);
        getCommand("get_lvl").setExecutor(experienceCommandHandler);
        getCommand("exp_give").setExecutor(experienceCommandHandler);
        getCommand("exp_give_p").setExecutor(experienceCommandHandler);

        playerLevelDisplayHandler = new PlayerLevelDisplayHandler(this);
        getServer().getPluginManager().registerEvents(playerLevelDisplayHandler, this);

        getCommand("bonus_exp").setExecutor(new BonusExpCommand(this));

        // Our new class system commands
        getCommand("chose_class").setExecutor(new ChoseClassCommand(this));
        getCommand("chose_ascendancy").setExecutor(new ChoseAscendancyCommand(this));
        getCommand("skilltree").setExecutor(new SkillTreeCommand(this));

        // Start the periodic reminder every 60 seconds
        new PeriodicClassReminder(this).runTaskTimer(this, 20L, 1200L); // 20L = 1s, 1200L = 60s
    }

    @Override
    public void onDisable() {
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
}
