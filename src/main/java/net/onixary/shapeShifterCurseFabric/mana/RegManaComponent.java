package net.onixary.shapeShifterCurseFabric.mana;

import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

public class RegManaComponent implements EntityComponentInitializer {
    public static final ComponentKey<ManaComponent> MANA = ComponentRegistry.getOrCreate(ShapeShifterCurseFabric.identifier("mana"), ManaComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry entityComponentFactoryRegistry) {
        entityComponentFactoryRegistry.registerForPlayers(
                MANA,
                ManaComponent::new,
                RespawnCopyStrategy.ALWAYS_COPY
        );
    }
}
