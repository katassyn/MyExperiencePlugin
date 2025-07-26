package com.maks.myexperienceplugin.Class.skills;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.HashMap;
import java.util.Map;

public class SkillTreeGUIListener implements Listener {
    private final MyExperiencePlugin plugin;
    private final SkillTreeManager skillTreeManager;
    private final SkillTreeGUI skillTreeGUI;
    private final SkillPurchaseManager purchaseManager;

    // Map to track node IDs in the GUI
    private final Map<String, Map<Integer, Integer>> nodeSlotMap;

    // Debugging flag
    private final int debuggingFlag = 0;

    public SkillTreeGUIListener(MyExperiencePlugin plugin,
                                SkillTreeManager skillTreeManager,
                                SkillTreeGUI skillTreeGUI,
                                SkillPurchaseManager purchaseManager) {
        this.plugin = plugin;
        this.skillTreeManager = skillTreeManager;
        this.skillTreeGUI = skillTreeGUI;
        this.purchaseManager = purchaseManager;
        this.nodeSlotMap = new HashMap<>();

        // Initialize the reverse mapping of slots to node IDs
        initializeNodeSlotMap();
    }

    private void initializeNodeSlotMap() {
        // Wspólny mapping slotów do ID węzłów dla wszystkich klas
        Map<Integer, Integer> commonSlotMap = new HashMap<>();
        commonSlotMap.put(10, 1);  // Slot 10 -> Skill 1
        commonSlotMap.put(13, 2);  // Slot 13 -> Skill 2
        commonSlotMap.put(16, 3);  // Slot 16 -> Skill 3

        commonSlotMap.put(19, 4);  // Slot 19 -> Skill 4
        commonSlotMap.put(22, 5);  // Slot 22 -> Skill 5
        commonSlotMap.put(25, 6);  // Slot 25 -> Skill 6

        commonSlotMap.put(27, 7);  // Slot 27 -> Skill 7
        commonSlotMap.put(29, 8);  // Slot 29 -> Skill 8
        commonSlotMap.put(31, 9);  // Slot 31 -> Skill 9
        commonSlotMap.put(33, 10); // Slot 33 -> Skill 10
        commonSlotMap.put(35, 11); // Slot 35 -> Skill 11

        commonSlotMap.put(36, 12); // Slot 36 -> Skill 12
        commonSlotMap.put(40, 13); // Slot 40 -> Skill 13
        commonSlotMap.put(49, 14); // Slot 49 -> Skill 14

        // Stosujemy ten sam mapping dla wszystkich klas
        nodeSlotMap.put("Ranger", new HashMap<>(commonSlotMap));
        nodeSlotMap.put("DragonKnight", new HashMap<>(commonSlotMap));
        nodeSlotMap.put("SpellWeaver", new HashMap<>(commonSlotMap)); // na przyszłość

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Zainicjalizowano mapping slotów dla wszystkich klas");
            plugin.getLogger().info("Układ: Skill 1-14 w slotach: 10,13,16, 19,22,25, 27,29,31,33,35, 36,40,49");
        }
    }    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        // Check if it's our skill tree GUI
        if (!title.equals(ChatColor.DARK_GREEN + "Skill Tree")) {
            return;
        }

        // Always cancel the event first to prevent double-processing
        event.setCancelled(true);

        // Ignore certain inventory actions that aren't left-clicks
        if (event.getAction() != InventoryAction.PICKUP_ALL &&
                event.getAction() != InventoryAction.PICKUP_HALF) {
            return;
        }

        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) {
            return;
        }

        String playerClass = plugin.getClassManager().getPlayerClass(player.getUniqueId());
        if ("NoClass".equalsIgnoreCase(playerClass)) {
            return;
        }

        Map<Integer, Integer> slotMap = nodeSlotMap.get(playerClass);
        if (slotMap == null) {
            return;
        }

        int slot = event.getRawSlot();
        if (!slotMap.containsKey(slot)) {
            return;
        }

        int nodeId = slotMap.get(slot);

        // Log the attempt for debugging
        if (debuggingFlag == 1) {
            plugin.getLogger().info("CLICK DETECTED: " + player.getName() +
                    " clicked on skill " + nodeId);
        }

        // Hand off to the purchase manager
        purchaseManager.requestSkillPurchase(player, nodeId);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();

        // Check if it's our skill tree GUI
        if (title.equals(ChatColor.DARK_GREEN + "Skill Tree")) {
            purchaseManager.handleInventoryClose(player);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("INVENTORY CLOSED: " + player.getName() +
                        " closed skill tree GUI");
            }
        }
    }
}
