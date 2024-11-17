import org.bukkit.inventory.ItemStack;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class MythicMobXPHandler implements Listener {

    private final MyExperiencePlugin plugin;

    public MythicMobXPHandler(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMythicMobDeath(MythicMobDeathEvent event) {
        // Sprawdzenie, czy zabójcą był gracz
        if (event.getKiller() instanceof Player) {
            Player player = (Player) event.getKiller();

            // Pobieramy listę dropów jako obiekty typu ItemStack
            List<ItemStack> dropTable = event.getDrops();

            for (ItemStack drop : dropTable) {
                // Sprawdzamy, czy przedmiot ma określoną nazwę (lub inne właściwości)
                if (drop.hasItemMeta() && drop.getItemMeta().hasDisplayName()) {
                    String itemName = drop.getItemMeta().getDisplayName();

                    // Sprawdzamy, czy nazwa przedmiotu zaczyna się od "custom_xp_drop"
                    if (itemName.startsWith("custom_xp_drop")) {
                        String[] parts = itemName.split(" ");
                        if (parts.length == 2) {
                            double xp = Double.parseDouble(parts[1]);  // Pobieramy wartość XP
                            plugin.addXP(player, xp);  // Dodajemy XP do gracza
                        }
                    }
                }
            }
        }
    }
}
