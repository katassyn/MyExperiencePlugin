package com.maks.myexperienceplugin.Class;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ClassGUI {

    private final MyExperiencePlugin plugin;

    public ClassGUI(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Called when the player has "NoClass" and needs to choose
     */
// Updated openBaseClassGUI method for ClassGUI.java

    public void openBaseClassGUI(Player player) {
        plugin.getLogger().info("[DEBUG] ClassGUI: Creating base class inventory for " + player.getName());

        Inventory inv = Bukkit.createInventory(null, 9, ChatColor.DARK_GREEN + "Choose Your Class");

        // Ranger
        ItemStack ranger = new ItemStack(org.bukkit.Material.BOW);
        ItemMeta rangerMeta = ranger.getItemMeta();
        rangerMeta.setDisplayName(ChatColor.GREEN + "Ranger");
        rangerMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "A guardian of the wilds",
                ChatColor.GRAY + "Excels at taming beasts or stealth",
                ChatColor.GRAY + "or a more defensive approach."
        ));
        ranger.setItemMeta(rangerMeta);

        // DragonKnight - Ensure proper capitalization
        ItemStack dragonknight = new ItemStack(org.bukkit.Material.IRON_SWORD);
        ItemMeta dkMeta = dragonknight.getItemMeta();
        dkMeta.setDisplayName(ChatColor.RED + "DragonKnight"); // Consistent capitalization
        dkMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "A mighty frontline fighter",
                ChatColor.GRAY + "Strong defensive capabilities"
        ));
        dragonknight.setItemMeta(dkMeta);

        // Spellweaver
        ItemStack spellweaver = new ItemStack(org.bukkit.Material.BLAZE_ROD);
        ItemMeta swMeta = spellweaver.getItemMeta();
        swMeta.setDisplayName(ChatColor.DARK_PURPLE + "SpellWeaver"); // Consistent capitalization
        swMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "A master of arcane arts",
                ChatColor.GRAY + "Focuses on ranged spell damage",
                ChatColor.GRAY + "and magical versatility."
        ));
        spellweaver.setItemMeta(swMeta);

        inv.setItem(2, ranger);
        inv.setItem(4, dragonknight);
        inv.setItem(6, spellweaver);

        plugin.getLogger().info("[DEBUG] ClassGUI: Inventory prepared. Scheduling openInventory on main thread...");

        // Force the open call on main thread
        Bukkit.getScheduler().runTask(plugin, () -> {
            plugin.getLogger().info("[DEBUG] ClassGUI: Running on main thread, opening inventory for " + player.getName());

            if (player.getGameMode() == GameMode.SPECTATOR) {
                plugin.getLogger().warning("[DEBUG] ClassGUI: " + player.getName()
                        + " is in SPECTATOR mode, cannot open inventory!");
                return;
            }

            player.openInventory(inv);
            plugin.getLogger().info("[DEBUG] ClassGUI: openInventory call completed for " + player.getName());
        });
    }
    /**
     * Called when the player hits >=20, has a base class, but no ascendancy
     */
    public void openAscendancyGUI(Player player, String baseClass) {
        plugin.getLogger().info("[DEBUG] ClassGUI: Creating ascendancy inventory for "
                + player.getName() + " with baseClass=" + baseClass);

        Inventory inv = Bukkit.createInventory(null, 9, ChatColor.DARK_BLUE + "Choose Ascendancy");

        if ("Ranger".equalsIgnoreCase(baseClass)) {
            inv.setItem(2, createAscendItem(org.bukkit.Material.BONE, "Beastmaster", Arrays.asList(
                    ChatColor.GRAY + "Empowers pets and",
                    ChatColor.GRAY + "wild nature-based abilities"
            )));
            inv.setItem(4, createAscendItem(org.bukkit.Material.IRON_SWORD, "Shadowstalker", Arrays.asList(
                    ChatColor.GRAY + "Masters stealth, surprise,",
                    ChatColor.GRAY + "and high mobility"
            )));
            inv.setItem(6, createAscendItem(org.bukkit.Material.OAK_SAPLING, "Earthwarden", Arrays.asList(
                    ChatColor.GRAY + "Harnesses nature's power",
                    ChatColor.GRAY + "for support & defense"
            )));
        } else if ("Dragonknight".equalsIgnoreCase(baseClass)) {
            inv.setItem(2, createAscendItem(org.bukkit.Material.CAMPFIRE, "FlameWarden", Arrays.asList(
                    ChatColor.GRAY + "Focuses on fire power,",
                    ChatColor.GRAY + "burning, and melee strength"
            )));
            inv.setItem(4, createAscendItem(org.bukkit.Material.SHIELD, "ScaleGuardian", Arrays.asList(
                    ChatColor.GRAY + "A living shield,",
                    ChatColor.GRAY + "high defense & taunts"
            )));
            inv.setItem(6, createAscendItem(org.bukkit.Material.DIAMOND_AXE, "Berserker", Arrays.asList(
                    ChatColor.GRAY + "Draconic rage,",
                    ChatColor.GRAY + "growing stronger in battle"
            )));
        } else if ("Spellweaver".equalsIgnoreCase(baseClass)) {
            inv.setItem(2, createAscendItem(org.bukkit.Material.FIRE_CHARGE, "Elementalist", Arrays.asList(
                    ChatColor.GRAY + "Masters elemental power,",
                    ChatColor.GRAY + "unleashing deadly spells"
            )));
            inv.setItem(4, createAscendItem(org.bukkit.Material.CLOCK, "Chronomancer", Arrays.asList(
                    ChatColor.GRAY + "Manipulates time for",
                    ChatColor.GRAY + "unique offense & support"
            )));
            inv.setItem(6, createAscendItem(org.bukkit.Material.ENDER_EYE, "ArcaneProtector", Arrays.asList(
                    ChatColor.GRAY + "Shields allies with arcane",
                    ChatColor.GRAY + "magic & protective spells"
            )));
        }

        plugin.getLogger().info("[DEBUG] ClassGUI: Ascendancy inventory prepared. Scheduling openInventory on main thread...");

        Bukkit.getScheduler().runTask(plugin, () -> {
            plugin.getLogger().info("[DEBUG] ClassGUI: Opening ascendancy GUI on main thread for " + player.getName());

            if (player.getGameMode() == GameMode.SPECTATOR) {
                plugin.getLogger().warning("[DEBUG] ClassGUI: " + player.getName()
                        + " is in SPECTATOR mode, cannot open inventory!");
                return;
            }

            player.openInventory(inv);
            plugin.getLogger().info("[DEBUG] ClassGUI: openInventory call done for ascendancy GUI.");
        });
    }

    private ItemStack createAscendItem(org.bukkit.Material mat, String ascendName, java.util.List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + ascendName);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
