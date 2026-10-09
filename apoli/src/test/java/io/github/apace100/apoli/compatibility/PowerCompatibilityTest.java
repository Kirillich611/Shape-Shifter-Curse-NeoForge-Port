package io.github.apace100.apoli.compatibility;

import io.github.apace100.apoli.util.FluidTagUtil;
import io.github.apace100.apoli.util.ToolLevelUtil;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;


/** Runs without a client, a world, or an additional test framework. */
public final class PowerCompatibilityTest {

    private static int checks;

    public static void main(String[] args) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        checkTools(0, Items.WOODEN_PICKAXE, Items.WOODEN_AXE, Items.WOODEN_SHOVEL, Items.WOODEN_HOE, Items.WOODEN_SWORD);
        checkTools(0, Items.GOLDEN_PICKAXE, Items.GOLDEN_AXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_HOE, Items.GOLDEN_SWORD);
        checkTools(1, Items.STONE_PICKAXE, Items.STONE_AXE, Items.STONE_SHOVEL, Items.STONE_HOE, Items.STONE_SWORD);
        checkTools(2, Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_SHOVEL, Items.IRON_HOE, Items.IRON_SWORD);
        checkTools(3, Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_HOE, Items.DIAMOND_SWORD);
        checkTools(4, Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE, Items.NETHERITE_SHOVEL, Items.NETHERITE_HOE, Items.NETHERITE_SWORD);
        checkTools(0, Items.STICK, Items.APPLE);
        check(ToolLevelUtil.getHarvestLevel(ItemStack.EMPTY) == 0, "Empty hand");

        ItemStack componentTool = new ItemStack(Items.STICK);
        componentTool.set(DataComponents.TOOL, new ItemStack(Items.DIAMOND_PICKAXE).get(DataComponents.TOOL));
        check(ToolLevelUtil.getHarvestLevel(componentTool) == 3, "Tool component on a non-tiered item");
        ItemStack modifiedTool = new ItemStack(Items.WOODEN_PICKAXE);
        modifiedTool.set(DataComponents.TOOL, new ItemStack(Items.IRON_PICKAXE).get(DataComponents.TOOL));
        check(ToolLevelUtil.getHarvestLevel(modifiedTool) == 2, "Tool component overrides the item's material");

        FluidProbe entity = new FluidProbe();
        entity.waterHeight = 0.75;
        entity.lavaHeight = 0.25;
        entity.waterOnEyes = true;
        entity.lavaOnEyes = false;
        // The inherited vanilla fluid caches are empty, as they are on NeoForge.
        check(FluidTagUtil.getFluidHeight(entity, FluidTags.WATER) == 0.75, "Water height uses the entity API");
        check(FluidTagUtil.getFluidHeight(entity, FluidTags.LAVA) == 0.25, "Lava height uses the entity API");
        check(FluidTagUtil.isSubmerged(entity, FluidTags.WATER), "Water eyes use the entity API");
        check(!FluidTagUtil.isSubmerged(entity, FluidTags.LAVA), "Lava eyes remain separate");
        entity.waterOnEyes = false;
        check(!FluidTagUtil.isSubmerged(entity, FluidTags.WATER), "Eyes above the surface");
        entity.waterHeight = 0.0;
        check(FluidTagUtil.getFluidHeight(entity, FluidTags.WATER) == 0.0, "Dry entity");
        check(!FluidTagUtil.isSubmerged(entity, null), "Null fluid tag is not submerged");
        check(FluidTagUtil.getFluidHeight(entity, null) == 0.0, "Null fluid tag has no height");

        System.out.println("Power compatibility: " + checks + " checks passed.");
    }

    private static void checkTools(int expected, Item... items) {
        for (Item item : items) {
            int actual = ToolLevelUtil.getHarvestLevel(new ItemStack(item));
            check(actual == expected, item + ": expected " + expected + ", got " + actual);
        }
    }

    private static void check(boolean passed, String description) {
        if (!passed) {
            throw new AssertionError(description);
        }
        checks++;
    }

    private static final class FluidProbe extends Entity {
        private double waterHeight;
        private double lavaHeight;
        private boolean waterOnEyes;
        private boolean lavaOnEyes;

        private FluidProbe() {
            super(EntityType.PIG, null);
        }

        @Override
        public double getFluidHeight(TagKey<Fluid> tag) {
            if (tag == FluidTags.WATER) {
                return waterHeight;
            }
            if (tag == FluidTags.LAVA) {
                return lavaHeight;
            }
            throw new AssertionError("Expected a canonical vanilla tag: " + tag);
        }

        @Override
        public boolean isEyeInFluid(TagKey<Fluid> tag) {
            if (tag == FluidTags.WATER) {
                return waterOnEyes;
            }
            if (tag == FluidTags.LAVA) {
                return lavaOnEyes;
            }
            throw new AssertionError("Expected a canonical vanilla tag: " + tag);
        }

        @Override
        protected void defineSynchedData(SynchedEntityData.Builder builder) {
        }

        @Override
        protected void readAdditionalSaveData(CompoundTag tag) {
        }

        @Override
        protected void addAdditionalSaveData(CompoundTag tag) {
        }
    }
}
