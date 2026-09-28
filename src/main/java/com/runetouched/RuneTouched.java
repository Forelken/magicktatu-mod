package com.runetouched;

import com.runetouched.client.RuneTouchedClient;
import com.runetouched.data.ModEntities;
import com.runetouched.data.ModItems;
import com.runetouched.logic.AltarRecipeManager;
import com.runetouched.logic.BossEvents;
import com.runetouched.logic.InscriptionManager;
import com.runetouched.logic.RuneEffects;
import com.runetouched.network.ModPackets;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(RuneTouched.MODID)
public class RuneTouched {
    public static final String MODID = "runetouched";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public RuneTouched() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::commonSetup);

        ModItems.ITEMS.register(bus);
        ModEntities.ENTITY_TYPES.register(bus);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new RuneEffects());
        MinecraftForge.EVENT_BUS.register(new InscriptionManager());
        MinecraftForge.EVENT_BUS.register(new BossEvents());
        MinecraftForge.EVENT_BUS.register(new AltarRecipeManager());

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC, "runetouched-common.toml");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModPackets.register();
            if (net.minecraftforge.api.distmarker.DistExecutor.callWhenOn(
                    net.minecraftforge.api.distmarker.Dist.CLIENT, () -> RuneTouchedClient::registerKeyBinds) != null) {
                // key binds registered client-side
            }
        });
    }
}
