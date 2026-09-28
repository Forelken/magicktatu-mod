package com.runetouched.network.payload;

import com.runetouched.client.ClientInscriptions;
import com.runetouched.logic.InscriptionData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server -> Client: full copy of the local player's inscription + mana. */
public class SyncInscriptionPayload {
    private final CompoundTag data;

    public SyncInscriptionPayload(CompoundTag data) { this.data = data; }

    public static void encode(SyncInscriptionPayload msg, FriendlyByteBuf buf) { buf.writeNbt(msg.data); }

    public static SyncInscriptionPayload decode(FriendlyByteBuf buf) { return new SyncInscriptionPayload(buf.readNbt()); }

    public static void handle(SyncInscriptionPayload msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientInscriptions.put(msg.data)));
        ctx.setPacketHandled(true);
    }
}
