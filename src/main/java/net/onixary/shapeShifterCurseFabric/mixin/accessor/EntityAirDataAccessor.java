package net.onixary.shapeShifterCurseFabric.mixin.accessor;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAirDataAccessor {
    @Accessor("DATA_AIR_SUPPLY_ID")
    static EntityDataAccessor<Integer> ssc$getAirData() {
        throw new AssertionError();
    }
}
