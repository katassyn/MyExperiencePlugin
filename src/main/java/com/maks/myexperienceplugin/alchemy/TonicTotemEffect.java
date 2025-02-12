package com.maks.myexperienceplugin.alchemy;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import com.maks.myexperienceplugin.MyExperiencePlugin;

public class TonicTotemEffect extends AlchemyEffect {

    public TonicTotemEffect(Player player, long durationMillis, long cooldownMillis, String effectName) {
        super(player, durationMillis, cooldownMillis, effectName);
    }

    @Override
    public void apply() {
        player.sendMessage("§a[" + effectName + "] Totem effect activated. It will protect you from the next fatal hit for "
                + (durationMillis / 60000) + " minutes.");
        // Rejestrujemy totem – wykorzystujemy istniejący TotemEffect (lub inny mechanizm ochronny)
        TotemManager.getInstance().registerTotem(player, new TotemEffect(player, cooldownMillis, effectName));
        // Automatycznie wygaszamy efekt po upływie określonego czasu
        Bukkit.getScheduler().runTaskLater(MyExperiencePlugin.getInstance(), this::remove, durationMillis / 50);
    }

    @Override
    public void remove() {
        TotemManager.getInstance().clearTotem(player);
        player.sendMessage("§c[" + effectName + "] Totem effect expired.");
    }
}
