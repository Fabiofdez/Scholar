package io.github.mortuusars.scholar.fabric.processors;

import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.sound.ControlifyClientSounds;
import io.github.mortuusars.scholar.fabric.ControllerBindings;
import io.github.mortuusars.scholar.gui.BookUI;
import io.github.mortuusars.scholar.screen.SpreadBookEditScreen;
import io.github.mortuusars.scholar.screen.SpreadBookEditScreen.ColorTool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class SpreadBookEditScreenProcessor<T extends SpreadBookEditScreen> extends BookScreenProcessor<T> {
    private List<BookUI.ToolImageButton> formatTools;
    private List<BookUI.ToolImageButton> colorTools;
    private BookUI.ToolImageButton currentTool = null;
    private boolean cycleThroughColorTools = false;
    private int currentToolIdx = -1;

    public SpreadBookEditScreenProcessor(T screen) {
        super(screen);
    }

    @Override
    protected void setInitialFocus() {
    }

    @Override
    protected void handleTabNavigation(ControllerEntity controller) {
        super.handleTabNavigation(controller);
        if (ControllerBindings.BOOK_PREV_PAGE.on(controller).justPressed()) {
            this.screen.prevButton.onPress();
        } else if (ControllerBindings.BOOK_NEXT_PAGE.on(controller).justPressed()) {
            this.screen.nextButton.onPress();
        }
    }

    @Override
    protected void handleButtons(ControllerEntity controller) {
        super.handleButtons(controller);
        if (ControllerBindings.BOOK_SIGN.on(controller).justPressed()) {
            playClackSound();
            this.screen.enterSignModeButton.onPress();
        } else if (ControllerBindings.BOOK_FORMATTING.on(controller).justPressed()) {
            this.screen.formatToolsToggleButton.onPress();
        } else if (ControlifyBindings.GUI_PRESS.on(controller).justPressed() && this.screen.formatToolsShown() && currentTool != null) {
            getToolCoords((x, y) -> currentTool.onClick(x, y));
        } else if (ControlifyBindings.GUI_PRESS.on(controller).justReleased() && this.screen.formatToolsShown() && currentTool != null) {
            getToolCoords((x, y) -> currentTool.onRelease(x, y));
        } else if (ControlifyBindings.GUI_BACK.on(controller).justPressed()) {
            this.screen.onClose();
        }
    }

    @Override
    protected void handleComponentNavigation(ControllerEntity controller) {
        super.handleComponentNavigation(controller);
        Runnable playPageFocusChange = () -> Minecraft.getInstance().getSoundManager()
            .play(SimpleSoundInstance.forUI(ControlifyClientSounds.SCREEN_FOCUS_CHANGE.get(), 1f));
        if (ControlifyBindings.GUI_NAVI_LEFT.on(controller).justPressed()) {
            this.screen.focusPage(SpreadBookEditScreen.Side.LEFT, playPageFocusChange);
        } else if (ControlifyBindings.GUI_NAVI_RIGHT.on(controller).justPressed()) {
            this.screen.focusPage(SpreadBookEditScreen.Side.RIGHT, playPageFocusChange);
        }
        if (!this.screen.formatToolsShown()) return;
        if (ControllerBindings.PICK_COLOR_TOOLS.on(controller).justPressed() && !cycleThroughColorTools) {
            cycleThroughColorTools = true;
            currentToolIdx = -1;
            moveToTool(1);
        } else if (ControllerBindings.PICK_FORMAT_TOOLS.on(controller).justPressed() && cycleThroughColorTools) {
            cycleThroughColorTools = false;
            currentToolIdx = -1;
            moveToTool(1);
        }
        InputBinding toPrevTool = ControllerBindings.FORMAT_TOOLS_PREV.on(controller);
        InputBinding toNextTool = ControllerBindings.FORMAT_TOOLS_NEXT.on(controller);

        if (holdRepeatHelper.shouldAction(toPrevTool)) {
            moveToTool(-1);
            holdRepeatHelper.onNavigate();
        } else if (holdRepeatHelper.shouldAction(toNextTool)) {
            moveToTool(1);
            holdRepeatHelper.onNavigate();
        }
    }

    @Override
    protected void buildGuides() {
        setLeftLayout(
            makeRow(
                makeGuideFor(ControllerBindings.BOOK_EXIT, ALWAYS, false)
            ),
            makeRow(
                makeGuideFor(ControllerBindings.FORMAT_TOOLS_PREV,
                    (screen) -> ((SpreadBookEditScreen) screen).formatToolsShown(), false)
            ),
            makeRow(
                makeGuideFor(ControllerBindings.BOOK_PREV_PAGE, ALWAYS, false),
                makeGuideFor(ControllerBindings.BOOK_SIGN, ALWAYS, false)
            )
        );

        setRightLayout(
            makeRow(
                makeGuideFor(ControllerBindings.BOOK_FORMATTING, ALWAYS, true)
            ),
            makeRow(
                makeGuideFor(ControllerBindings.FORMAT_TOOLS_NEXT,
                    (screen) -> ((SpreadBookEditScreen) screen).formatToolsShown(), true)
            ),
            makeRow(
                makeGuideFor(ControllerBindings.BOOK_NEXT_PAGE, ALWAYS, true)
            )
        );
    }

    private void updateColorToolRefs() {
        if (this.colorTools == null) {
            this.colorTools = new ArrayList<>();
        } else if (!this.colorTools.isEmpty()) {
            BookUI.ToolImageButton currentFirstRef = this.colorTools.get(0);
            BookUI.ToolImageButton realFirstRef = this.screen.colorTools.get(ColorTool.values()[0].LABEL_COLOR);
            if (currentFirstRef == realFirstRef) return;
            else this.colorTools.clear();
        }
        for (ColorTool toolInfo : ColorTool.values()) {
            if (!this.screen.colorTools.containsKey(toolInfo.LABEL_COLOR)) continue;
            this.colorTools.add(this.screen.colorTools.get(toolInfo.LABEL_COLOR));
        }
    }
    
    private void moveToTool(int offset) {
        if (this.formatTools == null) {
            this.formatTools = this.screen.formatTools;
        }
        updateColorToolRefs();
        List<BookUI.ToolImageButton> tools = cycleThroughColorTools ? this.colorTools : this.formatTools;
        int newIdx = currentToolIdx + offset;

        if (currentTool != null) {
            currentTool.setControllerHover(false);
            currentTool.setPressed(false);
            currentTool = null;
        }
        if (tools.isEmpty() || !tools.get(0).isActive()) {
            currentToolIdx = -1;
            return;
        }
        if (newIdx >= 0 && newIdx < tools.size()) {
            currentTool = tools.get(newIdx);
            currentTool.setControllerHover(true);
            playClackSound();
        }
        if (newIdx >= -1 && newIdx <= tools.size()) {
            currentToolIdx = newIdx;
        }
    }

    private void getToolCoords(BiConsumer<Integer, Integer> action) {
        int clickX = currentTool.getX() + currentTool.getWidth() / 2;
        int clickY = currentTool.getY() + currentTool.getHeight() / 2;
        action.accept(clickX, clickY);
    }
}
