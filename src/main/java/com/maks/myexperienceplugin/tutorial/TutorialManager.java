package com.maks.myexperienceplugin.tutorial;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TutorialManager {
    private final MyExperiencePlugin plugin;
    private final Map<UUID, TutorialProgress> playerProgress = new HashMap<>();
    private final Map<Integer, String> tutorialMessages = new HashMap<>();

    public TutorialManager(MyExperiencePlugin plugin) {
        this.plugin = plugin;
        initializeTutorialMessages();
        initializeTutorialTable();
    }

    private void initializeTutorialTable() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = plugin.getDatabaseManager().getConnection()) {
                String createTutorialTable = "CREATE TABLE IF NOT EXISTS player_tutorials (" +
                        "uuid VARCHAR(36)," +
                        "tutorial_level INT," +
                        "completed BOOLEAN DEFAULT TRUE," +
                        "PRIMARY KEY (uuid, tutorial_level))";
                
                try (PreparedStatement stmt = connection.prepareStatement(createTutorialTable)) {
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to initialize tutorial table: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void initializeTutorialMessages() {
        // Level 1 - Server basics
        tutorialMessages.put(1, 
            "§e========================================\n" +
            "§6§lWELCOME TO THE SERVER!\n" +
            "§e========================================\n" +
            "§aWelcome, adventurer! Here's how to get started:\n\n" +
            "§2» §fTo begin leveling up, approach the §6§lGrand EXP Portal§f\n" +
            "§2» §fChoose an experience area and enter the portal\n" +
            "§2» §fThere's no §c/spawn§f command - use §c/suicide§f instead\n" +
            "§2» §fCheck your inventory's top-right corner for the §6§lMain Menu§f button\n" +
            "§2» §fUse it to access storage, suicide, and skill trees!\n" +
            "§e========================================");

        // Level 2 - Innkeeper
        tutorialMessages.put(2,
            "§e========================================\n" +
            "§6§lINNKEEPER UNLOCKED!\n" +
            "§e========================================\n" +
            "§aYou've discovered the Innkeeper mechanic!\n\n" +
            "§2» §fFind the §6§lInnkeeper§f at spawn\n" +
            "§2» §fHe offers §adaily§f, §bweekly§f, and §dmonthly§f quests\n" +
            "§2» §fQuest difficulty and rewards scale with your level\n" +
            "§2» §fComplete these for valuable rewards and progression!\n" +
            "§e========================================");

        // Level 5 - Biologist
        tutorialMessages.put(5,
            "§e========================================\n" +
            "§6§lBIOLOGIST UNLOCKED!\n" +
            "§e========================================\n" +
            "§aThe Biologist mechanic is now available!\n\n" +
            "§2» §fFind the §6§lBiologist§f in §bHiraja§f (the city)\n" +
            "§2» §fHe gives research quests requiring materials from specific mobs\n" +
            "§2» §fEach item has a chance of acceptance when turned in\n" +
            "§2» §fYou can attempt submission once every §c24 hours§f\n" +
            "§2» §fUse §dBiologist's Mixture§f to refresh this limit!\n" +
            "§2» §f§l§cIMPORTANT:§f Complete his missions for powerful permanent buffs!\n" +
            "§e========================================");

        // Level 30 - Grave Keeper
        tutorialMessages.put(30,
            "§e========================================\n" +
            "§6§lGRAVE KEEPER UNLOCKED!\n" +
            "§e========================================\n" +
            "§aThe Grave Keeper system is now active!\n\n" +
            "§2» §fKill mobs of specific types (undead, poison, water, etc.)\n" +
            "§2» §fEarn §6permanent damage and defence bonuses§f against those mob types\n" +
            "§2» §fThe more you kill, the stronger you become against them\n" +
            "§2» §fCheck your progress and bonuses with the Grave Keeper\n" +
            "§e========================================");

        // Level 60 - Fisherman
        tutorialMessages.put(60,
            "§e========================================\n" +
            "§6§lFISHERMAN UNLOCKED!\n" +
            "§e========================================\n" +
            "§aYou can now access the Fisherman!\n\n" +
            "§2» §fPurchase a §bfishing rod§f to get started\n" +
            "§2» §fFish at the fishing grounds to level up your rod\n" +
            "§2» §fHigher rod levels = better drop chances\n" +
            "§2» §fComplete fishing quests for rewards\n" +
            "§2» §f§l§6PRIMARY SOURCE OF RUNES!§f\n" +
            "§2» §fAlso obtain §dtreasure maps§f that the §5§lPirate King§f can \"discover\"!\n" +
            "§e========================================");

        // Level 70 - Miner
        tutorialMessages.put(70,
            "§e========================================\n" +
            "§6§lMINER UNLOCKED!\n" +
            "§e========================================\n" +
            "§aThe mining system is now available!\n\n" +
            "§2» §fPurchase a §7pickaxe§f with limited durability\n" +
            "§2» §fYou have §c100 stamina§f that refills every §e12 hours§f from first use\n" +
            "§2» §fCheck stamina with §b/stamina§f command\n" +
            "§2» §fChoose between §anormal§f and §6premium§f mines (needs ticket)\n" +
            "§2» §fEach mine entry costs §c10 stamina§f\n" +
            "§2» §f§l§6MAIN SOURCE OF INCOME§f and gemstone creation\n" +
            "§2» §fFind unique §bspheres§f for pickaxe enchanting and repairs\n" +
            "§2» §fUse §dMiner Elixir§f to restore stamina!\n" +
            "§e========================================");

        // Level 75 - Beekeeper & Augmenter
        tutorialMessages.put(75,
            "§e========================================\n" +
            "§6§lBEEKEEPER & AUGMENTER UNLOCKED!\n" +
            "§e========================================\n" +
            "§aBoth systems are now available in §bHiraja§f!\n\n" +
            "§6§lBEEKEEPER:\n" +
            "§2» §fPurchase §ehives§f that passively collect §6honey§f and create §alarvae§f\n" +
            "§2» §fGrow larvae into adult §ebees§f, §5queens§f, or §7drones§f\n" +
            "§2» §fDevelop hives to obtain valuable honey\n\n" +
            "§6§lAUGMENTER:\n" +
            "§2» §fUse honey to apply §dquality§f to accessories\n" +
            "§2» §f§dQuality multiplies stats§f: 10% quality = stats × 0.1\n" +
            "§e========================================");

        // Level 80 - The Conjurer
        tutorialMessages.put(80,
            "§e========================================\n" +
            "§6§lTHE CONJURER UNLOCKED!\n" +
            "§e========================================\n" +
            "§aRunic word system is now available!\n\n" +
            "§2» §fAccess §5§lThe Conjurer§f for runic word application\n" +
            "§2» §fApply runic words to weapons for powerful effects\n" +
            "§2» §f§l§cFIRST:§f You must obtain runic word §eschemas§f\n" +
            "§2» §fFind schemas by completing the §6§lQ§f quest!\n" +
            "§2» §fUnlock this powerful weapon enhancement system\n" +
            "§e========================================");

        // Level 85 - Farmer
        tutorialMessages.put(85,
            "§e========================================\n" +
            "§6§lFARMER UNLOCKED!\n" +
            "§e========================================\n" +
            "§aFarming system available in §bHiraja§f!\n\n" +
            "§2» §fVisit your own §apersonal farm§f\n" +
            "§2» §fTend to different types of §2crops§f\n" +
            "§2» §fPurchase additional §efarm installations§f and upgrades\n" +
            "§2» §fSell your §aharvested fruits§f\n" +
            "§2» §fCollect §dcrafting materials§f that occasionally drop\n" +
            "§e========================================");

        // Level 40 - Dungeon Master
        tutorialMessages.put(40,
            "§e========================================\n" +
            "§6§lDUNGEON MASTER UNLOCKED!\n" +
            "§e========================================\n" +
            "§aGroup dungeons are now available in §bHiraja§f!\n\n" +
            "§2» §fFind the §5§lDungeon Master§f to access dungeons\n" +
            "§2» §fRequires a §dparty§f of appropriate size\n" +
            "§2» §fNeed specific §6dungeon keys§f for entry\n" +
            "§2» §fRewards: §eset items§f, §cboss hearts§f, and §dteam relics§f\n" +
            "§2» §f§c§lWARNING:§f Dungeons need much better gear than expected!\n" +
            "§2» §fBut they provide §6§lmassive XP rewards§f\n\n" +
            "§6§lSET IMPRINTING (Hephaistos):\n" +
            "§2» §fAdd set bonuses to any item of the same type\n" +
            "§2» §fExample: necklace to necklace, boots to boots\n" +
            "§2» §fImprinted items count toward that set\n" +
            "§2» §c§lWARNING: This action is irreversible!\n" +
            "§2» §c§lCannot upgrade imprinted items at blacksmith!\n" +
            "§e========================================");

        // Level 50 - Thread Weaving
        tutorialMessages.put(50,
                "§e========================================\n" +
                        "§6§lTHREAD WEAVING UNLOCKED!\n" +
                        "§e========================================\n" +
                        "§aThread weaving system is now available in §bHiraja§f!\n\n" +
                        "§2» §fFind the §5§lElf Taylor§f to enhance your armor\n" +
                        "§2» §fOnly §echestplates§f and §eleggings§f can be enhanced\n" +
                        "§2» §fChoose from 6 thread types: §7Shadow§f, §8Undead§f, §cFire§f, §9Water§f, §aWild§f, §2Poison§f\n" +
                        "§2» §fSelect quality: §62%§f, §e3%§f, or §c5%§f damage bonus\n" +
                        "§2» §fRequires: §fthread essence§f, §6rare essences§f, §5legendary essences§f\n" +
                        "§2» §fCosts: §6250M$§f, §e500M$§f, or §c1B$§f depending on quality\n\n" +
                        "§6§lEXTRACTION:\n" +
                        "§2» §fRemove thread weaving for §6100M$§f\n" +
                        "§2» §c§lWARNING:§f Materials are not returned!\n" +
                        "§e========================================");
        tutorialMessages.put(55,
                "§e========================================\n" +
                "§6§lBLOOD CHEST \n" +
                "§e========================================\n" +
                "§aReady to face the Blood Chest arena? Here's what to know:\n\n" +
                "§2» §fUse §cWizdar on spawn§f to enter the arena\n" +
                "§2» §fDefeat waves of §cBlood Sludges§f to summon Blood Chests\n" +
                "§2» §fOpen every chest to secure your loot before the arena seals\n" +
                "§2» §fA hidden timer will close the arena after §c10 minutes§f\n" +
                "§2» §fLeaving early or running out of time will end the challenge\n" +
                "§e========================================");
    }

    public void onPlayerLevelUp(Player player, int newLevel) {
        if (!tutorialMessages.containsKey(newLevel)) {
            return;
        }

        UUID playerId = player.getUniqueId();
        
        // Check if player already received this tutorial
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (!hasReceivedTutorial(playerId, newLevel)) {
                // Mark as received and send message
                markTutorialCompleted(playerId, newLevel);
                
                // Send message with 1 minute delay for level 1 tutorial
                long delay = newLevel == 1 ? 1200L : 40L; // 60 seconds for level 1, 2 seconds for others
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        player.sendMessage(tutorialMessages.get(newLevel));
                    }
                }, delay);
            }
        });
    }

    private boolean hasReceivedTutorial(UUID playerId, int level) {
        try (Connection connection = plugin.getDatabaseManager().getConnection()) {
            String sql = "SELECT completed FROM player_tutorials WHERE uuid = ? AND tutorial_level = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, playerId.toString());
                stmt.setInt(2, level);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to check tutorial completion: " + e.getMessage());
            return false; // Default to showing tutorial if there's an error
        }
    }

    private void markTutorialCompleted(UUID playerId, int level) {
        try (Connection connection = plugin.getDatabaseManager().getConnection()) {
            String sql = "INSERT INTO player_tutorials (uuid, tutorial_level, completed) VALUES (?, ?, TRUE) " +
                        "ON DUPLICATE KEY UPDATE completed = TRUE";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, playerId.toString());
                stmt.setInt(2, level);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to mark tutorial as completed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadPlayerTutorialProgress(Player player) {
        UUID playerId = player.getUniqueId();
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            TutorialProgress progress = new TutorialProgress();
            
            try (Connection connection = plugin.getDatabaseManager().getConnection()) {
                String sql = "SELECT tutorial_level FROM player_tutorials WHERE uuid = ? AND completed = TRUE";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, playerId.toString());
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            progress.addCompletedTutorial(rs.getInt("tutorial_level"));
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to load tutorial progress: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Cache the progress
            Bukkit.getScheduler().runTask(plugin, () -> {
                playerProgress.put(playerId, progress);
            });
        });
    }

    public void clearPlayerData(Player player) {
        playerProgress.remove(player.getUniqueId());
    }

    private static class TutorialProgress {
        private final java.util.Set<Integer> completedTutorials = new java.util.HashSet<>();
        
        public void addCompletedTutorial(int level) {
            completedTutorials.add(level);
        }
        
        public boolean hasCompletedTutorial(int level) {
            return completedTutorials.contains(level);
        }
    }
}