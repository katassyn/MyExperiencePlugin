package com.maks.myexperienceplugin;

import net.md_5.bungee.api.ChatColor;              // Bungee ChatColor
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.BukkitRunnable;

public class PeriodicClassReminder extends BukkitRunnable {

    private final MyExperiencePlugin plugin;

    // How long (in ticks) to keep the permission
    private final long PERMISSION_DURATION = 200L; // e.g. 200 ticks = 10 seconds

    public PeriodicClassReminder(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String baseClass = plugin.getClassManager().getPlayerClass(player.getUniqueId());
            String ascend = plugin.getClassManager().getPlayerAscendancy(player.getUniqueId());
            int level = plugin.getPlayerLevel(player);

            // If no base class => clickable message
            if ("NoClass".equalsIgnoreCase(baseClass)) {
                sendNoClassMessage(player);
            }
            // If they do have base class, but no ascendancy, and level >= 20 => clickable message
            else if (ascend.isEmpty() && level >= 20) {
                sendNoAscendancyMessage(player, baseClass);
            }
        }
    }

    private void sendNoClassMessage(Player player) {
        // 1) Temporarily grant "myplugin.class"
        //    so that /chose_class won't say "Unknown command."
        PermissionAttachment attachment = player.addAttachment(plugin, "myplugin.class", true);

        player.sendMessage("§e=============== §6[§aCLASS§6] §e===============");
        player.sendMessage("§cYou haven't selected your base class yet!");

        // Build clickable [CLICK HERE] => "/chose_class"
        TextComponent clickHere = new TextComponent("[CLICK HERE]");
        clickHere.setColor(ChatColor.YELLOW);
        clickHere.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new net.md_5.bungee.api.chat.ComponentBuilder("Click to open the class selection GUI").create()
        ));
        clickHere.setClickEvent(new ClickEvent(
                ClickEvent.Action.RUN_COMMAND,
                "/chose_class"
        ));

        TextComponent line = new TextComponent("§aYou can select your class now ");
        line.addExtra(clickHere);
        line.addExtra("§a!");
        player.spigot().sendMessage(line);

        player.sendMessage("§e======================================");

        // 2) Schedule removal of the permission after PERMISSION_DURATION ticks
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.removeAttachment(attachment);
        }, PERMISSION_DURATION);
    }

    private void sendNoAscendancyMessage(Player player, String baseClass) {
        // 1) Temporarily grant "myplugin.class"
        PermissionAttachment attachment = player.addAttachment(plugin, "myplugin.class", true);

        player.sendMessage("§e=============== §6[§aASCENDANCY§6] §e===============");
        player.sendMessage("§cYou haven't chosen an ascendancy yet for: §e" + baseClass);

        // Build clickable => "/chose_ascendancy"
        TextComponent clickHere = new TextComponent("[CLICK HERE]");
        clickHere.setColor(ChatColor.AQUA);
        clickHere.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new net.md_5.bungee.api.chat.ComponentBuilder("Click to open the ascendancy GUI").create()
        ));
        clickHere.setClickEvent(new ClickEvent(
                ClickEvent.Action.RUN_COMMAND,
                "/chose_ascendancy"
        ));

        TextComponent line = new TextComponent("§aYou can select your ascendancy now ");
        line.addExtra(clickHere);
        line.addExtra("§a!");
        player.spigot().sendMessage(line);

        player.sendMessage("§e======================================");

        // 2) Remove permission after a delay
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.removeAttachment(attachment);
        }, PERMISSION_DURATION);
    }
}
