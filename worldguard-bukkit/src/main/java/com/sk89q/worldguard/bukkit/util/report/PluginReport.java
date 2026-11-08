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
import org.bukkit.plugin.Plugin;

public class PluginReport extends DataReport {

    public PluginReport() {
        super("Plugins");

        Plugin[] plugins = Bukkit.getServer().getPluginManager().getPlugins();

        append("플러그인 수", plugins.length);

        for (Plugin plugin : plugins) {
            DataReport report = new DataReport("플러그인: " + plugin.getName());
            report.append("활성 여부", plugin.isEnabled());
            report.append("전체 이름", plugin.getDescription().getFullName());
            report.append("Version", plugin.getDescription().getVersion());
            report.append("Website", plugin.getDescription().getWebsite());
            report.append("Description", plugin.getDescription().getDescription());
            report.append("Authors", plugin.getDescription().getAuthors());
            report.append("먼저 불러올 플러그인", plugin.getDescription().getLoadBefore());
            report.append("Dependencies", plugin.getDescription().getDepend());
            report.append("선택 의존성", plugin.getDescription().getSoftDepend());
            report.append("Folder", plugin.getDataFolder().getAbsoluteFile());
            report.append("진입점", plugin.getDescription().getMain());
            report.append("Class", plugin.getClass().getName());
            report.append("클래스 출처", plugin.getClass().getProtectionDomain().getCodeSource().getLocation());
            append(report.getTitle(), report);
        }
    }

}
