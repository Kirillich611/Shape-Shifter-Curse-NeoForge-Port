package net.onixary.shapeShifterCurseFabric.mixin.forge;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.util.Accessory.CurioUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Curios 兼容层（NeoForge 版，重写自上游 Forge 预编译 CurioUtilsImpl.class）。
 * 把 {@link CurioUtils} 的全部 fallback 空桩替换为基于 Curios API 的真实现，
 * 并让 {@code isLoaded()} 返回 true。仅在 Curios mod 存在时经 MixinConfigPlugin 条件注入。
 * <p>
 * 之所以用 {@code @Overwrite} 而不是把实现直接写进 {@link CurioUtils}：
 * {@code CurioUtils} 在没有 Curios 的整合包里也必须能被安全加载（{@code DefaultAccessory:109} 用
 * {@code Class.forName} 探测它），所以它不能 import 任何 Curios 类。
 * <p>
 * 所有 {@code @Overwrite} 都写了 {@code remap = false}：{@link CurioUtils} 是本项目自己的类，
 * 不参与混淆，dev 与线上方法名一致。不加这个参数 Mixin AP 会去查混淆映射表并直接报
 * "Unable to locate obfuscation mapping for @Overwrite method"。
 * <p>
 * 写入方法都做了下标越界防护（上游预编译版没有，越界会直接抛 IndexOutOfBoundsException）。
 */
@Mixin(CurioUtils.class)
public class CurioUtilsImpl {

    /**
     * @author XuHaoNan
     * @reason 能走到这里就说明 Curios 门控已通过，直接返回 true。
     */
    @Overwrite(remap = false)
    public static boolean isLoaded() {
        return true;
    }

