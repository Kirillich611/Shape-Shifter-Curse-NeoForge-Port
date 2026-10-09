package io.github.apace100.apoli.util;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;

public final class FluidTagUtil {

    private FluidTagUtil() {
    }

    public static boolean isSubmerged(Entity entity, TagKey<Fluid> tag) {
        if (tag == null) {
            return false;
        }
        // NeoForge bridges these canonical tags to its FluidType state.
        if (tag.equals(FluidTags.WATER)) {
            return entity.isEyeInFluid(FluidTags.WATER);
        }
        if (tag.equals(FluidTags.LAVA)) {
            return entity.isEyeInFluid(FluidTags.LAVA);
        }

        double eyeY = entity.getEyeY();
        if (entity.getVehicle() instanceof Boat boat && !boat.isUnderWater()
                && boat.getBoundingBox().minY <= eyeY && boat.getBoundingBox().maxY >= eyeY) {
            return false;
        }
        BlockPos eyePos = BlockPos.containing(entity.getX(), eyeY, entity.getZ());
        FluidState fluid = entity.level().getFluidState(eyePos);
        return fluid.is(tag) && eyePos.getY() + fluid.getHeight(entity.level(), eyePos) > eyeY;
    }

    public static double getFluidHeight(Entity entity, TagKey<Fluid> tag) {
        if (tag == null) {
            return 0.0;
        }
        if (tag.equals(FluidTags.WATER)) {
            return entity.getFluidHeight(FluidTags.WATER);
        }
        if (tag.equals(FluidTags.LAVA)) {
            return entity.getFluidHeight(FluidTags.LAVA);
        }

        // Custom tags cannot be mapped to a single NeoForge FluidType. Measure
        // the matching states without applying fluid pushing or changing caches.
        Level level = entity.level();
        AABB box = entity.getBoundingBox().deflate(0.001);
        int minX = Mth.floor(box.minX);
        int minY = Mth.floor(box.minY);
        int minZ = Mth.floor(box.minZ);
        int maxX = Mth.ceil(box.maxX);
        int maxY = Mth.ceil(box.maxY);
        int maxZ = Mth.ceil(box.maxZ);
        if (!level.hasChunksAt(minX, minY, minZ, maxX - 1, maxY - 1, maxZ - 1)) {
            return 0.0;
        }

        double height = 0.0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    pos.set(x, y, z);
                    FluidState fluid = level.getFluidState(pos);
                    if (fluid.is(tag)) {
                        height = Math.max(height, y + fluid.getHeight(level, pos) - box.minY);
                    }
                }
            }
        }
        return height;
    }
}
