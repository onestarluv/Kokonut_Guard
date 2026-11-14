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

package com.sk89q.worldguard.protection.regions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ProtectedChunkRegionTest {

    /** An L of three chunks around the origin, one of them negative. */
    private final ProtectedChunkRegion region = new ProtectedChunkRegion("village-1", true,
            List.of(BlockVector2.at(0, 0), BlockVector2.at(-1, 0), BlockVector2.at(0, 1), BlockVector2.at(0, 1)), -64, 319);

    @Test
    public void containsOnlyItsChunks() {
        assertTrue(region.contains(BlockVector3.at(0, 64, 0)));
        assertTrue(region.contains(BlockVector3.at(15, -64, 15)));
        assertTrue(region.contains(BlockVector3.at(-1, 319, 0)), "chunk -1 starts at block -16");
        assertTrue(region.contains(BlockVector3.at(-16, 70, 15)));
        assertTrue(region.contains(BlockVector3.at(3, 70, 31)));
        assertFalse(region.contains(BlockVector3.at(-17, 70, 0)));
        assertFalse(region.contains(BlockVector3.at(-1, 70, 16)), "the missing corner of the L");
        assertFalse(region.contains(BlockVector3.at(0, 320, 0)), "above the top");
        assertEquals(3, region.getChunks().size(), "duplicates dropped");
        assertEquals(3 * 256 * 384, region.volume());
        assertEquals(BlockVector3.at(-16, -64, 0), region.getMinimumPoint());
        assertEquals(BlockVector3.at(15, 319, 31), region.getMaximumPoint());
    }

    @Test
    public void intersectsByShapeNotBoundingBox() {
        ProtectedCuboidRegion inGap = new ProtectedCuboidRegion("gap", BlockVector3.at(-10, 0, 20), BlockVector3.at(-5, 100, 25));
        ProtectedCuboidRegion inside = new ProtectedCuboidRegion("in", BlockVector3.at(1, 0, 20), BlockVector3.at(5, 100, 25));
        List<ProtectedRegion> hits = region.getIntersectingRegions(List.of(inGap, inside));
        assertEquals(List.of(inside), hits);
    }
}
