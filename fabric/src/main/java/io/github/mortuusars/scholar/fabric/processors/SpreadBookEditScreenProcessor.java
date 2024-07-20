package io.github.mortuusars.scholar.fabric.processors;

import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ScreenProcessor;
import io.github.mortuusars.scholar.fabric.ControllerBindings;
import io.github.mortuusars.scholar.screen.SpreadBookEditScreen;
import net.minecraft.client.gui.components.Button;

import java.util.Optional;

public class SpreadBookEditScreenProcessor<T extends SpreadBookEditScreen> extends ScreenProcessor<T> {
  public SpreadBookEditScreenProcessor(T screen) {
    super(screen);
  }

  @Override
  protected void handleTabNavigation(ControllerEntity controller) {
    if (ControllerBindings.BOOK_PREV_PAGE.on(controller).justPressed()) {
      this.screen.prevButton.onPress();
    } else if (ControllerBindings.BOOK_NEXT_PAGE.on(controller).justPressed()) {
      this.screen.nextButton.onPress();
    }
  }

  @Override
  protected void handleButtons(ControllerEntity controller) {
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
}
