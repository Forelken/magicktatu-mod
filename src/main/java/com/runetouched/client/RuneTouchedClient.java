package com.runetouched.client;

import com.runetouched.RuneTouched;
import com.runetouched.data.ModEntities;
import com.runetouched.data.Runes;
import com.runetouched.network.InscriptionMenu;
import com.runetouched.network.ModPackets;
import com.runetouched.network.payload.CastAbilityPayload;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.HoglinRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod.EventBusSubscriber(modid = RuneTouched.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class RuneTouchedClient {

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.CAST_ABILITY);
        event.register(KeyBindings.OPEN_INSCRIPTION);
    }

    /** Called from common setup to hook client-only registrations. */
    public static Runnable registerKeyBinds() {
        return () -> {
            MenuScreens.register(InscriptionMenu.TYPE.get(), InscriptionScreen::new);
            EntityRenderers.register(ModEntities.RUNE_HERALD.get(),
                    r -> new HoglinRenderer(r, false)); // placeholder model until custom geo lands
            FMLJavaModLoadingContext.get().getModEventBus().addListener(RuneTouchedClient::onRegisterKeyMappings);
        };
    }

    private RuneTouchedClient() {}
}
