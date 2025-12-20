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

import java.util.Optional;

public class AdvancedFogScreen extends Screen {
    private final MinecraftClient client;
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
        this.client = MinecraftClient.getInstance();
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

        this.inputColor = new TextFieldWidget(this.textRenderer, this.width / 2 - 152, 40, 300, 20, Text.translatable("screen.nemuelch.advanced_fog.color")) {
            @Override
            public boolean charTyped(char chr, int modifiers) {
                boolean validHexChar = chr == '#' && this.getCursor() == 0 || (chr >= '0' && chr <= '9');
                if ((chr >= 'a' && chr <= 'f') || (chr >= 'A' && chr <= 'F')) validHexChar = true;
                if (validHexChar) return super.charTyped(chr, modifiers);
                return false;
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

        context.drawTextWithShadow(this.textRenderer, Text.translatable("screen.nemuelch.advanced_fog.color"), this.width / 2 - 153, 30, 10526880);
        this.inputColor.render(context, mouseX, mouseY, delta);

        context.drawTextWithShadow(this.textRenderer, Text.translatable("screen.nemuelch.advanced_fog.min"), this.width / 2 - 153, 70, 10526880);
        this.inputMinX.render(context, mouseX, mouseY, delta);
        this.inputMinY.render(context, mouseX, mouseY, delta);
        this.inputMinZ.render(context, mouseX, mouseY, delta);

        context.drawTextWithShadow(this.textRenderer, Text.translatable("screen.nemuelch.advanced_fog.max"), this.width / 2 - 153, 110, 10526880);
        this.inputMaxX.render(context, mouseX, mouseY, delta);
        this.inputMaxY.render(context, mouseX, mouseY, delta);
        this.inputMaxZ.render(context, mouseX, mouseY, delta);


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
        double minX = parseDouble(this.inputMinX.getText()).orElse(0d);
        double minY = parseDouble(this.inputMinY.getText()).orElse(0d);
        double minZ = parseDouble(this.inputMinZ.getText()).orElse(0d);
        double maxX = parseDouble(this.inputMaxX.getText()).orElse(1d);
        double maxY = parseDouble(this.inputMaxY.getText()).orElse(1d);
        double maxZ = parseDouble(this.inputMaxZ.getText()).orElse(1d);

        StringBuilder color = new StringBuilder(this.inputColor.getText());
        if (color.charAt(0) == '#') color = new StringBuilder(color.substring(1));
        while (color.length() < 8) {
            color.append("0");
        }

        return new AdvancedFogBlockEntity.Data(
                new Box(minX, minY, minZ, maxX, maxY, maxZ),
                ColorHelper.hexToVectorWithAlpha(color.toString())
        );
    }

    private void done() {
        if (getCurrentData() == null) {
            this.cancel();
            return;
        }
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeLong(this.getBlockEntity().getPos().asLong());
        getCurrentData().toPacketByteBuf(buf);
        ClientPlayNetworking.send(NetworkIdentifiers.ADVANCED_FOG_SCREEN_DATA_CHANGE, buf);

        this.client.setScreen(null);
    }

    private void cancel() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeLong(this.getBlockEntity().getPos().asLong());
        ClientPlayNetworking.send(NetworkIdentifiers.ADVANCED_FOG_REQUEST_SELF_SYNC, buf);
        this.client.setScreen(null);
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
            return 0.0F;
        }
    }

    private Optional<Double> parseDouble(String string) {
        try {
            return Optional.of(Double.parseDouble(string));
        } catch (NumberFormatException var3) {
            return Optional.empty();
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
