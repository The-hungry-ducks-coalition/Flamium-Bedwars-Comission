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
import com.andrei1058.bedwars.api.configuration.ConfigManager;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.quests.QuestType;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Manages quests & challenges. Loads from quests.yml.
 * TODO: persist player progress to database (right now memory only)
 */
public class QuestManager extends ConfigManager {

    private static QuestManager instance;
    private final Map<String, QuestType> questDefs = new HashMap<>();
    private final Map<UUID, Map<String, PlayerQuestData>> playerData = new HashMap<>();

    public QuestManager() {
        super(BedWars.plugin, "quests", BedWars.plugin.getDataFolder().getPath());
        instance = this;
        saveDefaults();
        loadQuestDefs();
    }

    public static QuestManager getInstance() {
        return instance;
    }

    private void saveDefaults() {
        getYml().options().header("BedWars Quests & Challenges");

        // -- daily --
        getYml().addDefault(ConfigPath.QUESTS_DAILY_PATH + ".kill-players.type", QuestType.DAILY);
        getYml().addDefault(ConfigPath.QUESTS_DAILY_PATH + ".kill-players.display-name", "&aKill 5 Players");
        getYml().addDefault(ConfigPath.QUESTS_DAILY_PATH + ".kill-players.description", "&7Kill 5 enemy players in BedWars matches");
        getYml().addDefault(ConfigPath.QUESTS_DAILY_PATH + ".kill-players.objective", "kills");
        getYml().addDefault(ConfigPath.QUESTS_DAILY_PATH + ".kill-players.target", 5);
        getYml().addDefault(ConfigPath.QUESTS_DAILY_PATH + ".kill-players.reward-amount", 100);
        getYml().addDefault(ConfigPath.QUESTS_DAILY_PATH + ".kill-players.reward-type", "coins");

        getYml().addDefault(ConfigPath.QUESTS_DAILY_PATH + ".destroy-beds.type", QuestType.DAILY);
        getYml().addDefault(ConfigPath.QUESTS_DAILY_PATH + ".destroy-beds.display-name", "&cDestroy 2 Beds");
        getYml().addDefault(ConfigPath.QUESTS_DAILY_PATH + ".destroy-beds.description", "&7Destroy 2 enemy beds");
        getYml().addDefault(ConfigPath.QUESTS_DAILY_PATH + ".destroy-beds.objective", "beds_destroyed");
        getYml().addDefault(ConfigPath.QUESTS_DAILY_PATH + ".destroy-beds.target", 2);
        getYml().addDefault(ConfigPath.QUESTS_DAILY_PATH + ".destroy-beds.reward-amount", 200);
        getYml().addDefault(ConfigPath.QUESTS_DAILY_PATH + ".destroy-beds.reward-type", "coins");

        getYml().addDefault(ConfigPath.QUESTS_DAILY_PATH + ".win-games.type", QuestType.DAILY);
        getYml().addDefault(ConfigPath.QUESTS_DAILY_PATH + ".win-games.display-name", "&6Win 1 Game");
        getYml().addDefault(ConfigPath.QUESTS_DAILY_PATH + ".win-games.description", "&7Win a BedWars match");
        getYml().addDefault(ConfigPath.QUESTS_DAILY_PATH + ".win-games.objective", "wins");
        getYml().addDefault(ConfigPath.QUESTS_DAILY_PATH + ".win-games.target", 1);
        getYml().addDefault(ConfigPath.QUESTS_DAILY_PATH + ".win-games.reward-amount", 300);
        getYml().addDefault(ConfigPath.QUESTS_DAILY_PATH + ".win-games.reward-type", "coins");

        getYml().addDefault(ConfigPath.QUESTS_DAILY_PATH + ".final-kills.type", QuestType.DAILY);
        getYml().addDefault(ConfigPath.QUESTS_DAILY_PATH + ".final-kills.display-name", "&bGet 3 Final Kills");
        getYml().addDefault(ConfigPath.QUESTS_DAILY_PATH + ".final-kills.description", "&7Get 3 final kills in a game");
        getYml().addDefault(ConfigPath.QUESTS_DAILY_PATH + ".final-kills.objective", "final_kills");
        getYml().addDefault(ConfigPath.QUESTS_DAILY_PATH + ".final-kills.target", 3);
        getYml().addDefault(ConfigPath.QUESTS_DAILY_PATH + ".final-kills.reward-amount", 400);
        getYml().addDefault(ConfigPath.QUESTS_DAILY_PATH + ".final-kills.reward-type", "coins");

        // -- weekly --
        getYml().addDefault(ConfigPath.QUESTS_WEEKLY_PATH + ".final-kills-weekly.type", QuestType.WEEKLY);
        getYml().addDefault(ConfigPath.QUESTS_WEEKLY_PATH + ".final-kills-weekly.display-name", "&bGet 20 Final Kills");
        getYml().addDefault(ConfigPath.QUESTS_WEEKLY_PATH + ".final-kills-weekly.description", "&720 final kills over the week");
        getYml().addDefault(ConfigPath.QUESTS_WEEKLY_PATH + ".final-kills-weekly.objective", "final_kills");
        getYml().addDefault(ConfigPath.QUESTS_WEEKLY_PATH + ".final-kills-weekly.target", 20);
        getYml().addDefault(ConfigPath.QUESTS_WEEKLY_PATH + ".final-kills-weekly.reward-amount", 1500);
        getYml().addDefault(ConfigPath.QUESTS_WEEKLY_PATH + ".final-kills-weekly.reward-type", "coins");

        getYml().addDefault(ConfigPath.QUESTS_WEEKLY_PATH + ".play-games.type", QuestType.WEEKLY);
        getYml().addDefault(ConfigPath.QUESTS_WEEKLY_PATH + ".play-games.display-name", "&ePlay 15 Games");
        getYml().addDefault(ConfigPath.QUESTS_WEEKLY_PATH + ".play-games.description", "&7Play 15 matches over the week");
        getYml().addDefault(ConfigPath.QUESTS_WEEKLY_PATH + ".play-games.objective", "games_played");
        getYml().addDefault(ConfigPath.QUESTS_WEEKLY_PATH + ".play-games.target", 15);
        getYml().addDefault(ConfigPath.QUESTS_WEEKLY_PATH + ".play-games.reward-amount", 2000);
        getYml().addDefault(ConfigPath.QUESTS_WEEKLY_PATH + ".play-games.reward-type", "coins");

        getYml().addDefault(ConfigPath.QUESTS_WEEKLY_PATH + ".win-weekly.type", QuestType.WEEKLY);
        getYml().addDefault(ConfigPath.QUESTS_WEEKLY_PATH + ".win-weekly.display-name", "&6Win 5 Games");
        getYml().addDefault(ConfigPath.QUESTS_WEEKLY_PATH + ".win-weekly.description", "&7Win 5 matches over the week");
        getYml().addDefault(ConfigPath.QUESTS_WEEKLY_PATH + ".win-weekly.objective", "wins");
        getYml().addDefault(ConfigPath.QUESTS_WEEKLY_PATH + ".win-weekly.target", 5);
        getYml().addDefault(ConfigPath.QUESTS_WEEKLY_PATH + ".win-weekly.reward-amount", 2500);
        getYml().addDefault(ConfigPath.QUESTS_WEEKLY_PATH + ".win-weekly.reward-type", "coins");

        getYml().addDefault(ConfigPath.QUESTS_WEEKLY_PATH + ".kill-weekly.type", QuestType.WEEKLY);
        getYml().addDefault(ConfigPath.QUESTS_WEEKLY_PATH + ".kill-weekly.display-name", "&aKill 50 Players");
        getYml().addDefault(ConfigPath.QUESTS_WEEKLY_PATH + ".kill-weekly.description", "&750 kills over the week");
        getYml().addDefault(ConfigPath.QUESTS_WEEKLY_PATH + ".kill-weekly.objective", "kills");
        getYml().addDefault(ConfigPath.QUESTS_WEEKLY_PATH + ".kill-weekly.target", 50);
        getYml().addDefault(ConfigPath.QUESTS_WEEKLY_PATH + ".kill-weekly.reward-amount", 2000);
        getYml().addDefault(ConfigPath.QUESTS_WEEKLY_PATH + ".kill-weekly.reward-type", "coins");

        getYml().options().copyDefaults(true);
        save();
    }

