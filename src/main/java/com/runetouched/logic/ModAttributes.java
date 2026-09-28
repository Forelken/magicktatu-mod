package com.runetouched.logic;

import com.runetouched.data.Runes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Custom attributes used by rune combinations. Registered on the Forge bus so
 * they exist before any entity is spawned.
 */
public final class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, Runes.MODID);

    /** Extra maximum mana of the player (base pool comes from config). */
    public static final RegistryObject<Attribute> MAX_MANA = ATTRIBUTES.register("max_mana",
            () -> new RangedAttribute("attribute.name.runetouched.max_mana", 0.0D, -1024.0D, 1024.0D).setSyncable(true));

    /** Bonus attack damage dealt by tattooed runes. */
    public static final RegistryObject<Attribute> RUNE_DAMAGE = ATTRIBUTES.register("rune_damage",
            () -> new RangedAttribute("attribute.name.runetouched.rune_damage", 0.0D, -1024.0D, 1024.0D).setSyncable(true));

    /** Damage reduction granted by defensive tattoos (0..0.8). */
    public static final RegistryObject<Attribute> RUNE_DEFENSE = ATTRIBUTES.register("rune_defense",
            () -> new RangedAttribute("attribute.name.runetouched.rune_defense", 0.0D, 0.0D, 0.8D).setSyncable(true));

    private ModAttributes() {}
}
