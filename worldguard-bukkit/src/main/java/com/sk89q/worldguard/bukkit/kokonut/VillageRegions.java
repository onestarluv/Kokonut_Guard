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

package com.sk89q.worldguard.bukkit.kokonut;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedChunkRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import kr.gomsv.kokonutCore.api.village.VillageChangeEvent;
import kr.gomsv.kokonutCore.api.village.VillageService;
import kr.gomsv.kokonutCore.api.village.VillageTerritory;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * 에고버스: keeps one {@link ProtectedChunkRegion} per Kokonut-Core village ({@code village-<id>}) in the world that
 * holds its territory. Owners = the 마을장, members = every villager. Flags: {@code kokonut-hud} = village
 * name, {@code build} = deny for non-members (only villagers build).
 * <p>
 * The regions are transient (never saved): Kokonut-Core's database is the source, loaded at start and on every
 * {@link VillageChangeEvent}. Flags set on a village region live until the next restart; flags meant for every village
 * go on a region named {@value #DEFAULT_PARENT} in that world (any shape, e.g. a global or tiny cuboid region), which
 * becomes each village region's parent.
 * <p>
 * Only load this class after confirming Kokonut-Core is enabled.
 */
public final class VillageRegions implements Listener {

    public static final String PREFIX = "village-";
    public static final String DEFAULT_PARENT = "village-default";

    private final WorldGuardPlugin plugin;
    private final VillageService villages;
    private final Map<Long, VillageTerritory> known = new HashMap<>();

    private VillageRegions(WorldGuardPlugin plugin, VillageService villages) {
        this.plugin = plugin;
        this.villages = villages;
    }

    /**
     * Starts syncing; does nothing when Kokonut-Core has no village service.
     *
     * @param plugin this plugin
     */
    public static void start(WorldGuardPlugin plugin) {
        VillageService villages = plugin.getServer().getServicesManager().load(VillageService.class);
        if (villages == null) {
            plugin.getLogger().warning("Kokonut-Core 마을 서비스가 없어 마을 영역을 만들지 않습니다.");
            return;
        }
        VillageRegions sync = new VillageRegions(plugin, villages);
        plugin.getServer().getPluginManager().registerEvents(sync, plugin);
        villages.territories(all -> {
            all.forEach(sync::apply);
            plugin.getLogger().info("Kokonut-Core 마을 영토 " + all.size() + "곳을 영역으로 불러왔습니다.");
        });
        // "/영역 불러오기" replaces the loaded regions with the saved ones, which never include these.
        plugin.getServer().getScheduler().runTaskTimer(plugin, sync::restoreMissing, 20 * 60, 20 * 60);
    }

    @EventHandler
    public void onVillageChange(VillageChangeEvent event) {
        long id = event.villageId();
        villages.territory(id, territory -> {
            if (territory.isPresent()) {
                apply(territory.get());
            } else {
                VillageTerritory old = known.remove(id);
                if (old != null && !old.chunks().isEmpty()) {
                    remove(old.chunks().getFirst().world(), id);
                }
            }
        });
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        String name = event.getWorld().getName();
        for (VillageTerritory territory : List.copyOf(known.values())) {
            if (!territory.chunks().isEmpty() && territory.chunks().getFirst().world().equals(name)) {
                apply(territory);
            }
        }
    }

    private void restoreMissing() {
        for (VillageTerritory territory : List.copyOf(known.values())) {
            if (territory.chunks().isEmpty()) {
                continue;
            }
            RegionManager manager = manager(territory.chunks().getFirst().world());
            if (manager != null && !manager.hasRegion(PREFIX + territory.villageId())) {
                apply(territory);
            }
        }
    }

    private void apply(VillageTerritory territory) {
        long id = territory.villageId();
        VillageTerritory old = known.put(id, territory);
        String world = territory.chunks().isEmpty() ? null : territory.chunks().getFirst().world();
        if (old != null && !old.chunks().isEmpty() && !old.chunks().getFirst().world().equals(world)) {
            remove(old.chunks().getFirst().world(), id);
        }
        if (world == null) {
            return;
        }
        World bukkitWorld = plugin.getServer().getWorld(world);
        RegionManager manager = manager(world);
        if (bukkitWorld == null || manager == null) {
            return; // Territory on another server.
        }
        List<BlockVector2> chunks = new ArrayList<>();
        for (VillageTerritory.Chunk chunk : territory.chunks()) {
            chunks.add(BlockVector2.at(chunk.x(), chunk.z()));
        }
        String regionId = PREFIX + id;
        ProtectedChunkRegion region = new ProtectedChunkRegion(regionId, true, chunks,
                bukkitWorld.getMinHeight(), bukkitWorld.getMaxHeight() - 1);
        ProtectedRegion previous = manager.getRegion(regionId);
        if (previous != null) {
            region.setFlags(previous.getFlags());
            region.setPriority(previous.getPriority());
        }
        // HUD banner shows the village name (follows renames); only villagers may build.
        region.setFlag(Flags.KOKONUT_HUD, territory.villageName());
        region.setFlag(Flags.BUILD, StateFlag.State.DENY);
        region.setFlag(Flags.BUILD.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
        DefaultDomain owners = new DefaultDomain();
        owners.addPlayer(territory.owner());
        DefaultDomain members = new DefaultDomain();
        for (UUID member : territory.members()) {
            members.addPlayer(member);
        }
        region.setOwners(owners);
        region.setMembers(members);
        ProtectedRegion parent = manager.getRegion(DEFAULT_PARENT);
        if (parent != null) {
            try {
                region.setParent(parent);
            } catch (ProtectedRegion.CircularInheritanceException e) {
                plugin.getLogger().log(Level.WARNING, "마을 영역 기본 부모 설정 실패: " + regionId, e);
            }
        }
        manager.addRegion(region);
    }

    private void remove(String world, long id) {
        RegionManager manager = manager(world);
        if (manager != null) {
            manager.removeRegion(PREFIX + id);
        }
    }

    private static RegionManager manager(String world) {
        World bukkitWorld = org.bukkit.Bukkit.getWorld(world);
        if (bukkitWorld == null) {
            return null;
        }
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(bukkitWorld));
    }
}