    private void loadQuestDefs() {
        loadSection(ConfigPath.QUESTS_DAILY_PATH);
        loadSection(ConfigPath.QUESTS_WEEKLY_PATH);
        BedWars.debug("Loaded " + questDefs.size() + " quest definitions");
    }

    private void loadSection(String path) {
        if (getYml().getConfigurationSection(path) == null) return;

        for (String key : getYml().getConfigurationSection(path).getKeys(false)) {
            String full = path + "." + key;
            String type = getYml().getString(full + ".type", QuestType.DAILY);
            String name = getYml().getString(full + ".display-name", key);
            String desc = getYml().getString(full + ".description", "");
            String objective = getYml().getString(full + ".objective", "");
            int target = getYml().getInt(full + ".target", 1);
            int reward = getYml().getInt(full + ".reward-amount", 0);
            String rewardType = getYml().getString(full + ".reward-type", "coins");

            questDefs.put(key, new QuestType(key, type, name, desc, objective, target, reward, rewardType));
        }
    }

    public Map<String, QuestType> getQuests() {
        return Collections.unmodifiableMap(questDefs);
    }

    public List<QuestType> getQuestsByType(String type) {
        List<QuestType> out = new ArrayList<>();
        for (QuestType q : questDefs.values()) {
            if (q.getType().equalsIgnoreCase(type)) out.add(q);
        }
        return out;
    }

    public QuestType getQuest(String id) {
        return questDefs.get(id);
    }

    public PlayerQuestData getPlayerQuestData(UUID uuid, String questId) {
        Map<String, PlayerQuestData> data = playerData.get(uuid);
        return data != null ? data.get(questId) : null;
    }

    public void setPlayerQuestData(UUID uuid, PlayerQuestData data) {
        playerData.computeIfAbsent(uuid, k -> new HashMap<>()).put(data.getQuestId(), data);
    }

    public Map<String, PlayerQuestData> getAllPlayerData(UUID uuid) {
        return playerData.getOrDefault(uuid, Collections.emptyMap());
    }

    public void removePlayer(Player player) {
        playerData.remove(player.getUniqueId());
    }
}
