package com.overpoweredmobs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public final class FenceZoneManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path ZONE_PATH = FabricLoader.getInstance().getConfigDir().resolve("overpoweredmobs_zones.json");
    private static final int MAX_FENCE_SCAN = 500;
    private static final int EVICTION_SEARCH_RADIUS = 15;

    private static List<FenceZone> zones = new ArrayList<>();
    private static boolean scanning = false;

    private FenceZoneManager() {}

    public static void load() {
        if (ZONE_PATH.toFile().exists()) {
            try (FileReader reader = new FileReader(ZONE_PATH.toFile())) {
                Type type = new TypeToken<List<FenceZone>>(){}.getType();
                List<FenceZone> loaded = GSON.fromJson(reader, type);
                if (loaded != null) {
                    zones = loaded;
                    OverpoweredMobsLogger.info("Loaded " + zones.size() + " fence zones");
                    return;
                }
            } catch (IOException e) {
                OverpoweredMobs.LOGGER.error("Failed to load fence zones", e);
            }
        }
        zones = new ArrayList<>();
        save();
    }

    private static void save() {
        try (FileWriter writer = new FileWriter(ZONE_PATH.toFile())) {
            GSON.toJson(zones, writer);
        } catch (IOException e) {
            OverpoweredMobs.LOGGER.error("Failed to save fence zones", e);
        }
    }

    public static void onFencePlaced(Level level, BlockPos pos) {
        if (level.isClientSide() || scanning) return;
        scanning = true;
        try {
            Set<BlockPos> fenceGroup = findFenceGroup(level, pos);
            if (fenceGroup.size() < 4) return;

            FenceZone zone = detectEnclosure(level, fenceGroup);
            if (zone != null) {
                zones.removeIf(z -> z.dimension.equals(zone.dimension) && z.overlaps(zone));
                zones.add(zone);
                save();
                OverpoweredMobsLogger.info("Detected fence zone at " + zone);
            }
        } finally {
            scanning = false;
        }
    }

    public static void onFenceBroken(Level level, BlockPos pos, BlockState brokenState) {
        if (level.isClientSide()) return;
        if (!isFenceLike(brokenState)) return;

        Set<BlockPos> fenceGroup = findFenceGroup(level, pos);
        if (fenceGroup.size() >= 4) {
            FenceZone zone = detectEnclosure(level, fenceGroup);
            if (zone != null) {
                zones.removeIf(z -> z.dimension.equals(zone.dimension) && z.overlaps(zone));
                zones.add(zone);
                save();
                OverpoweredMobsLogger.info("Re-checked fence zone after break, still valid: " + zone);
                return;
            }
        }
        zones.removeIf(z -> z.containsCoord(pos));
        save();
        OverpoweredMobsLogger.info("Removed fence zone containing " + pos + " (breached)");
    }

    public static boolean isInAnyZone(ServerLevel level, BlockPos pos) {
        String dim = level.dimension().identifier().toString();
        for (FenceZone zone : zones) {
            if (zone.dimension.equals(dim) && zone.containsCoord(pos)) {
                return true;
            }
        }
        return false;
    }

    public static void evictFromZones(Mob mob) {
        if (!(mob.level() instanceof ServerLevel serverLevel)) return;
        BlockPos pos = mob.blockPosition();

        FenceZone targetZone = null;
        String dim = serverLevel.dimension().identifier().toString();
        for (FenceZone zone : zones) {
            if (zone.dimension.equals(dim) && zone.containsCoord(pos)) {
                targetZone = zone;
                break;
            }
        }
        if (targetZone == null) return;

        BlockPos target = findNearestOutside(serverLevel, pos);
        if (target != null) {
            mob.teleportTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
            OverpoweredMobsLogger.info("Evicted " + mob.getType() + " from fence zone " + targetZone + " to " + target);
        }
    }

    private static BlockPos findNearestOutside(ServerLevel level, BlockPos origin) {
        String dim = level.dimension().identifier().toString();
        for (int r = 1; r <= EVICTION_SEARCH_RADIUS; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) continue;
                    BlockPos check = origin.offset(dx, 0, dz);
                    boolean outside = true;
                    for (FenceZone zone : zones) {
                        if (zone.dimension.equals(dim) && zone.containsCoord(check)) {
                            outside = false;
                            break;
                        }
                    }
                    if (outside && level.getBlockState(check).isAir() && level.getBlockState(check.above()).isAir()) {
                        return check;
                    }
                }
            }
        }
        return null;
    }

    private static Set<BlockPos> findFenceGroup(Level level, BlockPos start) {
        Set<BlockPos> group = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        group.add(start);
        while (!queue.isEmpty() && group.size() < MAX_FENCE_SCAN) {
            BlockPos p = queue.poll();
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos n = p.relative(dir);
                if (!group.contains(n) && isFenceLike(level.getBlockState(n))) {
                    group.add(n);
                    queue.add(n);
                }
            }
        }
        return group;
    }

    private static boolean isFenceLike(BlockState state) {
        if (state.getBlock() instanceof FenceBlock) return true;
        if (state.getBlock() instanceof WallBlock) return true;
        if (state.getBlock() instanceof FenceGateBlock) {
            return !state.getValue(FenceGateBlock.OPEN);
        }
        return false;
    }

    private static FenceZone detectEnclosure(Level level, Set<BlockPos> fenceGroup) {
        int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        int fenceY = fenceGroup.iterator().next().getY();
        for (BlockPos p : fenceGroup) {
            if (p.getX() < minX) minX = p.getX();
            if (p.getZ() < minZ) minZ = p.getZ();
            if (p.getX() > maxX) maxX = p.getX();
            if (p.getZ() > maxZ) maxZ = p.getZ();
        }

        int scanY = fenceY + 1;
        int extMinX = minX - 1, extMinZ = minZ - 1;
        int extMaxX = maxX + 1, extMaxZ = maxZ + 1;

        Set<BlockPos> reachable = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();

        for (int x = extMinX; x <= extMaxX; x++) {
            for (int z = extMinZ; z <= extMaxZ; z++) {
                if (x == extMinX || x == extMaxX || z == extMinZ || z == extMaxZ) {
                    BlockPos edge = new BlockPos(x, scanY, z);
                    if (level.getBlockState(edge).isAir()) {
                        reachable.add(edge);
                        queue.add(edge);
                    }
                }
            }
        }

        while (!queue.isEmpty()) {
            BlockPos p = queue.poll();
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos n = p.relative(dir);
                if (n.getX() < extMinX || n.getX() > extMaxX || n.getZ() < extMinZ || n.getZ() > extMaxZ) continue;
                if (!reachable.contains(n) && level.getBlockState(n).isAir()) {
                    reachable.add(n);
                    queue.add(n);
                }
            }
        }

        Set<BlockPos> interior = new HashSet<>();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                BlockPos p = new BlockPos(x, scanY, z);
                if (level.getBlockState(p).isAir() && !reachable.contains(p)) {
                    interior.add(p);
                }
            }
        }

        if (interior.isEmpty()) return null;

        int iMinX = Integer.MAX_VALUE, iMinY = scanY, iMinZ = Integer.MAX_VALUE;
        int iMaxX = Integer.MIN_VALUE, iMaxY = scanY, iMaxZ = Integer.MIN_VALUE;
        for (BlockPos p : interior) {
            if (p.getX() < iMinX) iMinX = p.getX();
            if (p.getZ() < iMinZ) iMinZ = p.getZ();
            if (p.getX() > iMaxX) iMaxX = p.getX();
            if (p.getZ() > iMaxZ) iMaxZ = p.getZ();
        }

        return new FenceZone(level.dimension().identifier().toString(), iMinX, iMinY, iMinZ, iMaxX, iMaxY, iMaxZ);
    }

    public static class FenceZone {
        private String dimension;
        private int minX, minY, minZ, maxX, maxY, maxZ;

        public FenceZone() {}

        public FenceZone(String dimension, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            this.dimension = dimension;
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }

        public boolean containsCoord(BlockPos pos) {
            return pos.getX() >= minX && pos.getX() <= maxX
                && pos.getY() >= minY && pos.getY() <= maxY
                && pos.getZ() >= minZ && pos.getZ() <= maxZ;
        }

        public boolean overlaps(FenceZone other) {
            if (!dimension.equals(other.dimension)) return false;
            return minX <= other.maxX && maxX >= other.minX
                && minY <= other.maxY && maxY >= other.minY
                && minZ <= other.maxZ && maxZ >= other.minZ;
        }

        @Override
        public String toString() {
            return dimension + " [" + minX + "," + minY + "," + minZ + "] -> [" + maxX + "," + maxY + "," + maxZ + "]";
        }
    }
}
