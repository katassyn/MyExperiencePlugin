package com.maks.myexperienceplugin.Class;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ClassGUIListener implements Listener {

    private final MyExperiencePlugin plugin;

    public ClassGUIListener(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String title = event.getView().getTitle();
        if (title.equals(ChatColor.DARK_GREEN + "Choose Your Class")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;
            String clickedName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

            if (clickedName.equalsIgnoreCase("Ranger") ||
                    clickedName.equalsIgnoreCase("Dragonknight") ||
                    clickedName.equalsIgnoreCase("Spellweaver")) {

                plugin.getClassManager().setPlayerClass(player, clickedName);
                player.closeInventory();
                player.sendMessage(ChatColor.GOLD + "You chose the " + clickedName + " class!");
            }
        }
        else if (title.equals(ChatColor.DARK_BLUE + "Choose Ascendancy")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;
            String ascendName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

            plugin.getClassManager().setPlayerAscendancy(player, ascendName);
            player.closeInventory();
            player.sendMessage(ChatColor.GOLD + "You ascended to " + ascendName + "!");
        }
    }
}
