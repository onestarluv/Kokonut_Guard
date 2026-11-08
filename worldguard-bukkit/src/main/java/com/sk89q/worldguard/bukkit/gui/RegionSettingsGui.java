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

package com.sk89q.worldguard.bukkit.gui;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.permission.RegionPermissionModel;
import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.EnumFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Region editor. Every write goes through WorldGuard's command permission and value validation. */
public final class RegionSettingsGui implements Listener {
    private static final int PAGE_SIZE = 45;
    private final WorldGuardPlugin plugin;
    private final Map<UUID, PendingInput> pending = new ConcurrentHashMap<>();

    public RegionSettingsGui(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, String regionId) {
        pending.remove(player.getUniqueId());
        show(player, player.getWorld().getUID(), regionId, 0);
    }

    private RegionManager manager(UUID worldId) {
        World world = Bukkit.getWorld(worldId);
        return world == null ? null : WorldGuard.getInstance().getPlatform().getRegionContainer()
                .get(BukkitAdapter.adapt(world));
    }

    private RegionPermissionModel permissions(Player player) {
        return new RegionPermissionModel(plugin.wrapPlayer(player));
    }

    private void show(Player player, UUID worldId, String regionId, int requestedPage) {
        RegionManager manager = manager(worldId);
        if (manager == null) {
            player.sendMessage("§c이 월드에서는 영역 보호를 사용할 수 없습니다.");
            return;
        }
        Menu menu = new Menu(worldId, regionId);
        if (regionId == null) {
            manager.getRegions().values().stream().filter(region -> permissions(player).maySetFlag(region))
                    .sorted(Comparator.comparing(ProtectedRegion::getId)).forEach(menu.entries::add);
        } else {
            ProtectedRegion region = manager.getRegion(regionId);
            if (region == null || !permissions(player).maySetFlag(region)) {
                player.sendMessage("§c영역이 없거나 이 영역을 설정할 권한이 없습니다.");
                return;
            }
            WorldGuard.getInstance().getFlagRegistry().getAll().stream()
                    .sorted(Comparator.comparing(Flag::getName)).forEach(menu.entries::add);
        }
        int pages = Math.max(1, (menu.entries.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        menu.page = Math.max(0, Math.min(requestedPage, pages - 1));
        menu.inventory = Bukkit.createInventory(menu, 54, regionId == null ? "영역 선택" : "영역 설정: " + regionId);
        for (int slot = 0; slot < PAGE_SIZE; slot++) {
            int index = menu.page * PAGE_SIZE + slot;
            if (index >= menu.entries.size()) {
                break;
            }
            Object entry = menu.entries.get(index);
            if (entry instanceof ProtectedRegion region) {
                menu.inventory.setItem(slot, item(Material.MAP, "§e" + region.getId(), "§7클릭하여 설정 열기"));
            } else if (entry instanceof Flag<?> flag) {
                Object value = manager.getRegion(regionId).getFlag(flag);
                Material dye = value == null ? Material.GRAY_DYE
                        : value == StateFlag.State.DENY || Boolean.FALSE.equals(value)
                        ? Material.RED_DYE : Material.LIME_DYE;
                menu.inventory.setItem(slot, item(dye,
                        "§e" + KoreanFlagNames.name(flag.getName()), "§8" + flag.getName(),
                        "§f직접 설정값: " + display(value), "§7미설정이면 상속·기본값·영역 규칙을 따릅니다.",
                        "§a좌클릭: 값 변경 / 입력", "§c우클릭: 직접 설정 해제"));
            }
        }
        if (menu.page > 0) {
            menu.inventory.setItem(45, item(Material.ARROW, "§e이전 페이지"));
        }
        menu.inventory.setItem(48, item(Material.COMPASS, "§e영역 목록"));
        menu.inventory.setItem(49, item(Material.PAPER, "§f" + (menu.page + 1) + " / " + pages,
                "§7변경 내용은 기존 영역 자동 저장에 반영됩니다."));
        menu.inventory.setItem(50, item(Material.BARRIER, "§c닫기"));
        if (menu.page + 1 < pages) {
            menu.inventory.setItem(53, item(Material.ARROW, "§e다음 페이지"));
        }
        player.openInventory(menu.inventory);
    }

    private static String display(Object value) {
        if (value == null) {
            return "미설정";
        }
        return switch (value.toString()) {
            case "ALLOW" -> "허용";
            case "DENY" -> "거부";
            case "true" -> "켜짐";
            case "false" -> "꺼짐";
            default -> value.toString();
        };
    }

    private static ItemStack item(Material material, String title, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(title);
        meta.setLore(List.of(lore));
        item.setItemMeta(meta);
        return item;
    }

    private void later(Player player, Runnable action) {
        player.getScheduler().run(plugin, task -> {
            if (player.isOnline()) {
                action.run();
            }
        }, null);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof Menu menu)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getRawSlot() < 0) {
            return;
        }
        int slot = event.getRawSlot();
        if (slot == 50) {
            later(player, player::closeInventory);
        } else if (slot == 48) {
            later(player, () -> show(player, menu.worldId, null, 0));
        } else if ((slot == 45 || slot == 53) && menu.inventory.getItem(slot) != null) {
            later(player, () -> show(player, menu.worldId, menu.regionId, menu.page + (slot == 45 ? -1 : 1)));
        } else if (slot < PAGE_SIZE && slot + menu.page * PAGE_SIZE < menu.entries.size()) {
            Object entry = menu.entries.get(slot + menu.page * PAGE_SIZE);
            if (entry instanceof ProtectedRegion region) {
                later(player, () -> show(player, menu.worldId, region.getId(), 0));
            } else if (entry instanceof Flag<?> flag) {
                later(player, () -> edit(player, menu, flag, event.isRightClick()));
            }
        }
    }

