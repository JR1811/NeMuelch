package net.shirojr.nemuelch.screen.custom;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.screen.handler.CargoCrateScreenHandler;
import org.joml.Vector2i;

public class CargoCrateScreen extends HandledScreen<CargoCrateScreenHandler> {
    private static final Identifier TEXTURE = NeMuelch.getId("textures/gui/cargo_crate.png");
    private static final Vector2i PROGRESS_BAR_DIMENSION = new Vector2i(150, 8);

    public CargoCrateScreen(CargoCrateScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 225;
    }

    @Override
    protected void init() {
        super.init();
        if (this.client == null || this.client.interactionManager == null) return;
        this.titleX = this.backgroundWidth / 2 - this.textRenderer.getWidth(this.getTitle()) / 2;
        this.playerInventoryTitleY = this.backgroundHeight / 2;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(
                TEXTURE,
                (this.width - this.backgroundWidth) / 2, (this.height - this.backgroundHeight) / 2,
                0, 0, this.backgroundWidth, this.backgroundHeight
        );

        int currentY = (this.height - this.backgroundHeight) / 2 + 40;
        this.renderProgress(
                context,
                this.width / 2 - PROGRESS_BAR_DIMENSION.x / 2,
                currentY
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int currentY = (this.height - this.backgroundHeight) / 2 + 25;

        Text materialText = this.handler.getStoredMaterial();
        MutableText percentageText = Text.translatable("container.nemuelch.cargo_crate.percentage", (int) (this.handler.getNormalizedProgress() * 100));
        // MutableText ratioText = Text.translatable("container.nemuelch.cargo_crate.ratio", this.handler.getProgress(), this.handler.getMaxProgress());
        MutableText recentText = Text.translatable("container.nemuelch.cargo_crate.recent");

        context.drawText(this.textRenderer, materialText,
                centerX - this.textRenderer.getWidth(materialText) / 2, currentY,
                4210752, false);
        currentY += 30;
        context.drawText(this.textRenderer, percentageText,
                centerX - this.textRenderer.getWidth(percentageText) / 2, currentY,
                4210752, false);
        currentY += 15;
        context.drawText(this.textRenderer, recentText,
                (this.width - this.backgroundWidth) / 2 + 10, currentY,
                4210752, false);

        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    private void renderProgress(DrawContext context, int x, int y) {
        context.drawTexture(TEXTURE, x, y, 0, 240, PROGRESS_BAR_DIMENSION.x, PROGRESS_BAR_DIMENSION.y - 1);

        int scaledProgress = (int) (PROGRESS_BAR_DIMENSION.x * this.handler.getNormalizedProgress());
        context.drawTexture(TEXTURE, x, y, 0, 229, scaledProgress, PROGRESS_BAR_DIMENSION.y);
    }
}
