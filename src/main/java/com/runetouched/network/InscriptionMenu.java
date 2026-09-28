package com.runetouched.network;

import com.runetouched.data.Runes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Server-side "menu" for the tattoo GUI. It carries no real slots: the whole
 * body-map UI is drawn by the client screen InscriptionScreen.
 * Mana is synced through DataSlots so the client can draw a mana bar.
 */
public class InscriptionMenu extends AbstractContainerMenu {

    /** Menu type registry + factory. */
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Runes.MODID);

    @SuppressWarnings("unused")
    public static final RegistryObject<MenuType<InscriptionMenu>> TYPE =
            MENUS.register("inscription", () -> IForgeMenuType.create(
                    (IContainerFactory<InscriptionMenu>) (windowId, inv, data) -> new InscriptionMenu(windowId, inv)));

    private final int[] manaSync = new int[]{0};
    private final int[] maxManaSync = new int[]{0};

    public InscriptionMenu(int containerId, Inventory inv) {
        super(TYPE.get(), containerId);
        addDataSlot(new DataSlot() {
            @Override public int get() { return manaSync[0]; }
            @Override public void set(int value) { manaSync[0] = value; }
        });
        addDataSlot(new DataSlot() {
            @Override public int get() { return maxManaSync[0]; }
            @Override public void set(int value) { maxManaSync[0] = value; }
        });
    }

    public void setMana(float mana, float maxMana) {
        manaSync[0] = Math.round(mana * 10);
        maxManaSync[0] = Math.round(maxMana * 10);
        broadcastChanges();
    }

    public int getManaScaled()    { return manaSync[0]; }
    public int getMaxManaScaled() { return maxManaSync[0]; }

    @Override
    public boolean stillValid(net.minecraft.world.entity.player.Player player) {
        return true;
    }

    /** MenuProvider used by the Tome item to open the tattoo GUI. */
    public static class Provider implements net.minecraft.world.MenuProvider {
        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inv,
                net.minecraft.world.entity.player.Player player) {
            return new InscriptionMenu(id, inv);
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable("gui.runetouched.inscription.title");
        }
    }
}
