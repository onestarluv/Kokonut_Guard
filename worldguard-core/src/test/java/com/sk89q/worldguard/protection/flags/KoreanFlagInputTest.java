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

package com.sk89q.worldguard.protection.flags;

import org.junit.jupiter.api.Test;
import com.sk89q.worldedit.extension.platform.Actor;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KoreanFlagInputTest {
    private FlagContext input(String value) throws ReflectiveOperationException {
        // Parsing itself does not require the Bukkit platform notification in the builder.
        var constructor = FlagContext.class.getDeclaredConstructor(Actor.class, String.class, Map.class);
        constructor.setAccessible(true);
        return constructor.newInstance(null, value, Map.of());
    }

    @Test
    void stateInputKeepsEnglishCompatibility() throws Exception {
        StateFlag flag = new StateFlag("test-state", false);
        assertEquals(flag.parseInput(input("allow")), flag.parseInput(input("허용")));
        assertEquals(flag.parseInput(input("deny")), flag.parseInput(input("거부")));
        assertEquals(StateFlag.State.DENY, flag.parseInput(input("차단")));
        assertNull(flag.parseInput(input("해제")));
        assertNull(flag.parseInput(input("미설정")));
        assertThrows(InvalidFlagFormat.class, () -> flag.parseInput(input("잘못된 값")));
        assertEquals("allow", flag.marshal(StateFlag.State.ALLOW));
        assertEquals("deny", flag.marshal(StateFlag.State.DENY));
    }

    @Test
    void booleanInputKeepsEnglishCompatibility() throws Exception {
        BooleanFlag flag = new BooleanFlag("test-boolean");
        assertEquals(flag.parseInput(input("true")), flag.parseInput(input("켜기")));
        assertEquals(flag.parseInput(input("false")), flag.parseInput(input("끄기")));
        assertEquals(Boolean.TRUE, flag.parseInput(input("켜짐")));
        assertEquals(Boolean.FALSE, flag.parseInput(input("꺼짐")));
        assertThrows(InvalidFlagFormat.class, () -> flag.parseInput(input("잘못된 값")));
    }
}
