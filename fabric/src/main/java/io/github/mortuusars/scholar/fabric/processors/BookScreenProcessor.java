package io.github.mortuusars.scholar.fabric.processors;

import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.gui.guide.GuideAction;
import dev.isxander.controlify.gui.guide.GuideActionRenderer;
import dev.isxander.controlify.gui.layout.*;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.virtualmouse.VirtualMouseBehaviour;
import io.github.mortuusars.scholar.mixin.BookScreenAccessor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class BookScreenProcessor<T extends Screen> extends ScreenProcessor<T> {
    protected PositionedComponent<RenderComponent> leftLayout;
    protected PositionedComponent<RenderComponent> rightLayout;
    private ControllerEntity controller;

    protected Predicate<Screen> ALWAYS = (screen) -> true;

    public BookScreenProcessor(T screen) {
        super(screen);
        setController();
    }

    @Override
    public void onInputModeChanged(InputMode mode) {
        this.setRenderGuide(mode.isController());
    }

    @Override
    public VirtualMouseBehaviour virtualMouseBehaviour() {
        return VirtualMouseBehaviour.DISABLED;
    }

    @Override
    public void onControllerUpdate(ControllerEntity controller) {
        super.onControllerUpdate(controller);
        setController();
    }

    @Override
    protected void handleButtons(ControllerEntity controller) {
        super.handleButtons(controller);
        updateGuides();
    }

    @Override
    protected void handleTabNavigation(ControllerEntity controller) {
        super.handleTabNavigation(controller);
        updateGuides();
    }

    @Override
    public void onWidgetRebuild() {
        super.onWidgetRebuild();
        if (this.controller == null) return;

        buildGuides();

        if (ControlifyApi.get().currentInputMode().isController()) {
            this.setRenderGuide(true);
        }
    }

    protected void buildGuides() {
    }

    @SuppressWarnings("unchecked")
    private void updateGuides() {
        ((ColumnLayoutComponent<?>) this.leftLayout.getComponent()).getChildComponents()
            .forEach((row) -> ((RowLayoutComponent<?>) row).getChildComponents()
                .forEach((guide) -> ((GuideActionRenderer<T>) guide).updateName(screen)));

        ((ColumnLayoutComponent<?>) this.rightLayout.getComponent()).getChildComponents()
            .forEach((row) -> ((RowLayoutComponent<?>) row).getChildComponents()
                .forEach((guide) -> ((GuideActionRenderer<T>) guide).updateName(screen)));

        this.leftLayout.updatePosition(screen.width, screen.height);
        this.rightLayout.updatePosition(screen.width, screen.height);
    }

    private void setController() {
        this.controller = ControlifyApi.get()
            .getCurrentController()
            .filter((c) -> c.input().isPresent())
            .orElse(null);
    }

    protected void setLeftLayout(RowLayoutComponent<?> ...rows) {
        this.leftLayout = new PositionedComponent<>(
            makeColumn(ColumnLayoutComponent.ElementPosition.LEFT, rows),
            AnchorPoint.BOTTOM_LEFT, 0, 0, AnchorPoint.BOTTOM_LEFT);
    }

    protected void setRightLayout(RowLayoutComponent<?> ...rows) {
        this.rightLayout = new PositionedComponent<>(
            makeColumn(ColumnLayoutComponent.ElementPosition.RIGHT, rows),
            AnchorPoint.BOTTOM_RIGHT, 0, 0, AnchorPoint.BOTTOM_RIGHT);
    }

    protected RowLayoutComponent<RenderComponent> makeRow(GuideActionRenderer<?> ...elements) {
        return RowLayoutComponent
            .builder()
            .spacing(5)
            .rowPadding(0)
            .elementPosition(RowLayoutComponent.ElementPosition.MIDDLE)
            .elements(elements)
            .build();
    }

    protected ColumnLayoutComponent<?> makeColumn(ColumnLayoutComponent.ElementPosition side, RowLayoutComponent<?> ...elements) {
        return  ColumnLayoutComponent
            .builder()
            .spacing(2)
            .elementPosition(side)
            .colPadding(2)
            .elements(elements)
            .build();
    }

    protected GuideActionRenderer<Screen> makeGuideFor(InputBindingSupplier supplier, Predicate<Screen> predicate, boolean rtl) {
        InputBinding binding = supplier.on(this.controller);
        return new GuideActionRenderer<>(new GuideAction<>(
            binding, (screen) -> predicate.test(screen) ? Optional.of(binding.description()) : Optional.empty()
        ), rtl, false);
    }

    private void setRenderGuide(boolean render) {
        render &= ControlifyApi.get()
            .getCurrentController()
            .map((c) -> c.genericConfig().config().showScreenGuides)
            .orElse(false);

        List<Renderable> renderableList = ((BookScreenAccessor) screen).getRenderables();
        if (this.leftLayout != null && this.rightLayout != null) {
            if (render) {
                if (!renderableList.contains(this.leftLayout)) {
                    renderableList.add(this.leftLayout);
                }

                if (!renderableList.contains(this.rightLayout)) {
                    renderableList.add(this.rightLayout);
                }
            } else {
                renderableList.remove(this.leftLayout);
                renderableList.remove(this.rightLayout);
            }
        }
    }
}
