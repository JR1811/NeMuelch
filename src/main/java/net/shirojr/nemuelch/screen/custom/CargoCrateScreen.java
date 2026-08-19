package net.shirojr.nemuelch.screen.custom;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.screen.handler.CargoCrateScreenHandler;

public class CargoCrateScreen extends HandledScreen<CargoCrateScreenHandler> {
    private static final Identifier TEXTURE = NeMuelch.getId("textures/gui/cargo_crate.png");

    public CargoCrateScreen(CargoCrateScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        if (this.client == null || this.client.interactionManager == null) return;
        this.addDrawableChild(
                ButtonWidget.builder(Text.translatable("screen.nemuelch.cargo_crate.button.extract", 1),
                        button -> this.client.interactionManager.clickButton(this.handler.syncId, 0)
                ).dimensions(0, 0, 40, 20).build()
        );
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