    private void edit(Player player, Menu menu, Flag<?> flag, boolean clear) {
        RegionManager manager = manager(menu.worldId);
        ProtectedRegion region = manager == null ? null : manager.getRegion(menu.regionId);
        if (region == null || !permissions(player).maySetFlag(region)) {
            player.closeInventory();
            player.sendMessage("§c영역이 없거나 설정 권한이 없습니다.");
            return;
        }
        Object current = region.getFlag(flag);
        String value;
        if (clear) {
            value = null;
        } else if (flag instanceof StateFlag) {
            value = current == StateFlag.State.ALLOW ? "deny" : "allow";
        } else if (flag instanceof BooleanFlag) {
            value = Boolean.TRUE.equals(current) ? "false" : "true";
        } else if (flag instanceof EnumFlag<?> enumFlag) {
            Object[] values = enumFlag.getEnumClass().getEnumConstants();
            int next = current instanceof Enum<?> e ? (e.ordinal() + 1) % values.length : 0;
            value = values[next].toString();
        } else {
            pending.put(player.getUniqueId(), new PendingInput(menu, flag, System.currentTimeMillis() + 120_000));
            player.closeInventory();
            player.sendMessage("§e" + KoreanFlagNames.name(flag.getName()) + " 값을 채팅에 입력하세요. (2분 이내)");
            player.sendMessage("§7취소하려면 '취소'를 입력하세요. 위치 값은 'here', 목록은 쉼표로 구분합니다.");
            return;
        }
        apply(player, menu, flag, value);
    }

    private void apply(Player player, Menu menu, Flag<?> flag, String value) {
        World world = Bukkit.getWorld(menu.worldId);
        if (world == null) {
            player.sendMessage("§c대상 월드를 찾을 수 없습니다.");
            return;
        }
        List<String> args = new ArrayList<>(List.of("flag", "-w", world.getName(), menu.regionId, flag.getName()));
        if (value != null) {
            args.add(value);
        }
        plugin.onCommand(player, plugin.getCommand("영역"), "영역", args.toArray(String[]::new));
        show(player, menu.worldId, menu.regionId, menu.page);
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        PendingInput input = pending.remove(event.getPlayer().getUniqueId());
        if (input == null) {
            return;
        }
        event.setCancelled(true);
        String message = event.getMessage();
        later(event.getPlayer(), () -> {
            if (System.currentTimeMillis() > input.expiresAt || message.equals("취소")) {
                event.getPlayer().sendMessage("§7설정 입력이 취소되었습니다.");
                show(event.getPlayer(), input.menu.worldId, input.menu.regionId, input.menu.page);
            } else {
                apply(event.getPlayer(), input.menu, input.flag, message);
            }
        });
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof Menu) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        pending.remove(event.getPlayer().getUniqueId());
    }

    private record PendingInput(Menu menu, Flag<?> flag, long expiresAt) { }

    private static final class Menu implements InventoryHolder {
        private final UUID worldId;
        private final String regionId;
        private final List<Object> entries = new ArrayList<>();
        private int page;
        private Inventory inventory;

        private Menu(UUID worldId, String regionId) {
            this.worldId = worldId;
            this.regionId = regionId;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }
}
