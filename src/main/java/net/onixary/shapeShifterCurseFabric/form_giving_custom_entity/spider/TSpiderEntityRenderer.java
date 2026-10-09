package net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.spider;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

@Environment(EnvType.CLIENT)
public class TSpiderEntityRenderer extends MobRenderer<TransformativeSpiderEntity, SpiderModel<TransformativeSpiderEntity>> {
	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/entity/mob/t_spider.png");

	public TSpiderEntityRenderer(EntityRendererProvider.Context context) {
		super(context, new SpiderModel<>(context.bakeLayer(ModelLayers.SPIDER)), 0.8F);
	}

	public @NotNull ResourceLocation getTextureLocation(TransformativeSpiderEntity ocelotEntity) {
		return TEXTURE;
	}

	@Override
	protected void scale(TransformativeSpiderEntity entity, PoseStack matrices, float amount) {
		matrices.scale(0.5f, 0.5f, 0.5f);
	}
}