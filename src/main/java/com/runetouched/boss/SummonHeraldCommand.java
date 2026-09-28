package com.runetouched.boss;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.runetouched.data.ModEntities;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/** /runetouched summonherald [pos] - spawns the Rune Herald boss. */
public class SummonHeraldCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("summonherald")
                .executes(ctx -> summon(ctx.getSource(), ctx.getSource().getPosition()))
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(ctx -> summon(ctx.getSource(),
                                Vec3.atCenterOf(BlockPosArgument.getSpawnablePos(ctx, "pos")))));
    }

    private static int summon(CommandSourceStack src, Vec3 pos) {
        RuneHeraldEntity herald = ModEntities.RUNE_HERALD.get().create(src.getLevel());
        if (herald == null) return 0;
        herald.moveTo(pos.x, pos.y, pos.z, src.getRotation().y, 0);
        src.getLevel().addFreshEntity(herald);
        src.sendSuccess(() -> Component.translatable("msg.runetouched.summoned"), true);
        return 1;
    }

    private SummonHeraldCommand() {}
}
