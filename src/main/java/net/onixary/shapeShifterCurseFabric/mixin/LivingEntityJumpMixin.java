package net.onixary.shapeShifterCurseFabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.world.entity.LivingEntity;
import net.onixary.shapeShifterCurseFabric.additional_power.TripleJumpPower;
import net.onixary.shapeShifterCurseFabric.util.Interface.IJumpController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityJumpMixin implements IJumpController {

    @WrapMethod(method = "jumpFromGround")
    private void onJump(Operation<Void> original) {
        LivingEntity entity = (LivingEntity) (Object) this;
        PowerHolderComponent.getPowers(entity, TripleJumpPower.class).forEach(TripleJumpPower::onJump);
        original.call();
    }

    // 1.21 adds a float overload; modify the wrapper once, as in 1.20.1.
    @ModifyReturnValue(method = "getJumpPower()F", at = @At("RETURN"))
    private float modifyJumpVelocity(float originalVelocity) {
        LivingEntity entity = (LivingEntity) (Object) this;

        return PowerHolderComponent.getPowers(entity, TripleJumpPower.class).stream()
                .findFirst()
                .map(power -> {
                    float powerMultiplier = power.getActiveJumpMultiplier();

                    if (powerMultiplier != 1.0f) {
                        float baseJumpVelocity = 0.42F;
                        float additionalVelocity = originalVelocity - baseJumpVelocity;
                        return (baseJumpVelocity * powerMultiplier) + additionalVelocity;
                    }
                    return originalVelocity;
                })
                .orElse(originalVelocity);
    }

    @Inject(method = "getJumpPower()F", at = @At("HEAD"), cancellable = true)
    private void onGetJumpVelocity(CallbackInfoReturnable<Float> cir) {
        if (this.noJumpTick > 0) {
            cir.setReturnValue(0.0F);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (this.noJumpTick > 0) {
            this.noJumpTick--;
        }
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.level().isClientSide && entity instanceof net.minecraft.world.entity.player.Player player
                && player.isLocalPlayer()) {
            // Apoli only ticks powers on the server. The predicted jump counter must reset locally too.
            PowerHolderComponent.getPowers(entity, TripleJumpPower.class).forEach(TripleJumpPower::tick);
        }
    }

    @Unique
    public int noJumpTick = 0;

    @Override
    public void shape_shifter_curse$setNoJumpTick(int tick) {
        this.noJumpTick = tick;
    }

    @Override
    public int shape_shifter_curse$getNoJumpTick() {
        return this.noJumpTick;
    }
}
