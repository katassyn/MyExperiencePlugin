package com.maks.myexperienceplugin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ClassResetItemListener implements Listener {

    private final MyExperiencePlugin plugin;

    public ClassResetItemListener(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() == null) return;
        ItemStack item = event.getItem();
        if (item.getType() == Material.BLACK_WOOL && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase("Class Reset")) {
                event.setCancelled(true);
                Player player = event.getPlayer();

                // reset class data
                plugin.getClassManager().resetClassData(player);
                player.sendMessage(ChatColor.RED + "Your class has been reset!");

                // consume 1 item
                item.setAmount(item.getAmount() - 1);

                // re-open class GUI
                plugin.getClassGUI().openBaseClassGUI(player);
            }
        }
    }
}
