package net.shirojr.nemuelch.screen.custom;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.shirojr.nemuelch.compat.cca.util.RopeData;
import net.shirojr.nemuelch.network.packet.RopeModificationC2SPacket;

public class RopeModificationScreen extends Screen {
    private final RopeData ropeData;

    private TextFieldWidget segmentsInput, ropeWidthInput, slackInput;
    private CheckboxWidget isStableInput;

    public RopeModificationScreen(RopeData ropeData) {
        super(Text.translatable("screen.nemuelch.rope_modification.title"));
        this.ropeData = ropeData;
    }

    @Override
    protected void init() {
        super.init();
        if (client == null) return;

        int verticalGap = 30;

        this.segmentsInput = new TextFieldWidget(textRenderer, this.width / 2, this.height / 2 - verticalGap * 2, 100, 20, Text.translatable("screen.nemuelch.rope_modification.segments"));
        this.segmentsInput.setText(Integer.toString(ropeData.segments()));
        this.segmentsInput.setMaxLength(15);
        this.segmentsInput.setTextPredicate(s -> s.matches("\\d*"));
        this.addSelectableChild(this.segmentsInput);

        this.ropeWidthInput = new TextFieldWidget(textRenderer, this.width / 2, this.height / 2 - verticalGap, 100, 20, Text.translatable("screen.nemuelch.rope_modification.width"));
        this.ropeWidthInput.setText(Float.toString(ropeData.width()));
        this.ropeWidthInput.setMaxLength(15);
        this.ropeWidthInput.setTextPredicate(s -> s.matches("-?\\d*\\.?\\d*"));
        this.addSelectableChild(this.ropeWidthInput);

        this.slackInput = new TextFieldWidget(textRenderer, this.width / 2, this.height / 2, 100, 20, Text.translatable("screen.nemuelch.rope_modification.slack"));
        this.slackInput.setText(Float.toString(ropeData.slack()));
        this.slackInput.setMaxLength(15);
        this.slackInput.setTextPredicate(s -> s.matches("-?\\d*\\.?\\d*"));
        this.addSelectableChild(this.slackInput);

        this.isStableInput = new CheckboxWidget(this.width / 2, this.height / 2 + verticalGap, 20, 20, Text.translatable("screen.nemuelch.rope_modification.stable"), ropeData.stable(), true);
        this.addSelectableChild(this.isStableInput);

        this.addDrawableChild(
                ButtonWidget.builder(ScreenTexts.DONE, button -> this.done())
                        .dimensions(this.width / 2 + 100, this.height / 2 + verticalGap * 2, 150, 20).build()
        );
    }

    @Override
    public void tick() {
        super.tick();
        this.segmentsInput.tick();
        this.ropeWidthInput.tick();
        this.slackInput.tick();
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String segments = this.segmentsInput.getText();
        String ropeWith = this.ropeWidthInput.getText();
        boolean stable = this.isStableInput.isChecked();
        this.init();
        this.segmentsInput.setText(segments);
        this.ropeWidthInput.setText(ropeWith);
        if (this.isStableInput.isChecked() != stable) {
            this.isStableInput.onPress();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        this.segmentsInput.render(context, mouseX, mouseY, delta);
        this.ropeWidthInput.render(context, mouseX, mouseY, delta);
        this.slackInput.render(context, mouseX, mouseY, delta);
        this.isStableInput.render(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void done() {
        try {
            int segments = Integer.parseInt(this.segmentsInput.getText());
            float width = Float.parseFloat(this.ropeWidthInput.getText());
            float slack = Float.parseFloat(this.slackInput.getText());
            boolean stable = this.isStableInput.isChecked();
            RopeData modified = new RopeData(ropeData.pointA(), ropeData.pointB(), segments, width, slack, stable);
            new RopeModificationC2SPacket(modified).send();
        } catch (NumberFormatException ignored) {
        }

        if (this.client != null) {
            this.client.setScreen(null);
        }
    }
}
