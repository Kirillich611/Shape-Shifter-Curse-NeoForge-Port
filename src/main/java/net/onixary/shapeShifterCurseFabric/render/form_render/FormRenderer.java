package net.onixary.shapeShifterCurseFabric.render.form_render;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_form.skin.RegPlayerSkinComponent;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoObjectRenderer;

public class FormRenderer extends GeoObjectRenderer<FormAnimatable> {
    public FormAnimatable realAnimatable = null;
    public FormModel realModel = null;

    public FormRenderer(JsonObject modelJson) {
        super(new FormModel(modelJson));
        this.realModel = (FormModel) this.model;
        this.realAnimatable = new FormAnimatable();
        this.animatable = this.realAnimatable;
    }

    public void setPlayer(Player player, boolean slim) {
        this.realAnimatable.setPlayer(player);
        this.realModel.setPlayer(player, slim);
    }

}
