package net.shirojr.nemuelch.entity.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.entity.custom.DummyCloseQuarterEntity;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.init.NeMuelchEntityModelLayers;
import net.shirojr.nemuelch.util.data.DamageAccumulator;
import net.shirojr.nemuelch.util.helper.EntityGroupMapper;

import java.util.List;

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

        if (!client.options.hudHidden) {
            this.renderDamageNumber(entity, tickDelta, matrices, vertexConsumers, light);
        }
    }

    private void renderDamageNumber(DummyCloseQuarterEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        DamageAccumulator damageHandler = entity.getDamageHandler();
        if (damageHandler.isEmpty()) return;
        int hitAge = damageHandler.getNewestDamage().age();
        if (hitAge < 0) return;
        int elapsed = entity.age - hitAge;
        if (elapsed < 0) return;
        float normalizedProgress = MathHelper.clamp((elapsed + tickDelta) / DAMAGE_NUMBER_RENDERING_DURATION, 0, 1);
        if (normalizedProgress >= 1f) {
            // entity.resetClientHitData();
            return;
        }
        float alpha = normalizedProgress > 0.75f ? 1f - ((normalizedProgress - 0.75f) / 0.25f) : 1f;
        if (alpha < 0.1f) return;   // avoid transparency related flashing at low alpha
        float rise = /*normalizedProgress * 1.25f*/ 0f;
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        String singleHitDmgContent = String.format("%.1f", damageHandler.getNewestDamage().damage());
        Formatting textFormatting = EntityGroupMapper.of(entity.getGroup()).getTextFormatting();
        Text singleHitDmgText = Text.literal(singleHitDmgContent).formatted(textFormatting);

        String dpsDmgContent = "%s DPS".formatted(String.format("%.2f", damageHandler.getDamagePerSecond((float) entity.age, DAMAGE_NUMBER_RENDERING_DURATION)));
        Text dpsDmgText = Text.literal(dpsDmgContent);

        Text dmgTypeText = Text.literal("Type: ").append(Text.translatable(damageHandler.getNewestDamage().damageType()));

        String averageDmgContent = "%s Session Avrg.".formatted(String.format("%.2f", damageHandler.getAverageDamage()));
        Text averageDmgText = Text.literal(averageDmgContent);

        String totalDmgContent = "%s Session Total".formatted(String.format("%.2f", damageHandler.getTotalDamage()));
        Text totalDmgText = Text.literal(totalDmgContent);

        String totalHitsContent = "%s Session Hits".formatted(damageHandler.getHits());
        Text totalHitsText = Text.literal(totalHitsContent);

        matrices.push();
        matrices.translate(0, entity.getHeight() + 1.25 + rise, 0);

        matrices.multiply(client.getEntityRenderDispatcher().camera.getRotation());

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
        float scaleHeader = 0.025f;
        float scaleOther = 0.5f;

        matrices.scale(scaleHeader, scaleHeader, scaleHeader);

        int textColor = ColorHelper.Argb.getArgb((int) (alpha * 255), 255, 50, 50);

        this.drawHeaderLine(entity, singleHitDmgText, textColor, matrices, vertexConsumers, textRenderer, light);

        matrices.scale(scaleOther, scaleOther, scaleOther);

        this.drawAdditionalInfo(client.world,
                List.of(dpsDmgText, dmgTypeText, averageDmgText, totalDmgText, totalHitsText),
                ColorHelper.Argb.getArgb((int) (alpha * 255), 255, 255, 255),
                matrices, vertexConsumers, textRenderer, client.getItemRenderer(), entity.getEquippedStacks(true), light,
                (int) (entity.getBlockPos().asLong() + entity.age), scaleHeader * scaleOther
        );

        matrices.pop();
    }

    private void drawHeaderLine(DummyCloseQuarterEntity entity, Text singleHitDmgText, int textColor,
                                MatrixStack matrices, VertexConsumerProvider vertexConsumers, TextRenderer textRenderer, int light) {
        float x = -textRenderer.getWidth(singleHitDmgText) / 2f;
        if (entity.getGroup().equals(EntityGroup.DEFAULT)) {
            textRenderer.draw(singleHitDmgText, x, 0f, textColor, false, matrices.peek().getPositionMatrix(), vertexConsumers,
                    TextRenderer.TextLayerType.NORMAL, 0, light);
        } else {
            textRenderer.drawWithOutline(singleHitDmgText.asOrderedText(), x, 0f,
                    textColor, 0xFFFFFFFF, matrices.peek().getPositionMatrix(), vertexConsumers, 0xF000F0);
        }
    }

    private void drawAdditionalInfo(World world, List<Text> content, int textColor,
                                    MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                    TextRenderer textRenderer, ItemRenderer itemRenderer, List<ItemStack> equipment,
                                    int light, int seed, float currentScale) {
        int baseYOffset = 25;
        float lineSpace = 10;
        for (int i = 0; i < content.size(); i++) {
            Text line = content.get(i);
            float x = -textRenderer.getWidth(line) / 2f;
            textRenderer.draw(line, x, baseYOffset + lineSpace * i, textColor, false,
                    matrices.peek().getPositionMatrix(), vertexConsumers,
                    TextRenderer.TextLayerType.NORMAL, 0, light);
        }

        float itemRowY = baseYOffset + lineSpace * (content.size() + 1);
        matrices.translate(0, itemRowY, 0);

        float resetScale = 1 / currentScale;
        float itemScale = 0.2f;
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        matrices.scale(resetScale, resetScale, resetScale);
        matrices.scale(itemScale, itemScale, itemScale);

        double itemGapSize = 1;
        double totalWidth = (equipment.size() - 1) * itemGapSize;
        double startX = -totalWidth / 2.0;
        for (int i = 0; i < equipment.size(); i++) {
            ItemStack stack = equipment.get(i);
            if (stack.isEmpty()) continue;
            matrices.push();
            double xOffset = startX + i * itemGapSize;
            matrices.translate(xOffset, 0, 0);
            itemRenderer.renderItem(stack, ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, world, seed);
            matrices.pop();
        }
        matrices.pop();
    }
}
