package com.maks.myexperienceplugin.exp;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.TabCompleter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MoneyRewardHandler implements CommandExecutor, TabCompleter {

    private final Economy economy;
    private final JavaPlugin plugin; // Reference to your main plugin
    private FileConfiguration expMoneyConfig;

    public MoneyRewardHandler(Economy economy, JavaPlugin plugin) {
        this.economy = economy;
        this.plugin = plugin;
        loadExpMoneyConfig();
        // Register the command executor and tab completer for /exp_money
        plugin.getCommand("exp_money").setExecutor(this);
        plugin.getCommand("exp_money").setTabCompleter(this);
    }

    // Method to load the exp_money.yml file
    public void loadExpMoneyConfig() {
        File expMoneyFile = new File(plugin.getDataFolder(), "exp_money.yml");
        if (!expMoneyFile.exists()) {
            plugin.saveResource("exp_money.yml", false);
        }
        expMoneyConfig = YamlConfiguration.loadConfiguration(expMoneyFile);
    }

    // Method called when a player levels up
    public void onLevelUp(Player player, int newLevel) {
       // plugin.getLogger().info("Player " + player.getName() + " reached level " + newLevel); // Sprawdzenie poziomu
        double rewardAmount = expMoneyConfig.getDouble("rewards." + newLevel, 0);
      //  plugin.getLogger().info("Reward amount: " + rewardAmount); // Sprawdzenie nagrody
        if (rewardAmount > 0) {
            economy.depositPlayer(player, rewardAmount);
            player.sendMessage(String.format("§6[LEVEL UP]§r You have received §a%.2f$§r!", rewardAmount));
        } else {
            player.sendMessage("§6[LEVEL UP]§r No monetary reward is set for this level.");
        }
    }




    // Command handling
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("exp_money")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                reloadExpMoneyConfig();
                sender.sendMessage("exp_money.yml has been reloaded!");
                return true;
            }
        }
        return false;
    }

    // Reload the exp_money.yml configuration
    public void reloadExpMoneyConfig() {
        loadExpMoneyConfig();
    }

    // Tab completion for the command
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("exp_money") && args.length == 1) {
            if ("reload".startsWith(args[0].toLowerCase())) {
                suggestions.add("reload");
            }
        }
        return suggestions;
    }
    public void depositMoney(Player player, double amount) {
        if (economy != null) {
            economy.depositPlayer(player, amount);
        }
    }
}
