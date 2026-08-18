/*
 * BedWars1058 - A bed wars mini-game.
 * Copyright (C) 2021 Andrei Dascălu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Contact e-mail: andrew.dascalu@gmail.com
 */

package com.andrei1058.bedwars.cosmetics;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.arena.Arena;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.andrei1058.bedwars.api.language.Language.getMsg;

public class CosmeticsManager implements com.andrei1058.bedwars.api.cosmetics.CosmeticsUtil {

    private static CosmeticsManager instance;
    private final boolean enabled;
    // uuid -> equipped cosmetics
    private final Map<UUID, PlayerCosmeticsData> playerData = new ConcurrentHashMap<>();
    // registered cosmetic id -> data
    private final Map<String, CosmeticData> registeredCosmetics = new HashMap<>();

    private CosmeticsManager() {
        this.enabled = BedWars.config.getYml().getBoolean(ConfigPath.COSMETICS_ENABLED, true);
    }

    public static void init() {
        instance = new CosmeticsManager();
        if (!instance.enabled) {
            BedWars.debug("Cosmetics are disabled in config.");
            return;
        }

        Bukkit.getPluginManager().registerEvents(new CosmeticsListener(), BedWars.plugin);
        BedWars.debug("Cosmetics system enabled.");
    }

    public static CosmeticsManager getInstance() {
        return instance;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getKillEffect(Player player) {
        PlayerCosmeticsData d = playerData.get(player.getUniqueId());
        return d == null ? "" : d.killEffect;
    }

    @Override
    public void setKillEffect(Player player, String effectId) {
        getOrCreate(player).killEffect = effectId;
    }

    @Override
    public String getDeathAnimation(Player player) {
        PlayerCosmeticsData d = playerData.get(player.getUniqueId());
        return d == null ? "" : d.deathAnim;
    }

    @Override
    public void setDeathAnimation(Player player, String animId) {
        getOrCreate(player).deathAnim = animId;
    }

    @Override
    public String getVictoryDance(Player player) {
        PlayerCosmeticsData d = playerData.get(player.getUniqueId());
        return d == null ? "" : d.victoryDance;
    }

    @Override
    public void setVictoryDance(Player player, String danceId) {
        getOrCreate(player).victoryDance = danceId;
    }

    @Override
    public String getProjectileTrail(Player player) {
        PlayerCosmeticsData d = playerData.get(player.getUniqueId());
        return d == null ? "" : d.trailId;
    }

    @Override
    public void setProjectileTrail(Player player, String trailId) {
        getOrCreate(player).trailId = trailId;
    }

    // register a cosmetic so the manager knows about it
    public void registerCosmetic(CosmeticData data) {
        if (data == null) return;
        registeredCosmetics.put(data.getId(), data);
    }

    public Map<String, CosmeticData> getRegisteredCosmetics() {
        return Collections.unmodifiableMap(registeredCosmetics);
    }

    public List<CosmeticData> getCosmeticsByType(String type) {
        List<CosmeticData> out = new ArrayList<>();
        for (CosmeticData c : registeredCosmetics.values()) {
            if (c.getType().equalsIgnoreCase(type)) out.add(c);
        }
        return out;
    }

    public void removePlayer(Player player) {
        playerData.remove(player.getUniqueId());
    }

    private PlayerCosmeticsData getOrCreate(Player player) {
        return playerData.computeIfAbsent(player.getUniqueId(), k -> new PlayerCosmeticsData());
    }

    // ---- particle effects ----

    private void playKillEffect(Player killer, Player victim) {
        String id = getKillEffect(killer);
        if (id.isEmpty()) return;
        CosmeticData cos = registeredCosmetics.get(id);
        if (cos == null || cos.getParticles().isEmpty()) return;

        Effect p;
        try {
            p = Effect.valueOf(cos.getParticles());
        } catch (IllegalArgumentException ignored) {
            return;
        }

        Location loc = victim.getLocation();
        World w = loc.getWorld();
        if (w == null) return;

        // scatter particles around where they died
        for (int i = 0; i < 30; i++) {
            double rx = (Math.random() - 0.5) * 2.0;
            double ry = Math.random() * 2.5;
            double rz = (Math.random() - 0.5) * 2.0;
            w.playEffect(loc.clone().add(rx, ry, rz), p, 0);
        }
    }

    private void playDeathAnimation(Player victim) {
        String id = getDeathAnimation(victim);
        if (id.isEmpty()) return;
        CosmeticData cos = registeredCosmetics.get(id);
        if (cos == null) return;

        Effect p;
        try {
            p = Effect.valueOf(cos.getParticles());
        } catch (IllegalArgumentException ignored) {
            return;
        }

        // spinning circle of particles
        Location loc = victim.getLocation();
        int dur = Math.max(1, cos.getDuration()) * 20; // ticks

        new BukkitRunnable() {
            int t = 0;
            @Override
            public void run() {
                if (t >= dur || victim.isOnline() == false) {
                    cancel();
                    return;
                }
                for (int i = 0; i < 5; i++) {
                    double angle = Math.toRadians(t * 10 + i * 72);
                    double cx = Math.cos(angle) * 1.5;
                    double cz = Math.sin(angle) * 1.5;
                    loc.getWorld().playEffect(loc.clone().add(cx, 0.5, cz), p, 0);
                }
                t += 5;
            }
        }.runTaskTimer(BedWars.plugin, 0L, 5L);
    }

    private void playProjectileTrail(Projectile proj, Player shooter) {
        String id = getProjectileTrail(shooter);
        if (id.isEmpty()) return;
        CosmeticData cos = registeredCosmetics.get(id);
        if (cos == null) return;

        Effect p;
        try {
            p = Effect.valueOf(cos.getParticles());
        } catch (IllegalArgumentException ignored) {
            return;
        }

        // run every tick while projectile is alive
        new BukkitRunnable() {
            @Override
            public void run() {
                if (proj.isDead() || !proj.isValid()) {
                    cancel();
                    return;
                }
                proj.getWorld().playEffect(proj.getLocation(), p, 0);
            }
        }.runTaskTimer(BedWars.plugin, 0L, 1L);
    }

    // ---- listeners ----

    private static class CosmeticsListener implements Listener {

        @EventHandler
        public void onKill(PlayerKillEvent e) {
            CosmeticsManager m = getInstance();
            if (m == null || !m.enabled) return;
            m.playKillEffect(e.getKiller(), e.getVictim());
        }

        @EventHandler
        public void onDeath(PlayerKillEvent e) {
            CosmeticsManager m = getInstance();
            if (m == null || !m.enabled) return;
            m.playDeathAnimation(e.getVictim());
        }

        @EventHandler
        public void onProjectileLaunch(ProjectileLaunchEvent e) {
            CosmeticsManager m = getInstance();
            if (m == null || !m.enabled) return;

            if (!(e.getEntity().getShooter() instanceof Player)) return;
            Player p = (Player) e.getEntity().getShooter();
            IArena arena = Arena.getArenaByPlayer(p);
            if (arena == null) return;

            m.playProjectileTrail(e.getEntity(), p);
        }
    }

    // inner class to hold each player's equipped cosmetics
    private static class PlayerCosmeticsData {
        String killEffect = "";
        String deathAnim = "";
        String victoryDance = "";
        String trailId = "";
    }
}
