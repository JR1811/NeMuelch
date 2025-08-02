package net.shirojr.nemuelch.screen.custom;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.screen.handler.RopeWinchScreenHandler;

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

        this.buttons.add(this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.nemuelch.button.roper.pull"),
                        (button) -> {
                            handler.resetProgress();    // only client side
                            if (this.client != null && this.client.interactionManager != null) {
                                this.client.interactionManager.clickButton(this.handler.syncId, 0);
                                this.close();
                            }

                        }
                ).dimensions(buttonsX, buttonsY, buttonsWidth, buttonsHeight)
                .build()));
        this.buttons.add(this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.nemuelch.button.roper.unroll"),
                        (button) -> {
                            handler.applyProgress();
                            if (this.client != null && this.client.interactionManager != null) {
                                this.client.interactionManager.clickButton(this.handler.syncId, 1);
                                this.close();
                            }
                        }
                ).dimensions(buttonsX, buttonsY + 25, buttonsWidth, buttonsHeight)
                .build()));
        this.buttons.get(0).active = false;
        this.buttons.get(1).active = false;
        this.handledScreenTick();
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
        renderProgressArrow(context, x, y);
    }

    private void renderProgressArrow(DrawContext context, int x, int y) {
        if (handler.canPlaceMoreRopes()) {
            int scaledProgress = handler.getScaledProgress();
            context.drawTexture(TEXTURE, x + 79, y + 39, 176, 0, 18, scaledProgress);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
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
