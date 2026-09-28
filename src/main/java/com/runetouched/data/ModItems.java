package com.runetouched.data;

import com.runetouched.RuneTouched;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * All rune items, the "Tome of Inscriptions" (GUI opener) and altar-related items.
 */
public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, RuneTouched.MODID);

    /** Simple rune item that can be tattooed onto a body zone. */
    public static class RuneItem extends Item {
        public final Runes.RuneDef def;

        public RuneItem(Runes.RuneDef def) {
            super(new Item.Properties().stackTo(16).rarity(
                    def.tier() >= 2 ? net.minecraft.world.item.Rarity.RARE
                                    : net.minecraft.world.item.Rarity.UNCOMMON));
            this.def = def;
        }
    }

    /** Book item: right click opens the tattoo GUI. */
    public static class TomeItem extends Item {
        public TomeItem() {
            super(new Item.Properties().stackTo(1).rarity(net.minecraft.world.item.Rarity.EPIC));
        }

        @Override
        public net.minecraft.world.InteractionResult use(
                net.minecraft.world.item.ItemStack stack,
                net.minecraft.world.level.Level level,
                net.minecraft.world.entity.player.Player player,
                net.minecraft.world.InteractionHand hand,
                net.minecraft.world.phys.BlockHitResult hit) {
            if (!level.isClientSide) {
                player.openMenu(new com.runetouched.network.InscriptionMenu.Provider(),
                        buf -> {});
            }
            return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
        }
    }

    // ---- element runes -------------------------------------------------
    public static final RegistryObject<Item> RUNE_FIRE   = registerRune(Runes.FIRE);
    public static final RegistryObject<Item> RUNE_WATER  = registerRune(Runes.WATER);
    public static final RegistryObject<Item> RUNE_EARTH  = registerRune(Runes.EARTH);
    public static final RegistryObject<Item> RUNE_AIR    = registerRune(Runes.AIR);
    public static final RegistryObject<Item> RUNE_SHADOW = registerRune(Runes.SHADOW);
    public static final RegistryObject<Item> RUNE_LIGHT  = registerRune(Runes.LIGHT);

    // ---- body runes ----------------------------------------------------
    public static final RegistryObject<Item> RUNE_HEAD       = registerRune(Runes.HEAD);
    public static final RegistryObject<Item> RUNE_BODY       = registerRune(Runes.BODY_RUNE);
    public static final RegistryObject<Item> RUNE_ARM        = registerRune(Runes.ARM);
    public static final RegistryObject<Item> RUNE_LEG        = registerRune(Runes.LEG);
    public static final RegistryObject<Item> RUNE_SHIELD     = registerRune(Runes.SHIELD);

    // ---- misc ------------------------------------------------------------
    public static final RegistryObject<Item> INSCRIPTION_TOME = ITEMS.register("inscription_tome",
            () -> new TomeItem());

    public static final RegistryObject<Item> RUNIC_POWDER = ITEMS.register("runic_powder",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> HERES_BLOOD = ITEMS.register("herald_blood",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));

    private static RegistryObject<Item> registerRune(Runes.RuneDef def) {
        return ITEMS.register("rune_" + def.id(), () -> new RuneItem(def));
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }

    private ModItems() {}
}
