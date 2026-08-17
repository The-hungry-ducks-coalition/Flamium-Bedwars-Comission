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
import com.andrei1058.bedwars.api.events.gameplay.GameEndEvent;
import com.andrei1058.bedwars.api.events.player.PlayerBedBreakEvent;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.api.quests.QuestType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;

import static com.andrei1058.bedwars.api.language.Language.getMsg;

public class QuestListener implements Listener {

    @EventHandler
    public void onKill(PlayerKillEvent e) {
        handleProgress(e.getKiller(), "kills", 1);
    }

    @EventHandler
    public void onBedBreak(PlayerBedBreakEvent e) {
        handleProgress(e.getPlayer(), "beds_destroyed", 1);
    }

    @EventHandler
    public void onGameEnd(GameEndEvent e) {
        // everyone who played gets credit for games_played
        for (Player p : e.getArena().getPlayers()) {
            handleProgress(p, "games_played", 1);
        }

        // winners also get wins credit
        if (e.getArena().getWinner() == null) return;

        for (Player p : e.getArena().getPlayers()) {
            if (e.getArena().getTeam(p) == null) continue;
            if (!e.getArena().getTeam(p).equals(e.getArena().getWinner())) continue;
            handleProgress(p, "wins", 1);
        }
    }

    private void handleProgress(Player player, String objective, int amount) {
        QuestManager mgr = QuestManager.getInstance();
        if (mgr == null) return;

        for (Map.Entry<String, QuestType> entry : mgr.getQuests().entrySet()) {
            QuestType quest = entry.getValue();
            if (!quest.getObjective().equals(objective)) continue;

            PlayerQuestData data = mgr.getPlayerQuestData(player.getUniqueId(), quest.getId());
            if (data == null) {
                data = new PlayerQuestData(player.getUniqueId(), quest.getId(), 0, false, false);
            }
            if (data.isCompleted()) continue;

            data.setProgress(data.getProgress() + amount);

            if (data.getProgress() >= quest.getTarget()) {
                data.setCompleted(true);
                player.sendMessage(getMsg(player, Messages.QUESTS_COMPLETED)
                        .replace("{quest}", quest.getDisplayName()));
            } else {
                // progress update, don't spam too much
                // only tell them every 25% or so
                int prev = data.getProgress() - amount;
                int target = quest.getTarget();
                if (target <= 4 || data.getProgress() % Math.max(1, target / 4) == 0) {
                    player.sendMessage(getMsg(player, Messages.QUESTS_PROGRESS)
                            .replace("{quest}", quest.getDisplayName())
                            .replace("{progress}", String.valueOf(data.getProgress()))
                            .replace("{target}", String.valueOf(target)));
                }
            }

            mgr.setPlayerQuestData(player.getUniqueId(), data);
        }
    }
}
