package io.github.mortuusars.scholar.fabric;

import dev.isxander.controlify.api.bind.ControlifyBindApi;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.bindings.BindContext;
import io.github.mortuusars.scholar.Scholar;
import io.github.mortuusars.scholar.screen.BookSigningScreen;
import io.github.mortuusars.scholar.screen.SpreadBookEditScreen;
import io.github.mortuusars.scholar.screen.SpreadBookViewScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

import static dev.isxander.controlify.bindings.BindContext.REGISTRY;

public class ControllerBindings {
    public static InputBindingSupplier BOOK_PREV_PAGE;
    public static InputBindingSupplier BOOK_NEXT_PAGE;
    public static InputBindingSupplier BOOK_FORMATTING;
    public static InputBindingSupplier FORMAT_TOOL_CLICK;
    public static InputBindingSupplier FORMAT_TOOLS_PREV;
    public static InputBindingSupplier FORMAT_TOOLS_NEXT;
    public static InputBindingSupplier PICK_COLOR_TOOLS;
    public static InputBindingSupplier PICK_FORMAT_TOOLS;
    public static InputBindingSupplier BOOK_SIGN;
    public static InputBindingSupplier BOOK_FINALIZE;
    public static InputBindingSupplier BOOK_CANCEL_SIGN;
    public static InputBindingSupplier BOOK_EXIT;
    public static InputBindingSupplier LECTERN_TAKE_BOOK;

    public static void init() {
        Component scholarCategory = Component.translatable("gui.scholar.controlify.category");

        BindContext signContext = register("book_sign", (mc) -> mc.screen instanceof BookSigningScreen);
        BindContext viewContext = register("spread_book_view", (mc) -> mc.screen instanceof SpreadBookViewScreen);
        BindContext editContext = register("spread_book_edit", (mc) -> mc.screen instanceof SpreadBookEditScreen);

        BOOK_PREV_PAGE = ControlifyBindApi.get().registerBinding((builder) -> builder
            .id(Scholar.resource("book_prev_page"))
            .category(scholarCategory)
            .name(Component.translatable("gui.scholar.controlify.book_prev_page"))
            .description(Component.translatable("spectatorMenu.previous_page"))
            .allowedContexts(viewContext, editContext));

        BOOK_NEXT_PAGE = ControlifyBindApi.get().registerBinding((builder) -> builder
            .id(Scholar.resource("book_next_page"))
            .category(scholarCategory)
            .name(Component.translatable("gui.scholar.controlify.book_next_page"))
            .description(Component.translatable("spectatorMenu.next_page"))
            .allowedContexts(viewContext, editContext));

        BOOK_FORMATTING = ControlifyBindApi.get().registerBinding((builder) -> builder
            .id(Scholar.resource("book_toggle_format"))
            .category(scholarCategory)
            .name(Component.translatable("gui.scholar.controlify.toggle_format_tools"))
            .description(Component.translatable("gui.scholar.controlify.toggle_format_tools"))
            .allowedContexts(editContext)
        );

        FORMAT_TOOL_CLICK = ControlifyBindApi.get().registerBinding((builder) -> builder
            .id(Scholar.resource("format_tool_click"))
            .category(scholarCategory)
            .name(Component.translatable("gui.scholar.controlify.format_tool_click"))
            .allowedContexts(editContext)
        );

        PICK_COLOR_TOOLS = ControlifyBindApi.get().registerBinding((builder) -> builder
            .id(Scholar.resource("pick_color_tools"))
            .category(scholarCategory)
            .name(Component.translatable("gui.scholar.controlify.pick_color_tools"))
            .allowedContexts(editContext)
        );

        PICK_FORMAT_TOOLS = ControlifyBindApi.get().registerBinding((builder) -> builder
            .id(Scholar.resource("pick_format_tools"))
            .category(scholarCategory)
            .name(Component.translatable("gui.scholar.controlify.pick_format_tools"))
            .allowedContexts(editContext)
        );

        FORMAT_TOOLS_PREV = ControlifyBindApi.get().registerBinding((builder) -> builder
            .id(Scholar.resource("format_prev_tool"))
            .category(scholarCategory)
            .name(Component.translatable("gui.scholar.controlify.format_prev_tool"))
            .description(Component.translatable("gui.scholar.controlify.format_prev_tool.desc"))
            .allowedContexts(editContext)
        );

        FORMAT_TOOLS_NEXT = ControlifyBindApi.get().registerBinding((builder) -> builder
            .id(Scholar.resource("format_next_tool"))
            .category(scholarCategory)
            .name(Component.translatable("gui.scholar.controlify.format_next_tool"))
            .description(Component.translatable("gui.scholar.controlify.format_next_tool.desc"))
            .allowedContexts(editContext)
        );

        BOOK_SIGN = ControlifyBindApi.get().registerBinding((builder) -> builder
            .id(Scholar.resource("book_sign"))
            .category(scholarCategory)
            .name(Component.translatable("gui.scholar.controlify.book_sign"))
            .description(Component.translatable("book.signButton"))
            .allowedContexts(editContext));

        BOOK_FINALIZE = ControlifyBindApi.get().registerBinding((builder) -> builder
            .id(Scholar.resource("book_finalize"))
            .category(scholarCategory)
            .name(Component.translatable("gui.scholar.controlify.book_finalize"))
            .description(Component.translatable("book.finalizeButton"))
            .allowedContexts(signContext));

        BOOK_CANCEL_SIGN = ControlifyBindApi.get().registerBinding((builder) -> builder
            .id(Scholar.resource("book_cancel_sign"))
            .category(scholarCategory)
            .name(Component.translatable("gui.scholar.controlify.book_cancel_sign"))
            .description(CommonComponents.GUI_CANCEL)
            .allowedContexts(signContext));

        BOOK_EXIT = ControlifyBindApi.get().registerBinding((builder) -> builder
            .id(Scholar.resource("book_exit"))
            .category(scholarCategory)
            .name(Component.translatable("controlify.guide.container.exit"))
            .description(Component.translatable("gui.scholar.controlify.book_exit.desc"))
            .allowedContexts(viewContext, editContext));

        LECTERN_TAKE_BOOK = ControlifyBindApi.get().registerBinding((builder) -> builder
            .id(Scholar.resource("lectern_take_book"))
            .category(scholarCategory)
            .name(Component.translatable("lectern.take_book"))
            .description(Component.translatable("gui.scholar.controlify.lectern_take_book"))
            .allowedContexts(viewContext));


    }

    private static BindContext register(String path, Function<Minecraft, Boolean> predicate) {
        var context = new BindContext(Scholar.resource(path), predicate);
        Registry.register(REGISTRY, context.id(), context);
        return context;
    }
}
