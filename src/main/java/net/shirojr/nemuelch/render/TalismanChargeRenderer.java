package net.shirojr.nemuelch.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.NeMuelch;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class TalismanChargeRenderer implements WorldRenderEvents.AfterEntities {
    private static TalismanChargeRenderer instance = null;

    public static TalismanChargeRenderer getInstance() {
        if (instance == null) {
            instance = new TalismanChargeRenderer();
        }
        return instance;
    }


    private final List<Data> renderData = new ArrayList<>();

    public List<Data> getRenderData() {
        return renderData;
    }

    public boolean inProgress(ProjectileEntity projectile) {
        for (Data entry : getRenderData()) {
            if (entry.projectile.equals(projectile)) return true;
        }
        return false;
    }

    @Override
    public void afterEntities(WorldRenderContext context) {
        if (renderData.isEmpty()) return;
        for (Data entry : renderData) {
            float progress = entry.getProgress(context.tickDelta());
            Vec3d start = entry.getUserPos();
            Vec3d currentEnd = start.lerp(entry.getProjectilePos(), progress);
            renderRepeatingTexture(context.matrixStack(), context.consumers(), context.camera(), start, currentEnd);
        }

        renderData.removeIf(Data::isFinished);
    }

    private static void renderRepeatingTexture(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Camera camera, Vec3d start, Vec3d end) {
        Vec3d camPos = camera.getPos();
        Vec3d direction = end.subtract(start);
        double length = direction.length();
        if (length == 0) return;
        RenderLayer renderLayer = RenderLayer.getEntityTranslucent(NeMuelch.getId("textures/misc/talisman_charge.png"));
        VertexConsumer buffer = vertexConsumers.getBuffer(renderLayer);
        float segmentSize = 0.25f;
        int segmentsAmount = (int) Math.ceil(length / segmentSize);

        for (int i = 0; i < segmentsAmount; i++) {
            float segmentStart = i * segmentSize;
            float segmentEnd = Math.min((i + 1) * segmentSize, (float) length);
            float actualSegmentLength = segmentEnd - segmentStart;

            float segmentDelta = (float) (segmentStart / length);
            Vec3d segmentPos = start.lerp(end, segmentDelta);
            float uvCutoff = actualSegmentLength / segmentSize;

            matrices.push();

            matrices.translate(
                    segmentPos.getX() - camPos.getX(),
                    segmentPos.getY() - camPos.getY(),
                    segmentPos.getZ() - camPos.getZ()
            );
            matrices.multiply(camera.getRotation());
            Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
            Matrix3f normalMatrix = matrices.peek().getNormalMatrix();

            float halfSize = segmentSize / 2;
            float actualHalfSize = actualSegmentLength / 2;

            buffer.vertex(positionMatrix, -actualHalfSize, -halfSize, 0)
                    .color(255, 255, 255, 255)
                    .texture(0, 1)
                    .overlay(OverlayTexture.DEFAULT_UV)
                    .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                    .normal(normalMatrix, 0, 1, 0)
                    .next();
            buffer.vertex(positionMatrix, -actualHalfSize, halfSize, 0)
                    .color(255, 255, 255, 255)
                    .texture(0, 0)
                    .overlay(OverlayTexture.DEFAULT_UV)
                    .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                    .normal(normalMatrix, 0, 1, 0)
                    .next();
            buffer.vertex(positionMatrix, actualHalfSize, halfSize, 0)
                    .color(255, 255, 255, 255)
                    .texture(uvCutoff, 0)
                    .overlay(OverlayTexture.DEFAULT_UV)
                    .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                    .normal(normalMatrix, 0, 1, 0)
                    .next();
            buffer.vertex(positionMatrix, actualHalfSize, -halfSize, 0)
                    .color(255, 255, 255, 255)
                    .texture(uvCutoff, 1)
                    .overlay(OverlayTexture.DEFAULT_UV)
                    .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                    .normal(normalMatrix, 0, 1, 0)
                    .next();


            matrices.pop();
        }
    }


    public static class Data {
        public static int CHARGE_TRAVEL_TIME = 80;

        private final ProjectileEntity projectile;
        private final Vec3d userPos;
        @Nullable
        private final ItemStack stack;
        private final int startAge;

        public Data(ProjectileEntity projectile, Vec3d userPos, @Nullable ItemStack stack) {
            this.projectile = projectile;
            this.userPos = userPos;
            this.stack = stack;

            this.startAge = projectile.age;
        }

        public boolean isFinished() {
            return projectile.isRemoved() || projectile.age >= this.startAge + CHARGE_TRAVEL_TIME;
        }

        public float getProgress(float tickDelta) {
            float elapsedTicks = (projectile.age - startAge) + tickDelta;
            return Math.min(elapsedTicks / CHARGE_TRAVEL_TIME, 1.0f);
        }

        public Vec3d getProjectilePos() {
            return projectile.getPos().add(0, projectile.getHeight() / 2, 0);
        }

        public Vec3d getUserPos() {
            return userPos;
        }

        @Nullable
        public ItemStack getStack() {
            return stack;
        }
    }
}
