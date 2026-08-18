package com.andrei1058.bedwars.listeners;

import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class RipTombstoneListener implements Listener {

    private final Plugin plugin;

    public RipTombstoneListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerKill(PlayerKillEvent event) {
        // Check if it is a final kill
        if (event.isFinalKill()) {
            Player victim = event.getVictim();
            if (victim != null) {
                Location loc = victim.getLocation();
                
                // Spawn a temporary floating armor stand displaying RIP
                ArmorStand stand = loc.getWorld().spawn(loc.add(0, -0.5, 0), ArmorStand.class, armorStand -> {
                    armorStand.setVisible(false);
                    armorStand.setGravity(false);
                    armorStand.setCustomName("§c§lRIP " + victim.getName());
                    armorStand.setCustomNameVisible(true);
                    armorStand.setMarker(true);
                });

                // Remove the tombstone display after 10 seconds (200 ticks)
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (stand.isValid()) {
                        stand.remove();
                    }
                }, 200L);
            }
        }
    }
}
