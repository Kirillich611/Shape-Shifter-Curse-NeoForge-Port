package net.onixary.shapeShifterCurseFabric.perk;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

// Common Side
public class PerkTree {
    public static class PerkNode {
        public final ResourceLocation perkID;
        public final int tier;
        public final int y;
        public final @NotNull ArrayList<@NotNull ResourceLocation> dependentPerkIDs;

        public PerkNode(ResourceLocation perkID, int tier, int y) {
            this.perkID = perkID;
            this.tier = tier;
            this.y = y;
            this.dependentPerkIDs = new ArrayList<>();
        }

        public PerkNode(ResourceLocation perkID, int tier, int y, @NotNull ResourceLocation... dependentPerkIDs) {
            this.perkID = perkID;
            this.tier = tier;
            this.y = y;
            this.dependentPerkIDs = new ArrayList<>(Arrays.asList(dependentPerkIDs));
        }
    }

    public final ResourceLocation treeID;
    public final List<PerkNode> perkNodes = new ArrayList<>();
    public final Map<ResourceLocation, PerkNode> perkNodeMap = new HashMap<>();

    public PerkTree(ResourceLocation treeID) {
        this.treeID = treeID;
    }

    public ResourceLocation getID() {
        return treeID;
    }

    public PerkTree addNode(ResourceLocation perkID, int tier, int y) {
        return this.addNode(new PerkNode(perkID, tier, y));
    }

    public PerkTree addNode(ResourceLocation perkID, int tier, int y, ResourceLocation... dependentPerkIDs) {
        return this.addNode(new PerkNode(perkID, tier, y, dependentPerkIDs));
    }

    public PerkTree addNode(PerkNode perkNode) {
        perkNodes.add(perkNode);
        perkNodeMap.put(perkNode.perkID, perkNode);
        return this;
    }

    public @Nullable PerkNode getNode(ResourceLocation perkID) {
        return perkNodeMap.get(perkID);
    }

    public @NotNull List<PerkNode> getDependentNode(ResourceLocation perkID) {
        PerkNode perkNode = getNode(perkID);
        if (perkNode != null) {
            return perkNodes.stream().filter(perkNode1 -> perkNode1.dependentPerkIDs.contains(perkID)).toList();
        }
        return null;
    }

    public @NotNull List<ResourceLocation> getAllPerks() {
        return new ArrayList<>(perkNodeMap.keySet());
    }

    public @NotNull List<PerkNode> getAllNodes() {
        return new ArrayList<>(perkNodes);
    }
}
