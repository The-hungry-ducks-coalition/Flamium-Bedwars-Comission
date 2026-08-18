package com.andrei1058.bedwars.listeners;

import com.andrei1058.bedwars.api.events.game.GameStateChangeEvent;
import com.andrei1058.bedwars.api.arena.GameState;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.UUID;

public class VictoryCelebration implements Listener {

    @EventHandler
    public void onGameStateChange(GameStateChangeEvent event) {
        // Check if the game has ended (switching to restarting state)
        if (event.getNewState() == GameState.RESTARTING) {
            for (UUID uuid : event.getArena().getPlayers()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    // Play victory sound
                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                    
                    // Spawn celebratory firework
                    Location loc = player.getLocation();
                    Firework fw = loc.getWorld().spawn(loc, Firework.class);
                    FireworkMeta meta = fw.getFireworkMeta();
                    meta.addEffect(FireworkEffect.builder()
                            .flicker(true)
                            .trail(true)
                            .with(FireworkEffect.Type.BALL_LARGE)
                            .withColor(Color.ORANGE, Color.YELLOW)
                            .build());
                    meta.setPower(1);
                    fw.setFireworkMeta(meta);
                }
            }
        }
    }
}
