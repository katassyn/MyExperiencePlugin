package com.maks.myexperienceplugin.alchemy;

import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

public class TotemManager {
    private static TotemManager instance;
    private final Map<UUID, TotemEffect> activeTotems = new HashMap<>();

    private TotemManager() { }

    public static TotemManager getInstance() {
        if (instance == null) {
            instance = new TotemManager();
        }
        return instance;
    }

    public void registerTotem(Player player, TotemEffect effect) {
        activeTotems.put(player.getUniqueId(), effect);
    }

    public boolean hasTotem(Player player) {
        return activeTotems.containsKey(player.getUniqueId());
    }

    public void clearTotem(Player player) {
        activeTotems.remove(player.getUniqueId());
    }

    /**
     * Próbuje aktywować Totem – jeśli gracz otrzymał obrażenia, a Totem jest aktywny,
     * efekt zostanie usunięty i metoda zwróci true.
     */
    public boolean tryActivateTotem(Player player) {
        if (hasTotem(player)) {
            TotemEffect effect = activeTotems.get(player.getUniqueId());
            effect.remove();
            return true;
        }
        return false;
    }
}
