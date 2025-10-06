package com.maks.myexperienceplugin.party;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class PartyManageCommand implements CommandExecutor, Listener {
    private final MyExperiencePlugin plugin;
    private final PartyManager partyManager;
    private final Map<UUID, String> openGUIs = new HashMap<>();
    private final Set<UUID> clickCooldown = new HashSet<>();

    public PartyManageCommand(MyExperiencePlugin plugin, PartyManager partyManager) {
        this.plugin = plugin;
        this.partyManager = partyManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        openPartyManageGUI(player);
        return true;
    }

    private void openPartyManageGUI(Player player) {
        Party party = partyManager.getParty(player);

        if (party == null) {
            // Player has no party - show create party option
            openCreatePartyGUI(player);
        } else {
            // Player has party - show manage party GUI
            openManagePartyGUI(player, party);
        }
    }

    private void openCreatePartyGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§6§lCreate Your Party");

        // Create party button
        ItemStack createPartyItem = new ItemStack(Material.EMERALD);
        ItemMeta createPartyMeta = createPartyItem.getItemMeta();
        createPartyMeta.setDisplayName("§a§lCreate Party");
        createPartyMeta.setLore(Arrays.asList(
                "§7Click to create your own party!",
                "§7You can invite other players later.",
                "",
                "§eClick to create!"
        ));
        createPartyItem.setItemMeta(createPartyMeta);
        gui.setItem(13, createPartyItem);

        // Decoration
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < 27; i++) {
            if (i != 13) {
                gui.setItem(i, glass);
            }
        }

        openGUIs.put(player.getUniqueId(), "create_party");
        player.openInventory(gui);
    }

    private void openManagePartyGUI(Player player, Party party) {
        Inventory gui = Bukkit.createInventory(null, 54, "§6§lParty Management");

        // Party members section (slots 10-16)
        List<UUID> members = new ArrayList<>(party.getMembers());
        int memberSlot = 10;

        for (UUID memberId : members) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) {
                ItemStack memberItem = createPlayerSkull(member);
                gui.setItem(memberSlot++, memberItem);
            }
        }

        // Fill empty member slots with add player buttons
        for (int i = memberSlot; i <= 16 && i - 10 < 4; i++) {
            ItemStack addPlayerItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta addPlayerMeta = addPlayerItem.getItemMeta();
            addPlayerMeta.setDisplayName("§a§lInvite Player");
            addPlayerMeta.setLore(Arrays.asList(
                    "§7Click to see available players",
                    "§7to invite to your party.",
                    "",
                    "§eClick to invite!"
            ));
            addPlayerItem.setItemMeta(addPlayerMeta);
            gui.setItem(i, addPlayerItem);
        }

        // Party info
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§6§lParty Information");
        List<String> infoLore = new ArrayList<>();
        infoLore.add("§7Party Members: §e" + party.getMembers().size() + "/4");
        infoLore.add("");
        for (UUID memberId : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) {
                int level = plugin.getPlayerLevel(member);
                infoLore.add("§6▸ §a" + member.getName() + " §7(Level " + level + ")");
            }
        }
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        gui.setItem(4, infoItem);

        // Leave party button
        ItemStack leaveItem = new ItemStack(Material.RED_CONCRETE);
        ItemMeta leaveMeta = leaveItem.getItemMeta();
        leaveMeta.setDisplayName("§c§lLeave Party");
        leaveMeta.setLore(Arrays.asList(
                "§7Click to leave your current party.",
                "",
                "§cClick to leave!"
        ));
        leaveItem.setItemMeta(leaveMeta);
        gui.setItem(49, leaveItem);

        // Decoration
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < 54; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, glass);
            }
        }

        openGUIs.put(player.getUniqueId(), "manage_party");
        player.openInventory(gui);
    }

    private void openInvitePlayersGUI(Player player) {
        List<Player> availablePlayers = partyManager.getPlayersNotInParty();

        // Fixed size of 54 slots (6 rows) for consistent slot mapping
        int size = 54;
        Inventory gui = Bukkit.createInventory(null, size, "§6§lInvite Players");

        // Player heads go in slots 0-44 (first 5 rows)
        int slot = 0;
        for (Player availablePlayer : availablePlayers) {
            if (slot >= 45) break; // Stop at row 6 (leave bottom row for navigation)
            if (!availablePlayer.equals(player)) {
                ItemStack playerItem = createInvitePlayerSkull(availablePlayer);
                gui.setItem(slot++, playerItem);
            }
        }

        // Fill empty slots in first 5 rows with glass panes
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < 45; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, glass);
            }
        }

        // Fill bottom row (slots 45-53) with glass except back button
        for (int i = 45; i < 54; i++) {
            if (i != 49) { // Don't fill slot 49 (back button slot)
                gui.setItem(i, glass);
            }
        }

        // Back button - always in slot 49 (center of bottom row)
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName("§e§lBack to Party Management");
        backMeta.setLore(Arrays.asList("§7Click to return to party management"));
        backItem.setItemMeta(backMeta);
        gui.setItem(49, backItem);

        // Update the GUI type BEFORE opening
        openGUIs.put(player.getUniqueId(), "invite_players");
        player.openInventory(gui);
    }

    private ItemStack createPlayerSkull(Player player) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName("§a" + player.getName());

        int level = plugin.getPlayerLevel(player);
        List<String> lore = Arrays.asList(
                "§7Level: §e" + level,
                "§7Status: §aOnline",
                "",
                "§c§lRight-click to kick from party"
        );
        meta.setLore(lore);
        skull.setItemMeta(meta);
        return skull;
    }

    private ItemStack createInvitePlayerSkull(Player player) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName("§a" + player.getName());

        int level = plugin.getPlayerLevel(player);
        List<String> lore = Arrays.asList(
                "§7Level: §e" + level,
                "§7Status: §aOnline",
                "",
                "§e§lClick to invite to party"
        );
        meta.setLore(lore);
        skull.setItemMeta(meta);
        return skull;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        UUID playerId = player.getUniqueId();

        // Get GUI type from map
        String guiType = openGUIs.get(playerId);

        // Check if this is a party GUI by title
        String inventoryTitle = event.getView().getTitle();
        boolean isPartyGUIByTitle = inventoryTitle.contains("§6§lInvite Players") ||
                inventoryTitle.contains("§6§lParty Management") ||
                inventoryTitle.contains("§6§lCreate Your Party");

        // If it's not a party GUI at all, ignore it
        if (guiType == null && !isPartyGUIByTitle) {
            return;
        }

        // Cancel the event to prevent item extraction
        event.setCancelled(true);

        // If guiType is null but title matches, try to determine the type
        if (guiType == null && isPartyGUIByTitle) {
            // Try to determine GUI type from title and add to map
            if (inventoryTitle.contains("§6§lCreate Your Party")) {
                guiType = "create_party";
                openGUIs.put(playerId, guiType);
            } else if (inventoryTitle.contains("§6§lParty Management")) {
                guiType = "manage_party";
                openGUIs.put(playerId, guiType);
            } else if (inventoryTitle.contains("§6§lInvite Players")) {
                guiType = "invite_players";
                openGUIs.put(playerId, guiType);
            } else {
                // Can't determine type, just prevent item extraction
                return;
            }
        }

        // Add cooldown check to prevent spam clicking
        if (clickCooldown.contains(playerId)) {
            return;
        }

        // Only process left and right clicks
        if (event.getClick() != ClickType.LEFT && event.getClick() != ClickType.RIGHT) {
            return;
        }

        // Add cooldown AFTER validating the click
        clickCooldown.add(playerId);
        Bukkit.getScheduler().runTaskLater(plugin, () -> clickCooldown.remove(playerId), 10L);

        // Process the action based on GUI type
        switch (guiType) {
            case "create_party":
                handleCreatePartyClick(player, event);
                break;
            case "manage_party":
                handleManagePartyClick(player, event);
                break;
            case "invite_players":
                handleInvitePlayersClick(player, event);
                break;
        }
    }

    private void handleCreatePartyClick(Player player, InventoryClickEvent event) {
        if (event.getSlot() == 13) {
            try {
                // Create party
                partyManager.getOrCreateParty(player);
                player.closeInventory();
                player.sendMessage("§a§lParty created! You are now in your own party.");
                player.sendMessage("§7Use §e/manage_party §7to invite other players!");
            } catch (Exception e) {
                player.sendMessage("§cError creating party: " + e.getMessage());
                plugin.getLogger().severe("Error in handleCreatePartyClick: " + e.getMessage());
            }
        }
    }

    private void handleManagePartyClick(Player player, InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        int slot = event.getSlot();

        try {
            // Check if clicking on member slots (10-16)
            if (slot >= 10 && slot <= 16) {
                if (clickedItem.getType() == Material.PLAYER_HEAD) {
                    // Clicked on a party member
                    if (event.getClick() == ClickType.RIGHT) {
                        SkullMeta meta = (SkullMeta) clickedItem.getItemMeta();
                        if (meta != null && meta.getOwningPlayer() != null) {
                            Player target = Bukkit.getPlayer(meta.getOwningPlayer().getUniqueId());
                            if (target != null && !target.equals(player)) {
                                partyManager.kickPlayerFromParty(player, target);
                                player.closeInventory();
                                // Refresh GUI after a short delay
                                Bukkit.getScheduler().runTaskLater(plugin, () -> openPartyManageGUI(player), 5L);
                            }
                        }
                    }
                } else if (clickedItem.getType() == Material.LIME_STAINED_GLASS_PANE) {
                    // Clicked on invite player button - check if player is leader
                    Party party = partyManager.getParty(player);
                    if (party != null && party.isLeader(player.getUniqueId())) {
                        openInvitePlayersGUI(player);
                    } else {
                        player.sendMessage("§cOnly the party leader can invite players!");
                    }
                }
            } else if (slot == 49) {
                // Leave party
                partyManager.leaveParty(player);
                player.closeInventory();
            }
        } catch (Exception e) {
            player.sendMessage("§cError processing action: " + e.getMessage());
            plugin.getLogger().severe("Error in handleManagePartyClick: " + e.getMessage());
        }
    }

    private void handleInvitePlayersClick(Player player, InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        int slot = event.getSlot();

        try {
            // Back button - always in slot 49
            if (slot == 49 && clickedItem.getType() == Material.ARROW) {
                Party party = partyManager.getParty(player);
                if (party != null) {
                    openManagePartyGUI(player, party);
                } else {
                    player.closeInventory();
                    player.sendMessage("§cYou are no longer in a party!");
                }
                return;
            }

            // Player head clicks (slots 0-44)
            if (slot >= 0 && slot < 45 && clickedItem.getType() == Material.PLAYER_HEAD) {
                // Check if player is party leader before allowing invitations
                Party party = partyManager.getParty(player);
                if (party != null && party.isLeader(player.getUniqueId())) {
                    SkullMeta meta = (SkullMeta) clickedItem.getItemMeta();
                    if (meta != null && meta.getOwningPlayer() != null) {
                        Player target = Bukkit.getPlayer(meta.getOwningPlayer().getUniqueId());
                        if (target != null) {
                            partyManager.invitePlayer(player, target);
                            player.closeInventory();
                        }
                    }
                } else {
                    player.sendMessage("§cOnly the party leader can invite players!");
                }
            }
        } catch (Exception e) {
            player.sendMessage("§cError processing invite: " + e.getMessage());
            plugin.getLogger().severe("Error in handleInvitePlayersClick: " + e.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        UUID playerId = player.getUniqueId();
        String guiType = openGUIs.get(playerId);

        // Check if this is a party GUI by title
        String inventoryTitle = event.getView().getTitle();
        boolean isPartyGUI = guiType != null ||
                inventoryTitle.contains("§6§lInvite Players") ||
                inventoryTitle.contains("§6§lParty Management") ||
                inventoryTitle.contains("§6§lCreate Your Party");

        if (isPartyGUI) {
            // Cancel all dragging in party GUIs
            event.setCancelled(true);

            // Add to openGUIs if missing but title matches
            if (guiType == null) {
                if (inventoryTitle.contains("§6§lCreate Your Party")) {
                    openGUIs.put(playerId, "create_party");
                } else if (inventoryTitle.contains("§6§lParty Management")) {
                    openGUIs.put(playerId, "manage_party");
                } else if (inventoryTitle.contains("§6§lInvite Players")) {
                    openGUIs.put(playerId, "invite_players");
                }
            }
        }
    }

    // IMPORTANT: Add these additional event handlers to prevent ALL item manipulation

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        // Check if either inventory belongs to a player with an open party GUI
        if (event.getSource().getHolder() instanceof Player) {
            Player player = (Player) event.getSource().getHolder();
            if (openGUIs.containsKey(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
        if (event.getDestination().getHolder() instanceof Player) {
            Player player = (Player) event.getDestination().getHolder();
            if (openGUIs.containsKey(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryInteract(InventoryInteractEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String inventoryTitle = event.getView().getTitle();
        boolean isPartyGUI = openGUIs.containsKey(player.getUniqueId()) ||
                inventoryTitle.contains("§6§lInvite Players") ||
                inventoryTitle.contains("§6§lParty Management") ||
                inventoryTitle.contains("§6§lCreate Your Party");

        if (isPartyGUI) {
            event.setCancelled(true);
        }
    }

    // Prevent creative mode middle-click item duplication
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryCreative(InventoryCreativeEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String inventoryTitle = event.getView().getTitle();
        boolean isPartyGUI = openGUIs.containsKey(player.getUniqueId()) ||
                inventoryTitle.contains("§6§lInvite Players") ||
                inventoryTitle.contains("§6§lParty Management") ||
                inventoryTitle.contains("§6§lCreate Your Party");

        if (isPartyGUI) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            openGUIs.remove(event.getPlayer().getUniqueId());
            clickCooldown.remove(event.getPlayer().getUniqueId());
        }
    }
}