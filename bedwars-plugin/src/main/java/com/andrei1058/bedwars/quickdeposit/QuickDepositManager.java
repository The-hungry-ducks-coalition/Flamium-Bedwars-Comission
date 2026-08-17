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

package com.andrei1058.bedwars.quickdeposit;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.configuration.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static com.andrei1058.bedwars.api.language.Language.getMsg;

/**
 * Lets players quickly deposit resources into their team chest
 * by right-clicking it (or shift-clicking items in chest GUI).
 */
public class QuickDepositManager implements Listener, com.andrei1058.bedwars.api.quickdeposit.QuickDepositUtil {

    private static QuickDepositManager instance;
    private final Set<UUID> toggledOn = new HashSet<>();
    private final boolean enabled;

    private QuickDepositManager() {
        this.enabled = BedWars.config.getYml().getBoolean(ConfigPath.QUICK_DEPOSIT_ENABLED, true);
    }

    public static void init() {
        instance = new QuickDepositManager();
        if (instance.enabled) {
            Bukkit.getPluginManager().registerEvents(instance, BedWars.plugin);
            BedWars.debug("QuickDeposit enabled");
        }
    }

    public static QuickDepositManager getInstance() {
        return instance;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isPlayerEnabled(Player player) {
        return toggledOn.contains(player.getUniqueId());
    }

    @Override
    public void togglePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        if (toggledOn.contains(uuid)) {
            toggledOn.remove(uuid);
            player.sendMessage(getMsg(player, Messages.QUICK_DEPOSIT_TOGGLE_OFF));
        } else {
            toggledOn.add(uuid);
            player.sendMessage(getMsg(player, Messages.QUICK_DEPOSIT_TOGGLE_ON));
        }
    }

    @Override
    public boolean depositItem(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        IArena arena = Arena.getArenaByPlayer(player);
        if (arena == null || !arena.isPlayer(player)) return false;

        List<Block> chests = findTeamChests(player, arena);
        if (chests.isEmpty()) return false;

        for (Block chestBlock : chests) {
            Chest chest = (Chest) chestBlock.getState();
            HashMap<Integer, ItemStack> leftover = chest.getInventory().addItem(item.clone());
            if (leftover.isEmpty()) {
                item.setAmount(0);
                return true;
            } else {
                int deposited = item.getAmount() - leftover.get(0).getAmount();
                if (deposited > 0) {
                    item.setAmount(item.getAmount() - deposited);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Scans nearby chests for the player's team chest and deposits
     * all matching resource items from their inventory.
     */
    @Override
    public boolean depositItems(Player player) {
        IArena arena = Arena.getArenaByPlayer(player);
        if (arena == null || !arena.isPlayer(player)) return false;

        List<Block> chests = findTeamChests(player, arena);
        if (chests.isEmpty()) {
            player.sendMessage(getMsg(player, Messages.QUICK_DEPOSIT_NO_CHEST));
            return false;
        }

        int totalDeposited = 0;

        // iterate backwards to avoid concurrent modification issues
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = contents.length - 1; i >= 0; i--) {
            ItemStack item = contents[i];
            if (item == null || item.getType() == Material.AIR) continue;
            if (!isResource(item)) continue;

            for (Block chestBlock : chests) {
                Chest chest = (Chest) chestBlock.getState();
                Inventory chestInv = chest.getInventory();

                HashMap<Integer, ItemStack> leftover = chestInv.addItem(item.clone());
                if (leftover.isEmpty()) {
                    // all items fit
                    totalDeposited += item.getAmount();
                    player.getInventory().clear(i);
                    break;
                } else {
                    // partial deposit
                    ItemStack remaining = leftover.get(0);
                    int deposited = item.getAmount() - remaining.getAmount();
                    if (deposited > 0) {
                        totalDeposited += deposited;
                        item.setAmount(remaining.getAmount());
                    }
                    break;
                }
            }
        }

        if (totalDeposited > 0) {
            player.sendMessage(getMsg(player, Messages.QUICK_DEPOSIT_SUCCESS)
                    .replace("{amount}", String.valueOf(totalDeposited)));
            return true;
        }
        return false;
    }

    /**
     * Check if an item is a bedwars resource (iron, gold, diamond, emerald, etc.)
     */
    private boolean isResource(ItemStack item) {
        String name = item.getType().name();
        // this covers all versions pretty well
        return name.contains("IRON_INGOT") || name.contains("GOLD_INGOT")
                || name.contains("DIAMOND") || name.contains("EMERALD")
                || name.contains("IRON_NUGGET") || name.contains("GOLD_NUGGET");
    }

    /**
     * Find team chests within radius of the player.
     * Only returns chests that belong to the player's team.
     */
    private List<Block> findTeamChests(Player player, IArena arena) {
        List<Block> result = new ArrayList<>();

        int radius = BedWars.config.getYml().getInt(ConfigPath.QUICK_DEPOSIT_RADIUS, 5);
        Block center = player.getLocation().getBlock();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = center.getRelative(x, y, z);
                    Material type = block.getType();
                    if (type != Material.CHEST && type != Material.TRAPPED_CHEST) continue;

                    BlockState state = block.getState();
                    if (!(state instanceof Chest)) continue;

                    result.add(block);
                }
            }
        }
        return result;
    }

    // ---- event handlers ----

