package com.runetouched.network;

import com.runetouched.data.Runes;
import com.runetouched.network.payload.CastAbilityPayload;
import com.runetouched.network.payload.ClearZonePayload;
import com.runetouched.network.payload.PlaceRunePayload;
import com.runetouched.network.payload.SyncInscriptionPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/** SimpleChannel with the four packets of the mod. */
public final class ModPackets {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Runes.MODID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private static int id = 0;

    public static void register() {
        CHANNEL.messageBuilder(PlaceRunePayload.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(PlaceRunePayload::encode).decoder(PlaceRunePayload::decode)
                .consumerNetworkThread(PlaceRunePayload::handle).add();
        CHANNEL.messageBuilder(ClearZonePayload.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ClearZonePayload::encode).decoder(ClearZonePayload::decode)
                .consumerNetworkThread(ClearZonePayload::handle).add();
        CHANNEL.messageBuilder(CastAbilityPayload.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CastAbilityPayload::encode).decoder(CastAbilityPayload::decode)
                .consumerNetworkThread(CastAbilityPayload::handle).add();
        CHANNEL.messageBuilder(SyncInscriptionPayload.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncInscriptionPayload::encode).decoder(SyncInscriptionPayload::decode)
                .consumerNetworkThread(SyncInscriptionPayload::handle).add();
    }

    public static void sendToServer(Object msg) {
        CHANNEL.sendToServer(msg);
    }

    public static void sendToClient(Object msg, ServerPlayer player) {
        CHANNEL.sendTo(msg, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }

    private ModPackets() {}
}
