package net.frozenblock.wilderwild.registry;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.frozenblock.wilderwild.WilderWild;
import net.frozenblock.wilderwild.entity.SculkSensorTendrilEntity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RegisterEntities {
    public static final EntityType<SculkSensorTendrilEntity> TENDRIL_ENTITY = Registry.register(Registry.ENTITY_TYPE, new Identifier(WilderWild.MOD_ID, "sculk_sensor_tendril"), FabricEntityTypeBuilder.create(SpawnGroup.MISC, SculkSensorTendrilEntity::new).dimensions(EntityDimensions.fixed(1.0f, 0.0f)).build());


    public static void init() {
        FabricDefaultAttributeRegistry.register(TENDRIL_ENTITY, SculkSensorTendrilEntity.createLivingAttributes());
    }
}