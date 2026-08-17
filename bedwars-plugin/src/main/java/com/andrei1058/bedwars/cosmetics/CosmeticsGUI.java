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
import com.andrei1058.bedwars.api.cosmetics.CosmeticType;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.configuration.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static com.andrei1058.bedwars.api.language.Language.getMsg;

public class CosmeticsGUI implements Listener {

    private static final String INVENTORY_NAME = "Cosmetics";
    private static final int INVENTORY_SIZE = 54;

    public static void openCosmeticsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, INVENTORY_SIZE,
                getMsg(player, Messages.COSMETICS_GUI_NAME));

        CosmeticsManager mgr = CosmeticsManager.getInstance();
        if (mgr == null) return;

        // Kill Effects category
        ItemStack killEffectItem = createCategoryItem(Material.IRON_SWORD,
                getMsg(player, Messages.COSMETICS_CATEGORY_KILL_EFFECT),
                getMsg(player, Messages.COSMETICS_CATEGORY_KILL_EFFECT_LORE));
        inv.setItem(10, killEffectItem);

        // Death Animations category
        ItemStack deathAnimItem = createCategoryItem(Material.SKULL_ITEM,
                getMsg(player, Messages.COSMETICS_CATEGORY_DEATH_ANIMATION),
                getMsg(player, Messages.COSMETICS_CATEGORY_DEATH_ANIMATION_LORE));
        inv.setItem(12, deathAnimItem);

        // Victory Dances category
        ItemStack victoryDanceItem = createCategoryItem(Material.NOTE_BLOCK,
                getMsg(player, Messages.COSMETICS_CATEGORY_VICTORY_DANCE),
                getMsg(player, Messages.COSMETICS_CATEGORY_VICTORY_DANCE_LORE));
        inv.setItem(14, victoryDanceItem);

        // Projectile Trails category
        ItemStack trailItem = createCategoryItem(Material.ARROW,
                getMsg(player, Messages.COSMETICS_CATEGORY_TRAIL),
                getMsg(player, Messages.COSMETICS_CATEGORY_TRAIL_LORE));
        inv.setItem(16, trailItem);

        // Separator
        ItemStack separator = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
        ItemMeta sepMeta = separator.getItemMeta();
        if (sepMeta != null) {
            sepMeta.setDisplayName(" ");
            separator.setItemMeta(sepMeta);
        }
        for (int i = 0; i < 9; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, separator);
        }
        for (int i = 45; i < 54; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, separator);
        }

        player.openInventory(inv);
    }

    public static void openCategoryMenu(Player player, String type) {
        CosmeticsManager mgr = CosmeticsManager.getInstance();
        if (mgr == null) return;

        List<CosmeticData> cosmetics = mgr.getCosmeticsByType(type);
        int size = Math.max(27, ((cosmetics.size() / 9) + 1) * 9);

        Inventory inv = Bukkit.createInventory(null, size,
                getMsg(player, Messages.COSMETICS_GUI_NAME) + " - " + type);

        int slot = 10;
        for (CosmeticData cosmetic : cosmetics) {
            if (slot >= size - 9) break;
            ItemStack item = createCosmeticItem(cosmetic, player);
            inv.setItem(slot, item);
            slot++;
            if (slot % 9 == 8) slot += 2;
        }

        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(getMsg(player, Messages.COSMETICS_BACK_BUTTON));
            back.setItemMeta(backMeta);
        }
        inv.setItem(size - 9, back);

        player.openInventory(inv);
    }

    private static ItemStack createCategoryItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Collections.singletonList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createCosmeticItem(CosmeticData cosmetic, Player player) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(cosmetic.getDisplayName());
            List<String> lore = new ArrayList<>();
            lore.add(getMsg(player, Messages.COSMETICS_ITEM_PRICE).replace("{price}", String.valueOf((int) cosmetic.getPrice())));
            lore.add(getMsg(player, Messages.COSMETICS_ITEM_CURRENCY).replace("{currency}", cosmetic.getCurrency()));
            lore.add("");
            lore.add(getMsg(player, Messages.COSMETICS_ITEM_CLICK_TO_EQUIP));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        if (e.getInventory() == null) return;
        if (!e.getInventory().getTitle().contains(getMsg(player, Messages.COSMETICS_GUI_NAME))) return;

        e.setCancelled(true);
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        CosmeticsManager mgr = CosmeticsManager.getInstance();
        if (mgr == null) return;

        // Handle category clicks
        if (clicked.getType() == Material.IRON_SWORD) {
            openCategoryMenu(player, CosmeticType.KILL_EFFECT);
        } else if (clicked.getType() == Material.SKULL_ITEM) {
            openCategoryMenu(player, CosmeticType.DEATH_ANIMATION);
        } else if (clicked.getType() == Material.NOTE_BLOCK) {
            openCategoryMenu(player, CosmeticType.VICTORY_DANCE);
        } else if (clicked.getType() == Material.ARROW) {
            if (e.getSlot() >= e.getInventory().getSize() - 9) {
                openCosmeticsMenu(player);
            }
        } else if (clicked.getType() == Material.BOOK) {
            // Handle cosmetic equip
            ItemMeta meta = clicked.getItemMeta();
            if (meta != null && meta.getLore() != null) {
                for (Map.Entry<String, CosmeticData> entry : mgr.getRegisteredCosmetics().entrySet()) {
                    if (entry.getValue().getDisplayName().equals(meta.getDisplayName())) {
                        CosmeticData data = entry.getValue();
                        switch (data.getType()) {
                            case CosmeticType.KILL_EFFECT:
                                mgr.setKillEffect(player, data.getId());
                                break;
                            case CosmeticType.DEATH_ANIMATION:
                                mgr.setDeathAnimation(player, data.getId());
                                break;
                            case CosmeticType.VICTORY_DANCE:
                                mgr.setVictoryDance(player, data.getId());
                                break;
                            case CosmeticType.PROJECTILE_TRAIL:
                                mgr.setProjectileTrail(player, data.getId());
                                break;
                        }
                        player.sendMessage(getMsg(player, Messages.COSMETICS_EQUIPPED)
                                .replace("{cosmetic}", data.getDisplayName()));
                        openCategoryMenu(player, data.getType());
                        break;
                    }
                }
            }
        }
    }
}
