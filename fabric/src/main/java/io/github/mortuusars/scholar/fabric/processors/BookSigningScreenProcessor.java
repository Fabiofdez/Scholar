package io.github.mortuusars.scholar.fabric.processors;

import dev.isxander.controlify.controller.ControllerEntity;
import io.github.mortuusars.scholar.fabric.ControllerBindings;
import io.github.mortuusars.scholar.screen.BookSigningScreen;

public class BookSigningScreenProcessor<T extends BookSigningScreen> extends BookScreenProcessor<T> {

  public BookSigningScreenProcessor(T screen) {
    super(screen);
  }

  @Override
  protected void setInitialFocus() {
  }

  @Override
  protected void handleButtons(ControllerEntity controller) {
    super.handleButtons(controller);
    if (ControllerBindings.BOOK_FINALIZE.on(controller).justPressed()) {
      this.screen.signButton.onPress();
    } else if (ControllerBindings.BOOK_CANCEL_SIGN.on(controller).justPressed()) {
      this.screen.cancelSigningButton.onPress();
    }
  }

  @Override
  protected void buildGuides() {
    setLeftLayout(
        makeRow(
            makeGuideFor(ControllerBindings.BOOK_FINALIZE, (screen) -> ((BookSigningScreen) screen).signButton.active, false)
        )
    );

    setRightLayout(
        makeRow(
            makeGuideFor(ControllerBindings.BOOK_CANCEL_SIGN, ALWAYS, true)
        )
    );
  }
}
