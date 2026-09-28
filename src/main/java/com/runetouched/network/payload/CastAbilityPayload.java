package com.runetouched.network.payload;

import com.runetouched.logic.InscriptionData;
import com.runetouched.logic.RuneEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> Server: trigger the active (castable) ability of the inscription. */
public class CastAbilityPayload {

    public static void encode(CastAbilityPayload msg, FriendlyByteBuf buf) {}

    public static CastAbilityPayload decode(FriendlyByteBuf buf) { return new CastAbilityPayload(); }

    public static void handle(CastAbilityPayload msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            RuneEffects.castActiveAbility(player);
        });
        ctx.setPacketHandled(true);
    }
}
