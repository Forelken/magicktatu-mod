package com.runetouched.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.runetouched.data.Runes;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public final class GuiUtils {
    public static final ResourceLocation GUI_TEX = Runes.rl("textures/gui/inscription.png");

    public static void blitBorder(PoseStack poseStack, int x, int y, int w, int h, int color) {
        GuiComponent.fill(poseStack, x, y, x + w, y + 1, color);
        GuiComponent.fill(poseStack, x, y + h - 1, x + w, y + h, color);
        GuiComponent.fill(poseStack, x, y, x + 1, y + h, color);
        GuiComponent.fill(poseStack, x + w - 1, y, x + w, y + h, color);
    }

    /** Draws a 16x16 rune icon using the registered texture of the rune item. */
    public static void drawRuneIcon(PoseStack poseStack, int x, int y, String runeId) {
        ResourceLocation tex = Runes.rl("textures/rune/" + runeId + ".png");
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShaderTexture(0, tex);
        GuiComponent.blit(poseStack, x, y, 0, 0, 16, 16, 16, 16);
    }

    private GuiUtils() {}
}
