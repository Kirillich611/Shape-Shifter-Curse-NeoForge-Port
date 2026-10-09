package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.ModifySlipperinessPower;
import io.github.apace100.apoli.util.modifier.ModifierOperation;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

import java.util.function.Predicate;

public class ConditionedModifySlipperinessPower extends ModifySlipperinessPower {
    private final ConditionFactory<LivingEntity>.Instance condition;
    private final float slipperinessModifier;

    public ConditionedModifySlipperinessPower(PowerType<?> type, LivingEntity entity, Predicate<BlockInWorld> predicate, ConditionFactory<LivingEntity>.Instance condition, float slipperinessModifier) {
        super(type, entity, predicate);
        this.condition = condition;
        this.slipperinessModifier = slipperinessModifier;
        addModifier(ModifierUtil.createSimpleModifier(ModifierOperation.ADD_BASE_EARLY, slipperinessModifier));
    }

    public boolean doesApply(LevelReader world, BlockPos pos) {
        return super.doesApply(world, pos) && (condition == null || condition.test(entity));
    }

    public float getSlipperinessModifier() {
        return slipperinessModifier;
    }

    public static PowerFactory<?> createFactory() {
        return new PowerFactory<>(
                ShapeShifterCurseFabric.identifier("conditioned_modify_slipperiness"),
                new SerializableData()
                        .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                        .add("entity_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                        .add("modifier", SerializableDataTypes.FLOAT),
                data ->
                        (type, player) -> new ConditionedModifySlipperinessPower(
                                type,
                                player,
                                data.get("block_condition"),
                                data.get("entity_condition"),
                                data.getFloat("modifier"))
        ).allowCondition();
    }

}
