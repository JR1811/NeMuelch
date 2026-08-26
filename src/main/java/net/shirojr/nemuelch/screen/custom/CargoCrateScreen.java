package net.shirojr.nemuelch.screen.custom;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.screen.handler.CargoCrateScreenHandler;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class CargoCrateScreen extends HandledScreen<CargoCrateScreenHandler> {
    private static final Identifier TEXTURE = NeMuelch.getId("textures/gui/cargo_crate.png");
    // private static final Identifier TEXTURE = NeMuelch.getId("textures/gui/cargo_crate_no_player_inv.png");

    private static final Vector2i SMALL_BUTTON_DIMENSION = new Vector2i(30, 15);
    private static final Vector2i PROGRESS_BAR_DIMENSION = new Vector2i(150, 8);
    private static final int GAP_HORIZONTAL = 4;
    private static final int GAP_VERTICAL = 8;

    private final List<Pair<Integer, ButtonWidget>> extractButtons = new ArrayList<>();

    public CargoCrateScreen(CargoCrateScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 225;
        // this.playerInventoryTitle = Text.empty();
    }

    @Override
    protected void init() {
        super.init();
        if (this.client == null || this.client.interactionManager == null) return;
        this.titleX = this.backgroundWidth / 2 - this.textRenderer.getWidth(this.getTitle()) / 2;
        this.playerInventoryTitleY = this.backgroundHeight / 2 + 20;
        this.extractButtons.clear();

        int horizontalCenter = this.width / 2;
        int currentY = (this.height - this.backgroundHeight) / 2 + 112;
        int buttonCount = 4;
        int totalWidth = buttonCount * SMALL_BUTTON_DIMENSION.x + (buttonCount - 1) * GAP_HORIZONTAL;
        int startX = horizontalCenter - totalWidth / 2;

        Pair<Integer, ButtonWidget> extract1 = new Pair<>(1, this.addDrawableChild(
                ButtonWidget.builder(Text.translatable("container.nemuelch.cargo_crate.button.amount", 1),
                                button -> this.client.interactionManager.clickButton(this.handler.syncId, 0)
                        ).dimensions(
                                startX,
                                currentY,
                                SMALL_BUTTON_DIMENSION.x,
                                SMALL_BUTTON_DIMENSION.y
                        )
                        .build()
        ));
        Pair<Integer, ButtonWidget> extract9 = new Pair<>(9, this.addDrawableChild(
                ButtonWidget.builder(Text.translatable("container.nemuelch.cargo_crate.button.amount", 9),
                                button -> this.client.interactionManager.clickButton(this.handler.syncId, 1)
                        ).dimensions(
                                startX + (SMALL_BUTTON_DIMENSION.x + GAP_HORIZONTAL),
                                currentY,
                                SMALL_BUTTON_DIMENSION.x,
                                SMALL_BUTTON_DIMENSION.y
                        )
                        .build()
        ));
        Pair<Integer, ButtonWidget> extract27 = new Pair<>(27, this.addDrawableChild(
                ButtonWidget.builder(Text.translatable("container.nemuelch.cargo_crate.button.amount", 27),
                                button -> this.client.interactionManager.clickButton(this.handler.syncId, 2)
                        ).dimensions(
                                startX + 2 * (SMALL_BUTTON_DIMENSION.x + GAP_HORIZONTAL),
                                currentY,
                                SMALL_BUTTON_DIMENSION.x,
                                SMALL_BUTTON_DIMENSION.y
                        )
                        .build()
        ));
        Pair<Integer, ButtonWidget> extractAll = new Pair<>(-1, this.addDrawableChild(
                ButtonWidget.builder(Text.translatable("container.nemuelch.cargo_crate.button.amount_all"),
                                button -> this.client.interactionManager.clickButton(this.handler.syncId, 3)
                        ).dimensions(
                                startX + 3 * (SMALL_BUTTON_DIMENSION.x + GAP_HORIZONTAL),
                                currentY,
                                SMALL_BUTTON_DIMENSION.x,
                                SMALL_BUTTON_DIMENSION.y
                        )
                        .build()
        ));
        currentY += SMALL_BUTTON_DIMENSION.y + GAP_VERTICAL;


        this.extractButtons.addAll(List.of(extract1, extract9, extract27, extractAll));
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
        MutableText extractText = Text.translatable("container.nemuelch.cargo_crate.extract");

        context.drawText(this.textRenderer, materialText,
                centerX - this.textRenderer.getWidth(materialText) / 2, currentY,
                4210752, false);
        currentY += 30;
        context.drawText(this.textRenderer, percentageText,
                centerX - this.textRenderer.getWidth(percentageText) / 2, currentY,
                4210752, false);
        /*currentY += 15;
        context.drawText(this.textRenderer, ratioText,
                centerX - this.textRenderer.getWidth(ratioText) / 2, currentY,
                4210752, false);*/
        currentY += 15;
        context.drawText(this.textRenderer, recentText,
                (this.width - this.backgroundWidth) / 2 + 10, currentY,
                4210752, false);
        currentY += 30;
        context.drawText(this.textRenderer, extractText,
                (this.width - this.backgroundWidth) / 2 + 10, currentY,
                4210752, false);

        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    private void renderProgress(DrawContext context, int x, int y) {
        context.drawTexture(TEXTURE, x, y, 0, 240, PROGRESS_BAR_DIMENSION.x, PROGRESS_BAR_DIMENSION.y - 1);

        int scaledProgress = (int) (PROGRESS_BAR_DIMENSION.x * this.handler.getNormalizedProgress());
        context.drawTexture(TEXTURE, x, y, 0, 229, scaledProgress, PROGRESS_BAR_DIMENSION.y);
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        this.extractButtons.forEach(buttonEntry ->
                buttonEntry.getRight().active = this.handler.allowsExtract(buttonEntry.getLeft())
        );
    }
}
