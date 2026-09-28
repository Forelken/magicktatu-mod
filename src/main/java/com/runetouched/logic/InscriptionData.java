package com.runetouched.logic;

import com.runetouched.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The tattoo state ("inscription") of one player: which runes are inked on which
 * body zones, which combinations are currently active and the mana pool.
 * Stored in the player's PersistentData under key "runetouched_inscription".
 */
public class InscriptionData {
    public static final String NBT_KEY = "runetouched_inscription";
    public static final int ZONE_COUNT = 6;

    /** One combination slot: an element rune + a body rune. */
    public static class Slot {
        public String elementId = "";   // e.g. "fire"
        public String bodyId = "";      // e.g. "leg"

        public boolean isEmpty() { return elementId.isEmpty() || bodyId.isEmpty(); }

        public CompoundTag toNbt() {
            CompoundTag t = new CompoundTag();
            t.putString("element", elementId);
            t.putString("body", bodyId);
            return t;
        }

        public static Slot fromNbt(CompoundTag t) {
            Slot s = new Slot();
            s.elementId = t.getString("element");
            s.bodyId = t.getString("body");
            return s;
        }
    }

    public final Slot[] slots = new Slot[ZONE_COUNT];
    public float mana = -1f;              // -1 => full on first load
    public long lastManaTickMs = 0L;
    public final Set<String> discoveredCombos = new HashSet<>();

    public InscriptionData() {
        for (int i = 0; i < ZONE_COUNT; i++) slots[i] = new Slot();
    }

    // ---------------------------------------------------------------- combos

    /** Result of combining an element rune with a body rune. */
    public static class Combo {
        public final String id;                       // "fire_leg"
        public final Component displayName;
        public final boolean passive;                 // passive vs active (castable)
        public final int manaCost;                    // 0 for passives
        public final MobEffectInstance[] selfEffects; // applied continuously while slotted
        public final List<Component> tooltipLines;

        Combo(String id, Component displayName, boolean passive, int manaCost,
              MobEffectInstance[] selfEffects, List<Component> tooltipLines) {
            this.id = id;
            this.displayName = displayName;
            this.passive = passive;
            this.manaCost = manaCost;
            this.selfEffects = selfEffects;
            this.tooltipLines = tooltipLines;
        }
    }

    private static MobEffectInstance eff(MobEffect type, int durTicks, int amp) {
        return new MobEffectInstance(type, durTicks, amp, true, false);
    }

