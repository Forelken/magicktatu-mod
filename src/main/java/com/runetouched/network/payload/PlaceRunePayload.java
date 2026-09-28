package com.runetouched.network.payload;

import com.runetouched.data.Runes;
import com.runetouched.logic.InscriptionData;
import com.runetouched.logic.InscriptionManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> Server: ink the rune held in the main hand onto a body zone. */
public class PlaceRunePayload {
    private final int zoneIndex;
    private final boolean elementSlot;

    public PlaceRunePayload(int zoneIndex, boolean elementSlot) {
        this.zoneIndex = zoneIndex;
        this.elementSlot = elementSlot;
    }

    public static void encode(PlaceRunePayload msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.zoneIndex);
        buf.writeBoolean(msg.elementSlot);
    }

    public static PlaceRunePayload decode(FriendlyByteBuf buf) {
        return new PlaceRunePayload(buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(PlaceRunePayload msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            InscriptionData data = InscriptionData.load(player);
            if (msg.zoneIndex < 0 || msg.zoneIndex >= InscriptionData.ZONE_COUNT) return;

            net.minecraft.world.item.ItemStack held = player.getMainHandItem();
            String runeId = null;
            if (held.getItem() instanceof com.runetouched.data.ModItems.RuneItem ri) runeId = ri.def.id();
            if (runeId == null) {
                InscriptionManager.sendActionBar(player,
                        net.minecraft.network.chat.Component.translatable("msg.runetouched.need_rune"));
                return;
            }
            Runes.RuneDef def = Runes.byId(runeId);
            if (def == null) return;

            InscriptionData.Slot slot = data.slots[msg.zoneIndex];
            boolean isElement = def.element();
            // clicking with an element rune fills the element half, body rune the body half
            if (isElement) slot.elementId = runeId; else slot.bodyId = runeId;

            InscriptionData.Combo combo = InscriptionData.resolve(slot);
            if (combo != null && !data.discoveredCombos.contains(combo.id)) {
                data.discoveredCombos.add(combo.id);
                player.displayClientMessage(net.minecraft.network.chat.Component
                        .translatable("msg.runetouched.discovered", combo.displayName), true);
                if (com.runetouched.Config.COMBAT_LOGGING.get())
                    com.runetouched.RuneTouched.LOGGER.info("Player {} discovered combo {}",
                            player.getGameProfile().getName(), combo.id);
            } else if (combo == null && !slot.isEmpty()) {
                player.displayClientMessage(net.minecraft.network.chat.Component
                        .translatable("msg.runetouched.unknown_combo"), true);
            }

            InscriptionData.save(player, data);
            InscriptionManager.refreshPlayerEffects(player);
            // consume one rune from inventory (creative players keep their stock)
            if (!player.isCreative()) held.shrink(1);
        });
        ctx.setPacketHandled(true);
    }
}
