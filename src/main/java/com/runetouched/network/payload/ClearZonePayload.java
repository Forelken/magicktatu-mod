package com.runetouched.network.payload;

import com.runetouched.logic.InscriptionData;
import com.runetouched.logic.InscriptionManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> Server: wipe the tattoo on a body zone (runes are returned). */
public class ClearZonePayload {
    private final int zoneIndex;

    public ClearZonePayload(int zoneIndex) { this.zoneIndex = zoneIndex; }

    public static void encode(ClearZonePayload msg, FriendlyByteBuf buf) { buf.writeVarInt(msg.zoneIndex); }

    public static ClearZonePayload decode(FriendlyByteBuf buf) { return new ClearZonePayload(buf.readVarInt()); }

    public static void handle(ClearZonePayload msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || msg.zoneIndex < 0 || msg.zoneIndex >= InscriptionData.ZONE_COUNT) return;
            InscriptionData data = InscriptionData.load(player);
            InscriptionData.Slot slot = data.slots[msg.zoneIndex];
            if (!slot.isEmpty()) {
                for (String id : new String[]{slot.elementId, slot.bodyId}) {
                    if (id.isEmpty()) continue;
                    net.minecraft.resources.ResourceLocation rl = com.runetouched.data.Runes.rl("rune_" + id);
                    net.minecraft.world.item.Item item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(rl);
                    if (item != null) player.getInventory().add(new net.minecraft.world.item.ItemStack(item));
                }
                slot.elementId = "";
                slot.bodyId = "";
                InscriptionData.save(player, data);
                InscriptionManager.refreshPlayerEffects(player);
                InscriptionManager.syncToClient(player);
            }
        });
        ctx.setPacketHandled(true);
    }
}
