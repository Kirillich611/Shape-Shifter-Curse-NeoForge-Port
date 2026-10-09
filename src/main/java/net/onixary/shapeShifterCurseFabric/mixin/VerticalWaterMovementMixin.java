package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.onixary.shapeShifterCurseFabric.additional_power.InWaterSpeedModifierPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public class VerticalWaterMovementMixin {
    @ModifyArg(method = {"jumpInLiquid", "goDownInWater"}, require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"), index = 1)
    private double ssc$scaleVerticalWaterInput(double impulse) {
        LivingEntity entity = (LivingEntity)(Object)this;
        return entity.isInWater() && entity.getFluidHeight(FluidTags.WATER) > 0
                ? impulse * InWaterSpeedModifierPower.verticalWaterMultiplier(entity) : impulse;
    }
}
