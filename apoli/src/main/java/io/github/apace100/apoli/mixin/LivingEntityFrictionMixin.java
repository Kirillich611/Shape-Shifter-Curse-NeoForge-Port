package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifySlipperinessPower;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityFrictionMixin {

    @ModifyExpressionValue(method = "travel", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/Block;getFriction()F"))
    private float apoli$modifySlipperiness(float original) {
        LivingEntity entity = (LivingEntity)(Object)this;
        return PowerHolderComponent.modify(entity, ModifySlipperinessPower.class, original,
            power -> power.doesApply(entity.level(), entity.getBlockPosBelowThatAffectsMyMovement()));
    }
}
