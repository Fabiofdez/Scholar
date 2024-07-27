package io.github.mortuusars.scholar.screen;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.scholar.Config;
import io.github.mortuusars.scholar.Scholar;
import io.github.mortuusars.scholar.gui.BookUI;
import io.github.mortuusars.scholar.screen.textbox.TextBox;
import io.github.mortuusars.scholar.util.RenderUtil;
import io.github.mortuusars.scholar.visual.BookColors;
import io.github.mortuusars.scholar.visual.Formatting;
import io.netty.util.internal.StringUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Stream;

public class SpreadBookEditScreen extends Screen {

    private TextBox leftPageTextBox;
    private TextBox rightPageTextBox;

    public enum Side {
        LEFT,
        RIGHT;

        public int getSideIndex() {
            return ordinal();
        }

        public int getPageIndexFromSpread(int spreadIndex) {
            return spreadIndex * 2 + getSideIndex();
        }
    }

    public enum FormatTool {
        RESET("§r", "§r"), // 'unlabelled'
        BOLD("§lB§r", "§l"),
        ITALIC("§oI§r", "§o"),
        UNDERLINE("§nU§r", "§n"),
        STRIKETHROUGH("§mS§r", "§m");

        public final String LABEL;
        public final String SIGN;
        FormatTool(final String label, final String sign) {
            this.LABEL = label;
            this.SIGN = sign;
        }
    }

    public enum ColorTool {
        BLACK(0x333333,  "§0"),
        DARK_BLUE(0x0000AA,  "§1"),
        DARK_GREEN(0x00AA00,  "§2"),
        DARK_AQUA(0x00AAAA,  "§3"),
        DARK_RED(0xAA0000,  "§4"),
        DARK_PURPLE(0xAA00AA,  "§5"),
        GOLD(0xFFAA00,  "§6"),
        GRAY(0xBBBBBB,  "§7"),
        DARK_GRAY(0x777777,  "§8"),
        BLUE(0x5555FF,  "§9"),
        GREEN(0x55FF55,  "§a"),
        AQUA(0x55FFFF,  "§b"),
        RED(0xFF5555,  "§c"),
        LIGHT_PURPLE(0xFF55FF,  "§d"),
        YELLOW(0xFFFF55,  "§e"),
        WHITE(0xFFFFFF,  "§f");

        public final int LABEL_COLOR;
        public final String SIGN;
        ColorTool(final int color, final String sign) {
            this.LABEL_COLOR = color;
            this.SIGN = sign;
        }
    }

    public static final ResourceLocation TEXTURE = Scholar.resource("textures/gui/book_format_ext.png");

    public static final int BOOK_WIDTH = 295;
    public static final int BOOK_HEIGHT = 180;

    public static final int TEXT_LEFT_X = 23;
    public static final int TEXT_RIGHT_X = 158;
    public static final int TEXT_Y = 21;
    public static final int TEXT_WIDTH = 114;
    public static final int TEXT_HEIGHT = 128;

    public static final int SELECTION_COLOR = 0xFF664488;
    public static final int SELECTION_UNFOCUSED_COLOR = 0xFF775599;

    protected final Player owner;
    protected final ItemStack bookStack;
    protected final int bookColor;
    protected final InteractionHand hand;
    protected final int mainFontColor;
    protected final int secondaryFontColor;

    protected final List<String> pages = Lists.newArrayList();

    protected int leftPos;
    protected int topPos;
    public Button nextButton;
    public Button prevButton;
    public Button enterSignModeButton;
    @Nullable
    public Button insertSectionSignButton;
    public BookUI.ToggleImageButton formatToolsToggleButton;
    public List<BookUI.ToolImageButton> formatTools = Lists.newArrayList();
    public Map<Integer, BookUI.ToolImageButton> colorTools = new HashMap<>(16);

    protected int currentSpread;
    protected boolean isModified;
    protected boolean showFormatTools;

    public SpreadBookEditScreen(Player owner, ItemStack bookStack, InteractionHand hand) {
        super(GameNarrator.NO_TITLE);
        this.owner = owner;
        this.bookStack = bookStack;
        this.bookColor = BookColors.fromStack(bookStack);
        this.hand = hand;
        this.mainFontColor = Config.Client.getColor(Config.Client.MAIN_FONT_COLOR);
        this.secondaryFontColor = Config.Client.getColor(Config.Client.SECONDARY_FONT_COLOR);

        CompoundTag compoundtag = bookStack.getTag();
        if (compoundtag != null)
            BookViewScreen.loadPages(compoundtag, this.pages::add);

        while (this.pages.size() < 2) {
            this.pages.add("");
        }
    }

