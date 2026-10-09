package net.onixary.shapeShifterCurseFabric.player_form.utils;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.perk.RegPerks;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.util.InitialFormUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerFormComponent implements AutoSyncedComponent {
    public static final ComponentKey<PlayerFormComponent> COMPONENT = ComponentRegistry.getOrCreate(ShapeShifterCurseFabric.identifier("player_form"), PlayerFormComponent.class);

    public @NotNull IForm nowForm = RegPlayerForms.ORIGINAL_BEFORE_ENABLE;
    public @Nullable ResourceLocation nowFormID = nowForm.getFormID();
    public @NotNull ResourceLocation fallbackFormID = RegPlayerForms.ORIGINAL_BEFORE_ENABLE.getFormID();
    public final List<IForm> formHistory = new ArrayList<>();

    public boolean isCursedMoonApplied = false;
    public boolean lastTransformByCure = false;
    public @Nullable IForm BeforeCursedMoonAppliedForm = null;
    public @Nullable IForm AfterCursedMoonAppliedForm = null;

    public @Nullable IForm transformTargetForm = null;
    public ResourceLocation customPotionFormID = RegPlayerForms.ORIGINAL_BEFORE_ENABLE.getFormID();

    public float instinctValue = 0.0f;
    public float instinctRate = 0.0f;
    public HashMap<ResourceLocation, InstinctUtils.InstinctEffect> instinctEffects = new HashMap<>();

    public ResourceLocation nowPerkTree = RegPerks.EMPTY_PERK_TREE;
    public HashMap<ResourceLocation, List<ResourceLocation>> formPerkMap = new HashMap<>();

    // Переменные для клиентской плавной интерполяции (индивидуальные для каждого игрока)
    public int lastSyncTickCount = 0;
    public float clientInstinctValue = 0.0f;

    public Player player = null;

    public PlayerFormComponent(Player player) {
        this.player = player;
        this.nowForm = InitialFormUtils.getInitialForm(player);
        this.nowFormID = nowForm.getFormID();
        this.fallbackFormID = nowForm.getFormID();
    }

    public @NotNull IForm getFallbackForm() {
        IForm form = RegPlayerForms.getPlayerForm(this.fallbackFormID);
        if (form == null) {
            return InitialFormUtils.getInitialForm(player);
        }
        return form;
    }

    public void setFallbackForm(@Nullable ResourceLocation formID) {
        IForm form = RegPlayerForms.getPlayerForm(formID);
        if (form == null) {
            ShapeShifterCurseFabric.LOGGER.warn("Fallback form not found");
            formID = InitialFormUtils.getInitialForm(player).getFormID();
        } else if (form.isDynamicForm()) {
            ShapeShifterCurseFabric.LOGGER.warn("Fallback form not supported dynamic form");
            formID = InitialFormUtils.getInitialForm(player).getFormID();
        } else if (form instanceof NeedCheckUsableForm) {
            ShapeShifterCurseFabric.LOGGER.warn("Fallback form not supported need check usable form");
            formID = InitialFormUtils.getInitialForm(player).getFormID();
        }
        this.fallbackFormID = formID;
    }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        if (tag.contains("no_form_id") && tag.getBoolean("no_form_id")) {
            nowFormID = null;
            nowForm = RegPlayerForms.ORIGINAL_BEFORE_ENABLE;
        } else {
            if (tag.contains("nowFormID")) {
                nowFormID = ResourceLocation.tryParse(tag.getString("nowFormID"));
                nowForm = FormUtils.parseForm(nowFormID, RegPlayerForms.ORIGINAL_BEFORE_ENABLE);
            } else {
                nowFormID = RegPlayerForms.ORIGINAL_BEFORE_ENABLE.getFormID();
                nowForm = RegPlayerForms.ORIGINAL_BEFORE_ENABLE;
            }
        }
        if (tag.contains("fallbackFormID")) {
            ResourceLocation fallbackFormIDNullable = ResourceLocation.tryParse(tag.getString("fallbackFormID"));
            fallbackFormID = fallbackFormIDNullable == null ? RegPlayerForms.ORIGINAL_BEFORE_ENABLE.getFormID() : fallbackFormIDNullable;
        }
        if (tag.contains("currentForm")) {
            nowFormID = ResourceLocation.tryParse(tag.getString("currentForm"));
            nowForm = FormUtils.parseForm(nowFormID, RegPlayerForms.ORIGINAL_BEFORE_ENABLE);
        }
        if (tag.contains("formHistory")) {
            formHistory.clear();
            ListTag history = tag.getList("formHistory", Tag.TAG_STRING);
            formHistory.clear();
            for (Tag element : history) {
                IForm form = FormUtils.parseForm(ResourceLocation.tryParse(element.getAsString()), null);
                if (form != null) {
                    formHistory.add(form);
                }
            }
        }
        isCursedMoonApplied = tag.contains("isCursedMoonApplied") && tag.getBoolean("isCursedMoonApplied");
        lastTransformByCure = tag.contains("lastTransformByCure") && tag.getBoolean("lastTransformByCure");

        BeforeCursedMoonAppliedForm = tag.contains("BeforeCursedMoonAppliedForm") ? FormUtils.parseForm(ResourceLocation.tryParse(tag.getString("BeforeCursedMoonAppliedForm")), null) : null;
        AfterCursedMoonAppliedForm = tag.contains("AfterCursedMoonAppliedForm") ? FormUtils.parseForm(ResourceLocation.tryParse(tag.getString("AfterCursedMoonAppliedForm")), null) : null;
        transformTargetForm = tag.contains("transformTargetForm") ? FormUtils.parseForm(ResourceLocation.tryParse(tag.getString("transformTargetForm")), null) : null;

        customPotionFormID = tag.contains("customPotionFormID") ? ResourceLocation.tryParse(tag.getString("customPotionFormID")) : RegPlayerForms.ORIGINAL_BEFORE_ENABLE.getFormID();

        instinctValue = tag.contains("instinctValue") ? tag.getFloat("instinctValue") : 0f;
        instinctRate = tag.contains("instinctRate") ? tag.getFloat("instinctRate") : 0f;

        if (tag.contains("instinctEffects")) {
            instinctEffects.clear();
            CompoundTag effects = tag.getCompound("instinctEffects");
            for (String key : effects.getAllKeys()) {
                instinctEffects.put(ResourceLocation.tryParse(key), InstinctUtils.InstinctEffect.fromNBT(effects.getCompound(key)));
            }
        }
        if (tag.contains("now_perk_tree")) {
            nowPerkTree = ResourceLocation.tryParse(tag.getString("now_perk_tree"));
        }
        if (tag.contains("perks")) {
            formPerkMap.clear();
            CompoundTag perks = tag.getCompound("perks");
            for (String key : perks.getAllKeys()) {
                ResourceLocation treeID = ResourceLocation.tryParse(key);
                if (treeID != null) {
                    List<ResourceLocation> perkList = new ArrayList<>();
                    ListTag perkListNBT = perks.getList(key, Tag.TAG_STRING);
                    for (Tag element : perkListNBT) {
                        ResourceLocation perkID = ResourceLocation.tryParse(element.getAsString());
                        if (perkID != null && !perkList.contains(perkID)) {
                            perkList.add(perkID);
                        }
                    }
                    formPerkMap.put(treeID, perkList);
                }
            }
        }

        // Синхронизируем базовые значения для плавной отрисовки
        if (player != null && player.level().isClientSide) {
            this.clientInstinctValue = this.instinctValue;
            this.lastSyncTickCount = player.tickCount;
        }
    }

    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        if (nowFormID != null) {
            tag.putString("nowFormID", nowFormID.toString());
        } else {
            tag.putBoolean("no_form_id", true);
        }
        tag.putString("fallbackFormID", fallbackFormID.toString());
        ListTag history = new ListTag();
        for (IForm form : formHistory) {
            history.add(StringTag.valueOf(form.getFormID().toString()));
        }
        tag.put("formHistory", history);
        tag.putBoolean("isCursedMoonApplied", isCursedMoonApplied);
        tag.putBoolean("lastTransformByCure", lastTransformByCure);
        if (BeforeCursedMoonAppliedForm != null) {
            tag.putString("BeforeCursedMoonAppliedForm", BeforeCursedMoonAppliedForm.getFormID().toString());
        }
        if (AfterCursedMoonAppliedForm != null) {
            tag.putString("AfterCursedMoonAppliedForm", AfterCursedMoonAppliedForm.getFormID().toString());
        }
        if (transformTargetForm != null) {
            tag.putString("transformTargetForm", transformTargetForm.getFormID().toString());
        }
        if (customPotionFormID != null) {
            tag.putString("customPotionFormID", customPotionFormID.toString());
        }
        tag.putFloat("instinctValue", instinctValue);
        tag.putFloat("instinctRate", instinctRate);
        CompoundTag effects = new CompoundTag();
        for (Map.Entry<ResourceLocation, InstinctUtils.InstinctEffect> entry : instinctEffects.entrySet()) {
            CompoundTag effect = new CompoundTag();
            entry.getValue().toNBT(effect);
            effects.put(entry.getKey().toString(), effect);
        }
        tag.put("instinctEffects", effects);
        tag.putString("now_perk_tree", nowPerkTree.toString());
        CompoundTag perks = new CompoundTag();
        for (Map.Entry<ResourceLocation, List<ResourceLocation>> perkEntry : formPerkMap.entrySet()) {
            ListTag perkTree = new ListTag();
            for (ResourceLocation perkID : perkEntry.getValue()) {
                perkTree.add(StringTag.valueOf(perkID.toString()));
            }
            if (perkTree.isEmpty()) continue;
            perks.put(perkEntry.getKey().toString(), perkTree);
        }
        tag.put("perks", perks);
    }

    public void clear() {
        this.nowForm = InitialFormUtils.getInitialForm(this.player);
        this.nowFormID = nowForm.getFormID();
        formHistory.clear();
        isCursedMoonApplied = false;
        lastTransformByCure = false;
        BeforeCursedMoonAppliedForm = null;
        AfterCursedMoonAppliedForm = null;
        transformTargetForm = null;
        customPotionFormID = RegPlayerForms.ORIGINAL_BEFORE_ENABLE.getFormID();
        instinctValue = 0.0f;
        instinctRate = 0.0f;
        instinctEffects.clear();
        this.sync();
    }

    public void sync() {
        COMPONENT.sync(this.player);
    }

    public void setForm(IForm form) {
        nowForm = form;
        nowFormID = form.getFormID();
        this.sync();
    }

    public void setForm(ResourceLocation formID) {
        nowForm = FormUtils.parseForm(formID, RegPlayerForms.ORIGINAL_BEFORE_ENABLE);
        nowFormID = formID;
        this.sync();
    }
}