package com.andrei1058.bedwars.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ClientLanguageListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String locale = player.getLocale(); // Gets client language (e.g., "es_es", "fr_fr", "en_us"), ngl i dont know if this is gonna work it seems too simple
        
        // You can map this locale to BedWars1058's internal language API
        player.sendMessage("§b[BedWars] §eDetected client language: " + locale);
    }
}
