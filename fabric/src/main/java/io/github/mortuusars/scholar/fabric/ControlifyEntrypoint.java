package io.github.mortuusars.scholar.fabric;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import io.github.mortuusars.scholar.fabric.processors.BookSigningScreenProcessor;
import io.github.mortuusars.scholar.fabric.processors.SpreadBookEditScreenProcessor;
import io.github.mortuusars.scholar.fabric.processors.SpreadBookViewScreenProcessor;
import io.github.mortuusars.scholar.screen.BookSigningScreen;
import io.github.mortuusars.scholar.screen.LecternSpreadScreen;
import io.github.mortuusars.scholar.screen.SpreadBookEditScreen;
import io.github.mortuusars.scholar.screen.SpreadBookViewScreen;

public class ControlifyEntrypoint implements dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint {
  @Override
  public void onControllersDiscovered(ControlifyApi controlifyApi) {
  }

  @Override
  public void onControlifyInit(ControlifyApi controlify) {
    ControllerBindings.init();

    ScreenProcessorProvider.registerProvider(
        SpreadBookViewScreen.class,
        SpreadBookViewScreenProcessor::new
    );
    ScreenProcessorProvider.registerProvider(
        LecternSpreadScreen.class,
        SpreadBookViewScreenProcessor::new
    );
    ScreenProcessorProvider.registerProvider(
        SpreadBookEditScreen.class,
        SpreadBookEditScreenProcessor::new
    );
    ScreenProcessorProvider.registerProvider(
        BookSigningScreen.class,
        BookSigningScreenProcessor::new
    );
  }
}