    /** Resolve the combo carried by a slot, or null if the pair is unknown. */
    public static Combo resolve(Slot slot) {
        if (slot.isEmpty()) return null;
        String key = (slot.elementId + "_" + slot.bodyId).toLowerCase(Locale.ROOT);
        switch (key) {
            // ---- FIRE -------------------------------------------------
            case "fire_leg":
                return new Combo(key, Component.translatable("combo.runetouched.fire_leg"), false, 20,
                        new MobEffectInstance[]{},
                        List.of(Component.translatable("combo.runetouched.fire_leg.desc")));
            case "fire_body":
                return new Combo(key, Component.translatable("combo.runetouched.fire_body"), true, 0,
                        new MobEffectInstance[]{eff(MobEffects.FIRE_RESISTANCE, 80, 0)},
                        List.of(Component.translatable("combo.runetouched.fire_body.desc")));
            case "fire_head":
                return new Combo(key, Component.translatable("combo.runetouched.fire_head"), true, 0,
                        new MobEffectInstance[]{eff(MobEffects.DAMAGE_BOOST, 80, 0)},
                        List.of(Component.translatable("combo.runetouched.fire_head.desc")));
            case "fire_arm":
                return new Combo(key, Component.translatable("combo.runetouched.fire_arm"), true, 0,
                        new MobEffectInstance[]{},
                        List.of(Component.translatable("combo.runetouched.fire_arm.desc")));
            // ---- WATER ------------------------------------------------
            case "water_leg":
                return new Combo(key, Component.translatable("combo.runetouched.water_leg"), true, 0,
                        new MobEffectInstance[]{eff(MobEffects.DOLPHINS_GRACE, 80, 0)},
                        List.of(Component.translatable("combo.runetouched.water_leg.desc")));
            case "water_body":
                return new Combo(key, Component.translatable("combo.runetouched.water_body"), true, 0,
                        new MobEffectInstance[]{eff(MobEffects.WATER_BREATHING, 80, 0)},
                        List.of(Component.translatable("combo.runetouched.water_body.desc")));
            case "water_head":
                return new Combo(key, Component.translatable("combo.runetouched.water_head"), false, 25,
                        new MobEffectInstance[]{},
                        List.of(Component.translatable("combo.runetouched.water_head.desc")));
            case "water_arm":
                return new Combo(key, Component.translatable("combo.runetouched.water_arm"), true, 0,
                        new MobEffectInstance[]{eff(MobEffects.DIG_SPEED, 80, 0)},
                        List.of(Component.translatable("combo.runetouched.water_arm.desc")));
            // ---- EARTH ------------------------------------------------
            case "earth_body":
                return new Combo(key, Component.translatable("combo.runetouched.earth_body"), true, 0,
                        new MobEffectInstance[]{eff(MobEffects.DAMAGE_RESISTANCE, 80, 0),
                                                 eff(MobEffects.MOVEMENT_SLOWDOWN, 80, 0)},
                        List.of(Component.translatable("combo.runetouched.earth_body.desc")));
            case "earth_head":
                return new Combo(key, Component.translatable("combo.runetouched.earth_head"), true, 0,
                        new MobEffectInstance[]{eff(MobEffects.NIGHT_VISION, 220, 0)},
                        List.of(Component.translatable("combo.runetouched.earth_head.desc")));
            case "earth_arm":
                return new Combo(key, Component.translatable("combo.runetouched.earth_arm"), true, 0,
                        new MobEffectInstance[]{},
                        List.of(Component.translatable("combo.runetouched.earth_arm.desc")));
            // ---- AIR --------------------------------------------------
            case "air_leg":
                return new Combo(key, Component.translatable("combo.runetouched.air_leg"), true, 0,
                        new MobEffectInstance[]{eff(MobEffects.MOVEMENT_SPEED, 80, 0)},
                        List.of(Component.translatable("combo.runetouched.air_leg.desc")));
            case "air_head":
                return new Combo(key, Component.translatable("combo.runetouched.air_head"), false, 30,
                        new MobEffectInstance[]{},
                        List.of(Component.translatable("combo.runetouched.air_head.desc")));
            // ---- SHADOW -----------------------------------------------
            case "shadow_leg":
                return new Combo(key, Component.translatable("combo.runetouched.shadow_leg"), false, 30,
                        new MobEffectInstance[]{},
                        List.of(Component.translatable("combo.runetouched.shadow_leg.desc")));
            case "shadow_head":
                return new Combo(key, Component.translatable("combo.runetouched.shadow_head"), true, 0,
                        new MobEffectInstance[]{eff(MobEffects.INVISIBILITY, 100, 0)},
                        List.of(Component.translatable("combo.runetouched.shadow_head.desc")));
            case "shadow_arm":
                return new Combo(key, Component.translatable("combo.runetouched.shadow_arm"), true, 0,
                        new MobEffectInstance[]{},
                        List.of(Component.translatable("combo.runetouched.shadow_arm.desc")));
            // ---- LIGHT ------------------------------------------------
            case "light_body":
                return new Combo(key, Component.translatable("combo.runetouched.light_body"), true, 0,
                        new MobEffectInstance[]{eff(MobEffects.REGENERATION, 100, 0)},
                        List.of(Component.translatable("combo.runetouched.light_body.desc")));
            case "light_head":
                return new Combo(key, Component.translatable("combo.runetouched.light_head"), true, 0,
                        new MobEffectInstance[]{eff(MobEffects.MOVEMENT_SPEED, 80, 0)},
                        List.of(Component.translatable("combo.runetouched.light_head.desc")));
            // ---- SHIELD (body rune) combos -----------------------------
            case "fire_shield_rune":
                return new Combo(key, Component.translatable("combo.runetouched.fire_shield"), true, 0,
                        new MobEffectInstance[]{eff(MobEffects.FIRE_RESISTANCE, 80, 0)},
                        List.of(Component.translatable("combo.runetouched.fire_shield.desc")));
            case "water_shield_rune":
                return new Combo(key, Component.translatable("combo.runetouched.water_shield"), true, 0,
                        new MobEffectInstance[]{},
                        List.of(Component.translatable("combo.runetouched.water_shield.desc")));
            case "earth_shield_rune":
                return new Combo(key, Component.translatable("combo.runetouched.earth_shield"), true, 0,
                        new MobEffectInstance[]{eff(MobEffects.DAMAGE_RESISTANCE, 80, 0),
                                                 eff(MobEffects.MOVEMENT_SLOWDOWN, 80, 1)},
                        List.of(Component.translatable("combo.runetouched.earth_shield.desc")));
            case "air_shield_rune":
                return new Combo(key, Component.translatable("combo.runetouched.air_shield"), false, 25,
                        new MobEffectInstance[]{},
                        List.of(Component.translatable("combo.runetouched.air_shield.desc")));
            case "light_shield_rune":
                return new Combo(key, Component.translatable("combo.runetouched.light_shield"), true, 0,
                        new MobEffectInstance[]{eff(MobEffects.ABSORPTION, 100, 0)},
                        List.of(Component.translatable("combo.runetouched.light_shield.desc")));
            default:
                return null;
        }
    }

