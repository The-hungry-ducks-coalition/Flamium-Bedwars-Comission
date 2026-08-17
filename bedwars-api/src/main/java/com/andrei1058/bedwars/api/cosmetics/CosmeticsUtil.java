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

package com.andrei1058.bedwars.api.cosmetics;

import org.bukkit.entity.Player;

public interface CosmeticsUtil {

    /**
     * Check if cosmetics are enabled.
     */
    boolean isEnabled();

    /**
     * Get the equipped kill effect for a player.
     */
    String getKillEffect(Player player);

    /**
     * Set the kill effect for a player.
     */
    void setKillEffect(Player player, String effectId);

    /**
     * Get the equipped death animation for a player.
     */
    String getDeathAnimation(Player player);

    /**
     * Set the death animation for a player.
     */
    void setDeathAnimation(Player player, String animationId);

    /**
     * Get the equipped victory dance for a player.
     */
    String getVictoryDance(Player player);

    /**
     * Set the victory dance for a player.
     */
    void setVictoryDance(Player player, String danceId);

    /**
     * Get the equipped projectile trail for a player.
     */
    String getProjectileTrail(Player player);

    /**
     * Set the projectile trail for a player.
     */
    void setProjectileTrail(Player player, String trailId);
}
