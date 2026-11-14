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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 에고버스: a region made of whole chunks (any shape, holes allowed) over a height range. Kokonut-Core villages use
 * one such region per village. Chunk coordinates are block coordinates {@code >> 4}.
 */
public class ProtectedChunkRegion extends ProtectedRegion {

    private final List<BlockVector2> chunks;
    private final Set<Long> packed = new HashSet<>();

    /**
     * Construct a new instance.
     *
     * @param id the region id
     * @param transientRegion whether this region should only be kept in memory and not be saved
     * @param chunks chunk coordinates (at least one)
     * @param minY the lowest block y
     * @param maxY the highest block y
     */
    public ProtectedChunkRegion(String id, boolean transientRegion, Collection<BlockVector2> chunks, int minY, int maxY) {
        super(id, transientRegion);
        checkNotNull(chunks);
        checkArgument(!chunks.isEmpty(), "a chunk region needs at least one chunk");
        List<BlockVector2> list = new ArrayList<>();
        int minX = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (BlockVector2 chunk : chunks) {
            if (!packed.add(pack(chunk.x(), chunk.z()))) {
                continue;
            }
            list.add(chunk);
            minX = Math.min(minX, chunk.x());
            minZ = Math.min(minZ, chunk.z());
            maxX = Math.max(maxX, chunk.x());
            maxZ = Math.max(maxZ, chunk.z());
        }
        this.chunks = Collections.unmodifiableList(list);
        List<BlockVector3> bounds = new ArrayList<>();
        bounds.add(BlockVector3.at(minX << 4, Math.min(minY, maxY), minZ << 4));
        bounds.add(BlockVector3.at((maxX << 4) + 15, Math.max(minY, maxY), (maxZ << 4) + 15));
        setMinMaxPoints(bounds);
    }

    private static long pack(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    /**
     * Get the chunk coordinates of this region.
     *
     * @return an unmodifiable list of chunk coordinates
     */
    public List<BlockVector2> getChunks() {
        return chunks;
    }

    @Override
    public boolean isPhysicalArea() {
        return true;
    }

    /**
     * The corners of the bounding box; the exact shape is {@link #getChunks()}.
     */
    @Override
    public List<BlockVector2> getPoints() {
        List<BlockVector2> pts = new ArrayList<>();
        pts.add(BlockVector2.at(min.x(), min.z()));
        pts.add(BlockVector2.at(max.x(), min.z()));
        pts.add(BlockVector2.at(max.x(), max.z()));
        pts.add(BlockVector2.at(min.x(), max.z()));
        return pts;
    }

    @Override
    public boolean contains(BlockVector3 pt) {
        int y = pt.y();
        return y >= min.y() && y <= max.y() && packed.contains(pack(pt.x() >> 4, pt.z() >> 4));
    }

    @Override
    public RegionType getType() {
        return RegionType.CHUNKS;
    }

    @Override
    Area toArea() {
        Area area = new Area();
        for (BlockVector2 chunk : chunks) {
            area.add(new Area(new Rectangle(chunk.x() << 4, chunk.z() << 4, 16, 16)));
        }
        return area;
    }

    @Override
    public int volume() {
        long v = (long) chunks.size() * 256L * (max.y() - min.y() + 1L);
        return v > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) v;
    }

}
