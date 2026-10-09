package net.onixary.shapeShifterCurseFabric.util;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.additional_power.CriticalDamageModifierPower;
import net.onixary.shapeShifterCurseFabric.additional_power.EnhancedFallingAttackPower;

public final class CriticalAttackUtil {
    private CriticalAttackUtil() {}

    public static float modifyMultiplier(Player player, Entity target, float multiplier) {
        var criticalPowers = PowerHolderComponent.getPowers(player, CriticalDamageModifierPower.class);
        if (!criticalPowers.isEmpty()) {
            var power = criticalPowers.getFirst();
            multiplier *= power.getMultiplier();
            power.executeAction();
        }
        var fallingPowers = PowerHolderComponent.getPowers(player, EnhancedFallingAttackPower.class);
        if (!fallingPowers.isEmpty()) {
            // Read before the actions reset fallDistance on a successful critical strike.
            multiplier *= Math.clamp(player.fallDistance, 1.0f, 2.0f);
            for (var power : fallingPowers) {
                power.executeTargetAction(target);
                power.executeSelfAction();
            }
        }
        return multiplier;
    }
}
