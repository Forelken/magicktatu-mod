package com.runetouched.logic;

import com.runetouched.Config;
import com.runetouched.data.ModItems;
import com.runetouched.network.ModPackets;
import com.runetouched.network.payload.CastAbilityPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Combat hooks + the active abilities granted by rune combinations.
 */
public class RuneEffects {

    /** Per-player cooldown tracker (combo id -> tick when usable again). */
    private static final java.util.WeakHashMap<Player, Long> COOLDOWNS = new java.util.WeakHashMap<>();

    // --------------------------------------------------------- passive effects

    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        InscriptionData data = InscriptionManager.get(player);

        // "earth_shield_rune" / "earth_body": flat damage reduction via attribute
        double def = player.getAttributeValue(ModAttributes.RUNE_DEFENSE.get());
        if (def > 0) event.setAmount((float) (event.getAmount() * (1.0 - Math.min(0.8, def))));

        // thorns-like: fire_arm burns attackers, shadow_arm drains life
        for (InscriptionData.Combo c : data.activeCombos()) {
            if (c.id.equals("fire_arm") && event.getSource().getEntity() instanceof LivingEntity attacker) {
                attacker.setSecondsOnFire(3);
            }
            if (c.id.equals("shadow_arm") && event.getSource().getEntity() instanceof LivingEntity attacker) {
                float drained = Math.min(2.0F, attacker.getHealth() - 1.0F);
                if (drained > 0) {
                    attacker.hurt(event.getSource(), drained);
                    player.heal(drained);
                }
            }
        }
    }

    @SubscribeEvent
    public void onDealtDamage(LivingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        InscriptionData data = InscriptionManager.get(player);

        for (InscriptionData.Combo c : data.activeCombos()) {
            switch (c.id) {
                case "fire_head" -> target.setSecondsOnFire(4);                       // strikes ignite
                case "water_head" -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
                default -> { }
            }
        }
    }

    // ------------------------------------------------------------- castables

    /** Entry point of the ability keybind (client sends CastAbilityPayload). */
    public static void castActiveAbility(Player player) {
        InscriptionData data = InscriptionManager.get(player);
        InscriptionData.Combo combo = data.firstActive();
        if (combo == null) {
            InscriptionManager.sendActionBar(player,
                    net.minecraft.network.chat.Component.translatable("msg.runetouched.no_active"));
            return;
        }
        long now = player.level.getGameTime();
        Long ready = COOLDOWNS.get(player);
        if (ready != null && now < ready) {
            InscriptionManager.sendActionBar(player,
                    net.minecraft.network.chat.Component.translatable("msg.runetouched.cooldown"));
            return;
        }
        if (!data.spend(combo.manaCost)) {
            InscriptionManager.sendActionBar(player,
                    net.minecraft.network.chat.Component.translatable("msg.runetouched.no_mana"));
            return;
        }
        long cooldownTicks = (long) (Config.DASH_COOLDOWN_SECONDS.get() * 20);
        COOLDOWNS.put(player, now + cooldownTicks);

        switch (combo.id) {
            case "fire_leg"      -> fireDash(player);        break; // Огненный рывок
            case "water_head"    -> healingWave(player);     break;
            case "air_head"      -> leap(player);            break;
            case "shadow_leg"    -> blinkStrike(player);     break;
            case "air_shield_rune" -> guardianBulwark(player); break;
            default -> { }
        }
        InscriptionManager.syncToClient(player);
    }

    /** Fire rune + leg rune: a dash that sets enemies on fire. */
    private static void fireDash(Player player) {
        Vec3 look = player.getLookAngle();
        double dist = Config.DASH_DISTANCE.get();
        Vec3 start = player.position();
        Vec3 end = start.add(look.x * dist, Math.max(look.y * dist, -0.3), look.z * dist);
        player.teleportTo(end.x, end.y, end.z);
        AABB area = new AABB(start, end).inflate(2.5);
        for (LivingEntity e : player.level.getEntitiesOfClass(LivingEntity.class, area,
                en -> en != player && !en.isRemoved())) {
            e.setSecondsOnFire(6);
            e.hurt(player.damageSources().playerAttack(player), 4.0F);
        }
        spawnParticles(player, net.minecraft.core.particles.ParticleTypes.FLAME, start, end);
    }

    /** Water rune + head rune: heal self and nearby allies. */
    private static void healingWave(Player player) {
        AABB area = player.getBoundingBox().inflate(6);
        for (Player p : player.level.getPlayers(en -> en.distanceToSqr(player) < 36)) {
            p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
        }
        spawnParticles(player, net.minecraft.core.particles.ParticleTypes.BUBBLE,
                player.position(), player.position().add(0, 1, 0));
    }

    /** Air rune + head rune: jump high into the air. */
    private static void leap(Player player) {
        player.setDeltaMovement(player.getDeltaMovement().x, 1.4D, player.getDeltaMovement().z);
        player.hurtMarked = true;
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 80, 0));
        spawnParticles(player, net.minecraft.core.particles.ParticleTypes.CLOUD,
                player.position(), player.position());
    }

    /** Shadow rune + leg rune: teleport behind the nearest enemy and strike. */
    private static void blinkStrike(Player player) {
        LivingEntity best = null;
        double bd = 16.0D * 16.0D;
        for (LivingEntity e : player.level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(16), en -> en != player && en.isAlive())) {
            double d = e.distanceToSqr(player);
            if (d < bd) { bd = d; best = e; }
        }
        if (best == null) {
            InscriptionManager.sendActionBar(player,
                    net.minecraft.network.chat.Component.translatable("msg.runetouched.no_target"));
            return;
        }
        Vec3 behind = best.position().add(-best.getLookAngle().x * 1.5, 0, -best.getLookAngle().z * 1.5);
        player.teleportTo(behind.x, best.getY(), behind.z);
        best.hurt(player.damageSources().magic(), 7.0F);
        best.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
        spawnParticles(player, net.minecraft.core.particles.ParticleTypes.SMOKE,
                player.position(), best.position());
    }

    /** Air rune + shield rune: temporary damage immunity bulwark. */
    private static void guardianBulwark(Player player) {
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 2));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 160, 2));
        spawnParticles(player, net.minecraft.core.particles.ParticleTypes.END_ROD,
                player.position(), player.position().add(0, 2, 0));
    }

    private static void spawnParticles(Player player, net.minecraft.core.particles.SimpleParticleType type,
                                       Vec3 from, Vec3 to) {
        if (player instanceof ServerPlayer sp) {
            int n = 40;
            for (int i = 0; i < n; i++) {
                double t = i / (double) n;
                double x = from.x + (to.x - from.x) * t;
                double y = from.y + (to.y - from.y) * t + 1.0;
                double z = from.z + (to.z - from.z) * t;
                sp.level.sendParticles(sp, type, true, x, y, z, 2, 0.2, 0.2, 0.2, 0.01);
            }
        }
    }
}
