package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

public class MustCrawlingCondition {
    private static boolean IsHeadNotCollide(Entity e, float width, float height) {
        if (e.noPhysics || e.isSpectator()) {
            return true;
        }
        AABB body = e.getBoundingBox();
        double halfX = Math.min(width, body.getXsize()) / 2.0;
        double halfZ = Math.min(width, body.getZsize()) / 2.0;
        double centerX = (body.minX + body.maxX) / 2.0;
        double centerZ = (body.minZ + body.maxZ) / 2.0;
        // A scaled form's ceiling probe must not extend into adjacent walls.
        return e.level().noCollision(e, new AABB(centerX - halfX, body.minY,
                centerZ - halfZ, centerX + halfX, body.minY + height,
                centerZ + halfZ).deflate(1.0E-7));
    }

    public static boolean condition(SerializableData.Instance data, Entity e) {
        return !IsHeadNotCollide(e, data.getFloat("width"), data.getFloat("height"));
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
                ShapeShifterCurseFabric.identifier("must_crawling"),
                new SerializableData()
                        .add("width", SerializableDataTypes.FLOAT, 0.6f)
                        .add("height", SerializableDataTypes.FLOAT, 1.5f),
                MustCrawlingCondition::condition
        );
    }
}