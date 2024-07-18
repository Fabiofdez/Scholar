package io.github.mortuusars.scholar.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;

public class BookUI {
  public static class ImageButton extends Button {
    private final ResourceLocation TEXTURE;
    private final int U_OFFSET;
    private final int V_OFFSET;
    private final int FOCUSED_V_OFFSET;
    private final int TEXTURE_WIDTH;
    private final int TEXTURE_HEIGHT;

    public ImageButton(int x, int y, int width, int height,
                       int uOffset, int vOffset, int focusedVOffset, ResourceLocation texture, int textureWidth, int textureHeight, OnPress onPress) {
      super(x, y, width, height, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
      this.TEXTURE = texture;
      this.U_OFFSET = uOffset;
      this.V_OFFSET = vOffset;
      this.FOCUSED_V_OFFSET = focusedVOffset;
      this.TEXTURE_WIDTH = textureWidth;
      this.TEXTURE_HEIGHT = textureHeight;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      int vOffset = isHoveredOrFocused() ? FOCUSED_V_OFFSET : V_OFFSET;

      if (!isActive())
        vOffset = 2 * (FOCUSED_V_OFFSET - V_OFFSET);

      guiGraphics.blit(TEXTURE, getX(), getY(), getWidth(), getHeight(),
          U_OFFSET, vOffset, getWidth(), getHeight(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
  }
}