    @Override
    public boolean isPauseScreen() {
        return Config.Client.WRITABLE_PAUSE.get();
    }

    @Override
    public void tick() {
        leftPageTextBox.tick();
        rightPageTextBox.tick();
    }

    protected void init() {
        this.leftPos = (this.width - BOOK_WIDTH) / 2;
        this.topPos = (this.height - BOOK_HEIGHT) / 2;

        leftPageTextBox = new TextBox(font, leftPos + TEXT_LEFT_X, topPos + TEXT_Y, TEXT_WIDTH, TEXT_HEIGHT,
                () -> getPageText(Side.LEFT), text -> setPageText(Side.LEFT, text))
                .setFontColor(mainFontColor, mainFontColor)
                .setSelectionColor(SELECTION_COLOR, SELECTION_UNFOCUSED_COLOR);

        addRenderableWidget(leftPageTextBox);

        rightPageTextBox = new TextBox(font, leftPos + TEXT_RIGHT_X, topPos + TEXT_Y, TEXT_WIDTH, TEXT_HEIGHT,
                () -> getPageText(Side.RIGHT), text -> setPageText(Side.RIGHT, text))
                .setFontColor(mainFontColor, mainFontColor)
                .setSelectionColor(SELECTION_COLOR, SELECTION_UNFOCUSED_COLOR);

        addRenderableWidget(rightPageTextBox);

        BookUI.ImageButton prevButton = new BookUI.ImageButton(leftPos + 12, topPos + 156, 13, 15,
                295, 0, 15, TEXTURE, 512, 512,
                (b) -> this.pageBack());
        prevButton.setTooltip(Tooltip.create(Component.translatable("spectatorMenu.previous_page")));
        this.prevButton = addRenderableWidget(prevButton);

        BookUI.ImageButton nextButton = new BookUI.ImageButton(leftPos + 270, topPos + 156, 13, 15,
                308, 0, 15, TEXTURE, 512, 512,
                (b) -> this.pageForward());
        nextButton.setTooltip(Tooltip.create(Component.translatable("spectatorMenu.next_page")));
        this.nextButton = addRenderableWidget(nextButton);

        BookUI.ImageButton enterSignModeButton = new BookUI.ImageButton(leftPos - 24, topPos + 18, 22, 22,
                321, 0, 22, TEXTURE, 512, 512,
                (b) -> enterSignMode());
        enterSignModeButton.setMessage(Component.translatable("book.signButton"));
        enterSignModeButton.setTooltip(Tooltip.create(Component.translatable("book.signButton")));
        this.enterSignModeButton = addRenderableWidget(enterSignModeButton);

        if (isFormattingAllowed()) {
            BookUI.ImageButton insertSectionSignButton = new BookUI.ImageButton(width - 22, 2, 22, 22,
                    343, 0, 22, TEXTURE, 512, 512,
                    (b) -> insertSectionSign());
            insertSectionSignButton.setMessage(Component.translatable("gui.scholar.insert_section_sign"));
            MutableComponent tooltip = Component.translatable("gui.scholar.insert_section_sign")
                    .append(Component.literal(" [").withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.literal("CTRL+F").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY))
                    .append("\n\n")
                    .append(Component.translatable("gui.scholar.insert_section_sign.help1")
                            .withStyle(ChatFormatting.GRAY))
                    .append("\n\n")
                    .append(Component.translatable("gui.scholar.insert_section_sign.help2",
                                    Component.literal("F1").withStyle(ChatFormatting.GRAY))
                            .withStyle(ChatFormatting.DARK_GRAY)));
            insertSectionSignButton.setTooltip(Tooltip.create(tooltip));
            this.insertSectionSignButton = addRenderableOnly(insertSectionSignButton);

            BookUI.ToggleImageButton formatToolsToggle = new BookUI.ToggleImageButton(leftPos - 24, topPos + 50, 22, 22,
                365, 0, 22, 44, 66, TEXTURE, 512, 512,
                (b) -> toggleFormatTools(), showFormatTools);
            formatToolsToggle.setTooltips(
                Component.translatable("gui.scholar.hide_format_tools"),
                Component.translatable("gui.scholar.show_format_tools")
            );
            this.formatToolsToggleButton = this.addRenderableWidget(formatToolsToggle);

