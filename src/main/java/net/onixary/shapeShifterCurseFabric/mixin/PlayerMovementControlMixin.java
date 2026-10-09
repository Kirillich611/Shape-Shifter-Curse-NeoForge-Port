package net.onixary.shapeShifterCurseFabric.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.additional_power.BatBlockAttachPower;
import net.onixary.shapeShifterCurseFabric.additional_power.ActionOnJumpPower;
import net.onixary.shapeShifterCurseFabric.client.ClientPlayerStateManager;
import net.onixary.shapeShifterCurseFabric.additional_power.JumpEventCondition;
import net.onixary.shapeShifterCurseFabric.additional_power.SlowdownPercentPower;
import net.onixary.shapeShifterCurseFabric.additional_power.SprintingStateTracker;
import net.onixary.shapeShifterCurseFabric.networking.BytePayload;
import net.onixary.shapeShifterCurseFabric.networking.ModPackets;
import net.onixary.shapeShifterCurseFabric.util.Interface.IMoveController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Player.class)
public class PlayerMovementControlMixin implements IMoveController {

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void preventTravelWhenAttached(Vec3 movementInput, CallbackInfo ci) {
        Player player = (Player) (Object) this;

        // 添加空值检查
        PowerHolderComponent component = PowerHolderComponent.KEY.getNullable(player);
        if (component == null) {
            return; // 组件未初始化，跳过处理
        }

        BatBlockAttachPower attachPower = PowerHolderComponent.getPowers(player, BatBlockAttachPower.class)
                .stream()
                .filter(BatBlockAttachPower::isAttached)
                .findFirst()
                .orElse(null);

        if (attachPower != null) {
            // 完全取消移动，类似蜂蜜块的效果
            player.setDeltaMovement(0, 0, 0);
            ci.cancel();
        }
    }

    @Inject(method = "getSpeed()F", at = @At("RETURN"), cancellable = true)
    private void zeroMovementSpeedWhenAttached(CallbackInfoReturnable<Float> cir) {
        if (noMoveTick > 0) {
            cir.setReturnValue(0.0f);
        }
        Player player = (Player) (Object) this;

        // 添加空值检查
        PowerHolderComponent component = PowerHolderComponent.KEY.getNullable(player);
        if (component == null) {
            return; // 组件未初始化，跳过处理
        }

	    PowerHolderComponent.getPowers(player, BatBlockAttachPower.class)
			    .stream()
			    .filter(BatBlockAttachPower::isAttached)
			    .findFirst().ifPresent(attachPower -> cir.setReturnValue(0.0f));
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    private void handleJump(CallbackInfo ci) {
        Player player = (Player) (Object) this;

        // 添加空值检查
        PowerHolderComponent component = PowerHolderComponent.KEY.getNullable(player);
        if (component == null) {
            return; // 组件未初始化，跳过处理
        }

        BatBlockAttachPower attachPower = PowerHolderComponent.getPowers(player, BatBlockAttachPower.class)
                .stream()
                .filter(BatBlockAttachPower::isAttached)
                .findFirst()
                .orElse(null);

        if (attachPower != null) {
            // 处理跳跃取消吸附
            if (player.level().isClientSide() && player.isLocalPlayer()) {
                FriendlyByteBuf buf = PacketByteBufs.create();
                ClientPlayNetworking.send(new BytePayload(BytePayload.id(ModPackets.JUMP_DETACH_REQUEST_ID),  buf));
            }
            ci.cancel();
        }

    }

    @Inject(method = "jumpFromGround", at = @At("TAIL"))
    private void reportCompletedJump(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        JumpEventCondition.setJumping(player, true);
        if (player.level().isClientSide() && player.isLocalPlayer()) {
            // Predict movement only; effects and moisture stay on the server.
            PowerHolderComponent.getPowers(player, ActionOnJumpPower.class)
                    .forEach(ActionOnJumpPower::executeClientAction);
            ClientPlayNetworking.send(new BytePayload(BytePayload.id(ModPackets.JUMP_EVENT_ID), PacketByteBufs.create()));
        }
    }

    @Inject(method = "tryToStartFallFlying", at = @At("HEAD"), cancellable = true)
    private void preventElytraCheckWhenAttached(CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;

        // 添加空值检查
        PowerHolderComponent component = PowerHolderComponent.KEY.getNullable(player);
        if (component == null) {
            return; // 组件未初始化，跳过处理
        }

        BatBlockAttachPower attachPower = PowerHolderComponent.getPowers(player, BatBlockAttachPower.class)
                .stream()
                .filter(BatBlockAttachPower::isAttached)
                .findFirst()
                .orElse(null);

        if (attachPower != null) {

            if (player.level().isClientSide() && player.isLocalPlayer()) {
                FriendlyByteBuf buf = PacketByteBufs.create();
                ClientPlayNetworking.send(new BytePayload(BytePayload.id(ModPackets.JUMP_DETACH_REQUEST_ID),  buf));
            }

            // 重置鞘翅相关标志
            player.stopFallFlying();
            // 强制设置为在地面上，这样空格键就不会触发鞘翅
            player.setOnGround(true);
            // 取消鞘翅检测
            cir.setReturnValue(false);
        }
    }

    @Unique
    private boolean ssc$previousManualSneak;

    @Inject(method = "tick", at = @At("TAIL"))
    private void trackSprintingState(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (!player.level().isClientSide() || !player.isLocalPlayer()) return;
        boolean wasSprinting = SprintingStateTracker.wasSprintingLastTick(player);
        boolean manualSneak = ClientPlayerStateManager.manualSneak;
        SprintingStateTracker.updateSprintingState(player, player.isSprinting());
        if (wasSprinting && manualSneak && !ssc$previousManualSneak && SprintingStateTracker.canTrigger(player)) {
            SprintingStateTracker.setTriggered(player);
            ClientPlayNetworking.send(new BytePayload(BytePayload.id(ModPackets.SPRINTING_TO_SNEAKING_EVENT_ID), PacketByteBufs.create()));
        }
        ssc$previousManualSneak = manualSneak;
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void cleanupSprintingState(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        SprintingStateTracker.removePlayer(player);
    }

    @ModifyVariable(method = "makeStuckInBlock", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private Vec3 SlowdownPercentMixin(Vec3 multiplier) {
        Player player = (Player) (Object) this;
        List<SlowdownPercentPower> slowdownPower = PowerHolderComponent.getPowers(player, SlowdownPercentPower.class);
        float slowdownPercent = 1.0f;
        for (SlowdownPercentPower power : slowdownPower) {
            slowdownPercent *= power.Multiplier;
        }
        return multiplier.scale(slowdownPercent);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (this.noMoveTick > 0) {
            this.noMoveTick--;
        }
    }

    @Unique
    public int noMoveTick = 0;

    @Override
    public void shape_shifter_curse$setNoMoveTick(int tick) {
        this.noMoveTick = tick;
    }

    @Override
    public int shape_shifter_curse$getNoMoveTick() {
        return this.noMoveTick;
    }

}