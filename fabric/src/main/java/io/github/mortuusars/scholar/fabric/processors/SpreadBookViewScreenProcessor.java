package io.github.mortuusars.scholar.fabric.processors;

import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import io.github.mortuusars.scholar.fabric.ControllerBindings;
import io.github.mortuusars.scholar.screen.LecternSpreadScreen;
import io.github.mortuusars.scholar.screen.SpreadBookViewScreen;
import net.minecraft.network.chat.Component;

public class SpreadBookViewScreenProcessor<T extends SpreadBookViewScreen> extends BookScreenProcessor<T> {
  public SpreadBookViewScreenProcessor(T screen) {
    super(screen);
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
    if (ControlifyBindings.GUI_BACK.on(controller).justPressed()) {
      this.screen.onClose();
    } else if (this.screen instanceof LecternSpreadScreen && ControllerBindings.LECTERN_TAKE_BOOK.on(controller).justPressed()) {
      ((LecternSpreadScreen) this.screen).takeBook();
    }
  }

  @Override
  protected void buildGuides() {
    setLeftLayout(
        makeRow(
            makeGuideFor(ControllerBindings.BOOK_EXIT, ALWAYS, false)
        ),
        makeRow(
            makeGuideFor(ControllerBindings.BOOK_PREV_PAGE, ALWAYS, false)
        )
    );

    setRightLayout(
        makeRow(
            makeGuideFor(ControllerBindings.BOOK_NEXT_PAGE, ALWAYS, true)
        )
    );

    getWidget(Component.translatable("lectern.take_book"))
        .ifPresent((btn) -> ButtonGuideApi.addGuideToButton(btn,
            ControllerBindings.LECTERN_TAKE_BOOK,
            (b) -> screen instanceof LecternSpreadScreen
        ));
  }
}
