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

package com.andrei1058.bedwars.physics;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.arena.LastHit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.Collection;

import static com.andrei1058.bedwars.BedWars.config;

/**
 * Handles fireball knockback and TNT jump physics.
 * Values are loaded from config so server owners can tune them.
 *
 * TODO: maybe add per-arena overrides at some point
 */
public class DynamicPhysicsManager implements Listener, com.andrei1058.bedwars.api.physics.DynamicPhysicsUtil {

    private static DynamicPhysicsManager instance;

    // fireball stuff
    private double fbExplosionSize;
    private boolean fbMakeFire;
    private double fbHorizontal;
    private double fbVertical;
    private double fbDmgSelf, fbDmgEnemy, fbDmgTeam;

    // tnt jump stuff
    private double tntBaryY;
    private double tntStrengthReduction;
    private double tntYReduction;
    private boolean tntDmgSelf, tntDmgTeam, tntDmgOthers;

    // dynamic velocity multiplier
    private double velocityMult;
    private boolean dynKbEnabled;

    public DynamicPhysicsManager() {
        loadValues();
    }

    public static void init() {
        instance = new DynamicPhysicsManager();
        BedWars.debug("DynamicPhysics loaded (fireball + TNT jump tweaks)");
    }

    public static DynamicPhysicsManager getInstance() {
        return instance;
    }

    private void loadValues() {
        // fireball
        fbExplosionSize = config.getYml().getDouble(ConfigPath.GENERAL_FIREBALL_EXPLOSION_SIZE, 3.0);
        fbMakeFire = config.getYml().getBoolean(ConfigPath.GENERAL_FIREBALL_MAKE_FIRE, false);
        fbHorizontal = config.getYml().getDouble(ConfigPath.GENERAL_FIREBALL_KNOCKBACK_HORIZONTAL, 1.2) * -1;
        fbVertical = config.getYml().getDouble(ConfigPath.GENERAL_FIREBALL_KNOCKBACK_VERTICAL, 0.6);
        fbDmgSelf = config.getYml().getDouble(ConfigPath.GENERAL_FIREBALL_DAMAGE_SELF, 4.0);
        fbDmgEnemy = config.getYml().getDouble(ConfigPath.GENERAL_FIREBALL_DAMAGE_ENEMY, 4.0);
        fbDmgTeam = config.getYml().getDouble(ConfigPath.GENERAL_FIREBALL_DAMAGE_TEAMMATES, 0.0);

        // tnt
        tntBaryY = config.getYml().getDouble(ConfigPath.GENERAL_TNT_JUMP_BARYCENTER_IN_Y, 0.8);
        tntStrengthReduction = config.getYml().getDouble(ConfigPath.GENERAL_TNT_JUMP_STRENGTH_REDUCTION, 1.0);
        tntYReduction = config.getYml().getDouble(ConfigPath.GENERAL_TNT_JUMP_Y_REDUCTION, 0.5);
        tntDmgSelf = config.getYml().getBoolean(ConfigPath.GENERAL_TNT_JUMP_DAMAGE_SELF, false);
        tntDmgTeam = config.getYml().getBoolean(ConfigPath.GENERAL_TNT_JUMP_DAMAGE_TEAMMATES, false);
        tntDmgOthers = config.getYml().getBoolean(ConfigPath.GENERAL_TNT_JUMP_DAMAGE_OTHERS, true);

        // dynamic settings
        dynKbEnabled = config.getYml().getBoolean(ConfigPath.DYNAMIC_PHYSICS_ENABLED, true);
        velocityMult = config.getYml().getDouble(ConfigPath.DYNAMIC_PHYSICS_VELOCITY_MULTIPLIER, 1.0);
    }

    public void reload() {
        loadValues();
        BedWars.debug("DynamicPhysics config reloaded");
    }

    @Override
    public boolean isEnabled() {
        return dynKbEnabled;
    }

