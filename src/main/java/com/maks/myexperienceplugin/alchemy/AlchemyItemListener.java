package com.maks.myexperienceplugin.alchemy;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class AlchemyItemListener implements Listener {
    private final AlchemyLevelConfig levelConfig;
    private final MyExperiencePlugin plugin;

    public AlchemyItemListener(MyExperiencePlugin plugin, AlchemyLevelConfig levelConfig) {
        this.plugin = plugin;
        this.levelConfig = levelConfig;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) return;

        // Ustal klucz efektu na podstawie itemu
        String key = AlchemyItemMatcher.matchItem(item);
        if (key == null) return;

        // Sprawdź wymagany poziom
        int requiredLevel = levelConfig.getRequiredLevel(key);
        int playerLevel = plugin.getPlayerLevel(player);

        if (playerLevel < requiredLevel) {
            player.sendMessage("§cWymagany poziom: " + requiredLevel + " aby użyć tego przedmiotu!");
            event.setCancelled(true);
            return;
        }

        // Utwórz efekt przy pomocy fabryki
        AlchemyEffect effect = AlchemyEffectFactory.createEffect(key, player);
        if (effect == null) return;

        // Określ kategorię efektu na podstawie klucza
        AlchemyManager.AlchemyCategory category;
        if (key.startsWith("potion")) {
            category = AlchemyManager.AlchemyCategory.ELIXIR;
        } else if (key.startsWith("tonic")) {
            category = AlchemyManager.AlchemyCategory.TONIC;
        } else if (key.startsWith("phycis")) {
            category = AlchemyManager.AlchemyCategory.PHYCIS;
        } else {
            return;
        }

        // Spróbuj nałożyć efekt
        AlchemyManager.getInstance().applyEffect(player, category, effect);

        // Zużyj jedną sztukę przedmiotu
        int amount = item.getAmount();
        if (amount > 1) {
            item.setAmount(amount - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        event.setCancelled(true);
    }
}