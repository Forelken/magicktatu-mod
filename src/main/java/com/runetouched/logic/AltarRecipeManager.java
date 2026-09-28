package com.runetouched.logic;

import com.runetouched.data.ModItems;
import com.runetouched.data.Runes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * The "Rune Altar" is built in-world out of vanilla blocks:
 * a Lodestone (or Enchanting Table) center, ringed by 4 soul-lantern pillars.
 * Right-click the center with runes in hand to transmute them at the altar.
 */
public class AltarRecipeManager {

    public record AltarOut(ItemStack stack, int manaCost) {}

    private static ItemStack rune(String id, int count) {
        return new ItemStack(ModItems.ITEMS.getEntries().stream()
                .filter(e -> e.getId().getPath().equals("rune_" + id))
                .findFirst().map(e -> (net.minecraft.world.item.Item) e.value())
                .orElse(net.minecraft.world.item.Items.AIR), count);
    }

    /** Try to match the held stacks against an altar recipe. Returns output or null. */
    public static AltarOut match(ItemStack a, ItemStack b) {
        String ia = itemKey(a), ib = itemKey(b);
        if (ia == null || ib == null) return null;
        // element + element => higher tier element
        if (ia.equals("fire") && ib.equals("water")) return new AltarOut(rune("air", 1), 20);
        if (ia.equals("water") && ib.equals("fire")) return new AltarOut(rune("air", 1), 20);
        if (ia.equals("fire") && ib.equals("earth")) return new AltarOut(rune("shadow", 1), 30);
        if (ia.equals("earth") && ib.equals("fire")) return new AltarOut(rune("shadow", 1), 30);
        if (ia.equals("water") && ib.equals("light_basic")) return new AltarOut(rune("light", 1), 25);
        // glistering melon-like "light source": glowstone dust counts as light_basic
        if (ia.equals("shadow") && ib.equals("glowstone")) return new AltarOut(rune("light", 1), 35);
        if (ia.equals("glowstone") && ib.equals("shadow")) return new AltarOut(rune("light", 1), 35);
        // body rune duplication at altar using runic powder
        if ((ia.equals("leg") || ia.equals("arm") || ia.equals("head") || ia.equals("body")) && ib.equals("powder"))
            return new AltarOut(rune(ia, 1), 15);
        if ((ib.equals("leg") || ib.equals("arm") || ib.equals("head") || ib.equals("body")) && ia.equals("powder"))
            return new AltarOut(rune(ib, 1), 15);
        if (ia.equals("shield_rune") && ib.equals("powder")) return new AltarOut(rune("shield_rune", 1), 25);
        if (ib.equals("shield_rune") && ia.equals("powder")) return new AltarOut(rune("shield_rune", 1), 25);
        return null;
    }

    private static String itemKey(ItemStack s) {
        if (s.isEmpty()) return null;
        if (s.getItem() instanceof ModItems.RuneItem ri) return ri.def.id();
        if (s.is(net.minecraft.world.item.Items.GLOWSTONE_DUST)) return "glowstone";
        if (s.is(ModItems.RUNIC_POWDER.get())) return "powder";
        if (s.is(net.minecraft.world.item.Items.PRISMARINE_SHARD)) return "light_basic";
        return null;
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        BlockState state = level.getBlockState(pos);

        if (!isAltarCenter(state)) return;
        if (level.isClientSide) return;

        if (!isAltarComplete(level, pos)) {
            player.displayClientMessage(Component.translatable("msg.runetouched.altar_incomplete"), true);
            return;
        }

        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        AltarOut out = match(main, off);
        if (out == null) {
            InscriptionData data = InscriptionManager.get(player);
            player.displayClientMessage(Component.translatable("msg.runetouched.altar_hint",
                    String.format("%.0f/%.0f", data.mana, data.maxMana(player))), true);
            return;
        }

        InscriptionData data = InscriptionManager.get(player);
        if (!data.spend(out.manaCost())) {
            player.displayClientMessage(Component.translatable("msg.runetouched.no_mana"), true);
            return;
        }
        main.shrink(1);
        off.shrink(1);
        if (!player.getInventory().add(out.stack())) {
            player.drop(out.stack(), false);
        }
        level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ENCHANTMENT_TABLE_USE,
                net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.2F);
        player.displayClientMessage(Component.translatable("msg.runetouched.altar_crafted",
                out.stack().getHoverName()), true);
        InscriptionManager.syncToClient(player);
        event.setCanceled(true);
    }

    private static boolean isAltarCenter(BlockState state) {
        return state.is(net.minecraft.world.level.block.Blocks.LODESTONE)
                || state.is(net.minecraft.world.level.block.Blocks.ENCHANTING_TABLE);
    }

    /** Check for up to 4 soul lantern pillars within 2 blocks around the center. */
    private static boolean isAltarComplete(Level level, BlockPos center) {
        int pillars = 0;
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = -1; y <= 2; y++) {
                    BlockState s = level.getBlockState(center.offset(x, y, z));
                    if (s.is(net.minecraft.world.level.block.Blocks.SOUL_LANTERN)) pillars++;
                }
            }
        }
        return pillars >= 4;
    }
}
