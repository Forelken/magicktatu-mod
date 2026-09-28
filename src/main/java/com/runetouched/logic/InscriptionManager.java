package com.runetouched.logic;

import com.runetouched.data.ModItems;
import com.runetouched.data.Runes;
import com.runetouched.network.ModPackets;
import com.runetouched.network.InscriptionMenu;
import com.runetouched.network.payload.SyncInscriptionPayload;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PlayMessages;

import java.util.UUID;

/**
 * Owns the lifecycle of a player's inscription: load on login, tick mana &
 * passive effects, sync to client, apply attribute modifiers per combo.
 */
public class InscriptionManager {

    private static final UUID MANA_UUID   = UUID.fromString("4b2c9a3e-9d1f-4f57-9b6a-71ce9f0a1001");
    private static final UUID DEFENSE_UUID = UUID.fromString("4b2c9a3e-9d1f-4f57-9b6a-71ce9f0a1002");

    /** Per-player cache so we do not re-parse NBT every tick. */
    private static final java.util.WeakHashMap<Player, InscriptionData> CACHE = new java.util.WeakHashMap<>();

    public static InscriptionData get(Player p) {
        return CACHE.computeIfAbsent(p, InscriptionData::load);
    }

    public static void invalidate(Player p) { CACHE.remove(p); }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof Player p) {
            invalidate(p);
            syncToClient(p);
        }
    }

    @SubscribeEvent
    public void onPlayerChangedDim(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof Player p) syncToClient(p);
    }

    @SubscribeEvent
    public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof Player p) { invalidate(p); syncToClient(p); }
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof Player p) || p.isSpectator()) return;
        InscriptionData data = get(p);
        data.tickMana(p);

        // Re-apply passive combos every 2 seconds (effects have ~4s duration)
        if (p.tickCount % 40 == 0) {
            for (InscriptionData.Combo c : data.activeCombos()) {
                if (!c.passive) continue;
                for (net.minecraft.world.effect.MobEffectInstance eff : c.selfEffects) {
                    p.addEffect(new net.minecraft.world.effect.MobEffectInstance(eff));
                }
            }
            syncToClient(p);
        }

        // keep open menu mana bar updated
        AbstractContainerMenu menu = p.containerMenu;
        if (menu instanceof InscriptionMenu im) {
            im.setMana(data.mana, data.maxMana(p));
        }
    }

    /** Recompute attribute modifiers from the current inscription. */
    public static void refreshPlayerEffects(Player p) {
        invalidate(p);
        InscriptionData data = get(p);

        AttributeInstance maxManaAttr = p.getAttribute(ModAttributes.MAX_MANA.get());
        if (maxManaAttr != null) {
            maxManaAttr.removeModifier(MANA_UUID);
            double bonus = 0;
            for (InscriptionData.Slot s : data.slots) {
                InscriptionData.Combo c = InscriptionData.resolve(s);
                if (c != null) bonus += 5.0D; // each working combo widens the mana pool
            }
            if (bonus > 0)
                maxManaAttr.addPermanentModifier(new AttributeModifier(
                        MANA_UUID, "rune_inscriptions", bonus, AttributeModifier.Operation.ADDITION));
        }

        AttributeInstance defAttr = p.getAttribute(ModAttributes.RUNE_DEFENSE.get());
        if (defAttr != null) {
            defAttr.removeModifier(DEFENSE_UUID);
            double def = 0;
            for (InscriptionData.Combo c : data.activeCombos()) {
                if (c.id.equals("water_shield_rune")) def += 0.10D;
            }
            if (def > 0)
                defAttr.addPermanentModifier(new AttributeModifier(
                        DEFENSE_UUID, "rune_guard", def, AttributeModifier.Operation.ADDITION));
        }
        syncToClient(p);
    }

    public static void syncToClient(Player p) {
        if (p instanceof net.minecraft.server.level.ServerPlayer sp) {
            InscriptionData data = get(p);
            ModPackets.sendToClient(new SyncInscriptionPayload(data.toNbt()), sp);
        }
    }

    public static void sendActionBar(Player p, net.minecraft.network.chat.Component text) {
        if (p instanceof net.minecraft.server.level.ServerPlayer sp) {
            sp.connection.send(new net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket(text));
        }
    }
}
