package net.onixary.shapeShifterCurseFabric.mixin.integration;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.util.CriticalAttackUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class NeoForgePlayerCriticalMixin {
    @ModifyExpressionValue(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/neoforged/neoforge/event/entity/player/CriticalHitEvent;getDamageMultiplier()F", remap = false))
    private float ssc$modifyCriticalMultiplier(float multiplier, @Local(argsOnly = true) Entity target) {
        return CriticalAttackUtil.modifyMultiplier((Player)(Object)this, target, multiplier);
    }
}
