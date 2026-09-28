package com.runetouched.client;

import net.minecraft.nbt.CompoundTag;

/** Holds the last synced inscription copy of the local player (for HUD & GUI). */
public final class ClientInscriptions {
    private static CompoundTag current = new CompoundTag();

    public static void put(CompoundTag tag) { current = tag == null ? new CompoundTag() : tag; }

    public static CompoundTag get() { return current; }

    private ClientInscriptions() {}
}
