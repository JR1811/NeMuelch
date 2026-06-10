package net.shirojr.nemuelch.screen.custom;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.compat.cca.util.RopeData;
import net.shirojr.nemuelch.item.custom.adminToolItem.RopeToolItem;
import net.shirojr.nemuelch.network.packet.RopeDeletionC2SPacket;
import net.shirojr.nemuelch.network.packet.RopeModificationC2SPacket;
import org.joml.Vector2i;

public class RopeModificationScreen extends Screen {
    private final RopeData ropeData;

    private Vector2i centeredTitlePos;
    private Vector2i posALabelPos;
    private Vector2i posBLabelPos;
    private Vector2i segmentsInputPos;
    private Vector2i ropeWidthInputPos;
    private Vector2i slackInputPos;

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

        int inputFieldWidth = 100;
        int inputFieldGap = 10;
        int verticalGap = 40;
        int buttonWidth = 100;
        int extraInfoGroupWidth = inputFieldWidth * 3 + inputFieldGap * 2;

        centeredTitlePos = new Vector2i(this.width / 2, 10);
        posALabelPos = new Vector2i(this.width / 2 - extraInfoGroupWidth / 2, verticalGap);
        posBLabelPos = new Vector2i(this.width / 2 - extraInfoGroupWidth / 2, verticalGap + verticalGap / 2);
        segmentsInputPos = new Vector2i(this.width / 2 - extraInfoGroupWidth / 2, verticalGap * 3);
        ropeWidthInputPos = new Vector2i(this.width / 2 + (inputFieldWidth + inputFieldGap) - extraInfoGroupWidth / 2, verticalGap * 3);
        slackInputPos = new Vector2i(this.width / 2 + (inputFieldWidth + inputFieldGap) * 2 - extraInfoGroupWidth / 2, verticalGap * 3);
        Vector2i isStableInputPos = new Vector2i(this.width / 2 - extraInfoGroupWidth / 2 - 1, verticalGap * 4 - 10);
        Vector2i resegmentButtonPos = new Vector2i(ropeWidthInputPos.x, verticalGap * 4 - 10);
        Vector2i deleteButtonPos = new Vector2i(slackInputPos.x, verticalGap * 4 - 10);
        Vector2i doneButtonPos = new Vector2i(slackInputPos.x, verticalGap * 5 - 10);

        this.segmentsInput = new TextFieldWidget(
                textRenderer,
                segmentsInputPos.x, segmentsInputPos.y,
                inputFieldWidth, 20,
                Text.translatable("screen.nemuelch.rope_modification.segments")
        );
        this.segmentsInput.setText(Integer.toString(ropeData.segments()));
        this.segmentsInput.setMaxLength(15);
        this.segmentsInput.setTextPredicate(s -> s.matches("\\d*"));
        this.addSelectableChild(this.segmentsInput);

        this.ropeWidthInput = new TextFieldWidget(
                textRenderer,
                ropeWidthInputPos.x, ropeWidthInputPos.y,
                inputFieldWidth, 20,
                Text.translatable("screen.nemuelch.rope_modification.width")
        );
        this.ropeWidthInput.setText(Float.toString(ropeData.width()));
        this.ropeWidthInput.setMaxLength(15);
        this.ropeWidthInput.setTextPredicate(s -> s.matches("-?\\d*\\.?\\d*"));
        this.addSelectableChild(this.ropeWidthInput);

        this.slackInput = new TextFieldWidget(
                textRenderer,
                slackInputPos.x, slackInputPos.y,
                inputFieldWidth, 20,
                Text.translatable("screen.nemuelch.rope_modification.slack")
        );
        this.slackInput.setText(Float.toString(ropeData.slack()));
        this.slackInput.setMaxLength(15);
        this.slackInput.setTextPredicate(s -> s.matches("-?\\d*\\.?\\d*"));
        this.addSelectableChild(this.slackInput);

        this.isStableInput = new CheckboxWidget(
                isStableInputPos.x, isStableInputPos.y,
                20, 20,
                Text.translatable("screen.nemuelch.rope_modification.stable"),
                ropeData.stable(), true
        );
        this.addSelectableChild(this.isStableInput);

        this.addDrawableChild(
                ButtonWidget.builder(Text.translatable("screen.nemuelch.generic.delete"), button -> this.delete())
                        .dimensions(deleteButtonPos.x, deleteButtonPos.y, buttonWidth, 20).build()
        );
        this.addDrawableChild(
                ButtonWidget.builder(Text.translatable("screen.nemuelch.generic.update"), button -> this.updateRope())
                        .dimensions(doneButtonPos.x, doneButtonPos.y, buttonWidth, 20).build()
        );
        this.addDrawableChild(
                ButtonWidget.builder(Text.translatable("screen.nemuelch.rope_modification.resegment"), button -> this.recalculateSegments())
                        .dimensions(resegmentButtonPos.x, resegmentButtonPos.y, buttonWidth, 20).build()
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

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, centeredTitlePos.x, centeredTitlePos.y, 16777215);

        Vec3d posA = this.ropeData.pointA();
        Vec3d posB = this.ropeData.pointB();
        context.drawTextWithShadow(
                this.textRenderer,
                Text.translatable(
                        "screen.nemuelch.rope_modification.pos_a",
                        RopeToolItem.shortenDouble(posA.x), RopeToolItem.shortenDouble(posA.y), RopeToolItem.shortenDouble(posA.z)
                ),
                this.posALabelPos.x, this.posALabelPos.y,
                10526880
        );
        context.drawTextWithShadow(
                this.textRenderer,
                Text.translatable(
                        "screen.nemuelch.rope_modification.pos_b",
                        RopeToolItem.shortenDouble(posB.x), RopeToolItem.shortenDouble(posB.y), RopeToolItem.shortenDouble(posB.z)),
                this.posBLabelPos.x, this.posBLabelPos.y,
                10526880
        );

        context.drawTextWithShadow(
                this.textRenderer,
                Text.translatable("screen.nemuelch.rope_modification.segments"),
                this.segmentsInputPos.x, this.segmentsInputPos.y - 10,
                10526880
        );
        this.segmentsInput.render(context, mouseX, mouseY, delta);
        context.drawTextWithShadow(
                this.textRenderer,
                Text.translatable("screen.nemuelch.rope_modification.width"),
                this.ropeWidthInputPos.x, this.ropeWidthInputPos.y - 10,
                10526880
        );
        this.ropeWidthInput.render(context, mouseX, mouseY, delta);
        context.drawTextWithShadow(
                this.textRenderer,
                Text.translatable("screen.nemuelch.rope_modification.slack"),
                this.slackInputPos.x, this.slackInputPos.y - 10,
                10526880
        );
        this.slackInput.render(context, mouseX, mouseY, delta);
        this.isStableInput.render(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void updateRope() {
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

    private void delete() {
        new RopeDeletionC2SPacket(this.ropeData).send();

        if (this.client != null) {
            this.client.setScreen(null);
        }
    }

    private void recalculateSegments() {
        if (this.ropeData == null) return;
        float slackFromInput;
        try {
            slackFromInput = Float.parseFloat(this.slackInput.getText());
        } catch (NumberFormatException e) {
            return;
        }
        int newSegments = RopeData.getApproximatedSegmentCount(this.ropeData.pointA(), this.ropeData.pointB(), slackFromInput, 4);
        this.segmentsInput.setText(String.valueOf(newSegments));
    }
}
