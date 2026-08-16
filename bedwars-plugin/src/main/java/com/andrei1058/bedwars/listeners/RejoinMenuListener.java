package com.andrei1058.bedwars.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class RejoinMenuListener implements Listener {

    private final JavaPlugin plugin;
    private final String guiTitle = "§8Rejoin Last Match?";

    public RejoinMenuListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // 1. FOR SERVERS WITH AUTHME: Waits for successful login, then delays 1 second (20 ticks)
    @EventHandler(priority = EventPriority.MONITOR)
    public void onAuthMeLogin(fr.xephi.authme.events.LoginEvent event) {
        Player player = event.getPlayer();
        scheduleGUIPopup(player);
    }

    // 2. FALLBACK FOR OTHER LOGIN PLUGINS: Waits 5 seconds after joining to give time to log in
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // If AuthMe is installed, let the event above handle it instead
        if (Bukkit.getPluginManager().isPluginEnabled("AuthMe")) return;

        Player player = event.getPlayer();
        
        // Generous 5-second delay to give them time to complete their login command
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                openRejoinGUI(player);
            }
        }, 100L); // 100 ticks = 5 seconds
    }

    private void scheduleGUIPopup(Player player) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                openRejoinGUI(player);
            }
        }, 20L); // 20 ticks = Exactly 1 second after successful authentication
    }

    private void openRejoinGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, guiTitle);

        ItemStack acceptItem = createItem("LIME_WOOL", (short) 5);
        ItemMeta acceptMeta = acceptItem.getItemMeta();
        if (acceptMeta != null) {
            acceptMeta.setDisplayName("§a§lYES, REJOIN");
            acceptItem.setItemMeta(acceptMeta);
        }

        ItemStack declineItem = createItem("RED_WOOL", (short) 14);
        ItemMeta declineMeta = declineItem.getItemMeta();
        if (declineMeta != null) {
            declineMeta.setDisplayName("§c§lNO, CANCEL");
            declineItem.setItemMeta(declineMeta);
        }

        ItemStack background = createItem("BLACK_STAINED_GLASS_PANE", (short) 15);
        ItemMeta bgMeta = background.getItemMeta();
        if (bgMeta != null) {
            bgMeta.setDisplayName("§7");
            background.setItemMeta(bgMeta);
        }

        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, background);
        }

        gui.setItem(11, acceptItem);
        gui.setItem(15, declineItem);

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!guiTitle.equals(event.getView().getTitle())) return;
        
        event.setCancelled(true); 
        if (event.getCurrentItem() == null) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()) {
            String name = clicked.getItemMeta().getDisplayName();
            
            if (name.contains("YES")) {
                player.closeInventory();
                player.performCommand("bw rejoin"); 
            } else if (name.contains("NO")) {
                player.closeInventory();
                player.sendMessage("§7You declined to rejoin the game.");
            }
        }
    }

    private ItemStack createItem(String modernName, short legacyData) {
        Material mat = Material.getMaterial(modernName);
        if (mat != null) {
            return new ItemStack(mat);
        } else {
            String legacyMaterialName = modernName.contains("GLASS") ? "STAINED_GLASS_PANE" : "WOOL";
            Material legacyMat = Material.getMaterial(legacyMaterialName);
            if (legacyMat == null) legacyMat = Material.STONE;
            return new ItemStack(legacyMat, 1, legacyData);
        }
    }
}
