package io.github.apace100.apoli.mixin.integration.connector;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifySlipperinessPower;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityFrictionMixin {

    // NeoForge 21.1 obtains friction from the state with world/entity context.
    // Only the invocation is unmapped: this method is injected into mapped travel.
    @ModifyExpressionValue(method = "travel", at = @At(value = "INVOKE", remap = false,
        target = "Lnet/minecraft/world/level/block/state/BlockState;getFriction(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)F"))
    private float apoli$modifySlipperiness(float original) {
        LivingEntity entity = (LivingEntity)(Object)this;
        return PowerHolderComponent.modify(entity, ModifySlipperinessPower.class, original,
            power -> power.doesApply(entity.level(), entity.getBlockPosBelowThatAffectsMyMovement()));
    }
}