    public List<Combo> activeCombos() {
        List<Combo> list = new ArrayList<>();
        for (Slot s : slots) {
            Combo c = resolve(s);
            if (c != null) list.add(c);
        }
        return list;
    }

    /** First active (castable) combo in the inscription, used by the ability keybind. */
    public Combo firstActive() {
        for (Slot s : slots) {
            Combo c = resolve(s);
            if (c != null && !c.passive) return c;
        }
        return null;
    }

    // ------------------------------------------------------------------ mana

    public float maxMana(Player p) {
        double bonus = p.getAttributeValue(ModAttributes.MAX_MANA.get());
        return (float) (Config.MAX_MANA.get() + bonus);
    }

    public void tickMana(Player p) {
        if (mana < 0f) { mana = maxMana(p); lastManaTickMs = System.currentTimeMillis(); return; }
        long now = System.currentTimeMillis();
        if (now - lastManaTickMs >= 1000L) {
            int regen = Math.max(1, Config.MANA_REGEN_PER_SECOND.get());
            mana = Math.min(maxMana(p), mana + regen);
            lastManaTickMs = now;
        }
    }

    public boolean spend(float cost) {
        if (mana < cost) return false;
        mana -= cost;
        return true;
    }

    // ------------------------------------------------------------------- nbt

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (Slot s : slots) list.add(s.toNbt());
        tag.put("slots", list);
        tag.putFloat("mana", mana);
        tag.putLong("lastManaTickMs", lastManaTickMs);
        ListTag disc = new ListTag();
        for (String d : discoveredCombos) disc.add(StringTag.valueOf(d));
        tag.put("discovered", disc);
        return tag;
    }

    public static InscriptionData fromNbt(CompoundTag tag) {
        InscriptionData data = new InscriptionData();
        if (tag.contains("slots")) {
            ListTag list = tag.getList("slots", 10);
            for (int i = 0; i < Math.min(ZONE_COUNT, list.size()); i++) {
                data.slots[i] = Slot.fromNbt(list.getCompound(i));
            }
        }
        if (tag.contains("mana")) data.mana = tag.getFloat("mana");
        if (tag.contains("lastManaTickMs")) data.lastManaTickMs = tag.getLong("lastManaTickMs");
        if (tag.contains("discovered")) {
            ListTag disc = tag.getList("discovered", 8);
            for (int i = 0; i < disc.size(); i++) data.discoveredCombos.add(disc.getString(i));
        }
        return data;
    }

    public static InscriptionData load(Player p) {
        CompoundTag tag = p.getPersistentData().getCompound(NBT_KEY);
        InscriptionData data = tag.isEmpty() ? new InscriptionData() : InscriptionData.fromNbt(tag);
        if (data.mana < 0f) data.mana = data.maxMana(p);
        return data;
    }

    public static void save(Player p, InscriptionData data) {
        p.getPersistentData().put(NBT_KEY, data.toNbt());
    }
}
