package com.maks.myexperienceplugin.alchemy;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import com.maks.myexperienceplugin.MyExperiencePlugin;

public class AbsorptionEffect extends AlchemyEffect {
    private final double absorptionAmount;

    public AbsorptionEffect(Player player, double absorptionAmount, long durationMillis, long cooldownMillis, String effectName) {
        super(player, durationMillis, cooldownMillis, effectName);
        this.absorptionAmount = absorptionAmount;
    }

    @Override
    public void apply() {
        player.sendMessage("§a[" + effectName + "] Effect started: You gained " + absorptionAmount + " absorption hearts.");
        player.setAbsorptionAmount(player.getAbsorptionAmount() + absorptionAmount);
        Bukkit.getScheduler().runTaskLater(MyExperiencePlugin.getInstance(), this::remove, durationMillis / 50);
    }

    @Override
    public void remove() {
        double current = player.getAbsorptionAmount();
        double newAmount = Math.max(0, current - absorptionAmount);
        player.setAbsorptionAmount(newAmount);
        player.sendMessage("§c[" + effectName + "] Effect ended: Absorption effect expired.");
    }
}
