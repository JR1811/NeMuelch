package net.shirojr.nemuelch.entity.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.entity.custom.DummyCloseQuarterEntity;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.init.NeMuelchEntityModelLayers;
import net.shirojr.nemuelch.util.data.DamageAccumulator;
import net.shirojr.nemuelch.util.helper.EntityGroupMapper;
import net.shirojr.nemuelch.util.logger.LoggerUtil;

public class DummyCloseQuarterEntityRenderer extends EntityRenderer<DummyCloseQuarterEntity> {
    private static final Identifier TEXTURE = NeMuelch.getId("textures/entity/dummy_cqc.png");
    public static final float DAMAGE_NUMBER_RENDERING_DURATION = NeMuelchConfigInit.CONFIG.dummyEntityData.getDisplayDuration();
    private final DummyCloseQuarterEntityModel<DummyCloseQuarterEntity> model;

    public DummyCloseQuarterEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.model = new DummyCloseQuarterEntityModel<>(ctx.getPart(NeMuelchEntityModelLayers.DUMMY_CQC));
    }

    @Override
    public Identifier getTexture(DummyCloseQuarterEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(DummyCloseQuarterEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.model.getLayer(getTexture(entity)));
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        matrices.push();
        matrices.translate(0, 1.5, 0);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
        this.model.setAngles(entity, 0, 0, entity.age + tickDelta, 0, 0);
        this.model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1f, 1f, 1f, 1f);
        matrices.pop();

        this.renderDamageNumber(entity, tickDelta, matrices, vertexConsumers, light);
    }

    private void renderDamageNumber(DummyCloseQuarterEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        DamageAccumulator damageHandler = entity.getDamageHandler();
        if (damageHandler.getDamages().isEmpty()) return;
        DamageAccumulator.DamageEntry newestDamage = damageHandler.getNewestDamage();
        int hitAge = newestDamage.age();
        if (hitAge < 0) return;
        int elapsed = entity.age - hitAge;
        if (elapsed <= 0) return;
        float normalizedProgress = MathHelper.clamp((elapsed + tickDelta) / DAMAGE_NUMBER_RENDERING_DURATION, 0, 1);
        if (normalizedProgress >= 1f) {
            // entity.resetClientHitData();
            return;
        }
        float alpha = normalizedProgress > 0.75f ? 1f - ((normalizedProgress - 0.75f) / 0.25f) : 1f;
        if (alpha < 0.001f) return;
        float rise = /*normalizedProgress * 1.25f*/ 0f;
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        String textContent = String.format("%.1f", newestDamage.damage()) + "   [%s DPS]".formatted(
                String.format("%.2f", damageHandler.getDamagePerSecond((float) entity.age, DAMAGE_NUMBER_RENDERING_DURATION))
        );
        Formatting textFormatting = EntityGroupMapper.of(entity.getGroup()).getTextFormatting();
        Text text = Text.literal(textContent).formatted(textFormatting);

        matrices.push();
        matrices.translate(0, entity.getHeight() + 0.5 + rise, 0);

        matrices.multiply(client.getEntityRenderDispatcher().camera.getRotation());
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
        float scale = 0.025f;
        matrices.scale(scale, scale, scale);

        int textColor = ColorHelper.Argb.getArgb((int) (alpha * 255), 255, 50, 50);
        float x = -textRenderer.getWidth(text) / 2f;

        LoggerUtil.devLogger(String.valueOf(alpha));

        if (entity.getGroup().equals(EntityGroup.DEFAULT)) {
            textRenderer.draw(text, x, 0f, textColor, false, matrices.peek().getPositionMatrix(), vertexConsumers,
                    TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
        } else {
            textRenderer.drawWithOutline(text.asOrderedText(), x, 0f,
                    textColor, 0xFFFFFFFF, matrices.peek().getPositionMatrix(), vertexConsumers, 0xF000F0);
        }
        matrices.pop();
    }
}
