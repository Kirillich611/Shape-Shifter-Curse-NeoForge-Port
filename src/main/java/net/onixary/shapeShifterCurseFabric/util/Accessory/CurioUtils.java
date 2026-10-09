package net.onixary.shapeShifterCurseFabric.util.Accessory;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

// XuHaoNan:
// 新API未测试(大概率能用 基本CV旧API) 得等我互联版更新后用我拓展测试 我这边kilt老有问题
//
// ⚠ 本类**刻意不 import 任何 Curios 类**：没装 Curios 时它也必须能安全加载（DefaultAccessory 里的
// Class.forName 探测依赖这一点）。这里全部是 fallback 空桩，真实现由 mixin/forge/CurioUtilsImpl.java
// 在 Curios 存在时经 MixinConfigPlugin 的 "curios" 门控 @Overwrite 掉。
public class CurioUtils {
    public static boolean isLoaded() { return false; }

    public static boolean isEquipped(LivingEntity entity, Item item) {
        return false;
    }

    public static Map<String, List<ItemStack>> getEntitySlots(LivingEntity entity) {
        return Map.of();
    }

    public static List<ItemStack> getEntitySlot(LivingEntity entity, String SlotName) {
        return List.of();
    }

    public static void setEntitySlot(LivingEntity entity, String SlotName, int Index, ItemStack stack) {
        return;
    }

    public static Map<String, List<ItemStack>> getEntityCosmeticSlots(LivingEntity entity) { return Map.of(); }

    public static List<ItemStack> getEntityCosmeticSlot(LivingEntity entity, String SlotName) { return List.of(); }

    public static void setEntityCosmeticSlot(LivingEntity entity, String SlotName, int Index, ItemStack stack) { return; }

    public static Map<String, List<Boolean>> getEntitySlotRenders(LivingEntity entity) { return Map.of(); }

    public static List<Boolean> getEntitySlotRender(LivingEntity entity, String SlotName) { return List.of(); }

    public static void setEntitySlotRender(LivingEntity entity, String SlotName, int Index, boolean render) { return; }
}
