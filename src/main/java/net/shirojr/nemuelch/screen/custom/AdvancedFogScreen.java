package net.shirojr.nemuelch.screen.custom;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.shirojr.nemuelch.block.entity.custom.AdvancedFogBlockEntity;
import net.shirojr.nemuelch.network.util.NetworkIdentifiers;
import net.shirojr.nemuelch.util.helper.ColorHelper;

public class AdvancedFogScreen extends Screen {
    private final AdvancedFogBlockEntity blockEntity;

    private TextFieldWidget inputColor;
    private TextFieldWidget inputMinX;
    private TextFieldWidget inputMinY;
    private TextFieldWidget inputMinZ;
    private TextFieldWidget inputMaxX;
    private TextFieldWidget inputMaxY;
    private TextFieldWidget inputMaxZ;

    public AdvancedFogScreen(AdvancedFogBlockEntity blockEntity) {
        super(Text.translatable("screen.nemuelch.advanced_fog.settings"));
        this.blockEntity = blockEntity;
    }

    @Override
    public void tick() {
        super.tick();

        inputColor.tick();

        inputMinX.tick();
        inputMinY.tick();
        inputMinZ.tick();

        inputMaxX.tick();
        inputMaxY.tick();
        inputMaxZ.tick();
    }

    public AdvancedFogBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    protected void init() {
        super.init();
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 210, 150, 20).build());
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 210, 150, 20).build());

        this.inputColor = new TextFieldWidget(this.textRenderer, this.width / 2 - 152, 40, 300, 20, Text.translatable("structure_block.structure_name")) {
            @Override
            public boolean charTyped(char chr, int modifiers) {
                int curserPos = this.getCursor();
                if (curserPos == 0 && chr == '#') return true;
                return (chr >= '0' && chr <= '9') || (chr >= 'a' && chr <= 'f') || (chr >= 'A' && chr <= 'F');
            }
        };
        this.inputColor.setMaxLength(9);
        this.inputColor.setText(ColorHelper.vectorToHexWithAlpha(this.blockEntity.getColor()));
        this.addSelectableChild(this.inputColor);

        this.inputMinX = new TextFieldWidget(this.textRenderer, this.width / 2 - 152, 80, 80, 20, Text.literal("Min X"));
        this.inputMinX.setMaxLength(15);
        this.inputMinX.setText(Double.toString(this.blockEntity.getRenderedFaces().minX));
        this.addSelectableChild(this.inputMinX);
        this.inputMinY = new TextFieldWidget(this.textRenderer, this.width / 2 - 72, 80, 80, 20, Text.literal("Min Y"));
        this.inputMinY.setMaxLength(15);
        this.inputMinY.setText(Double.toString(this.blockEntity.getRenderedFaces().minY));
        this.addSelectableChild(this.inputMinY);
        this.inputMinZ = new TextFieldWidget(this.textRenderer, this.width / 2 + 8, 80, 80, 20, Text.literal("Min Z"));
        this.inputMinZ.setMaxLength(15);
        this.inputMinZ.setText(Double.toString(this.blockEntity.getRenderedFaces().minZ));
        this.addSelectableChild(this.inputMinZ);

        this.inputMaxX = new TextFieldWidget(this.textRenderer, this.width / 2 - 152, 120, 80, 20, Text.literal("Max X"));
        this.inputMaxX.setMaxLength(15);
        this.inputMaxX.setText(Double.toString(this.blockEntity.getRenderedFaces().maxX));
        this.addSelectableChild(this.inputMaxX);
        this.inputMaxY = new TextFieldWidget(this.textRenderer, this.width / 2 - 72, 120, 80, 20, Text.literal("Max Y"));
        this.inputMaxY.setMaxLength(15);
        this.inputMaxY.setText(Double.toString(this.blockEntity.getRenderedFaces().maxY));
        this.addSelectableChild(this.inputMaxY);
        this.inputMaxZ = new TextFieldWidget(this.textRenderer, this.width / 2 + 8, 120, 80, 20, Text.literal("Max Z"));
        this.inputMaxZ.setMaxLength(15);
        this.inputMaxZ.setText(Double.toString(this.blockEntity.getRenderedFaces().maxZ));
        this.addSelectableChild(this.inputMaxZ);

        this.setInitialFocus(this.inputColor);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 16777215);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String minX = this.inputMinX.getText();
        String minY = this.inputMinY.getText();
        String minZ = this.inputMinZ.getText();
        String maxX = this.inputMaxX.getText();
        String maxY = this.inputMaxY.getText();
        String maxZ = this.inputMaxZ.getText();
        this.init(client, width, height);
        this.inputMinX.setText(minX);
        this.inputMinY.setText(minY);
        this.inputMinZ.setText(minZ);
        this.inputMaxX.setText(maxX);
        this.inputMaxY.setText(maxY);
        this.inputMaxZ.setText(maxZ);
    }

    public AdvancedFogBlockEntity.Data getCurrentData() {
        return new AdvancedFogBlockEntity.Data(
                new Box(
                        parseDouble(this.inputMinX.getText()),
                        parseDouble(this.inputMinY.getText()),
                        parseDouble(this.inputMinZ.getText()),
                        parseDouble(this.inputMaxX.getText()),
                        parseDouble(this.inputMaxY.getText()),
                        parseDouble(this.inputMaxZ.getText())
                ),
                ColorHelper.hexToVectorWithAlpha(this.inputColor.getText())
        );
    }

    private void done() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeLong(this.getBlockEntity().getPos().asLong());
        getCurrentData().toPacketByteBuf(buf);
        ClientPlayNetworking.send(NetworkIdentifiers.ADVANCED_FOG_SCREEN_DATA_CHANGE, buf);
    }

    private void cancel() {

    }

    private long parseLong(String string) {
        try {
            return Long.parseLong(string);
        } catch (NumberFormatException var3) {
            return 0L;
        }
    }

    private float parseFloat(String string) {
        try {
            return Float.parseFloat(string);
        } catch (NumberFormatException var3) {
            return 1.0F;
        }
    }

    private double parseDouble(String string) {
        try {
            return Double.parseDouble(string);
        } catch (NumberFormatException var3) {
            return 1.0;
        }
    }

    private int parseInt(String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException var3) {
            return 0;
        }
    }
}
