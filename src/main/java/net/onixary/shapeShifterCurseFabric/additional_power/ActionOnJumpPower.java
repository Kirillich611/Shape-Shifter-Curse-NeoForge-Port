package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

import java.util.function.Predicate;

public class ActionOnJumpPower extends Power {

    private final ActionFactory<Entity>.Instance entityAction;
    private final Predicate<Entity> entityCondition;
    private final ActionFactory<Entity>.Instance clientAction;

    public ActionOnJumpPower(PowerType<?> type, LivingEntity entity, ActionFactory<Entity>.Instance entityAction, Predicate<Entity> condition) {
        this(type, entity, entityAction, condition, null);
    }

    public ActionOnJumpPower(PowerType<?> type, LivingEntity entity, ActionFactory<Entity>.Instance entityAction,
                            Predicate<Entity> condition, ActionFactory<Entity>.Instance clientAction) {
        super(type, entity);
        this.clientAction = clientAction;
        this.entityAction = entityAction;
        this.entityCondition = condition;
    }

    public void executeAction() {
        if (entity instanceof Player player) {
            // 检查condition是否满足
            if (entityCondition == null || entityCondition.test(player)) {
                // 执行action
                if (entityAction != null) {
                    entityAction.accept(player);
                }
            }
        }
    }

    public void executeClientAction() {
        if (entity instanceof Player player && player.level().isClientSide() && player.isLocalPlayer()
                && (entityCondition == null || entityCondition.test(player)) && clientAction != null) {
            clientAction.accept(player);
        }
    }

    public static PowerFactory<?> createFactory() {
        return new PowerFactory<>(
                ShapeShifterCurseFabric.identifier("action_on_jump"),
                new SerializableData()
                        .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                        .add("client_action", ApoliDataTypes.ENTITY_ACTION, null)
                        .add("entity_condition", ApoliDataTypes.ENTITY_CONDITION, null),
                data -> (type, entity) -> new ActionOnJumpPower(
                        type,
                        entity,
                        data.get("entity_action"),
                        data.get("entity_condition"),
                        data.get("client_action")
                )
        ).allowCondition();
    }
}
