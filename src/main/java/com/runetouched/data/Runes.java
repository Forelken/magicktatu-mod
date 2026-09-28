package com.runetouched.data;

import net.minecraft.resources.ResourceLocation;

/**
 * Static definition of every rune known to the mod.
 * Element runes carry a magical essence, body runes define where the essence is tattooed.
 */
public final class Runes {
    public static final String MODID = "runetouched";

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MODID, path);
    }

    /** Body zones available on the tattoo GUI map. */
    public enum Zone {
        HEAD("head", 0),
        BODY("body", 1),
        LEFT_ARM("left_arm", 2),
        RIGHT_ARM("right_arm", 3),
        LEFT_LEG("left_leg", 4),
        RIGHT_LEG("right_leg", 5);

        private final String key;
        private final int index;

        Zone(String key, int index) { this.key = key; this.index = index; }
        public String key() { return key; }
        public int index() { return index; }

        public static Zone byIndex(int i) {
            for (Zone z : values()) if (z.index == i) return z;
            return null;
        }
    }

    /** All craftable / droppable runes. */
    public record RuneDef(String id, boolean element, int tier) {
        public ResourceLocation itemId() { return rl("rune_" + id); }
    }

    // ELEMENT runes
    public static final RuneDef FIRE   = new RuneDef("fire",   true, 1);
    public static final RuneDef WATER  = new RuneDef("water",  true, 1);
    public static final RuneDef EARTH  = new RuneDef("earth",  true, 1);
    public static final RuneDef AIR    = new RuneDef("air",    true, 1);
    public static final RuneDef SHADOW = new RuneDef("shadow", true, 2);
    public static final RuneDef LIGHT  = new RuneDef("light",  true, 2);

    // BODY runes
    public static final RuneDef HEAD      = new RuneDef("head",       false, 1);
    public static final RuneDef BODY_RUNE = new RuneDef("body",       false, 1);
    public static final RuneDef ARM       = new RuneDef("arm",        false, 1);
    public static final RuneDef LEG       = new RuneDef("leg",        false, 1);
    public static final RuneDef SHIELD    = new RuneDef("shield_rune", false, 2);

    public static final RuneDef[] ALL = {
            FIRE, WATER, EARTH, AIR, SHADOW, LIGHT,
            HEAD, BODY_RUNE, ARM, LEG, SHIELD
    };

    public static RuneDef byId(String id) {
        for (RuneDef r : ALL) if (r.id().equals(id)) return r;
        return null;
    }

    private Runes() {}
}
