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

import java.util.UUID;

public class PlayerQuestData {

    private final UUID playerUuid;
    private final String questId;
    private int progress;
    private boolean completed;
    private boolean claimed;

    public PlayerQuestData(UUID playerUuid, String questId, int progress, boolean completed, boolean claimed) {
        this.playerUuid = playerUuid;
        this.questId = questId;
        this.progress = progress;
        this.completed = completed;
        this.claimed = claimed;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getQuestId() {
        return questId;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isClaimed() {
        return claimed;
    }

    public void setClaimed(boolean claimed) {
        this.claimed = claimed;
    }
}
