package net.onixary.shapeShifterCurseFabric.mixin.integration;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.LivingEntity;
import net.onixary.shapeShifterCurseFabric.additional_power.InWaterSpeedModifierPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// NeoForge's water branch reads SWIM_SPEED; other fluid types bypass this read.
@Mixin(targets = "net.neoforged.neoforge.common.extensions.ILivingEntityExtension", remap = false)
public interface NeoForgeVerticalWaterMovementMixin {
    @ModifyExpressionValue(method = {"jumpInFluid", "sinkInFluid"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getAttributeValue(Lnet/minecraft/core/Holder;)D", remap = true))
    private double ssc$scaleWaterInput(double swimSpeed) {
        return swimSpeed * InWaterSpeedModifierPower.verticalWaterMultiplier((LivingEntity)(Object)this);
    }
}