            createFormatAndColorTools();
        }

        this.updateButtonVisibility();

        this.createMenuControls();

        setInitialFocus(leftPageTextBox);
    }

    protected void createMenuControls() {
        if (Config.Client.WRITABLE_SHOW_DONE_BUTTON.get()) {
            this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE,
                    (button) -> this.onClose()).bounds(this.width / 2 - 60, topPos + BOOK_HEIGHT + 12, 120, 20).build());
        }
    }

    private float randomPitch(float min, float max) {
        return min + (float) Math.random() * (max - min);
    }

    protected void createFormatAndColorTools() {
        this.formatTools.clear();
        this.colorTools.clear();

        // Formatting Quill Nibs
        int buttonY = topPos + 17;
        for (FormatTool toolInfo : FormatTool.values()) {
            createFormatTool(toolInfo, buttonY);
            buttonY += 25;
        }

        // Color Ink Flasks
        ColorTool[] colorTools = ColorTool.values();
        int initialX = leftPos - 74;
        int initialY = topPos + 81;
        int xGap = 3, yGap = 7;
        for (int y = 0; y < 4; y++) {
            int rowY = initialY + (18 * y) + (yGap * y);
            for (int x = 0; x < 4; x++) {
                int toolX = initialX + (14 * x) + (xGap * x);
                createColorTool(colorTools[x + 4 * y], toolX, rowY);
            }
        }
    }

    protected void createFormatTool(FormatTool toolInfo, int y) {
        BookUI.LabeledImageButton btn = new BookUI.LabeledImageButton(
            leftPos + BOOK_WIDTH + 13, y, 56, 21,
            387, 0, 21, 42, 63, TEXTURE, 512, 512,
            (b) -> {
                insertFormatting(toolInfo.SIGN);
                Minecraft.getInstance().getSoundManager()
                    .play(SimpleSoundInstance.forUI(SoundEvents.ITEM_FRAME_ADD_ITEM, randomPitch(1f, 1.5f), 0.5f));
            }, font, 18, toolInfo.LABEL
        );
        btn.setLabelColors(0x603000, 0x502000);
        btn.visible = showFormatTools;
        formatTools.add(this.addRenderableOnly(btn));
    }

    protected void createColorTool(ColorTool toolInfo, int x, int y) {
        BookUI.ToolImageButton btn = new BookUI.ToolImageButton(
            x, y, 14, 18,
            443, 0, 18, 36, 54, TEXTURE, 512, 512,
            (b) -> {
                SoundManager soundMgr = Minecraft.getInstance().getSoundManager();
                insertFormatting(toolInfo.SIGN);
                soundMgr.play(SimpleSoundInstance.forUI(SoundEvents.ITEM_FRAME_ADD_ITEM, randomPitch(1f, 1.5f), 0.5f));
                soundMgr.play(SimpleSoundInstance.forUI(SoundEvents.BOTTLE_FILL, randomPitch(1.5f, 2f), 0.3f));
            }
        );
        btn.visible = showFormatTools;
        colorTools.put(toolInfo.LABEL_COLOR, this.addRenderableOnly(btn));
    }

    protected void toggleFormatTools() {
        this.showFormatTools = formatToolsToggleButton.isToggledOn();
        Minecraft.getInstance()
            .getSoundManager()
            .play(SimpleSoundInstance.forUI(SoundEvents.ARMOR_EQUIP_GOLD, randomPitch(0.8f, 1.2f), 0.8f));

        if (insertSectionSignButton != null) {
            insertSectionSignButton.visible = !showFormatTools;
        }
        formatTools.forEach((tool) -> tool.visible = showFormatTools);
        colorTools.values().forEach((tool) -> tool.visible = showFormatTools);
    }

    public boolean formatToolsShown() {
        return this.showFormatTools;
    }

    protected void enterSignMode() {
        Objects.requireNonNull(minecraft).setScreen(new BookSigningScreen(this, bookColor));
    }

    private void updateButtonVisibility() {
        this.prevButton.visible = this.currentSpread > 0;
        this.nextButton.visible = this.currentSpread < 49; // 100 pages max
    }

    private void clearDisplayCacheAfterPageChange() {
        leftPageTextBox.setCursorToEnd();
        rightPageTextBox.setCursorToEnd();
    }

    public void focusPage(Side side, Runnable action) {
        TextBox newFocus = side == Side.LEFT ? leftPageTextBox : rightPageTextBox;
        if (!newFocus.isFocused()) action.run();
        this.setFocused(side == Side.LEFT ? leftPageTextBox : rightPageTextBox);
    }

    protected void pageBack() {
        if (this.currentSpread > 0) {
            this.currentSpread--;
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 0.8f));

            this.updateButtonVisibility();
            this.clearDisplayCacheAfterPageChange();
        }
    }

    protected void pageForward() {
        if (this.currentSpread < 49) {
            this.currentSpread++;
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1f));
            while (this.pages.size() < (currentSpread + 1) * 2)
                appendPageToBook();

            this.updateButtonVisibility();
            this.clearDisplayCacheAfterPageChange();
        }
    }

    private void appendPageToBook() {
        if (this.pages.size() < 100) {
            this.pages.add("");
            this.isModified = true;
        }
    }

    protected String getPageText(Side side) {
        int pageIndex = side.getPageIndexFromSpread(currentSpread);
        return pageIndex >= 0 && pageIndex < this.pages.size() ? this.pages.get(pageIndex) : "";
    }

    protected void setPageText(Side side, String text) {
        int pageIndex = side.getPageIndexFromSpread(currentSpread);
        if (pageIndex >= 0 && pageIndex < this.pages.size()) {
            this.pages.set(pageIndex, text);
            this.isModified = true;
        }
    }

    public void saveChanges(boolean sign, @Nullable String title) {
        if (this.isModified || sign) {
            if (!sign)
                title = null;
            this.eraseEmptyTrailingPages();
            this.updateLocalCopy(sign, title);
            int slotId = this.hand == InteractionHand.MAIN_HAND ? this.owner.getInventory().selected : 40;

            Objects.requireNonNull(Minecraft.getInstance().getConnection()).send(
                    new ServerboundEditBookPacket(slotId, this.pages, Optional.ofNullable(title)));
        }
    }

    protected void eraseEmptyTrailingPages() {
        ListIterator<String> iterator = this.pages.listIterator(this.pages.size());
        while (iterator.hasPrevious() && iterator.previous().isEmpty()) {
            iterator.remove();
        }
    }

    protected void updateLocalCopy(boolean sign, @Nullable String title) {
        ListTag listTag = new ListTag();
        this.pages.stream().map(StringTag::valueOf).forEach(listTag::add);
        if (!this.pages.isEmpty()) {
            this.bookStack.addTagElement("pages", listTag);
        }

        if (sign) {
            Preconditions.checkState(!StringUtil.isNullOrEmpty(title), "Title cannot be null or empty when signing a book.");
            this.bookStack.addTagElement("author", StringTag.valueOf(this.owner.getGameProfile().getName()));
            this.bookStack.addTagElement("title", StringTag.valueOf(title));
        }

    }

    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        boolean typing = getFocused() instanceof TextBox;
        if (insertSectionSignButton != null) {
            insertSectionSignButton.active = typing;
        }
        formatTools.forEach((tool) -> tool.active = typing);
        colorTools.forEach((inkColor, tool) -> {
            tool.active = typing;
            if (!tool.visible) return;

            RenderUtil.withColorMultiplied(inkColor, () -> {
                // Ink Flask Color Label
                guiGraphics.blit(TEXTURE, tool.getX(), tool.getY() + 11, 457, 0,
                    14, 5, 512, 512);
            });
        });

        drawPageNumbers(guiGraphics, currentSpread);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        RenderUtil.withColorMultiplied(bookColor, () -> {
            // Cover
            guiGraphics.blit(TEXTURE, leftPos, topPos, BOOK_WIDTH, BOOK_HEIGHT,
                0, 0, BOOK_WIDTH, BOOK_HEIGHT, 512, 512);

            // Enter Sign Mode BG
            guiGraphics.blit(TEXTURE, leftPos - 29, topPos + 14, 0, 360,
                29, 28, 512, 512);

            // Format Tools Toggle BG
            guiGraphics.blit(TEXTURE, leftPos - 29, topPos + 46, 0, 388,
                29, 28, 512, 512);
        });

        // Pages
        guiGraphics.blit(TEXTURE, leftPos, topPos, BOOK_WIDTH, BOOK_HEIGHT,
            0, 180, BOOK_WIDTH, BOOK_HEIGHT, 512, 512);

        if (showFormatTools) {
            // Format Tools Bundle
            guiGraphics.blit(TEXTURE, leftPos + BOOK_WIDTH + 5, topPos + 6,
                295, 180, 40, 161, 512, 512);

            // Color Ink Shelves
            guiGraphics.blit(TEXTURE, leftPos - 81, topPos + 90,
                335, 180, 76, 88, 512, 512);
        }
    }

    protected void drawPageNumbers(GuiGraphics guiGraphics, int currentSpreadIndex) {
        String leftPageNumber = Integer.toString(currentSpreadIndex * 2 + 1);
        guiGraphics.drawString(font, leftPageNumber, leftPos + 69 + (8 - font.width(leftPageNumber) / 2),
                topPos + 157, secondaryFontColor, false);

        String rightPageNumber = Integer.toString(currentSpreadIndex * 2 + 2);
        guiGraphics.drawString(font, rightPageNumber, leftPos + 208 + (8 - font.width(rightPageNumber) / 2),
                topPos + 157, secondaryFontColor, false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (getFocused() instanceof TextBox
                && Screen.hasControlDown()
                && keyCode == InputConstants.KEY_F) {
            insertSectionSign();
            return true;
        }

        if (insertSectionSignButton != null && insertSectionSignButton.isHovered() && keyCode == InputConstants.KEY_F1) {
            openFormattingWikiPage();
            return true;
        }

        if (!(getFocused() instanceof TextBox)) {
            if (Minecraft.getInstance().options.keyInventory.matches(keyCode, scanCode)) {
                this.onClose();
                return true;
            }

            if (keyCode == InputConstants.KEY_LEFT || keyCode == InputConstants.KEY_PAGEUP || Minecraft.getInstance().options.keyLeft.matches(keyCode, scanCode)) {
                pageBack();
                return true;
            }

            if (keyCode == InputConstants.KEY_RIGHT || keyCode == InputConstants.KEY_PAGEDOWN || Minecraft.getInstance().options.keyRight.matches(keyCode, scanCode)) {
                pageForward();
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    protected void insertFormatting(String sign) {
        if (isFormattingAllowed() && getFocused() instanceof TextBox textBox) {
            textBox.textFieldHelper.insertText(sign);
        }
    }

    protected void insertSectionSign() {
        if (isFormattingAllowed() && getFocused() instanceof TextBox textBox) {
            textBox.textFieldHelper.insertText("§");
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(Scholar.SoundEvents.FORMATTING_CLICK.get(), 1f, 0.5f));
        }
    }

    protected boolean isFormattingAllowed() {
        return Config.Common.WRITABLE_SURVIVAL_FORMATTING.get() || (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isCreative());
    }

    protected void openFormattingWikiPage() {
        String page = "https://minecraft.wiki/Formatting_codes";

        try {
            URI uri = new URI(page);
            String protocol = uri.getScheme();
            if (protocol == null)
                throw new URISyntaxException(page, "Missing protocol");
            if (!Sets.newHashSet("http", "https").contains(protocol.toLowerCase(Locale.ROOT)))
                throw new URISyntaxException(page, "Unsupported protocol: " + protocol.toLowerCase(Locale.ROOT));

            Minecraft.getInstance().setScreen(new ConfirmLinkScreen(shouldOpen -> {
                if (shouldOpen)
                    Util.getPlatform().openUri(uri);
                Minecraft.getInstance().setScreen(this);
            }, page, true));
        } catch (URISyntaxException uRISyntaxException) {
            LogUtils.getLogger().error("Can't open url {} - {}", page, uRISyntaxException);
        }
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        boolean handled = super.charTyped(pCodePoint, pModifiers);

        if (handled && getFocused() instanceof TextBox textBox) {
            onTextBoxCharTyped(textBox);
        }

        return handled;
    }

    private static void onTextBoxCharTyped(TextBox textBox) {
        // Plays a sound when formatting code char is typed:

        int cursorPos = textBox.textFieldHelper.getCursorPos();
        String text = textBox.getText();

        if (cursorPos < 2 || cursorPos > text.length())
            return;

        int sectionSymbolIndex = cursorPos - 2;
        int formattingCharIndex = sectionSymbolIndex + 1;
        String enteredFormattingCode = text.substring(sectionSymbolIndex, formattingCharIndex + 1);

        for (Formatting formatting : Formatting.values()) {
            if (formatting.getCode().equals(enteredFormattingCode)) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(Scholar.SoundEvents.FORMATTING_CLICK.get(), 1f, 0.5f));
                return;
            }
        }
    }

    private Optional<BookUI.ToolImageButton> getHoveredTool() {
        return Stream.concat(formatTools.stream(), colorTools.values().stream())
            .filter(BookUI.ToolImageButton::isHovered)
            .filter((btn) -> {
                if (btn.hasControllerHover()) {
                    btn.setControllerHover(false);
                    return false;
                }
                return true;
            })
            .findFirst();
    }



    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (insertSectionSignButton != null && insertSectionSignButton.isHovered()) {
            insertSectionSign();
            return true;
        }
        Optional<BookUI.ToolImageButton> hoveredTool = getHoveredTool();

        if (hoveredTool.isPresent()) {
            hoveredTool.get().onClick(mouseX, mouseY);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Optional<BookUI.ToolImageButton> hoveredTool = getHoveredTool();

        if (hoveredTool.isPresent()) {
            hoveredTool.get().onRelease(mouseX, mouseY);
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        saveChanges(false, null);
        super.onClose();
    }
}
