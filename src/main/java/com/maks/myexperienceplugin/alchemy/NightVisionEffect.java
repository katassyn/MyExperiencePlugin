package com.maks.myexperienceplugin.alchemy;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.maks.myexperienceplugin.MyExperiencePlugin;

public class NightVisionEffect extends AlchemyEffect {
    private final int amplifier;

    public NightVisionEffect(Player player, int amplifier, long durationMillis, long cooldownMillis, String effectName) {
        super(player, durationMillis, cooldownMillis, effectName);
        this.amplifier = amplifier;
    }

    @Override
    public void apply() {
        player.sendMessage("§a[" + effectName + "] Effect started: Night vision activated.");
        int durationTicks = (int) (durationMillis / 50);
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, durationTicks, amplifier, false, false));
        Bukkit.getScheduler().runTaskLater(MyExperiencePlugin.getInstance(), this::remove, durationTicks);
    }

    @Override
    public void remove() {
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.sendMessage("§c[" + effectName + "] Effect ended: Night vision expired.");
    }
}