    /**
     * @author XuHaoNan
     * @reason 用 Curios API 替换 fallback 空桩。
     */
    @Overwrite(remap = false)
    public static boolean isEquipped(LivingEntity entity, Item item) {
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (handler == null) {
            return false;
        }
        for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
            IDynamicStackHandler stacks = stacksHandler.getStacks();
            for (int i = 0; i < stacks.getSlots(); i++) {
                if (stacks.getStackInSlot(i).is(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @author XuHaoNan
     * @reason 用 Curios API 替换 fallback 空桩。
     */
    @Overwrite(remap = false)
    public static Map<String, List<ItemStack>> getEntitySlots(LivingEntity entity) {
        Map<String, List<ItemStack>> map = new HashMap<>();
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (handler == null) {
            return map;
        }
        for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
            IDynamicStackHandler stacks = entry.getValue().getStacks();
            List<ItemStack> list = new ArrayList<>();
            for (int i = 0; i < stacks.getSlots(); i++) {
                list.add(stacks.getStackInSlot(i));
            }
            map.put(entry.getKey(), list);
        }
        return map;
    }

    /**
     * @author XuHaoNan
     * @reason 用 Curios API 替换 fallback 空桩。
     */
    @Overwrite(remap = false)
    public static List<ItemStack> getEntitySlot(LivingEntity entity, String SlotName) {
        List<ItemStack> list = new ArrayList<>();
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (handler == null) {
            return list;
        }
        ICurioStacksHandler stacksHandler = handler.getCurios().get(SlotName);
        if (stacksHandler != null) {
            IDynamicStackHandler stacks = stacksHandler.getStacks();
            for (int i = 0; i < stacks.getSlots(); i++) {
                list.add(stacks.getStackInSlot(i));
            }
        }
        return list;
    }

    /**
     * @author XuHaoNan
     * @reason 用 Curios API 替换 fallback 空桩。
     */
    @Overwrite(remap = false)
    public static void setEntitySlot(LivingEntity entity, String SlotName, int Index, ItemStack stack) {
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (handler == null) {
            return;
        }
        ICurioStacksHandler stacksHandler = handler.getCurios().get(SlotName);
        if (stacksHandler != null && Index >= 0 && Index < stacksHandler.getStacks().getSlots()) {
            stacksHandler.getStacks().setStackInSlot(Index, stack);
        }
    }

    /**
     * @author XuHaoNan
     * @reason 用 Curios API 替换 fallback 空桩（外观槽 = getCosmeticStacks）。
     */
    @Overwrite(remap = false)
    public static Map<String, List<ItemStack>> getEntityCosmeticSlots(LivingEntity entity) {
        Map<String, List<ItemStack>> map = new HashMap<>();
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (handler == null) {
            return map;
        }
        for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
            IDynamicStackHandler stacks = entry.getValue().getCosmeticStacks();
            List<ItemStack> list = new ArrayList<>();
            for (int i = 0; i < stacks.getSlots(); i++) {
                list.add(stacks.getStackInSlot(i));
            }
            map.put(entry.getKey(), list);
        }
        return map;
    }

    /**
     * @author XuHaoNan
     * @reason 用 Curios API 替换 fallback 空桩（外观槽 = getCosmeticStacks）。
     */
    @Overwrite(remap = false)
    public static List<ItemStack> getEntityCosmeticSlot(LivingEntity entity, String SlotName) {
        List<ItemStack> list = new ArrayList<>();
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (handler == null) {
            return list;
        }
        ICurioStacksHandler stacksHandler = handler.getCurios().get(SlotName);
        if (stacksHandler != null) {
            IDynamicStackHandler stacks = stacksHandler.getCosmeticStacks();
            for (int i = 0; i < stacks.getSlots(); i++) {
                list.add(stacks.getStackInSlot(i));
            }
        }
        return list;
    }

    /**
     * @author XuHaoNan
     * @reason 用 Curios API 替换 fallback 空桩（外观槽 = getCosmeticStacks）。
     */
    @Overwrite(remap = false)
    public static void setEntityCosmeticSlot(LivingEntity entity, String SlotName, int Index, ItemStack stack) {
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (handler == null) {
            return;
        }
        ICurioStacksHandler stacksHandler = handler.getCurios().get(SlotName);
        if (stacksHandler != null && Index >= 0 && Index < stacksHandler.getCosmeticStacks().getSlots()) {
            stacksHandler.getCosmeticStacks().setStackInSlot(Index, stack);
        }
    }

    /**
     * @author XuHaoNan
     * @reason 用 Curios API 替换 fallback 空桩（渲染开关 = getRenders）。
     */
    @Overwrite(remap = false)
    public static Map<String, List<Boolean>> getEntitySlotRenders(LivingEntity entity) {
        Map<String, List<Boolean>> map = new HashMap<>();
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (handler == null) {
            return map;
        }
        for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
            map.put(entry.getKey(), new ArrayList<>(entry.getValue().getRenders()));
        }
        return map;
    }

    /**
     * @author XuHaoNan
     * @reason 用 Curios API 替换 fallback 空桩（渲染开关 = getRenders）。
     */
    @Overwrite(remap = false)
    public static List<Boolean> getEntitySlotRender(LivingEntity entity, String SlotName) {
        List<Boolean> list = new ArrayList<>();
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (handler == null) {
            return list;
        }
        ICurioStacksHandler stacksHandler = handler.getCurios().get(SlotName);
        if (stacksHandler != null) {
            list.addAll(stacksHandler.getRenders());
        }
        return list;
    }

    /**
     * @author XuHaoNan
     * @reason 用 Curios API 替换 fallback 空桩（渲染开关 = getRenders）。
     */
    @Overwrite(remap = false)
    public static void setEntitySlotRender(LivingEntity entity, String SlotName, int Index, boolean render) {
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (handler == null) {
            return;
        }
        ICurioStacksHandler stacksHandler = handler.getCurios().get(SlotName);
        if (stacksHandler != null) {
            NonNullList<Boolean> renders = stacksHandler.getRenders();
            if (Index >= 0 && Index < renders.size()) {
                renders.set(Index, render);
            }
        }
    }
}
