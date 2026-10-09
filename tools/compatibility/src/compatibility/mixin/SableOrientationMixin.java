package compatibility.mixin;

import compatibility.CompatibilitySmoke;
import net.minecraft.world.entity.Entity;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Only test players receive an orientation; Sable performs the actual jump.
@Mixin(targets = "dev.ryanhcode.sable.api.entity.EntitySubLevelUtil", remap = false)
public abstract class SableOrientationMixin {
    @Inject(method = "getCustomEntityOrientation", at = @At("HEAD"), cancellable = true)
    private static void testOrientation(Entity entity, float partialTick, CallbackInfoReturnable<Quaterniondc> result) {
        if (entity instanceof CompatibilitySmoke.ProbePlayer player && player.orientedJump) {
            result.setReturnValue(new Quaterniond());
        }
    }
}
