package com.maks.myexperienceplugin.party;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * API dla systemu Party, umożliwiające integrację z innymi pluginami
 */
public class PartyAPI {
    private static MyExperiencePlugin plugin;
    private static PartyManager partyManager;
    private static final int debuggingFlag = 1;

    /**
     * Inicjalizuje PartyAPI
     * Metoda powinna być wywołana w metodzie onEnable() MyExperiencePlugin
     */
    public static void initialize(MyExperiencePlugin pluginInstance) {
        plugin = pluginInstance;
        partyManager = plugin.getPartyManager();
        
        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[PartyAPI] Party API initialized successfully");
        }
    }

    /**
     * Sprawdza czy gracz jest w party
     * @param player Gracz do sprawdzenia
     * @return true jeśli gracz jest w party, false w przeciwnym razie
     */
    public static boolean isInParty(Player player) {
        if (partyManager == null) return false;
        return partyManager.isInParty(player);
    }

    /**
     * Pobiera rozmiar party gracza
     * @param player Gracz którego party chcemy sprawdzić
     * @return Rozmiar party lub 1 jeśli gracz nie jest w party
     */
    public static int getPartySize(Player player) {
        if (partyManager == null || !isInParty(player)) return 1;
        
        Party party = partyManager.getParty(player);
        return party != null ? party.getMembers().size() : 1;
    }

    /**
     * Sprawdza czy party ma odpowiedni rozmiar
     * @param player Gracz do sprawdzenia
     * @param minSize Minimalny rozmiar party
     * @param maxSize Maksymalny rozmiar party
     * @return true jeśli party ma odpowiedni rozmiar, false w przeciwnym razie
     */
    public static boolean hasValidPartySize(Player player, int minSize, int maxSize) {
        int partySize = getPartySize(player);
        return partySize >= minSize && partySize <= maxSize;
    }

    /**
     * Pobiera wszystkich członków party jako obiekty Player
     * @param player Gracz którego party chcemy pobrać
     * @return Lista graczy w party lub lista zawierająca tylko tego gracza jeśli nie jest w party
     */
    public static List<Player> getPartyMembers(Player player) {
        List<Player> members = new ArrayList<>();
        
        if (partyManager == null || !isInParty(player)) {
            members.add(player);
            return members;
        }
        
        Party party = partyManager.getParty(player);
        if (party == null) {
            members.add(player);
            return members;
        }
        
        for (UUID memberId : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                members.add(member);
            }
        }
        
        return members;
    }

    /**
     * Teleportuje wszystkich członków party na wskazaną lokalizację
     * @param player Gracz którego party chcemy przeteleportować
     * @param location Lokalizacja docelowa
     */
    public static void teleportParty(Player player, Location location) {
        List<Player> members = getPartyMembers(player);
        
        for (Player member : members) {
            member.teleport(location);
            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[PartyAPI] Teleported party member " + member.getName() + " to location");
            }
        }
    }

    /**
     * Wysyła wiadomość do wszystkich członków party
     * @param player Gracz którego party ma otrzymać wiadomość
     * @param message Wiadomość do wysłania
     */
    public static void sendMessageToParty(Player player, String message) {
        List<Player> members = getPartyMembers(player);
        
        for (Player member : members) {
            member.sendMessage(message);
        }
    }

    /**
     * Sprawdza czy gracz jest liderem party
     * Uwaga: w obecnej implementacji Party nie ma koncepcji lidera,
     * więc zakładamy, że pierwsza osoba, która utworzyła party, jest liderem
     * 
     * @param player Gracz do sprawdzenia
     * @return true jeśli gracz jest liderem party, false w przeciwnym razie
     */
    public static boolean isPartyLeader(Player player) {
        if (partyManager == null || !isInParty(player)) return false;
        
        Party party = partyManager.getParty(player);
        if (party == null || party.getMembers().isEmpty()) return false;
        
        // Zakładamy, że pierwszy członek w kolekcji jest liderem
        UUID firstMemberId = party.getMembers().iterator().next();
        return player.getUniqueId().equals(firstMemberId);
    }
}