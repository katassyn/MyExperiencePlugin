package com.maks.myexperienceplugin.exp;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;

public class ChatLevelHandler implements Listener {

    private final MyExperiencePlugin plugin;
    private final int maxLevel = 100;

    public ChatLevelHandler(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        int level = plugin.getPlayerLevel(player);

        String levelTag = (level >= maxLevel)
                ? "§4§l[MAX LEVEL]§r"
                : String.format("§b[%d]§r", level);

        String baseClass = plugin.getClassManager().getPlayerClass(player.getUniqueId());
        String ascendancy = plugin.getClassManager().getPlayerAscendancy(player.getUniqueId());

        String classTag;
        if ("NoClass".equalsIgnoreCase(baseClass)) {
            classTag = "NoClass";
        } else if (!ascendancy.isEmpty()) {
            classTag = ascendancy;
        } else {
            classTag = baseClass;
        }
        classTag = colorizeClassTag(classTag);

        String bracketedClass = "§3[" + classTag + "§3]§r";
        String displayName = player.getDisplayName();
        String formatted = String.format(
                "%s %s %s: %s",
                levelTag,
                bracketedClass,
                displayName,
                event.getMessage()
        );
        event.setFormat(formatted);
    }

    private String colorizeClassTag(String raw) {
        switch (raw.toLowerCase()) {
            case "ranger": return "§aRanger";
            case "dragonknight": return "§cDragonknight";
            case "spellweaver": return "§5Spellweaver";
            case "beastmaster": return "§2Beastmaster";
            case "shadowstalker": return "§8Shadowstalker";
            case "earthwarden": return "§2Earthwarden";
            case "flame warden": return "§6Flame Warden";
            case "scale guardian": return "§bScale Guardian";
            case "berserker": return "§4Berserker";
            case "elementalist": return "§dElementalist";
            case "chronomancer": return "§9Chronomancer";
            case "arcane protector": return "§3Arcane Protector";
            case "noclass": return "§7NoClass";
            default:
                return raw;
        }
    }
}
