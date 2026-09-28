package com.runetouched.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.runetouched.data.Runes;
import com.runetouched.logic.InscriptionData;
import com.runetouched.network.InscriptionMenu;
import com.runetouched.network.ModPackets;
import com.runetouched.network.payload.ClearZonePayload;
import com.runetouched.network.payload.PlaceRunePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * The tattoo GUI: a body schematic with 6 zones. Each zone shows two small slots
 * (element rune + body rune). Click a zone while holding a rune item to ink it.
 */
public class InscriptionScreen extends AbstractContainerScreen<InscriptionMenu> {

    private static final int BODY_X = 28;   // body map origin inside the screen
    private static final int BODY_Y = 24;
    private static final int ZONE_W = 20;
    private static final int ZONE_H = 20;

    /** Rectangles of the 6 clickable zones on the body map. */
    private static final int[][] ZONES = {
            // x offset, y offset relative to BODY_X/BODY_Y
            {16, 0},     // head
            {8, 22},     // body (torso)
            {-8, 22},    // left arm
            {40, 22},    // right arm
            {8, 56},     // left leg
            {28, 56},    // right leg
    };

    private InscriptionData data = new InscriptionData();

    public InscriptionScreen(InscriptionMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 220;
        this.imageHeight = 170;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    private void reloadData() {
        net.minecraft.nbt.CompoundTag tag = ClientInscriptions.get();
        if (tag != null && !tag.isEmpty()) data = InscriptionData.fromNbt(tag);
    }

    @Override
    protected void init() {
        super.init();
        leftAlign = (width - imageWidth) / 2;
        topAlign = (height - imageHeight) / 2;
        reloadData();
    }

    private int leftAlign, topAlign;

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        drawBodyMap(poseStack);
        drawCombos(poseStack, mouseX, mouseY);
        drawManaBar(poseStack);
        renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShaderTexture(0, GuiUtils.GUI_TEX);
        blit(poseStack, leftAlign, topAlign, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        font.drawShadow(poseStack, this.title, (float)((imageWidth - font.width(this.title)) / 2), 6, 0x4020A0);
    }

    private void drawBodyMap(PoseStack poseStack) {
        for (int i = 0; i < 6; i++) {
            int zx = leftAlign + BODY_X + ZONES[i][0];
            int zy = topAlign + BODY_Y + ZONES[i][1];
            InscriptionData.Slot slot = data.slots[i];

            boolean hover = mouseX >= zx && mouseX < zx + ZONE_W && mouseY >= zy && mouseY < zy + ZONE_H;
            fill(poseStack, zx, zy, zx + ZONE_W, zy + ZONE_H, hover ? 0x60FFFFFF : 0x40808080);
            GuiUtils.blitBorder(poseStack, zx, zy, ZONE_W, ZONE_H, 0xE0A08040);

            // element half (left) and body half (right)
            if (!slot.elementId.isEmpty())
                GuiUtils.drawRuneIcon(poseStack, zx + 1, zy + 1, slot.elementId);
            if (!slot.bodyId.isEmpty())
                GuiUtils.drawRuneIcon(poseStack, zx + ZONE_W / 2, zy + 1, slot.bodyId);

            InscriptionData.Combo combo = InscriptionData.resolve(slot);
            if (combo != null) {
                GuiUtils.blitBorder(poseStack, zx - 1, zy - 1, ZONE_W + 2, ZONE_H + 2, 0xFF40FF80);
            }
        }
    }

    private void drawCombos(PoseStack poseStack, int mouseX, int mouseY) {
        int y = topAlign + 20;
        int x = leftAlign + 110;
        font.drawShadow(poseStack, Component.translatable("gui.runetouched.combos"), x, topAlign + 8, 0x202020);
        for (InscriptionData.Combo c : data.activeCombos()) {
            font.drawShadow(poseStack, c.displayName, x, y, c.passive ? 0x106010 : 0x604010);
            y += 10;
        }
        if (data.firstActive() != null) {
            font.drawShadow(poseStack, Component.translatable("gui.runetouched.press_cast"), x, y + 4, 0x4040C0);
        }
    }

    private void drawManaBar(PoseStack poseStack) {
        int bx = leftAlign + 110, by = topAlign + imageHeight - 40;
        int manaScaled = menu.getManaScaled();
        int maxScaled = Math.max(1, menu.getMaxManaScaled());
        int w = 100;
        fill(poseStack, bx, by, bx + w, by + 6, 0xFF202020);
        int fw = (int) (w * (manaScaled / (double) maxScaled));
        fill(poseStack, bx, by, bx + fw, by + 6, 0xFF3060E0);
        font.drawShadow(poseStack, Component.translatable("gui.runetouched.mana",
                manaScaled / 10f, maxScaled / 10f), bx, by - 10, 0x303030);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        reloadData();
        for (int i = 0; i < 6; i++) {
            int zx = leftAlign + BODY_X + ZONES[i][0];
            int zy = topAlign + BODY_Y + ZONES[i][1];
            if (mouseX >= zx && mouseX < zx + ZONE_W && mouseY >= zy && mouseY < zy + ZONE_H) {
                if (button == 1) {
                    ModPackets.sendToServer(new ClearZonePayload(i));
                } else {
                    ModPackets.sendToServer(new PlaceRunePayload(i, true));
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
