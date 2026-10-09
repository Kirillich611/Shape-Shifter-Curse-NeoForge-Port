package net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.axolotl;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.data.StaticParams;
import net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.ITMob;
import net.onixary.shapeShifterCurseFabric.items.RegCustomItem;
import net.onixary.shapeShifterCurseFabric.status_effects.BaseTransformativeStatusEffect;
import net.onixary.shapeShifterCurseFabric.status_effects.TStatusApplier;
import org.jetbrains.annotations.NotNull;

import static net.onixary.shapeShifterCurseFabric.status_effects.RegTStatusEffect.TO_AXOLOTL_0_EFFECT;

public class TransformativeAxolotlEntity extends Axolotl implements Bucketable, ITMob {
    protected static final ImmutableList<? extends SensorType<? extends Sensor<? super Axolotl>>> SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_ADULT, SensorType.HURT_BY, TAxolotlEntitySensor.T_AXOLOTL_ENTITY_SENSOR, SensorType.AXOLOTL_TEMPTATIONS);

    public TransformativeAxolotlEntity(EntityType<? extends Axolotl> entityType, Level world) {
        super(entityType, world);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.ATTACK_DAMAGE, StaticParams.CUSTOM_MOB_DEFAULT_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, 1.0);
    }

    public static boolean canCustomSpawn(EntityType<TransformativeAxolotlEntity> type, ServerLevelAccessor world, MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        float Chance = ShapeShifterCurseFabric.commonConfig.transformativeAxolotlSpawnChance;
        if (Chance <= 0.0f) { return false; }
        if (Chance >= 1.0f) { return true; }
        if (random.nextFloat() < Chance) { return true; }
        return checkAxolotlSpawnRules(type, world, spawnReason, pos, random);
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        return Bucketable.bucketMobPickup(player, hand, this).orElse(super.mobInteract(player, hand));
    }

    @Override
    public @NotNull ItemStack getBucketItemStack() {
        return new ItemStack(RegCustomItem.TRANSFORMATIVE_AXOLOTL_BUCKET);
    }

    @Override
    public float getStatusChance() {
        return 0.7f;
    }

    @Override
    public BaseTransformativeStatusEffect getStatusEffect() {
        return TO_AXOLOTL_0_EFFECT;
    }

    @Override
    public void tick() {
        super.tick();
        this.TMob_Tick(this);
    }

    @Override
    public void setLastHurtMob(Entity target) {
        if (target instanceof Player player) {
            TStatusApplier.applyStatusByChance(this.getStatusChance(), player, this.getStatusEffect());
        }
        super.setLastHurtMob(target);
    }

    @Override
    protected Brain.@NotNull Provider<Axolotl> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSORS);
    }
}