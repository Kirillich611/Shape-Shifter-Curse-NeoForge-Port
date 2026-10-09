package io.github.apace100.apoli.util;

import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;

public final class ToolLevelUtil {

    private ToolLevelUtil() {
    }

    public static int getHarvestLevel(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        Tool tool = stack.get(DataComponents.TOOL);
        if (tool != null) {
            for (Tool.Rule rule : tool.rules()) {
                if (rule.correctForDrops().orElse(true)
                        || !(rule.blocks() instanceof HolderSet.Named<Block> named)) {
                    continue;
                }
                int level = getHarvestLevel(named.key());
                if (level >= 0) {
                    return level;
                }
            }
        }
        // Swords and some modded tiered items do not encode their material in
        // TOOL rules. Preserve Apoli's legacy material levels for these items.
        if (stack.getItem() instanceof TieredItem tiered) {
            return Math.max(0, getHarvestLevel(tiered.getTier().getIncorrectBlocksForDrops()));
        }
        return 0;
    }

    private static int getHarvestLevel(TagKey<Block> incorrectBlocks) {
        if (incorrectBlocks.equals(BlockTags.INCORRECT_FOR_NETHERITE_TOOL)) {
            return 4;
        }
        if (incorrectBlocks.equals(BlockTags.INCORRECT_FOR_DIAMOND_TOOL)) {
            return 3;
        }
        if (incorrectBlocks.equals(BlockTags.INCORRECT_FOR_IRON_TOOL)) {
            return 2;
        }
        if (incorrectBlocks.equals(BlockTags.INCORRECT_FOR_STONE_TOOL)) {
            return 1;
        }
        if (incorrectBlocks.equals(BlockTags.INCORRECT_FOR_WOODEN_TOOL)
                || incorrectBlocks.equals(BlockTags.INCORRECT_FOR_GOLD_TOOL)) {
            return 0;
        }
        return -1;
    }
}
