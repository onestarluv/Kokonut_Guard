/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldguard.bukkit.util.report;

import com.sk89q.worldedit.util.report.DataReport;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WorldReport extends DataReport {

    public WorldReport() {
        super("Worlds");

        List<World> worlds = Bukkit.getServer().getWorlds();

        append("월드 수", worlds.size());

        for (World world : worlds) {
            DataReport report = new DataReport("월드: " + world.getName());
            report.append("UUID", world.getUID());
            report.append("월드 종류", world.getWorldType());
            report.append("Environment", world.getEnvironment());
            ChunkGenerator generator = world.getGenerator();
            report.append("청크 생성기", generator != null ? generator.getClass().getName() : "<Default>");

            DataReport spawning = new DataReport("Spawning");
            spawning.append("동물 허용 여부", world.getAllowAnimals());
            spawning.append("몬스터 허용 여부", world.getAllowMonsters());
            spawning.append("주변 생물 생성 제한", world.getAmbientSpawnLimit());
            spawning.append("동물 생성 제한", world.getAnimalSpawnLimit());
            spawning.append("몬스터 생성 제한", world.getMonsterSpawnLimit());
            spawning.append("수중 생물 생성 제한", world.getWaterAnimalSpawnLimit());
            report.append(spawning.getTitle(), spawning);

            DataReport config = new DataReport("Configuration");
            config.append("Difficulty", world.getDifficulty());
            config.append("최대 높이", world.getMaxHeight());
            config.append("해수면 높이", world.getSeaLevel());
            report.append(config.getTitle(), config);

            DataReport state = new DataReport("State");
            state.append("스폰 위치", world.getSpawnLocation());
            state.append("전체 시간", world.getFullTime());
            state.append("날씨 지속 시간", world.getWeatherDuration());
            state.append("천둥 지속 시간", world.getThunderDuration());
            report.append(state.getTitle(), state);

            DataReport protection = new DataReport("Protection");
            protection.append("플레이어 간 전투 허용 여부", world.getPVP());
            protection.append("게임 규칙", Arrays.stream(world.getGameRules())
                    .map(name -> name + "=" + world.getGameRuleValue(name))
                    .collect(Collectors.joining(", ")));
            report.append(protection.getTitle(), protection);

            append(report.getTitle(), report);
        }
    }
}
