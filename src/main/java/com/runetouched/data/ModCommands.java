package com.runetouched.data;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.runetouched.boss.SummonHeraldCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ModCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ArgumentBuilder<CommandSourceStack, ?> herald = SummonHeraldCommand.register();
        event.getDispatcher().register(
                Commands.literal("runetouched")
                        .requires(src -> src.hasPermission(2))
                        .then(herald));
    }

    private ModCommands() {}
}