    @EventHandler
    public void onPunchChest(PlayerInteractEvent e) {
        if (e.getAction() != Action.LEFT_CLICK_BLOCK) return;
        Player player = e.getPlayer();

        if (!Permissions.hasPermission(player, Permissions.PERMISSION_QUICK_DEPOSIT)) return;

        IArena arena = Arena.getArenaByPlayer(player);
        if (arena == null || !arena.isPlayer(player)) return;

        Block clicked = e.getClickedBlock();
        if (clicked == null) return;
        Material mat = clicked.getType();
        if (mat != Material.CHEST && mat != Material.TRAPPED_CHEST && mat != Material.ENDER_CHEST) return;

        ItemStack held = player.getItemInHand();
        if (held == null || held.getType() == Material.AIR) return;

        QuickDepositManager mgr = getInstance();
        if (mgr == null || !mgr.isEnabled() || !mgr.isPlayerEnabled(player)) return;

        e.setCancelled(true);

        // deposit directly into the clicked chest
        if (mat == Material.ENDER_CHEST || mat == Material.CHEST || mat == Material.TRAPPED_CHEST) {
            Chest chest = (Chest) clicked.getState();
            HashMap<Integer, ItemStack> leftover = chest.getInventory().addItem(held.clone());
            if (leftover.isEmpty()) {
                int amt = held.getAmount();
                player.getInventory().setItemInHand(new ItemStack(Material.AIR));
                player.updateInventory();
                player.sendMessage(getMsg(player, Messages.QUICK_DEPOSIT_SUCCESS)
                        .replace("{amount}", String.valueOf(amt)));
            } else {
                int deposited = held.getAmount() - leftover.get(0).getAmount();
                if (deposited > 0) {
                    held.setAmount(held.getAmount() - deposited);
                    player.updateInventory();
                    player.sendMessage(getMsg(player, Messages.QUICK_DEPOSIT_SUCCESS)
                            .replace("{amount}", String.valueOf(deposited)));
                }
            }
        }
    }

    @EventHandler
    public void onRightClickChest(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = e.getPlayer();

        if (!Permissions.hasPermission(player, Permissions.PERMISSION_QUICK_DEPOSIT)) return;

        IArena arena = Arena.getArenaByPlayer(player);
        if (arena == null || !arena.isPlayer(player)) return;

        Block clicked = e.getClickedBlock();
        if (clicked == null) return;
        if (clicked.getType() != Material.CHEST && clicked.getType() != Material.TRAPPED_CHEST) return;

        QuickDepositManager mgr = getInstance();
        if (mgr == null || !mgr.isEnabled() || !mgr.isPlayerEnabled(player)) return;

        e.setCancelled(true);
        Chest chest = (Chest) clicked.getState();
        ItemStack heldItem = player.getItemInHand();
        if (heldItem != null && heldItem.getType() != Material.AIR) {
            HashMap<Integer, ItemStack> leftover = chest.getInventory().addItem(heldItem.clone());
            if (leftover.isEmpty()) {
                int amt = heldItem.getAmount();
                player.getInventory().setItemInHand(new ItemStack(Material.AIR));
                player.updateInventory();
                player.sendMessage(getMsg(player, Messages.QUICK_DEPOSIT_SUCCESS)
                        .replace("{amount}", String.valueOf(amt)));
            } else {
                int deposited = heldItem.getAmount() - leftover.get(0).getAmount();
                if (deposited > 0) {
                    heldItem.setAmount(heldItem.getAmount() - deposited);
                    player.updateInventory();
                    player.sendMessage(getMsg(player, Messages.QUICK_DEPOSIT_SUCCESS)
                            .replace("{amount}", String.valueOf(deposited)));
                }
            }
        }
    }

    @EventHandler
    public void onShiftClickInChest(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();

        IArena arena = Arena.getArenaByPlayer(player);
        if (arena == null || !arena.isPlayer(player)) return;

        QuickDepositManager mgr = getInstance();
        if (mgr == null || !mgr.isEnabled() || !mgr.isPlayerEnabled(player)) return;

        // only when viewing a chest
        if (e.getInventory().getType() != InventoryType.CHEST) return;
        if (!e.isShiftClick()) return;

        ItemStack cursor = e.getCurrentItem();
        if (cursor == null || cursor.getType() == Material.AIR) return;

        e.setCancelled(true);

        // deposit into the chest being viewed
        if (e.getInventory().getHolder() instanceof Chest) {
            Chest chest = (Chest) e.getInventory().getHolder();
            HashMap<Integer, ItemStack> leftover = chest.getInventory().addItem(cursor.clone());
            if (leftover.isEmpty()) {
                e.getCurrentItem().setAmount(0);
                player.sendMessage(getMsg(player, Messages.QUICK_DEPOSIT_SUCCESS)
                        .replace("{amount}", String.valueOf(cursor.getAmount())));
            } else {
                int deposited = cursor.getAmount() - leftover.get(0).getAmount();
                if (deposited > 0) {
                    cursor.setAmount(cursor.getAmount() - deposited);
                    player.sendMessage(getMsg(player, Messages.QUICK_DEPOSIT_SUCCESS)
                            .replace("{amount}", String.valueOf(deposited)));
                }
            }
        }
    }

    public void removePlayer(Player player) {
        toggledOn.remove(player.getUniqueId());
    }
}
