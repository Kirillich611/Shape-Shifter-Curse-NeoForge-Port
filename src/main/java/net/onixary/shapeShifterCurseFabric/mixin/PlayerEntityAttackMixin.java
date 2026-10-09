package net.onixary.shapeShifterCurseFabric.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.additional_power.AlwaysSweepingPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;


@Mixin(Player.class)
public abstract class PlayerEntityAttackMixin {

    /**
     * This mixin is used to force the sweeping attack effect when the AlwaysSweepingPower is active.
     * Used in ocelot_3 form.
     */
    @ModifyVariable(
            method = {"attack"},
            at = @At("STORE"),
            ordinal = 3
    )
    private boolean forceSweeping(boolean value) {

        PowerHolderComponent component = PowerHolderComponent.KEY.get(this);

        for (AlwaysSweepingPower power : component.getPowers(AlwaysSweepingPower.class)) {
            if (power.isActive()) {
                return true;
            }
        }
        return value;
    }

}
