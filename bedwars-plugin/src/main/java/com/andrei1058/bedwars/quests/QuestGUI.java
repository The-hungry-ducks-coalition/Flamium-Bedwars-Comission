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

package com.andrei1058.bedwars.quests;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.api.quests.QuestType;
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

public class QuestGUI implements Listener {

    private static final int SIZE = 54;

    public static void openQuestMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, SIZE, getMsg(player, Messages.QUESTS_GUI_NAME));

        QuestManager mgr = QuestManager.getInstance();
        if (mgr == null) return;

        inv.setItem(10, makeItem(Material.WATCH, getMsg(player, Messages.QUESTS_CATEGORY_DAILY),
                getMsg(player, Messages.QUESTS_CATEGORY_DAILY_LORE)));
        inv.setItem(12, makeItem(Material.WATCH, getMsg(player, Messages.QUESTS_CATEGORY_WEEKLY),
                getMsg(player, Messages.QUESTS_CATEGORY_WEEKLY_LORE)));
        inv.setItem(14, makeItem(Material.NETHER_STAR, getMsg(player, Messages.QUESTS_CATEGORY_SPECIAL),
                getMsg(player, Messages.QUESTS_CATEGORY_SPECIAL_LORE)));

        // gray glass panes as filler
        ItemStack pane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
        ItemMeta pm = pane.getItemMeta();
        if (pm != null) { pm.setDisplayName(" "); pane.setItemMeta(pm); }

        for (int i = 0; i < 9; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, pane);
        }
        for (int i = 45; i < SIZE; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, pane);
        }

        player.openInventory(inv);
    }

    public static void openQuestCategory(Player player, String type) {
        QuestManager mgr = QuestManager.getInstance();
        if (mgr == null) return;

        List<QuestType> quests = mgr.getQuestsByType(type);
        int rows = Math.max(3, (quests.size() / 7) + 3); // at least 3 rows
        int size = rows * 9;

        Inventory inv = Bukkit.createInventory(null, size,
                getMsg(player, Messages.QUESTS_GUI_NAME) + " - " + type);

        int slot = 10;
        for (QuestType q : quests) {
            if (slot >= size - 9) break;
            inv.setItem(slot, makeQuestItem(q, player, mgr));
            slot++;
            // skip rightmost column so back button doesn't overlap
            if (slot % 9 == 8) slot += 2;
        }

        // back arrow
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bm = back.getItemMeta();
        if (bm != null) {
            bm.setDisplayName(getMsg(player, Messages.QUESTS_BACK_BUTTON));
            back.setItemMeta(bm);
        }
        inv.setItem(size - 9, back);

        player.openInventory(inv);
    }

    private static ItemStack makeItem(Material mat, String name, String lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Collections.singletonList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack makeQuestItem(QuestType quest, Player player, QuestManager mgr) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(quest.getDisplayName());

        List<String> lore = new ArrayList<>();
        lore.add(quest.getDescription());
        lore.add("");

        PlayerQuestData data = mgr.getPlayerQuestData(player.getUniqueId(), quest.getId());
        int prog = data != null ? data.getProgress() : 0;
        boolean done = data != null && data.isCompleted();
        boolean claimed = data != null && data.isClaimed();

        if (claimed) {
            lore.add(getMsg(player, Messages.QUESTS_STATUS_CLAIMED));
        } else if (done) {
            lore.add(getMsg(player, Messages.QUESTS_STATUS_COMPLETED_CLICK));
        } else {
            lore.add(getMsg(player, Messages.QUESTS_STATUS_PROGRESS)
                    .replace("{progress}", String.valueOf(prog))
                    .replace("{target}", String.valueOf(quest.getTarget())));
        }

        lore.add("");
        lore.add(getMsg(player, Messages.QUESTS_REWARD_INFO)
                .replace("{amount}", String.valueOf(quest.getRewardAmount()))
                .replace("{type}", quest.getRewardType()));

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        Inventory inv = e.getInventory();
        if (inv == null) return;

        String title = inv.getTitle();
        String menuTitle = getMsg(player, Messages.QUESTS_GUI_NAME);
        if (!title.contains(menuTitle)) return;

        e.setCancelled(true);
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        QuestManager mgr = QuestManager.getInstance();
        if (mgr == null) return;

        Material mat = clicked.getType();

        // category icons
        if (mat == Material.WATCH) {
            openQuestCategory(player, QuestType.DAILY);
        } else if (mat == Material.WATCH) {
            openQuestCategory(player, QuestType.WEEKLY);
        } else if (mat == Material.NETHER_STAR) {
            openQuestCategory(player, QuestType.SPECIAL);
        } else if (mat == Material.ARROW) {
            // back button - check if we're in a sub-menu
            if (title.contains("-")) {
                openQuestMenu(player);
            }
        } else if (mat == Material.PAPER) {
            // clicked on a quest item - try to claim reward
            ItemMeta meta = clicked.getItemMeta();
            if (meta == null || meta.getLore() == null) return;

            String clickedName = meta.getDisplayName();
            for (Map.Entry<String, QuestType> entry : mgr.getQuests().entrySet()) {
                QuestType q = entry.getValue();
                if (!q.getDisplayName().equals(clickedName)) continue;

                PlayerQuestData data = mgr.getPlayerQuestData(player.getUniqueId(), q.getId());
                if (data != null && data.isCompleted() && !data.isClaimed()) {
                    data.setClaimed(true);
                    mgr.setPlayerQuestData(player.getUniqueId(), data);
                    player.sendMessage(getMsg(player, Messages.QUESTS_REWARD_CLAIMED)
                            .replace("{amount}", String.valueOf(q.getRewardAmount()))
                            .replace("{type}", q.getRewardType()));
                    // refresh the menu
                    openQuestCategory(player, q.getType());
                }
                break;
            }
        }
    }
}
