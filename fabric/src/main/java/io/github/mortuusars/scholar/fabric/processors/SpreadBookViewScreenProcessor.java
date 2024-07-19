package io.github.mortuusars.scholar.fabric.processors;

import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.virtualmouse.VirtualMouseBehaviour;
import io.github.mortuusars.scholar.fabric.ControllerBindings;
import io.github.mortuusars.scholar.screen.LecternSpreadScreen;
import io.github.mortuusars.scholar.screen.SpreadBookViewScreen;
import net.minecraft.network.chat.Component;

public class SpreadBookViewScreenProcessor<T extends SpreadBookViewScreen> extends ScreenProcessor<T> {
  public SpreadBookViewScreenProcessor(T screen) {
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
    if (ControlifyBindings.GUI_BACK.on(controller).justPressed()) {
      this.screen.onClose();
    } else if (this.screen instanceof LecternSpreadScreen && ControllerBindings.LECTERN_TAKE_BOOK.on(controller).justPressed()) {
      ((LecternSpreadScreen) this.screen).takeBook();
    }
  }

  @Override
  public VirtualMouseBehaviour virtualMouseBehaviour() {
    return VirtualMouseBehaviour.DISABLED;
  }

  @Override
  public void onWidgetRebuild() {
    super.onWidgetRebuild();

    getWidget(Component.translatable("lectern.take_book")).ifPresent((doneButton) -> {
      ButtonGuideApi.addGuideToButton(
          doneButton,
          ControllerBindings.LECTERN_TAKE_BOOK,
          ButtonGuidePredicate.always()
      );
    });
  }
}
