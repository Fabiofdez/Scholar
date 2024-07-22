package io.github.mortuusars.scholar.fabric.processors;

import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import io.github.mortuusars.scholar.fabric.ControllerBindings;
import io.github.mortuusars.scholar.screen.SpreadBookEditScreen;
import net.minecraft.client.gui.components.Button;

import java.util.Optional;

public class SpreadBookEditScreenProcessor<T extends SpreadBookEditScreen> extends BookScreenProcessor<T> {
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
      this.screen.enterSignModeButton.onPress();
    } else if (ControllerBindings.BOOK_INSERT_FORMAT.on(controller).justPressed()) {
      Optional.ofNullable(this.screen.insertSectionSignButton).ifPresent(Button::onPress);
    } else if (ControlifyBindings.GUI_BACK.on(controller).justPressed()) {
      this.screen.onClose();
    }
    Optional.ofNullable(this.screen.insertSectionSignButton)
        .ifPresent((b) -> b.setFocused(ControllerBindings.BOOK_INSERT_FORMAT.on(controller).digitalNow()));
  }

  @Override
  protected void buildGuides() {
    setLeftLayout(
        makeRow(
            makeGuideFor(ControllerBindings.BOOK_EXIT, ALWAYS, false)
        ),
        makeRow(
            makeGuideFor(ControllerBindings.BOOK_PREV_PAGE, ALWAYS, false),
            makeGuideFor(ControllerBindings.BOOK_SIGN, ALWAYS, false)
        )
    );

    setRightLayout(
        makeRow(
            makeGuideFor(ControllerBindings.BOOK_INSERT_FORMAT, ALWAYS, true)
        ),
        makeRow(
            makeGuideFor(ControllerBindings.BOOK_NEXT_PAGE, ALWAYS, true)
        )
    );
  }
}
