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
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Server;

public class ServerReport extends DataReport {

    public ServerReport() {
        super("서버 정보");

        Server server = Bukkit.getServer();

        append("Bukkit 버전", server.getBukkitVersion());
        append("Implementation", server.getName() + " " + server.getVersion());
        append("플레이어 수", "%d/%d", Bukkit.getOnlinePlayers().size(), server.getMaxPlayers());
        append("서버 클래스 출처", server.getClass().getProtectionDomain().getCodeSource().getLocation());

        DataReport onlineMode = new DataReport("온라인 모드");
        onlineMode.append("활성 여부", server.getOnlineMode());
        if (PaperLib.isSpigot()) {
            onlineMode.append("BungeeCord 지원 여부", Bukkit.spigot().getConfig().getBoolean("settings.bungeecord", false));
        }
        if (PaperLib.isPaper()) {
            onlineMode.append("Velocity 지원 여부", Bukkit.spigot().getPaperConfig().getBoolean("proxies.velocity.enabled", false));
        }
        append(onlineMode.getTitle(), onlineMode);

        DataReport spawning = new DataReport("Spawning");
        spawning.append("주변 생물 생성 제한", server.getAmbientSpawnLimit());
        spawning.append("동물 생성 제한", server.getAnimalSpawnLimit());
        spawning.append("몬스터 생성 제한", server.getMonsterSpawnLimit());
        spawning.append("동물 생성 간격(틱)", server.getTicksPerAnimalSpawns());
        spawning.append("몬스터 생성 간격(틱)", server.getTicksPerMonsterSpawns());
        append(spawning.getTitle(), spawning);

        DataReport config = new DataReport("Configuration");
        config.append("네더 활성 여부", server.getAllowNether());
        config.append("엔드 활성 여부", server.getAllowEnd());
        config.append("구조물 생성 여부", server.getGenerateStructures());
        config.append("비행 허용 여부", server.getAllowFlight());
        config.append("접속 제한 간격", server.getConnectionThrottle());
        config.append("잠수 시간 제한", server.getIdleTimeout());
        config.append("종료 메시지", server.getShutdownMessage());
        config.append("기본 게임 모드", server.getDefaultGameMode());
        config.append("주 월드 종류", server.getWorldType());
        config.append("시야 거리", server.getViewDistance());
        append(config.getTitle(), config);

        DataReport protection = new DataReport("Protection");
        protection.append("스폰 반경", server.getSpawnRadius());
        append(protection.getTitle(), protection);
    }

}