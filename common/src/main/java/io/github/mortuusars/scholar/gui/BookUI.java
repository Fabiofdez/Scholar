package io.github.mortuusars.scholar.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class BookUI {
    public static class ImageButton extends Button {
        protected final ResourceLocation TEXTURE;
        protected final int U_OFFSET;
        protected final int V_OFFSET;
        protected final int HOVER_V_OFFSET;
        protected final int FOCUSED_V_OFFSET;
        protected final int DISABLED_V_OFFSET;
        protected final int TEXTURE_WIDTH;
        protected final int TEXTURE_HEIGHT;

        public ImageButton(int x, int y, int width, int height,
                           int uOffset, int vOffset, int focusedVOffset,
                           ResourceLocation texture, int textureWidth, int textureHeight, OnPress onPress) {
            this(x, y, width, height,
                uOffset, vOffset, -1, focusedVOffset, 2 * focusedVOffset - vOffset,
                texture, textureWidth, textureHeight, onPress);
        }

        public ImageButton(int x, int y, int width, int height,
                           int uOffset, int vOffset, int hoveredVOffset, int focusedVOffset, int disabledVOffset,
                           ResourceLocation texture, int textureWidth, int textureHeight, OnPress onPress) {
            super(x, y, width, height, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
            this.TEXTURE = texture;
            this.U_OFFSET = uOffset;
            this.V_OFFSET = vOffset;
            this.HOVER_V_OFFSET = hoveredVOffset;
            this.FOCUSED_V_OFFSET = focusedVOffset;
            this.DISABLED_V_OFFSET = disabledVOffset;
            this.TEXTURE_WIDTH = textureWidth;
            this.TEXTURE_HEIGHT = textureHeight;
        }

        protected int getVOffset() {
            if (!isActive()) return DISABLED_V_OFFSET;
            if (isFocused()) return FOCUSED_V_OFFSET;
            if (isHovered() && HOVER_V_OFFSET >= 0) return HOVER_V_OFFSET;
            return V_OFFSET;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.blit(TEXTURE, getX(), getY(), getWidth(), getHeight(),
                U_OFFSET, getVOffset(), getWidth(), getHeight(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }
    }

    public static class ToggleImageButton extends ImageButton {
        private final int ON_V_OFFSET;
        private final int ON_FOCUSED_V_OFFSET;
        private Component msgWhenOn;
        private Component msgWhenOff;
        private boolean toggledOn;

        public ToggleImageButton(int x, int y, int width, int height,
                                 int uOffset, int vOffset, int focusedVOffset, int toggledVOffset, int toggledFocusedVOffset,
                                 ResourceLocation texture, int textureWidth, int textureHeight, OnPress onPress, boolean initializeOn) {
            super(x, y, width, height, uOffset, vOffset, focusedVOffset, texture, textureWidth, textureHeight, onPress);
            this.ON_V_OFFSET = toggledVOffset;
            this.ON_FOCUSED_V_OFFSET = toggledFocusedVOffset;
            this.toggledOn = initializeOn;
        }

        public void setTooltips(@NotNull Component msgOn, @NotNull Component msgOff) {
            this.msgWhenOn = msgOn;
            this.msgWhenOff = msgOff;
            updateTooltip();
        }

        private void updateTooltip() {
            Component msg = toggledOn ? msgWhenOn : msgWhenOff;
            this.setMessage(msg);
            this.setTooltip(Tooltip.create(msg));
        }

        public boolean isToggledOn() {
            return toggledOn;
        }

        @Override
        public void onPress() {
            toggledOn = !toggledOn;
            updateTooltip();
            super.onPress();
        }

        @Override
        protected int getVOffset() {
            return isHoveredOrFocused()
                ? toggledOn ? ON_FOCUSED_V_OFFSET : FOCUSED_V_OFFSET
                : toggledOn ? ON_V_OFFSET : V_OFFSET;
        }
    }

    public static class ToolImageButton extends ImageButton {
        private boolean isPressed;
        private boolean hasControllerHover;

        public ToolImageButton(int x, int y, int width, int height,
                                  int uOffset, int vOffset, int hoveredVOffset, int focusedVOffset, int disabledVOffset,
                                  ResourceLocation texture, int textureWidth, int textureHeight, OnPress onPress) {
            super(x, y, width, height,
                uOffset, vOffset, hoveredVOffset, focusedVOffset, disabledVOffset,
                texture, textureWidth, textureHeight, onPress);
        }

        @Override
        public void onRelease(double mouseX, double mouseY) {
            this.setPressed(false);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            if (!this.isActive()) return;
            this.setPressed(true);
            super.onPress();
        }

        @Override
        public boolean isHovered() {
            return super.isHovered() || this.hasControllerHover;
        }

        @Override
        public boolean isFocused() {
            return super.isFocused() || this.isPressed;
        }

        public void setPressed(boolean pressed) {
            this.isPressed = pressed;
        }

        public boolean hasControllerHover() {
            return this.hasControllerHover;
        }

        public void setControllerHover(boolean hover) {
            this.hasControllerHover = hover;
        }
    }

    public static class LabeledImageButton extends ToolImageButton {
        private final int LABEL_X;
        private final String LABEL;
        private final Font FONT;
        private int labelColor = 0xFFFFFF;
        private int disabledLabelColor = 0xAAAAAA;

        public LabeledImageButton(int x, int y, int width, int height,
                                  int uOffset, int vOffset, int hoveredVOffset, int focusedVOffset, int disabledVOffset,
                                  ResourceLocation texture, int textureWidth, int textureHeight, OnPress onPress, Font font, int labelX, String label) {

            super(x, y, width, height,
                uOffset, vOffset, hoveredVOffset, focusedVOffset, disabledVOffset,
                texture, textureWidth, textureHeight, onPress);
            this.LABEL_X = labelX;
            this.LABEL = label;
            this.FONT = font;
        }

        public void setLabelColors(int active, int disabled) {
            this.labelColor = active;
            this.disabledLabelColor = disabled;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

            int labelXOffset = isActive() ? isHoveredOrFocused() ? 8 : 0 : -1;
            int labelX = getX() + LABEL_X + labelXOffset - (FONT.width(FormattedText.of(LABEL)) / 2);
            int labelY = getY() + (getHeight() - FONT.lineHeight) / 2;
            int color = this.active ? labelColor : disabledLabelColor;

            guiGraphics.drawString(FONT, LABEL, labelX, labelY, color, false);
        }
    }
}
