package net.shirojr.nemuelch.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;

import java.util.List;

public class RopeWinchScreen extends HandledScreen<RopeWinchScreenHandler> {
    private static final Identifier TEXTURE = new Identifier(NeMuelch.MOD_ID, "textures/gui/roper_gui.png");
    private final List<ButtonWidget> buttons = Lists.newArrayList();

    public RopeWinchScreen(RopeWinchScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        handler.addListener(new ScreenHandlerListener() {
            @Override
            public void onSlotUpdate(ScreenHandler handlerX, int slotId, ItemStack stack) {
            }

            @Override
            public void onPropertyUpdate(ScreenHandler handlerX, int property, int value) {
            }
        });
    }

    @Override
    protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
        int buttonsWidth = 40;
        int buttonsHeight = 20;
        int buttonsX = (this.width / 2) + (backgroundWidth / 2) + 5;
        int buttonsY = this.height / 2 - 63;


        this.buttons.add(this.addDrawableChild(new ButtonWidget(buttonsX, buttonsY, buttonsWidth, buttonsHeight,
                new TranslatableText("screen.nemuelch.button.roper.pull"), (button) -> {

            handler.resetProgress();    // only client side

            if (this.client != null) {
                this.client.interactionManager.clickButton(this.handler.syncId, 0);
                this.close();
            }

        })));

        this.buttons.add(this.addDrawableChild(new ButtonWidget(buttonsX, buttonsY + 25, buttonsWidth, buttonsHeight,
                new TranslatableText("screen.nemuelch.button.roper.unroll"), (button) -> {

            handler.applyProgress();

            if (this.client != null) {
                this.client.interactionManager.clickButton(this.handler.syncId, 1);
                this.close();
            }
        })));

        this.buttons.get(0).active = false;
        this.buttons.get(1).active = false;
        this.handledScreenTick();
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);

        renderProgressArrow(matrices, x, y);
    }

    private void renderProgressArrow(MatrixStack matrices, int x, int y) {
        if (handler.canPlaceMoreRopes()) {
            int scaledProgress = handler.getScaledProgress();
            drawTexture(matrices, x + 79, y + 39, 176, 0, 18, scaledProgress);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();

        int savedRopes = handler.ropesInSavedState();
        boolean unrollable = handler.canPlaceMoreRopes() && handler.getSlot(0).inventory.getStack(0).getCount() > 0;

        if (this.buttons.get(0).active != savedRopes > 0) {
            this.buttons.get(0).active = savedRopes > 0;
        }

        if (this.buttons.get(1).active != unrollable) {
            this.buttons.get(1).active = unrollable;
        }
    }
}
