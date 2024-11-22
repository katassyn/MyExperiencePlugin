package com.maks.myexperienceplugin;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
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
    private PartyManager partyManager; // Added PartyManager
    private PlayerLevelDisplayHandler playerLevelDisplayHandler;
    private final int maxLevel = 100;

    public MoneyRewardHandler getMoneyRewardHandler() {
        return moneyRewardHandler;
    }

    public static MyExperiencePlugin getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public PartyManager getPartyManager() {
        return partyManager;
    }

    @Override
    public void onEnable() {
        instance = this;

        // Load configuration
        saveDefaultConfig();
        String host = getConfig().getString("database.host");
        String port = getConfig().getString("database.port");
        String database = getConfig().getString("database.name");
        String username = getConfig().getString("database.user");
        String password = getConfig().getString("database.password");

        // Initialize database
        databaseManager = new DatabaseManager(host, port, database, username, password);

        // Initialize PartyManager before any class that uses it
        partyManager = new PartyManager(this);

        // Set up economy
        if (!setupEconomy()) {
            getLogger().severe("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        moneyRewardHandler = new MoneyRewardHandler(economy, this);

        Bukkit.getLogger().info("MyExperiencePlugin has been enabled!");
        getServer().getPluginManager().registerEvents(this, this);
        initializeXPLevels();  // Initialize XP levels

        loadExpTable();

        // Register event listeners
        getServer().getPluginManager().registerEvents(new MythicMobXPHandler(this), this);
        getServer().getPluginManager().registerEvents(new ChatLevelHandler(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDisconnectListener(partyManager), this);

        // Register commands and their executors
        PartyCommand partyCommand = new PartyCommand(this, partyManager);
        getCommand("party").setExecutor(partyCommand);
        getCommand("party").setTabCompleter(partyCommand);

        getCommand("exp_table").setExecutor(new ExpTableCommand(this));
        getCommand("exp_table").setTabCompleter(new ExpTableCommand(this));

        getCommand("exp_money").setExecutor(moneyRewardHandler);
        getCommand("exp_money").setTabCompleter(moneyRewardHandler);

        getCommand("exp").setExecutor(this); // Assuming the main class handles /exp command
        getCommand("reload_bonus").setExecutor(new ReloadBonusCommand(this));

        getCommand("top").setExecutor(new TopCommand(this));
        ExperienceCommandHandler experienceCommandHandler = new ExperienceCommandHandler(this);
        getCommand("get_lvl").setExecutor(experienceCommandHandler);
        getCommand("exp_give").setExecutor(experienceCommandHandler);
        getCommand("exp_give_p").setExecutor(experienceCommandHandler);
        playerLevelDisplayHandler = new PlayerLevelDisplayHandler(this);
        getServer().getPluginManager().registerEvents(playerLevelDisplayHandler, this);
        getCommand("bonus_exp").setExecutor(new BonusExpCommand(this));
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("MyExperiencePlugin has been disabled!");
        // databaseManager.closeConnection();
    }

    // Economy setup
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

    // Initialize XP levels
    private void initializeXPLevels() {
        double previousXP = 100;  // Starting XP value for level 2

        for (int level = 1; level <= 100; level++) {
            previousXP = Math.round(Math.pow(previousXP + 100, 1.013) * 100.0) / 100.0;
            xpPerLevel.put(level, previousXP);
        }
    }

    // Function to grant XP
    public void addXP(Player player, double xp) {
        UUID playerId = player.getUniqueId();
        double currentXP = playerCurrentXP.getOrDefault(playerId, 0.0);

        // Add XP to current total
        currentXP += xp;
        playerCurrentXP.put(playerId, currentXP);

        // Check if player leveled up
        checkLevelUp(player);

        // Update XP bar after gaining XP
        updatePlayerXPBar(player);
    }

    // Function to check if player leveled up
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

            // Wyślij wiadomość o awansie dla specjalnych poziomów
            broadcastLevelUpMessage(player, currentLevel);

            // Przyznaj nagrody pieniężne
            MyExperiencePlugin.getInstance().getMoneyRewardHandler().onLevelUp(player, currentLevel);

            // Zapisz nowy poziom i XP do bazy danych
            MyExperiencePlugin.getInstance().getDatabaseManager().savePlayerData(player, currentLevel, currentXP);

            // Odśwież wyświetlanie nicku w tabie i nad głową
            updatePlayerDisplay(player);
        }

        // Zaktualizuj XP gracza w mapach
        playerCurrentXP.put(playerId, currentXP);
        playerRequiredXP.put(playerId, requiredXP);

        // Zaktualizuj pasek XP gracza
        updatePlayerXPBar(player);

        // Zapisz dane gracza do bazy danych
        MyExperiencePlugin.getInstance().getDatabaseManager().savePlayerData(player, currentLevel, currentXP);
    }

    private void updatePlayerDisplay(Player player) {
        int level = playerLevels.getOrDefault(player.getUniqueId(), 1);

        // Ustaw nick w Tab
        player.setPlayerListName(String.format("§b[ %d ] §r%s", level, player.getName()));

        // Ustaw nick nad głową
        player.setCustomName(String.format("§b[ %d ] §r%s", level, player.getName()));
        player.setCustomNameVisible(true);
        Bukkit.getLogger().info("Updating display for " + player.getName() + " with level " + level);

    }




    public int getPlayerLevel(Player player) {
        return playerLevels.getOrDefault(player.getUniqueId(), 1);
    }

    // Function to update the Minecraft XP bar
    public void updatePlayerXPBar(Player player) {
        UUID playerId = player.getUniqueId();
        double currentXP = playerCurrentXP.getOrDefault(playerId, 0.0);
        double requiredXP = playerRequiredXP.getOrDefault(playerId, xpPerLevel.get(playerLevels.getOrDefault(playerId, 1)));
        int level = playerLevels.getOrDefault(playerId, 1);

        // Set player's level on the bar
        player.setLevel(level);

        // If player has max level, set XP bar to 0%
        if (level >= maxLevel) {
            player.setExp(0.0f);
        } else {
            // Set XP bar to percentage fill
            float progress = (float) currentXP / (float) requiredXP;
            player.setExp(progress);
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

        // Load XP values for each mob from the file
        for (String mobName : expTableConfig.getConfigurationSection("xp_per_mob").getKeys(false)) {
            double xp = expTableConfig.getDouble("xp_per_mob." + mobName);
            xpPerMob.put(mobName, xp);
        }
    }

    public double getXpForMob(String mobName) {
        return xpPerMob.getOrDefault(mobName, 0.0);
    }

    // Command /exp
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            UUID playerId = player.getUniqueId();
            int level = playerLevels.getOrDefault(playerId, 1);
            double currentXP = playerCurrentXP.getOrDefault(playerId, 0.0);
            double requiredXP = playerRequiredXP.getOrDefault(playerId, xpPerLevel.get(level));

            double progress;

            if (level >= maxLevel) {
                progress = 100.0;
                currentXP = requiredXP;
            } else {
                progress = (currentXP / requiredXP) * 100;
            }

            // Format numbers
            String formattedCurrentXP = formatNumber(currentXP);
            String formattedRequiredXP = formatNumber(requiredXP);
            String formattedProgress = String.format("%.2f%%", progress);

            // Send formatted messages
            player.sendMessage("§e===== §6[§aXP Info§6] §e=====");
            player.sendMessage("§6Level: §a" + level);
            player.sendMessage("§6XP: §a" + formattedCurrentXP + "§6 / §a" + formattedRequiredXP);
            player.sendMessage("§6Progress: §a" + formattedProgress);
            player.sendMessage("§e======================");

            return true;
        } else if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage("This command works only for players.");
        }
        return false;
    }
    public void broadcastLevelUpMessage(Player player, int level) {
        // Lista poziomów dla specjalnych wiadomości
        List<Integer> specialLevels = Arrays.asList(20, 30, 40, 50, 60, 70, 75, 80, 85, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99);

        // Sprawdź, czy poziom jest specjalnym poziomem
        if (specialLevels.contains(level)) {
            String message = String.format("§7[§6INFO§7] §6%s §7has reached level §6%d§7!", player.getName(), level);
            Bukkit.broadcastMessage(message);
        }

        // Specjalna wiadomość dla maksymalnego poziomu (100)
        if (level == 100) {
            String maxLevelMessage = String.format("§7[§4LEGENDARY§7] §6%s §7has reached the §4MAX LEVEL§7! They are now a server legend!", player.getName());
            Bukkit.broadcastMessage(maxLevelMessage);
        }
    }

    // Method to format large numbers with K, M, B
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
    public PlayerLevelDisplayHandler getPlayerLevelDisplayHandler() {
        return playerLevelDisplayHandler;
    }
    public boolean isBonusExpEnabled() {
        return getConfig().getBoolean("Bonus_exp.Enabled", false);
    }

    public double getBonusExpValue() {
        return getConfig().getDouble("Bonus_exp.Value", 100.0);
    }

}
