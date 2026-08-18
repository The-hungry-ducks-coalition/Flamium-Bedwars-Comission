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

import java.lang.reflect.Method;

public class RejoinMenuListener implements Listener {

    private final JavaPlugin plugin;
    private final String TITLE = "§8Rejoin Last Match?";
    
    private boolean authme;
    private Method getAuthPlayer;

    public RejoinMenuListener(JavaPlugin plugin) {
        this.plugin = plugin;
        try {
            Class<?> ev = Class.forName("fr.xephi.authme.events.LoginEvent");
            this.getAuthPlayer = ev.getMethod("getPlayer");
            this.authme = true;
        } catch (Exception e) {
            this.authme = false;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogin(org.bukkit.event.Event e) {
        if (!authme || !e.getClass().getName().equals("fr.xephi.authme.events.LoginEvent")) return;
        try {
            Player p = (Player) getAuthPlayer.invoke(e);
            if (p != null) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> openGui(p), 20L);
            }
        } catch (Exception ignored) {}
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (Bukkit.getPluginManager().isPluginEnabled("AuthMe")) return;
        
        Player p = e.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (p.isOnline()) openGui(p);
        }, 100L);
    }

    private void openGui(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        ItemStack yes = new ItemStack(Material.LIME_WOOL);
        ItemMeta yesM = yes.getItemMeta();
        yesM.setDisplayName("§a§lYES, REJOIN");
        yes.setItemMeta(yesM);

        ItemStack no = new ItemStack(Material.RED_WOOL);
        ItemMeta noM = no.getItemMeta();
        noM.setDisplayName("§c§lNO, CANCEL");
        no.setItemMeta(noM);

        ItemStack bg = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta bgM = bg.getItemMeta();
        bgM.setDisplayName("§7");
        bg.setItemMeta(bgM);

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, bg);
        }

        inv.setItem(11, yes);
        inv.setItem(15, no);

        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(TITLE)) return;
        e.setCancelled(true);
        
        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;

        Player p = (Player) e.getWhoClicked();
        String name = e.getCurrentItem().getItemMeta().getDisplayName();

        if (name.equals("§a§lYES, REJOIN")) {
            p.closeInventory();
            p.performCommand("bw rejoin");
        } else if (name.equals("§c§lNO, CANCEL")) {
            p.closeInventory();
            p.sendMessage("§7You declined to rejoin the game.");
        }
    }
}
