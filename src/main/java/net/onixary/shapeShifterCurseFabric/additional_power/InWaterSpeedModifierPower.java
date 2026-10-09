package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.world.entity.player.Player;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.LivingEntity;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

public class InWaterSpeedModifierPower extends Power {
    private final float Modifier;

    public InWaterSpeedModifierPower(PowerType<?> type, LivingEntity entity, float Modifier) {
        super(type, entity);
        this.Modifier = Modifier;
    }

    public float getSpeedModifier() {
        return Modifier;
    }

    public static double verticalWaterMultiplier(LivingEntity entity) {
        if (!(entity instanceof Player)) return 1.0;
        return PowerHolderComponent.getPowers(entity, InWaterSpeedModifierPower.class).stream()
                .mapToDouble(InWaterSpeedModifierPower::getSpeedModifier).reduce(1.0, (a, b) -> a * b);
    }

    public static PowerFactory<Power> createFactory() {
        return new PowerFactory<>(
                ShapeShifterCurseFabric.identifier("in_water_speed_modifier"),
                new SerializableData()
                        .add("modifier", SerializableDataTypes.FLOAT, 1.0f),
                data -> (type, entity) -> new InWaterSpeedModifierPower(type, entity, data.getFloat("modifier"))
        ).allowCondition();
    }
}
