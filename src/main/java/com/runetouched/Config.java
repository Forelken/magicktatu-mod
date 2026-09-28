package com.runetouched;

import net.minecraftforge.common.ForgeConfigSpec;

public final class Config {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue MAX_MANA;
    public static final ForgeConfigSpec.IntValue MANA_REGEN_PER_SECOND;
    public static final ForgeConfigSpec.DoubleValue DASH_DISTANCE;
    public static final ForgeConfigSpec.DoubleValue DASH_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.BooleanValue COMBAT_LOGGING;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("mana");
        MAX_MANA = b.comment("Maximum mana pool of a rune-touched player").defineInRange("maxMana", 100, 10, 10000);
        MANA_REGEN_PER_SECOND = b.comment("Mana regenerated per second").defineInRange("manaRegenPerSecond", 2, 0, 100);
        b.pop();

        b.push("abilities");
        DASH_DISTANCE = b.comment("Distance of the dash ability in blocks").defineInRange("dashDistance", 6.0D, 1.0D, 32.0D);
        DASH_COOLDOWN_SECONDS = b.comment("Cooldown of active abilities in seconds").defineInRange("dashCooldownSeconds", 8.0D, 1.0D, 60.0D);
        b.pop();

        b.push("debug");
        COMBAT_LOGGING = b.comment("Log discovered combinations to server log").define("combatLogging", true);
        b.pop();

        SPEC = b.build();
    }

    private Config() {}
}
