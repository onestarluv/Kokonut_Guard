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

package com.sk89q.worldguard.bukkit.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.BukkitWorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.event.DelegateEvent;
import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.PlaceBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.UseBlockEvent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.SignChangeEvent;

/**
 * Handle events that need to be processed by the chest protection.
 */
public class ChestProtectionListener extends AbstractListener {

    /**
     * Construct the listener.
     *
     * @param plugin an instance of WorldGuardPlugin
     */
    public ChestProtectionListener(WorldGuardPlugin plugin) {
        super(plugin);
    }

    private void sendMessage(DelegateEvent event, Player player, String message) {
        if (!event.isSilent()) {
            player.sendMessage(message);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlaceBlock(final PlaceBlockEvent event) {
        final Player player = event.getCause().getFirstPlayer();

        if (player != null) {
            final BukkitWorldConfiguration wcfg = getWorldConfig(event.getWorld());

            // Early guard
            if (!wcfg.signChestProtection) {
                return;
            }

            event.filter(target -> {
                if (wcfg.getChestProtection().isChest(BukkitAdapter.asBlockType(event.getEffectiveMaterial())) && wcfg.isChestProtected(BukkitAdapter.adapt(target.getBlock().getLocation()),
                        WorldGuardPlugin.inst().wrapPlayer(player))) {
                    sendMessage(event, player, ChatColor.DARK_RED + "이 위치는 접근 권한이 없는 상자의 자리입니다.");
                    return false;
                }

                return true;
            }, true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreakBlock(final BreakBlockEvent event) {
        final Player player = event.getCause().getFirstPlayer();

        final BukkitWorldConfiguration wcfg = getWorldConfig(event.getWorld());

        // Early guard
        if (!wcfg.signChestProtection) {
            return;
        }

        if (player != null) {
            final LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
            event.filter(target -> {
                if (wcfg.isChestProtected(BukkitAdapter.adapt(target.getBlock().getLocation()), localPlayer)) {
                    sendMessage(event, player, ChatColor.DARK_RED + "보호된 상자입니다.");
                    return false;
                }

                return true;
            }, true);
        } else {
            event.filter(target -> !wcfg.isChestProtected(BukkitAdapter.adapt(target.getBlock().getLocation())));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onUseBlock(final UseBlockEvent event) {
        final Player player = event.getCause().getFirstPlayer();

        final BukkitWorldConfiguration wcfg = getWorldConfig(event.getWorld());

        // Early guard
        if (!wcfg.signChestProtection) {
            return;
        }

        if (player != null) {
            final LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
            event.filter(target -> {
                if (wcfg.isChestProtected(BukkitAdapter.adapt(target.getBlock().getLocation()), localPlayer)) {
                    sendMessage(event, player, ChatColor.DARK_RED + "보호된 상자입니다.");
                    return false;
                }

                return true;
            }, true);
        } else {
            event.filter(target -> !wcfg.isChestProtected(BukkitAdapter.adapt(target.getBlock().getLocation())));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        final BukkitWorldConfiguration wcfg = getWorldConfig(event.getBlock().getWorld());

        if (wcfg.signChestProtection) {
            if ("[Lock]".equalsIgnoreCase(event.getLine(0))) {
                if (wcfg.isChestProtectedPlacement(BukkitAdapter.adapt(event.getBlock().getLocation()), WorldGuardPlugin.inst().wrapPlayer(player))) {
                    player.sendMessage(ChatColor.DARK_RED + "옆 상자의 소유자가 아닙니다.");
                    event.getBlock().breakNaturally();
                    event.setCancelled(true);
                    return;
                }

                if (!Tag.STANDING_SIGNS.isTagged(event.getBlock().getType())) {
                    player.sendMessage(ChatColor.RED
                            + "[Lock] 표지판은 벽이 아닌 바닥에 설치해야 합니다.");

                    event.getBlock().breakNaturally();
                    event.setCancelled(true);
                    return;
                }

                if (!player.getName().equalsIgnoreCase(event.getLine(1))) {
                    player.sendMessage(ChatColor.RED
                            + "첫 번째 소유자 줄에는 자신의 이름을 적어야 합니다.");

                    event.getBlock().breakNaturally();
                    event.setCancelled(true);
                    return;
                }

                Material below = event.getBlock().getRelative(0, -1, 0).getType();

                if (below == Material.TNT || below == Material.SAND
                        || below == Material.GRAVEL || Tag.STANDING_SIGNS.isTagged(below)) {
                    player.sendMessage(ChatColor.RED
                            + "이 블록에는 보호 표지판을 안전하게 설치할 수 없습니다.");

                    event.getBlock().breakNaturally();
                    event.setCancelled(true);
                    return;
                }

                event.setLine(0, "[Lock]");
                player.sendMessage(ChatColor.YELLOW
                        + "위에 있는 상자 또는 큰 상자를 보호했습니다.");
            }
        } else if (!wcfg.disableSignChestProtectionCheck) {
            if ("[Lock]".equalsIgnoreCase(event.getLine(0))) {
                player.sendMessage(ChatColor.RED
                        + "WorldGuard의 표지판 상자 보호 기능이 비활성화되어 있습니다.");

                event.getBlock().breakNaturally();
                event.setCancelled(true);
            }
        }
    }

}
