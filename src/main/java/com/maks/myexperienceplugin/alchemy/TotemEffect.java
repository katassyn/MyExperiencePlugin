package com.maks.myexperienceplugin.alchemy;

import org.bukkit.entity.Player;
import com.maks.myexperienceplugin.MyExperiencePlugin;

public class TotemEffect extends AlchemyEffect {

    public TotemEffect(Player player, long cooldownMillis, String effectName) {
        // Efekt nie ma określonego czasu trwania – czeka na aktywację (np. przy krytycznym trafieniu)
        super(player, 0, cooldownMillis, effectName);
    }

    @Override
    public void apply() {
        player.sendMessage("§a[" + effectName + "] Totem effect activated. It will protect you from the next fatal hit.");
        TotemManager.getInstance().registerTotem(player, this);
    }

    @Override
    public void remove() {
        TotemManager.getInstance().clearTotem(player);
        player.sendMessage("§c[" + effectName + "] Totem effect has been consumed.");
    }
}
