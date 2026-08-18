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

package com.andrei1058.bedwars.api.quests;

public class QuestType {

    public static final String DAILY = "daily";
    public static final String WEEKLY = "weekly";
    public static final String SPECIAL = "special";

    private final String id;
    private final String type;
    private final String displayName;
    private final String description;
    private final String objective;
    private final int target;
    private final int rewardAmount;
    private final String rewardType;

    public QuestType(String id, String type, String displayName, String description, String objective, int target, int rewardAmount, String rewardType) {
        this.id = id;
        this.type = type;
        this.displayName = displayName;
        this.description = description;
        this.objective = objective;
        this.target = target;
        this.rewardAmount = rewardAmount;
        this.rewardType = rewardType;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getObjective() {
        return objective;
    }

    public int getTarget() {
        return target;
    }

    public int getRewardAmount() {
        return rewardAmount;
    }

    public String getRewardType() {
        return rewardType;
    }
}
