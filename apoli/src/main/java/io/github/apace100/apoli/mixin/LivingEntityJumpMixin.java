package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyJumpPower;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

// Apply after the mod's triple-jump and ordinary jump-attribute calculations.
@Mixin(value = LivingEntity.class, priority = 900)
public abstract class LivingEntityJumpMixin {

    @Unique
    private boolean apoli$jumpInProgress;

    @WrapMethod(method = "jumpFromGround")
    private void apoli$withJumpContext(Operation<Void> original) {
        boolean previous = apoli$jumpInProgress;
        apoli$jumpInProgress = true;
        try {
            original.call();
        } finally {
            apoli$jumpInProgress = previous;
        }
    }

    @ModifyReturnValue(method = "getJumpPower()F", at = @At("RETURN"))
    private float apoli$modifyJumpPower(float original) {
        // Sable's oriented jump also calls this getter, but skips Minecraft's
        // normal jump body. Modifying its result covers both execution paths.
        return PowerHolderComponent.modify((LivingEntity)(Object)this, ModifyJumpPower.class, original, power -> {
            if (apoli$jumpInProgress) {
                power.executeAction();
            }
            return true;
        });
    }
}
