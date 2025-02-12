package com.maks.myexperienceplugin.alchemy;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AlchemyManager {
    public enum AlchemyCategory {
        ELIXIR, TONIC, PHYCIS, TOTEM
    }

    // Aktywne efekty: dla każdego gracza (UUID) przechowujemy efekt w danej kategorii
    private final Map<UUID, Map<AlchemyCategory, AlchemyEffect>> activeEffects = new HashMap<>();
    // Cooldowny: dla każdego gracza, dla każdej kategorii – czas (w ms), do kiedy nie można ponownie użyć efektu
    private final Map<UUID, Map<AlchemyCategory, Long>> cooldowns = new HashMap<>();

    private static AlchemyManager instance;

    private AlchemyManager() { }

    public static AlchemyManager getInstance() {
        if (instance == null) {
            instance = new AlchemyManager();
        }
        return instance;
    }

    /** Sprawdza, czy gracz może użyć efektu danej kategorii (cooldown minął) */
    public boolean canApplyEffect(Player player, AlchemyCategory category) {
        long now = System.currentTimeMillis();
        Map<AlchemyCategory, Long> playerCooldowns = cooldowns.getOrDefault(player.getUniqueId(), new HashMap<>());
        long availableAt = playerCooldowns.getOrDefault(category, 0L);
        return now >= availableAt;
    }

    /**
     * Próbuje zastosować efekt. Jeśli gracz ma już aktywny efekt w danej kategorii lub cooldown nie minął,
     * wysyłamy odpowiedni komunikat i efekt nie zostaje zastosowany.
     */
    public void applyEffect(Player player, AlchemyCategory category, AlchemyEffect effect) {
        UUID uuid = player.getUniqueId();

        // Jeśli już mamy aktywny efekt w tej kategorii, nie pozwalamy na nałożenie kolejnego.
        Map<AlchemyCategory, AlchemyEffect> playerEffects = activeEffects.get(uuid);
        if (playerEffects != null && playerEffects.containsKey(category)) {
            player.sendMessage("§cYou already have an active effect in this category. Wait until it ends.");
            return;
        }

        // Sprawdzamy cooldown
        if (!canApplyEffect(player, category)) {
            player.sendMessage("§cYou must wait for the cooldown before using another effect in this category.");
            return;
        }

        // Zapisujemy i aktywujemy efekt
        if (playerEffects == null) {
            playerEffects = new HashMap<>();
            activeEffects.put(uuid, playerEffects);
        }
        playerEffects.put(category, effect);
        effect.apply();

        // Ustawiamy cooldown – liczony od momentu użycia efektu
        Map<AlchemyCategory, Long> playerCooldowns = cooldowns.getOrDefault(uuid, new HashMap<>());
        playerCooldowns.put(category, System.currentTimeMillis() + effect.getCooldownMillis());
        cooldowns.put(uuid, playerCooldowns);
    }

    /** Usuwa efekt z danej kategorii dla gracza (np. po zakończeniu działania) */
    public void removeEffect(Player player, AlchemyCategory category) {
        UUID uuid = player.getUniqueId();
        Map<AlchemyCategory, AlchemyEffect> playerEffects = activeEffects.get(uuid);
        if (playerEffects != null && playerEffects.containsKey(category)) {
            AlchemyEffect effect = playerEffects.get(category);
            effect.remove();
            playerEffects.remove(category);
        }
    }
}
