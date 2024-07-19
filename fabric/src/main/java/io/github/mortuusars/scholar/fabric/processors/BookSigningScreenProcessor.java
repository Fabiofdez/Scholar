package io.github.mortuusars.scholar.fabric.processors;

import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.virtualmouse.VirtualMouseBehaviour;
import io.github.mortuusars.scholar.fabric.ControllerBindings;
import io.github.mortuusars.scholar.screen.BookSigningScreen;

public class BookSigningScreenProcessor<T extends BookSigningScreen> extends ScreenProcessor<T> {

  public BookSigningScreenProcessor(T screen) {
    super(screen);
  }

  @Override
  protected void handleButtons(ControllerEntity controller) {
    if (ControllerBindings.BOOK_FINALIZE.on(controller).justPressed()) {
      this.screen.signButton.onPress();
    } else if (ControllerBindings.BOOK_CANCEL_SIGN.on(controller).justPressed()) {
      this.screen.cancelSigningButton.onPress();
    }
  }

  @Override
  public VirtualMouseBehaviour virtualMouseBehaviour() {
    return VirtualMouseBehaviour.DISABLED;
  }
}
