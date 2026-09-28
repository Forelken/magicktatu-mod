package com.runetouched.data;

import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * DeferredRegister for entity types. Currently holds the boss: The Rune Herald.
 */
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Runes.MODID);

    public static final RegistryObject<EntityType<com.runetouched.boss.RuneHeraldEntity>> RUNE_HERALD =
            ENTITY_TYPES.register("rune_herald",
                    () -> EntityType.Builder
                            .of(RuneHeraldEntity::new, EntityType.MobCategory.MONSTER)
                            .sized(1.0F, 2.4F)
                            .build("rune_herald"));

    private ModEntities() {}
}