    // ============================================================
    //  FIREBALL
    // ============================================================

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFireballHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Fireball)) return;
        if (!dynKbEnabled) return;

        Fireball fb = (Fireball) e.getEntity();
        ProjectileSource src = fb.getShooter();
        if (!(src instanceof Player)) return;
        Player shooter = (Player) src;

        IArena arena = Arena.getArenaByPlayer(shooter);
        if (arena == null) return;

        Location hitLoc = fb.getLocation();
        World world = hitLoc.getWorld();
        if (world == null) return;

        Collection<Entity> nearby = world.getNearbyEntities(hitLoc, fbExplosionSize, fbExplosionSize, fbExplosionSize);

        Vector hitVec = hitLoc.toVector();

        for (Entity ent : nearby) {
            if (!(ent instanceof Player)) continue;
            Player target = (Player) ent;
            if (!arena.isPlayer(target)) continue;

            Vector targetVec = target.getLocation().toVector();
            Vector dir = hitVec.clone().subtract(targetVec).normalize();

            double dist = target.getLocation().distance(hitLoc);
            // further away = less knockback, makes sense
            double distMult = Math.max(0.4, 1.0 - (dist / (fbExplosionSize * 2.0)));

            double hPush = fbHorizontal * distMult * velocityMult;
            Vector horizontal = dir.multiply(hPush);

            double yComponent = dir.getY();
            double vPush;
            if (yComponent < 0) {
                yComponent += 1.5;
            }
            if (yComponent <= 0.5) {
                vPush = fbVertical * 1.5 * velocityMult;
            } else {
                vPush = yComponent * fbVertical * 1.5 * velocityMult;
            }

            target.setVelocity(horizontal.setY(vPush));

            // track last hit for kill credit
            LastHit lh = LastHit.getLastHit(target);
            if (lh != null) {
                lh.setDamager(shooter);
                lh.setTime(System.currentTimeMillis());
            } else {
                new LastHit(target, shooter, System.currentTimeMillis());
            }

            // damage
            if (target.equals(shooter)) {
                if (fbDmgSelf > 0) target.damage(fbDmgSelf);
            } else if (arena.getTeam(target) != null && arena.getTeam(shooter) != null
                    && arena.getTeam(target).equals(arena.getTeam(shooter))) {
                if (fbDmgTeam > 0) target.damage(fbDmgTeam);
            } else {
                if (fbDmgEnemy > 0) target.damage(fbDmgEnemy);
            }
        }
    }

    @EventHandler
    public void onFireballDirectHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Fireball)) return;
        if (!(e.getEntity() instanceof Player)) return;
        if (Arena.getArenaByPlayer((Player) e.getEntity()) == null) return;
        // don't deal normal fireball damage, we handle it ourselves
        e.setCancelled(true);
    }

    @EventHandler
    public void onFireballExplode(ExplosionPrimeEvent e) {
        if (!(e.getEntity() instanceof Fireball)) return;
        ProjectileSource shooter = ((Fireball) e.getEntity()).getShooter();
        if (!(shooter instanceof Player)) return;
        if (Arena.getArenaByPlayer((Player) shooter) == null) return;
        e.setFire(fbMakeFire);
    }

    // ============================================================
    //  TNT JUMP
    // ============================================================

    @EventHandler(priority = EventPriority.HIGH)
    public void onTntExplode(EntityExplodeEvent e) {
        if (!(e.getEntity() instanceof TNTPrimed)) return;
        if (!dynKbEnabled) return;

        TNTPrimed tnt = (TNTPrimed) e.getEntity();
        Location tntLoc = tnt.getLocation();
        World world = tntLoc.getWorld();
        if (world == null) return;

        Player source = null;
        if (tnt.getSource() instanceof Player) {
            source = (Player) tnt.getSource();
        }

        double maxDist = 5.0; // hard cap, no point going further
        Collection<Entity> nearby = world.getNearbyEntities(tntLoc, maxDist, maxDist, maxDist);

        for (Entity ent : nearby) {
            if (!(ent instanceof Player)) continue;
            Player p = (Player) ent;

            IArena arena = Arena.getArenaByPlayer(p);
            if (arena == null || !arena.isPlayer(p)) continue;

            Vector pVec = p.getLocation().toVector();
            Vector tVec = tntLoc.toVector();
            Vector pushDir = pVec.clone().subtract(tVec);

            double dist = p.getLocation().distance(tntLoc);
            if (dist < 0.1) dist = 0.1; // div by zero guard

            // strength falls off with distance
            double strength = (1.0 - (dist / maxDist)) * tntStrengthReduction * velocityMult;
            if (strength < 0) strength = 0;

            Vector knockback = pushDir.normalize().multiply(strength);

            // vertical boost also scales with distance
            double vBoost = tntBaryY * (1.0 - (dist * tntYReduction / maxDist));
            if (vBoost < 0) vBoost = 0;

            p.setVelocity(knockback.setY(vBoost));

            // tnt jump damage
            if (source != null && !p.equals(source)) {
                boolean sameTeam = arena.getTeam(p) != null && arena.getTeam(source) != null
                        && arena.getTeam(p).equals(arena.getTeam(source));
                if (sameTeam) {
                    if (tntDmgTeam) p.damage(2.0);
                } else {
                    if (tntDmgOthers) p.damage(4.0);
                }
            } else if (p.equals(source) && tntDmgSelf) {
                p.damage(1.0);
            }
        }
    }

    // ---- getters for the API ----

    @Override
    public double getFireballExplosionSize() { return fbExplosionSize; }
    @Override
    public double getFireballHorizontalKnockback() { return fbHorizontal; }
    @Override
    public double getFireballVerticalKnockback() { return fbVertical; }
    @Override
    public double getTntBarycenterY() { return tntBaryY; }
    @Override
    public double getTntStrengthReduction() { return tntStrengthReduction; }
    @Override
    public double getVelocityMultiplier() { return velocityMult; }

    @Override
    public void setVelocityMultiplier(double val) { this.velocityMult = val; }
    public void setDynamicKnockbackEnabled(boolean val) { this.dynKbEnabled = val; }
}
