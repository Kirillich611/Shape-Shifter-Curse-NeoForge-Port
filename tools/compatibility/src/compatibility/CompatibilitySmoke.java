package compatibility;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.common.util.FakePlayer;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;

@Mod("compatibility_smoke")
public class CompatibilitySmoke {
    private int assertions;

    public CompatibilitySmoke() {
        NeoForge.EVENT_BUS.addListener(this::started);
    }

    private void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
        assertions++;
    }

    private void started(ServerStartedEvent event) {
        Pig pig = null;
        try {
            Class<?> registryClass = Class.forName("io.github.apace100.apoli.power.PowerTypeRegistry");
            Map<?, ?> powers = (Map<?, ?>)registryClass.getMethod("get").invoke(null);
            String ids = new String(getClass().getResourceAsStream("/expected-powers.txt").readAllBytes(), StandardCharsets.UTF_8);
            for (String id : ids.split("\\R")) {
                if (!id.isBlank()) check(powers.containsKey(ResourceLocation.parse(id.strip())), "Missing power " + id);
            }

            ServerLevel level = event.getServer().overworld();
            // A freshly generated world starts at tick zero; let default cooldowns expire before probing.
            var levelData = event.getServer().getWorldData().overworldData();
            levelData.setGameTime(Math.max(1000L, levelData.getGameTime()));
            Class<?> packetClass = Class.forName("io.github.apace100.apoli.networking.PowerListPacket");
            net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, Object> codec =
                (net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, Object>)packetClass.getField("CODEC").get(null);
            net.minecraft.network.RegistryFriendlyByteBuf wire = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), level.registryAccess());
            Object packet = packetClass.getConstructor(Map.class).newInstance(powers);
            codec.encode(wire, packet);
            Map<?, ?> clientPowers = (Map<?, ?>)packetClass.getMethod("factories").invoke(codec.decode(wire));
            check(clientPowers.keySet().equals(powers.keySet()), "Client receives every registered power");
            check(wire.readableBytes() == 0, "Power-list decoder consumes the full packet");
            wire.release();
            BlockPos origin = new BlockPos(level.getSharedSpawnPos().getX(), 180, level.getSharedSpawnPos().getZ());
            // Reset the entire jump corridor: later wall probes persist above the water fixture.
            for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++)
                for (int y = 0; y <= 8; y++) level.setBlockAndUpdate(origin.offset(x, y, z), Blocks.AIR.defaultBlockState());
            for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) {
                level.setBlockAndUpdate(origin.offset(x, -1, z), Blocks.STONE.defaultBlockState());
                for (int y = 0; y <= 2; y++) level.setBlockAndUpdate(origin.offset(x, y, z), Blocks.WATER.defaultBlockState());
            }
            pig = EntityType.PIG.create(level);
            pig.setNoGravity(true);
            pig.setPos(origin.getX() + 0.5, origin.getY() + 0.1, origin.getZ() + 0.5);
            level.addFreshEntity(pig);
            pig.baseTick();
            pig.baseTick();
            Class<?> access = Class.forName("io.github.apace100.apoli.access.SubmergableEntity");
            Method height = access.getMethod("getFluidHeightLoosely", TagKey.class);
            Method submerged = access.getMethod("isSubmergedInLoosely", TagKey.class);
            TagKey<?> alias = TagKey.create(Registries.FLUID, ResourceLocation.parse("compatibility_smoke:water_alias"));
            check(pig.getFluidHeight(FluidTags.WATER) > 0, "Native water detection");
            check((double)height.invoke(pig, FluidTags.WATER) > 0, "Apoli water height under NeoForge");
            check((boolean)submerged.invoke(pig, FluidTags.WATER), "Apoli submerged eyes under NeoForge");
            check((double)height.invoke(pig, alias) > 0, "Custom water tag height");
            check((boolean)submerged.invoke(pig, alias), "Custom water tag eyes");
            check((double)height.invoke(pig, FluidTags.LAVA) == 0, "Water is not lava");

            Class<?> dataTypes = Class.forName("io.github.apace100.calio.data.SerializableDataTypes");
            Object stackType = dataTypes.getField("ITEM_STACK").get(null);
            Method readStack = Class.forName("io.github.apace100.calio.data.SerializableDataType")
                .getMethod("read", com.google.gson.JsonElement.class, net.minecraft.core.HolderLookup.Provider.class);
            for (String field : new String[]{"item", "id"}) {
                net.minecraft.world.item.ItemStack empty = (net.minecraft.world.item.ItemStack)readStack.invoke(stackType,
                    com.google.gson.JsonParser.parseString("{\"" + field + "\":\"minecraft:air\",\"count\":1}"), level.registryAccess());
                check(empty.isEmpty(), "Virtual totem accepts empty " + field + " stack");
            }

            // Eyes and a shallow fluid occupy the same block; eyes are above it.
            BlockPos shallow = origin.above(5);
            level.setBlockAndUpdate(shallow, Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 7));
            pig.setPos(shallow.getX() + 0.5, shallow.getY() + 0.05, shallow.getZ() + 0.5);
            pig.baseTick(); pig.baseTick();
            check(BlockPos.containing(pig.getX(), pig.getEyeY(), pig.getZ()).equals(shallow), "Eye position shares the shallow water block");
            check(level.getFluidState(shallow).is(FluidTags.WATER), "Shallow water remains in the eye block");
            check(!(boolean)submerged.invoke(pig, FluidTags.WATER), "Eyes above water surface");
            check(!(boolean)submerged.invoke(pig, alias), "Custom tag eyes above surface");
            level.setBlockAndUpdate(shallow, Blocks.AIR.defaultBlockState());

            // Clear water before placing lava to avoid making obsidian.
            for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++)
                for (int y = 0; y <= 2; y++) level.setBlockAndUpdate(origin.offset(x, y, z), Blocks.AIR.defaultBlockState());
            for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++)
                for (int y = 0; y <= 2; y++) level.setBlockAndUpdate(origin.offset(x, y, z), Blocks.LAVA.defaultBlockState());
            pig.setPos(origin.getX() + 0.5, origin.getY() + 0.1, origin.getZ() + 0.5);
            pig.baseTick(); pig.baseTick();
            check((double)height.invoke(pig, FluidTags.LAVA) > 0, "Apoli lava height under NeoForge");
            check((boolean)submerged.invoke(pig, FluidTags.LAVA), "Apoli lava eyes under NeoForge");
            check((double)height.invoke(pig, FluidTags.WATER) == 0, "Lava is not water");
            check((double)height.invoke(pig, alias) == 0, "Custom water tag excludes lava");

            for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++)
                for (int y = 0; y <= 2; y++) level.setBlockAndUpdate(origin.offset(x, y, z), Blocks.AIR.defaultBlockState());
            pig.setPos(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5);
            pig.baseTick(); pig.baseTick();
            check((double)height.invoke(pig, FluidTags.WATER) == 0, "Dry entity water height");
            check(!(boolean)submerged.invoke(pig, FluidTags.WATER), "Dry entity eyes");

            Class<?> apoliRegistries = Class.forName("io.github.apace100.apoli.registry.ApoliRegistries");
            net.minecraft.core.Registry<?> conditions = (net.minecraft.core.Registry<?>)apoliRegistries.getField("ENTITY_CONDITION").get(null);
            Object pitchFactory = conditions.get(ResourceLocation.parse("apoli:pitch"));
            Class<?> factoryClass = Class.forName("io.github.apace100.apoli.power.factory.condition.ConditionFactory");
            Object pitch = factoryClass.getMethod("read", com.google.gson.JsonObject.class, net.minecraft.core.HolderLookup.Provider.class)
                .invoke(pitchFactory, com.google.gson.JsonParser.parseString("{\"comparison\":\"<=\",\"compare_to\":-30.0}").getAsJsonObject(), level.registryAccess());
            java.util.function.Predicate<Object> pitchTest = (java.util.function.Predicate<Object>)pitch;
            pig.setXRot(-45);
            check(pitchTest.test(pig), "Pitch enables jump when looking up");
            pig.setXRot(0);
            check(!pitchTest.test(pig), "Pitch disables jump at a level gaze");
            pig.setXRot(-30);
            check(pitchTest.test(pig), "Pitch comparison includes its boundary");
            pig.setXRot(30);
            check(!pitchTest.test(pig), "Pitch disables jump when looking down");

            Class<?> componentClass = Class.forName("io.github.apace100.apoli.component.PowerHolderComponent");
            Object key = componentClass.getField("KEY").get(null);
            Method getComponent = java.util.Arrays.stream(key.getClass().getMethods())
                .filter(m -> m.getName().equals("get") && m.getParameterCount() == 1).findFirst().orElseThrow();
            Object component = getComponent.invoke(key, pig);
            Class<?> powerClass = Class.forName("io.github.apace100.apoli.power.PowerType");
            Method add = componentClass.getMethod("addPower", powerClass, ResourceLocation.class);
            Method remove = componentClass.getMethod("removePower", powerClass, ResourceLocation.class);
            ResourceLocation source = ResourceLocation.parse("compatibility_smoke:test");
            double baseline = travel(pig, origin);
            Object allay = powers.get(ResourceLocation.parse("shape-shifter-curse:form_allay_sp_slipperiness"));
            add.invoke(component, allay, source);
            double allayMotion = travel(pig, origin);
            check(Math.abs(allayMotion / baseline - 1.3) < 0.002, "Allay friction hook: " + allayMotion + " / " + baseline);
            remove.invoke(component, allay, source);
            Object axolotl = powers.get(ResourceLocation.parse("shape-shifter-curse:form_axolotl_3_slipperiness"));
            add.invoke(component, axolotl, source);
            pig.setSprinting(true);
            double axolotlMotion = travel(pig, origin);
            check(Math.abs(axolotlMotion / baseline - (0.95 / 0.6)) < 0.002, "Axolotl friction hook: " + axolotlMotion + " / " + baseline);
            pig.setSprinting(false);
            check(Math.abs(travel(pig, origin) - baseline) < 0.002, "Axolotl friction condition disabled");
            remove.invoke(component, axolotl, source);
            Object jump = clientPowers.get(ResourceLocation.parse("shape-shifter-curse:high_jump_2"));
            Object falling = powers.get(ResourceLocation.parse("shape-shifter-curse:slow_falling"));
            add.invoke(component, jump, source);
            pig.setNoGravity(false);
            pig.setOnGround(true);
            pig.setDeltaMovement(Vec3.ZERO);
            pig.jumpFromGround();
            System.out.println("BAT2_JUMP_PROBE: " + pig.getDeltaMovement().y);
            check(Math.abs(pig.getDeltaMovement().y - 0.62) < 0.00001, "Original bat 2 jump impulse (+0.2)");
            add.invoke(component, falling, source);
            pig.setOnGround(false);
            pig.travel(Vec3.ZERO);
            System.out.println("BAT2_ASCENT_PROBE: " + pig.getDeltaMovement().y);
            check(Math.abs(pig.getDeltaMovement().y - (0.62 - 0.08) * 0.98) < 0.00001, "Bat 2 ascent preserves ordinary gravity");
            pig.setDeltaMovement(0, -0.2, 0);
            pig.travel(Vec3.ZERO);
            System.out.println("BAT2_FALL_PROBE: " + pig.getDeltaMovement().y);
            check(Math.abs(pig.getDeltaMovement().y - (-0.2 - 0.01) * 0.98) < 0.00001, "Bat 2 slow falling gravity matches original");
            ProbePlayer player = new ProbePlayer(level);
            player.setPos(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5);
            player.setOnGround(true);
            player.setSprinting(false);
            Object playerPowers = getComponent.invoke(key, player);
            com.google.gson.JsonObject batOrigin = com.google.gson.JsonParser.parseString(new String(
                getClass().getResourceAsStream("/bat2.json").readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
            for (com.google.gson.JsonElement id : batOrigin.getAsJsonArray("powers")) {
                Object type = powers.get(ResourceLocation.parse(id.getAsString()));
                add.invoke(playerPowers, type, source);
            }
            Class<?> formsClass = Class.forName("net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms");
            Class<?> formClass = Class.forName("net.onixary.shapeShifterCurseFabric.player_form.IForm");
            Object bat2 = formsClass.getField("BAT_2").get(null);
            formClass.getMethod("applyScale", Player.class).invoke(bat2, player);
            Class<?> formComponentClass = Class.forName("net.onixary.shapeShifterCurseFabric.player_form.utils.PlayerFormComponent");
            Object formKey = formComponentClass.getField("COMPONENT").get(null);
            Object formComponent = getComponent.invoke(formKey, player);
            formComponentClass.getField("nowForm").set(formComponent, bat2);
            formComponentClass.getField("nowFormID").set(formComponent, ResourceLocation.parse("shape-shifter-curse:bat_2"));
            player.setDeltaMovement(Vec3.ZERO);
            System.out.println("BAT2_ALTERNATE_GETTER: " + player.inspectJumpPower());
            check(Math.abs(player.inspectJumpPower() - 0.62) < 0.00001, "Alternate jump implementation receives bat 2 modifier");
            player.jumpFromGround();
            System.out.println("BAT2_PLAYER_JUMP: " + player.getDeltaMovement().y);
            double maxHeight = 0;
            for (int tick = 0; tick < 20; tick++) {
                player.travel(Vec3.ZERO);
                maxHeight = Math.max(maxHeight, player.getY() - origin.getY());
            }
            System.out.println("BAT2_PLAYER_PEAK: " + maxHeight);
            check(maxHeight > 2.3, "Full bat 2 player reaches original jump height");
            player.orientedJump = true;
            player.setOnGround(true);
            player.setDeltaMovement(Vec3.ZERO);
            player.jumpFromGround();
            System.out.println("BAT2_SABLE_JUMP: " + player.getDeltaMovement().y);
            check(Math.abs(player.getDeltaMovement().y - 0.62) < 0.00001, "Sable's actual oriented jump applies the bat modifier");
            player.orientedJump = false;

            Object triple = powers.get(ResourceLocation.parse("shape-shifter-curse:form_snow_fox_2_triple_jump"));
            ProbePlayer fox = new ProbePlayer(level);
            Object foxPowers = getComponent.invoke(key, fox);
            add.invoke(foxPowers, triple, source);
            fox.setPos(origin.getX() + 0.5, origin.getY() + 3, origin.getZ() + 0.5);
            fox.setSprinting(true);
            fox.setYRot(0);
            fox.orientedJump = true;
            double[] multipliers = {1.4, 1.5, 1.65};
            for (double multiplier : multipliers) {
                fox.setOnGround(true);
                fox.setDeltaMovement(Vec3.ZERO);
                fox.jumpFromGround();
                check(Math.abs(fox.getDeltaMovement().y - 0.42 * multiplier) < 0.00001, "Triple jump applies once: " + multiplier);
                check(Math.abs(fox.getDeltaMovement().z - 0.2) < 0.00001, "Triple jump keeps original sprint momentum");
            }
            fox.setOnGround(false);
            fox.setDeltaMovement(0.1, 0.1, 0);
            fox.travel(Vec3.ZERO);
            check(Math.abs(fox.getDeltaMovement().x - 0.091) < 0.00001, "Triple jump preserves original air drag");
            remove.invoke(foxPowers, triple, source);

            for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++)
                for (int y = 0; y <= 2; y++) level.setBlockAndUpdate(origin.offset(x, y, z), Blocks.WATER.defaultBlockState());
            ProbePlayer swimmer = new ProbePlayer(level);
            Object swimmerPowers = getComponent.invoke(key, swimmer);
            swimmer.setNoGravity(true);
            swimmer.setPos(origin.getX() + 0.5, origin.getY() + 0.1, origin.getZ() + 0.5);
            swimmer.baseTick(); swimmer.baseTick();
            Object flex = powers.get(ResourceLocation.parse("shape-shifter-curse:form_axolotl_2_new_water_flexibility"));
            add.invoke(swimmerPowers, flex, source);
            swimmer.setDeltaMovement(0.2, 0.1, 0.2);
            swimmer.travel(Vec3.ZERO);
            double drag = 0.8 + (0.98 - 0.8) * 0.93;
            check(Math.abs(swimmer.getDeltaMovement().x - 0.2 * drag) < 0.00001, "Axolotl flexibility preserves original horizontal drag");
            check(Math.abs(swimmer.getDeltaMovement().y - 0.08) < 0.00001, "Axolotl flexibility leaves vertical drag unchanged");
            remove.invoke(swimmerPowers, flex, source);
            Object swimSpeed = powers.get(ResourceLocation.parse("shape-shifter-curse:form_axolotl_0_swim_speed"));
            add.invoke(swimmerPowers, swimSpeed, source);
            swimmer.setDeltaMovement(Vec3.ZERO);
            swimmer.travel(new Vec3(0, 0, 1));
            check(Math.abs(swimmer.getDeltaMovement().z - 0.02 * 2.2 * 0.8) < 0.00001, "Legacy water-speed bonus 1.2 produces a 2.2 factor");
            remove.invoke(swimmerPowers, swimSpeed, source);

            Object dash = powers.get(ResourceLocation.parse("shape-shifter-curse:form_axolotl_2_water_spurt"));
            add.invoke(swimmerPowers, dash, source);
            Object dashPower = componentClass.getMethod("getPower", powerClass).invoke(swimmerPowers, dash);
            dashPower.getClass().getMethod("setCooldown", int.class).invoke(dashPower, 100);
            check((boolean)dashPower.getClass().getMethod("canUse").invoke(dashPower), "Axolotl dash is ready while fully submerged");
            swimmer.setDeltaMovement(Vec3.ZERO);
            swimmer.setXRot(-45);
            swimmer.setYRot(0);
            dashPower.getClass().getMethod("onUse").invoke(dashPower);
            System.out.println("AXOLOTL_DASH: " + swimmer.getDeltaMovement());
            check(Math.abs(swimmer.getDeltaMovement().y - 1.6 / Math.sqrt(2)) < 0.00001, "Axolotl dash follows upward gaze");
            check(Math.abs(swimmer.getDeltaMovement().z - 1.6 / Math.sqrt(2)) < 0.00001, "Axolotl dash preserves local-space speed");

            for (com.google.gson.JsonElement element : com.google.gson.JsonParser.parseString(new String(
                    getClass().getResourceAsStream("/original-scales.json").readAllBytes(), StandardCharsets.UTF_8)).getAsJsonArray()) {
                com.google.gson.JsonObject expected = element.getAsJsonObject();
                Object form = formsClass.getField(expected.get("field").getAsString()).get(null);
                formClass.getMethod("applyScale", Player.class).invoke(form, player);
                for (String type : new String[]{"WIDTH", "HEIGHT", "EYE_HEIGHT", "HITBOX_HEIGHT"}) {
                    Class<?> scaleTypes = Class.forName("virtuoel.pehkui.api.ScaleTypes");
                    Object scaleType = scaleTypes.getField(type).get(null);
                    Object scaleData = scaleType.getClass().getMethod("getScaleData", Entity.class).invoke(scaleType, player);
                    // EYE/HITBOX inherit HEIGHT. Compare the explicit bases set
                    // by the original ScalePower, before Pehkui composes them.
                    float value = (float)scaleData.getClass().getMethod("getBaseScale").invoke(scaleData);
                    double target = expected.get(type.equals("WIDTH") || type.equals("HEIGHT") ? "scale" : "eye").getAsDouble();
                    check(Math.abs(value - target) < 0.00001, "Original " + expected.get("field") + " " + type + " scale");
                }
            }
            for (String mob : new String[]{"spider", "wolf"}) {
                String json = new String(getClass().getResourceAsStream("/loot-" + mob + ".json").readAllBytes(), StandardCharsets.UTF_8);
                com.google.gson.JsonElement conditionJson = com.google.gson.JsonParser.parseString(json).getAsJsonObject()
                    .getAsJsonArray("pools").get(0).getAsJsonObject().getAsJsonArray("conditions").get(0);
                net.minecraft.world.level.storage.loot.predicates.LootItemCondition decoded =
                    net.minecraft.world.level.storage.loot.predicates.LootItemCondition.DIRECT_CODEC.parse(
                        net.minecraft.resources.RegistryOps.create(com.mojang.serialization.JsonOps.INSTANCE, level.registryAccess()), conditionJson).getOrThrow();
                check(decoded instanceof net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition,
                    "Original " + mob + " loot keeps enchantment-aware chance");
                var chance = (net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition)decoded;
                double base = mob.equals("spider") ? 0.5 : 0.3;
                check(Math.abs(chance.unenchantedChance() - base) < 0.00001, "Original unenchanted " + mob + " drop chance");
                for (int enchantmentLevel = 1; enchantmentLevel <= 3; enchantmentLevel++) {
                    check(Math.abs(chance.enchantedChance().calculate(enchantmentLevel) - (base + 0.1 * enchantmentLevel)) < 0.00001,
                        "Original " + mob + " Looting " + enchantmentLevel + " drop chance");
                }
            }
            // Regression: an initial CCA packet arriving before the power list.
            Class<?> impl = Class.forName("io.github.apace100.apoli.component.PowerHolderComponentImpl");
            Object queued = impl.getConstructor(net.minecraft.world.entity.LivingEntity.class).newInstance(swimmer);
            net.minecraft.nbt.CompoundTag snapshot = new net.minecraft.nbt.CompoundTag();
            componentClass.getMethod("writeToNbt", net.minecraft.nbt.CompoundTag.class, net.minecraft.core.HolderLookup.Provider.class)
                    .invoke(swimmerPowers, snapshot, level.registryAccess());
            Map registry = (Map)powers;
            Object heldDefinition = registry.remove(ResourceLocation.parse("shape-shifter-curse:form_axolotl_2_water_spurt"));
            try {
                impl.getMethod("applySyncedData", net.minecraft.nbt.CompoundTag.class, net.minecraft.core.HolderLookup.Provider.class)
                        .invoke(queued, snapshot, level.registryAccess());
                check(((java.util.List)componentClass.getMethod("getPowers").invoke(queued)).isEmpty(), "Early power data waits for definitions");
            } finally {
                registry.put(ResourceLocation.parse("shape-shifter-curse:form_axolotl_2_water_spurt"), heldDefinition);
            }
            impl.getMethod("clientTick").invoke(queued);
            check((boolean)componentClass.getMethod("hasPower", powerClass).invoke(queued, dash), "Cold connection retries power data after definitions arrive");
            net.minecraft.nbt.CompoundTag emptySnapshot = new net.minecraft.nbt.CompoundTag();
            emptySnapshot.put("Powers", new net.minecraft.nbt.ListTag());
            impl.getMethod("applySyncedData", net.minecraft.nbt.CompoundTag.class, net.minecraft.core.HolderLookup.Provider.class)
                    .invoke(queued, emptySnapshot, level.registryAccess());
            check(((java.util.List)componentClass.getMethod("getPowersFromSource", ResourceLocation.class).invoke(queued, source)).isEmpty(), "Replacing powers removes stale sources");

            ProbePlayer ax = new ProbePlayer(level);
            Object axPowers = getComponent.invoke(key, ax);
            Object ax3 = formsClass.getField("AXOLOTL_3").get(null);
            formClass.getMethod("applyScale", Player.class).invoke(ax3, ax);
            formComponentClass.getField("nowForm").set(getComponent.invoke(formKey, ax), ax3);
            ax.setPos(origin.getX() + 0.725, origin.getY(), origin.getZ() + 0.5);
            for (int y = 0; y < 3; y++) {
                level.setBlockAndUpdate(origin.offset(0, y, 0), Blocks.AIR.defaultBlockState());
                level.setBlockAndUpdate(origin.offset(1, y, 0), Blocks.STONE.defaultBlockState());
            }
            Object crawlPower = powers.get(ResourceLocation.parse("shape-shifter-curse:form_axolotl_keep_sneaking_when_head_collide"));
            add.invoke(axPowers, crawlPower, source);
            Object crawlInstance = componentClass.getMethod("getPower", powerClass).invoke(axPowers, crawlPower);
            Method active = Class.forName("io.github.apace100.apoli.power.Power").getMethod("isActive");
            check(!(boolean)active.invoke(crawlInstance), "Scaled axolotl next to a wall is not forced to crawl");
            level.setBlockAndUpdate(origin.offset(0, 1, 0), Blocks.STONE.defaultBlockState());
            check((boolean)active.invoke(crawlInstance), "Low ceiling still forces crawling");
            level.setBlockAndUpdate(origin.offset(0, 1, 0), Blocks.AIR.defaultBlockState());
            remove.invoke(axPowers, crawlPower, source);
            Object waterBreath = powers.get(ResourceLocation.parse("shape-shifter-curse:form_axolotl_3_water_breathing"));
            if (waterBreath == null) throw new AssertionError("Axolotl moisture power id changed");
            add.invoke(axPowers, waterBreath, source);
            ax.setAirSupply(120);
            for (int i = 0; i < 20; i++) ax.tick();
            check(ax.getAirSupply() == 120, "Axolotl 3 moisture remains stable on dry land");

            Object groundJump = powers.get(ResourceLocation.parse("shape-shifter-curse:form_axolotl_3_sprinting_jump"));
            add.invoke(axPowers, groundJump, source);
            ax.setPos(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5);
            ax.baseTick(); ax.setSprinting(true); ax.setOnGround(false);
            ax.setDeltaMovement(0, 0.462, 0.2);
            Class.forName("net.onixary.shapeShifterCurseFabric.additional_power.JumpEventCondition")
                    .getMethod("setJumping", Player.class, boolean.class).invoke(null, ax, true);
            Object jumpInstance = componentClass.getMethod("getPower", powerClass).invoke(axPowers, groundJump);
            jumpInstance.getClass().getMethod("executeAction").invoke(jumpInstance);
            check(Math.abs(ax.getDeltaMovement().y - 0.462) < 0.00001, "Late server jump action preserves vertical velocity");
            check(ax.getAirSupply() == 117, "Late server jump action consumes moisture even after leaving ground");
            Object attackType = powers.get(ResourceLocation.parse("shape-shifter-curse:form_axolotl_3_sprinting_attack"));
            add.invoke(axPowers, attackType, source);
            Object attackPower = componentClass.getMethod("getPower", powerClass).invoke(axPowers, attackType);
            Method hit = attackPower.getClass().getMethod("onHit", Entity.class, net.minecraft.world.damagesource.DamageSource.class, float.class);
            // Keep actor and target apart: a zero direction vector has no defined knockback direction.
            pig.setPos(ax.getX(), ax.getY(), ax.getZ() + 1);
            ax.setOnGround(true); ax.setSprinting(false); pig.setDeltaMovement(Vec3.ZERO);
            hit.invoke(attackPower, pig, ax.damageSources().playerAttack(ax), 1.0f);
            check(pig.getDeltaMovement().lengthSqr() == 0, "Walking barehand attack has no axolotl impulse");
            ax.setSprinting(true);
            hit.invoke(attackPower, pig, ax.damageSources().playerAttack(ax), 1.0f);
            check(Math.abs(pig.getDeltaMovement().length() - 4.0) < 0.00001, "Sprinting attack preserves original target knockback");

            Object exitType = powers.get(ResourceLocation.parse("shape-shifter-curse:jump_out_water"));
            add.invoke(axPowers, exitType, source);
            Object exitPower = componentClass.getMethod("getPower", powerClass).invoke(axPowers, exitType);
            level.setBlockAndUpdate(origin, Blocks.WATER.defaultBlockState());
            ax.setPos(origin.getX() + 0.5, origin.getY() + 0.8, origin.getZ() + 0.5);
            ax.baseTick(); ax.baseTick(); ax.setOnGround(false); ax.yo = ax.getY();
            ax.setDeltaMovement(0, 0.1, 0); ax.setXRot(0);
            check(!(boolean)active.invoke(exitPower), "Water exit boost requires looking up");
            ax.setXRot(-45); ax.setDeltaMovement(0, -0.1, 0);
            check(!(boolean)active.invoke(exitPower), "Falling into water never triggers an exit boost");
            ax.setDeltaMovement(0, 0.1, 0); ax.setOnGround(true);
            check(!(boolean)active.invoke(exitPower), "Walking out of shallow water never triggers an exit boost");
            ax.setOnGround(false);
            check((boolean)active.invoke(exitPower), "Rising out of water while looking up enables the boost");
            level.setBlockAndUpdate(origin, Blocks.AIR.defaultBlockState());
            Object burstType = powers.get(ResourceLocation.parse("shape-shifter-curse:form_axolotl_3_sprinting_sneaking_water_explode"));
            add.invoke(axPowers, burstType, source);
            Object burst = componentClass.getMethod("getPower", powerClass).invoke(axPowers, burstType);
            Class<?> activeBurst = Class.forName("io.github.apace100.apoli.power.ActiveCooldownPower");
            Class<?> sneakAction = Class.forName("net.onixary.shapeShifterCurseFabric.additional_power.ActionOnSprintingToSneakingPower");
            check(activeBurst.isInstance(burst), "Axolotl explosion is an active ability");
            check(!sneakAction.isInstance(burst), "Shift event cannot execute the explosion");
            check(((java.util.List)componentClass.getMethod("getPowers", Class.class).invoke(axPowers, sneakAction)).isEmpty(), "Axolotl has no sprint-to-sneak explosion handler");
            Object burstKey = activeBurst.getMethod("getKey").invoke(burst);
            check(burstKey.getClass().getField("key").get(burstKey).equals("key.shape-shifter-curse.active_skill_1"), "Explosion uses active ability 1");
            check(!(boolean)burstKey.getClass().getField("continuous").get(burstKey), "Holding the active key does not repeat the explosion");
            ax.setPos(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5);
            ax.baseTick(); ax.setOnGround(true); ax.setSprinting(false); ax.setAirSupply(120); ax.setDeltaMovement(Vec3.ZERO);
            check((boolean)active.invoke(burst), "Button ability works without sprinting or crouching");
            activeBurst.getMethod("onUse").invoke(burst);
            check(ax.getAirSupply() == 105, "Active explosion keeps its original moisture cost");
            check(Math.abs(ax.getDeltaMovement().y - 0.6) < 0.00001, "Active explosion keeps its original launch impulse");
            ax.setAirSupply(0);
            check(!(boolean)active.invoke(burst), "Active explosion still requires moisture");
            ProbePlayer vertical = new ProbePlayer(level);
            Object verticalPowers = getComponent.invoke(key, vertical);
            for (int y = 0; y < 4; y++) level.setBlockAndUpdate(origin.above(y), Blocks.WATER.defaultBlockState());
            vertical.setNoGravity(true);
            vertical.setPos(origin.getX() + 0.5, origin.getY() + 1.5, origin.getZ() + 0.5);
            vertical.baseTick(); vertical.baseTick();
            vertical.setDeltaMovement(Vec3.ZERO);
            vertical.jumpInFluid(net.neoforged.neoforge.common.NeoForgeMod.WATER_TYPE.value());
            check(Math.abs(vertical.getDeltaMovement().y - 0.04) < 0.000001, "Human water rise stays vanilla");
            vertical.setDeltaMovement(Vec3.ZERO);
            vertical.sinkInFluid(net.neoforged.neoforge.common.NeoForgeMod.WATER_TYPE.value());
            check(Math.abs(vertical.getDeltaMovement().y + 0.04) < 0.000001, "Human water descent stays vanilla");
            Object axSpeed = powers.get(ResourceLocation.parse("shape-shifter-curse:form_axolotl_1_swim_speed"));
            add.invoke(verticalPowers, axSpeed, source);
            vertical.setDeltaMovement(0.2, 0, 0.3);
            vertical.jumpInFluid(net.neoforged.neoforge.common.NeoForgeMod.WATER_TYPE.value());
            check(Math.abs(vertical.getDeltaMovement().y - 0.10) < 0.000001, "NeoForge water rise uses axolotl speed multiplier");
            check(Math.abs(vertical.getDeltaMovement().x - 0.2) < 0.000001 && Math.abs(vertical.getDeltaMovement().z - 0.3) < 0.000001, "Vertical input preserves horizontal velocity");
            vertical.setDeltaMovement(Vec3.ZERO);
            vertical.sinkInFluid(net.neoforged.neoforge.common.NeoForgeMod.WATER_TYPE.value());
            check(Math.abs(vertical.getDeltaMovement().y + 0.10) < 0.000001, "NeoForge water descent uses axolotl speed multiplier");
            vertical.setDeltaMovement(Vec3.ZERO);
            vertical.jumpInFluid(net.neoforged.neoforge.common.NeoForgeMod.LAVA_TYPE.value());
            check(Math.abs(vertical.getDeltaMovement().y - 0.04) < 0.000001, "Axolotl water bonus never applies to lava input");
            for (boolean rising : new boolean[]{true, false}) {
                vertical.setDeltaMovement(Vec3.ZERO);
                double distance = 0;
                for (int tick = 0; tick < 80; tick++) {
                    vertical.setPos(origin.getX() + 0.5, origin.getY() + 1.5, origin.getZ() + 0.5);
                    vertical.baseTick();
                    if (rising) vertical.jumpInFluid(net.neoforged.neoforge.common.NeoForgeMod.WATER_TYPE.value());
                    else vertical.sinkInFluid(net.neoforged.neoforge.common.NeoForgeMod.WATER_TYPE.value());
                    double previousY = vertical.getY();
                    vertical.travel(Vec3.ZERO);
                    if (tick >= 60) distance += Math.abs(vertical.getY() - previousY);
                }
                System.out.println("AXOLOTL_VERTICAL_" + (rising ? "UP" : "DOWN") + "_BLOCKS_PER_SECOND: " + distance);
                check(Math.abs(distance - 10.0) < 0.02, "Axolotl vertical cruise reaches 10 blocks/s: " + distance);
            }
            remove.invoke(verticalPowers, axSpeed, source);
            vertical.setDeltaMovement(Vec3.ZERO);
            vertical.jumpInFluid(net.neoforged.neoforge.common.NeoForgeMod.WATER_TYPE.value());
            check(Math.abs(vertical.getDeltaMovement().y - 0.04) < 0.000001, "Removing form removes vertical bonus");
            ProbePlayer airFox = new ProbePlayer(level);
            Object airFoxPowers = getComponent.invoke(key, airFox);
            Object airSpeedType = powers.get(ResourceLocation.parse("shape-shifter-curse:form_snow_fox_3_air_speed"));
            airFox.setNoGravity(true);
            airFox.setPos(origin.getX() + 0.5, origin.getY() + 10, origin.getZ() + 0.5);
            airFox.baseTick(); airFox.baseTick(); airFox.setOnGround(false); airFox.setSprinting(false);
            check(Math.abs(airFox.inspectAirSpeed() - 0.02) < 0.000001, "Human airborne acceleration stays vanilla");
            airFox.setDeltaMovement(Vec3.ZERO); airFox.travel(new Vec3(0, 0, 1));
            double humanAirMotion = airFox.getDeltaMovement().z;
            add.invoke(airFoxPowers, airSpeedType, source);
            check(Math.abs(airFox.inspectAirSpeed() - 0.02 * 2.25) < 0.000001, "Polar fox player override applies air speed exactly once");
            airFox.setDeltaMovement(Vec3.ZERO); airFox.travel(new Vec3(0, 0, 1));
            check(Math.abs(airFox.getDeltaMovement().z / humanAirMotion - 2.25) < 0.00001, "Actual airborne movement gains original polar fox acceleration");
            airFox.setSprinting(true);
            check(Math.abs(airFox.inspectAirSpeed() - 0.026 * 2.25) < 0.000001, "Polar fox sprint acceleration applies air modifier once");
            airFox.getAbilities().flying = true;
            check(Math.abs(airFox.inspectAirSpeed() - airFox.getAbilities().getFlyingSpeed() * 2 * 2.25) < 0.000001, "Creative flying speed applies air modifier once");
            airFox.getAbilities().flying = false;
            remove.invoke(airFoxPowers, airSpeedType, source);
            check(Math.abs(airFox.inspectAirSpeed() - 0.026) < 0.000001, "Leaving polar fox form removes airborne bonus");
            // Server jump cosmetics must never publish stale horizontal player motion.
            for (int stage : new int[]{2, 3}) {
                ProbePlayer momentumFox = new ProbePlayer(level);
                Object momentumPowers = getComponent.invoke(key, momentumFox);
                Object comboType = powers.get(ResourceLocation.parse("shape-shifter-curse:form_snow_fox_" + stage + "_triple_jump"));
                add.invoke(momentumPowers, comboType, source);
                momentumFox.setPos(origin.getX() + 0.5, origin.getY() + 15, origin.getZ() + 0.5);
                momentumFox.setSprinting(true);
                for (int jumpIndex = 1; jumpIndex <= 3; jumpIndex++) {
                    momentumFox.setOnGround(true);
                    momentumFox.setDeltaMovement(0.25, 0, 0.5);
                    momentumFox.hurtMarked = false;
                    momentumFox.setYRot(0);
                    momentumFox.jumpFromGround();
                    check(!momentumFox.hurtMarked, "Fox " + stage + " jump " + jumpIndex + " cannot publish a stale velocity packet");
                    check(Math.abs(momentumFox.getDeltaMovement().x - 0.25) < 0.000001, "Fox combo preserves lateral momentum");
                    check(Math.abs(momentumFox.getDeltaMovement().z - 0.7) < 0.000001, "Fox combo preserves and adds sprint momentum");
                }
            }

            CombatProbePlayer attacker = new CombatProbePlayer(level);
            attacker.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).setBaseValue(4);
            attacker.setPos(origin.getX() + 0.5, origin.getY() + 15, origin.getZ() + 0.5);
            attacker.baseTick(); attacker.baseTick();
            Object combatPowers = getComponent.invoke(key, attacker);
            double groundedDamage = strike(attacker, level, true, 0);
            check(Math.abs(groundedDamage - 4) < 0.001 && attacker.crits == 0, "Normal attack has vanilla damage and no crit");
            double vanillaCrit = strike(attacker, level, false, 2);
            check(Math.abs(vanillaCrit - 6) < 0.001 && attacker.crits == 1, "Vanilla falling critical deals 1.5x damage");
            Object enhancedType = powers.get(ResourceLocation.parse("shape-shifter-curse:form_snow_fox_3_enhanced_falling_attack"));
            add.invoke(combatPowers, enhancedType, source);
            double enhancedCrit = strike(attacker, level, false, 2);
            check(Math.abs(enhancedCrit - 12) < 0.001, "Polar fox falling crit gains original 2x multiplier");
            check(attacker.fallDistance == 0, "Successful fox crit executes fall-distance reset");
            check(Math.abs(strike(attacker, level, false, 1.5f) - 9) < 0.001, "Fox crit interpolates falling damage");
            check(Math.abs(strike(attacker, level, false, 0.5f) - 6) < 0.001, "Short falling crit keeps vanilla base multiplier");
            check(Math.abs(strike(attacker, level, true, 2) - 4) < 0.001 && attacker.fallDistance == 2, "Grounded attack never runs crit-only actions");
            remove.invoke(combatPowers, enhancedType, source);
            Object critType = powers.get(ResourceLocation.parse("shape-shifter-curse:form_snow_fox_1_critical_attack_up"));
            add.invoke(combatPowers, critType, source);
            check(Math.abs(strike(attacker, level, false, 2) - 7.8) < 0.001, "Fox 1 critical modifier uses original 1.3 bonus");
            check(Math.abs(strike(attacker, level, true, 0) - 4) < 0.001, "Critical bonus never boosts grounded attacks");
            remove.invoke(combatPowers, critType, source);
            System.out.println("COMPATIBILITY_SMOKE_PASSED: " + assertions + " checks; registered powers=" + powers.size());
        } catch (Throwable error) {
            System.out.println("COMPATIBILITY_SMOKE_FAILED");
            error.printStackTrace();
        } finally {
            if (pig != null) pig.discard();
            event.getServer().halt(false);
        }
    }

    private double travel(Pig pig, BlockPos origin) {
        pig.setPos(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5);
        pig.setOnGround(true);
        pig.setDeltaMovement(new Vec3(0.1, 0, 0));
        pig.travel(Vec3.ZERO);
        return pig.getDeltaMovement().x;
    }

    private double strike(CombatProbePlayer attacker, ServerLevel level, boolean grounded, float fall) {
        Pig victim = EntityType.PIG.create(level);
        victim.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100);
        victim.setHealth(100);
        victim.setPos(attacker.getX(), attacker.getY(), attacker.getZ() + 1);
        level.addFreshEntity(victim);
        attacker.setOnGround(grounded);
        attacker.setSprinting(false);
        attacker.fallDistance = fall;
        attacker.setDeltaMovement(0, -0.1, 0);
        attacker.attack(victim);
        double damage = 100 - victim.getHealth();
        victim.discard();
        return damage;
    }

    public static class CombatProbePlayer extends ProbePlayer {
        public int crits;
        CombatProbePlayer(ServerLevel level) {
            super(level);
            try {
                // FakePlayer has no socket; CCA's optional payload check still needs a Netty channel.
                var channel = net.minecraft.network.Connection.class.getDeclaredField("channel");
                channel.setAccessible(true);
                channel.set(connection.getConnection(), new io.netty.channel.embedded.EmbeddedChannel());
            } catch (ReflectiveOperationException error) { throw new RuntimeException(error); }
        }
        @Override public float getAttackStrengthScale(float partialTick) { return 1.0f; }
        @Override public void crit(Entity target) { crits++; }
    }

    public static class ProbePlayer extends FakePlayer {
        public boolean orientedJump;
        ProbePlayer(ServerLevel level) {
            super(level, new GameProfile(UUID.fromString("12d227ac-645f-4e21-bbfe-deeb20e3d401"), "ParityProbe"));
        }

        @Override
        public boolean isControlledByLocalInstance() {
            return true;
        }

        public float inspectAirSpeed() {
            return getFlyingSpeed();
        }

        public float inspectJumpPower() {
            return getJumpPower();
        }
    }
}
