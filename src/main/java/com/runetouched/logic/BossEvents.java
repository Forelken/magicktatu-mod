package com.runetouched.logic;

import com.runetouched.boss.RuneHeraldEntity;
import com.runetouched.data.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Extra guaranteed drops & kill feedback for the boss. */
public class BossEvents {

    @SubscribeEvent
    public void onBossDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof RuneHeraldEntity herald)) return;
        var level = herald.level;
        // guarantee one random tier-2 rune even if the loot table rolled empty
        ItemStack drop = new ItemStack(java.util.List.of(
                        ModItems.RUNE_SHADOW.get(), ModItems.RUNE_LIGHT.get(), ModItems.RUNE_SHIELD.get())
                .get(level.random.nextInt(3)));
        var it = herald.spawnAtLocation(drop);
        if (it != null) it.setInvulnerable(true);
    }
}
