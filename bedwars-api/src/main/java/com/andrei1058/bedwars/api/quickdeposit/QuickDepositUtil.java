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

package com.andrei1058.bedwars.api.quickdeposit;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface QuickDepositUtil {

    /**
     * Check if quick deposit is enabled.
     */
    boolean isEnabled();

    /**
     * Check if a player has quick deposit enabled.
     */
    boolean isPlayerEnabled(Player player);

    /**
     * Toggle quick deposit for a player.
     */
    void togglePlayer(Player player);

    /**
     * Deposit matching items from player inventory into nearby team chest.
     *
     * @param player the player performing the deposit
     * @return true if items were deposited
     */
    boolean depositItems(Player player);

    /**
     * Deposit a specific item stack into nearby team chest.
     *
     * @param player the player performing the deposit
     * @param item the item to deposit
     * @return true if items were deposited
     */
    boolean depositItem(Player player, ItemStack item);
}
