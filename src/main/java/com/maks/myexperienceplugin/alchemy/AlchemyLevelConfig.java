package com.maks.myexperienceplugin.alchemy;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AlchemyLevelConfig {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File configFile;
    private final Map<String, Integer> levelRequirements = new HashMap<>();

    public AlchemyLevelConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "alchemy_levels.yml");
        }

        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            plugin.saveResource("alchemy_levels.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        loadLevelRequirements();
    }

    private void loadLevelRequirements() {
        levelRequirements.clear();
        if (config.contains("level_requirements")) {
            for (String key : config.getConfigurationSection("level_requirements").getKeys(false)) {
                levelRequirements.put(key, config.getInt("level_requirements." + key));
            }
        }
    }

    public int getRequiredLevel(String key) {
        return levelRequirements.getOrDefault(key, 1);
    }

    public void reload() {
        loadConfig();
    }
}
