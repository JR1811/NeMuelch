package net.shirojr.nemuelch.item.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.compat.cca.implementation.RopesComponent;
import net.shirojr.nemuelch.compat.cca.util.RopeData;
import net.shirojr.nemuelch.screen.custom.RopeModificationScreen;

import java.util.List;
import java.util.Optional;

public class RopeModificationHandler {

    public static boolean attemptScreenOpening() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) return false;
        Optional<RopeData> hoveredRope = getHoveredRope(client.world, client.gameRenderer.getCamera());
        if (hoveredRope.isPresent()) {
            client.setScreen(new RopeModificationScreen(hoveredRope.get()));
            return true;
        }
        return false;
    }

    public static Optional<RopeData> getHoveredRope(World world, Camera camera) {
        if (!world.isClient()) return Optional.empty();
        RopesComponent component = RopesComponent.get(world);
        List<RopeData> ropes = component.getRopes();
        if (ropes.isEmpty()) return Optional.empty();

        Vec3d origin = camera.getPos();
        Vec3d lookDirection = Vec3d.fromPolar(camera.getPitch(), camera.getYaw());

        RopeData closest = null;
        double closestDist = 0.025;

        for (RopeData rope : ropes) {
            if (rope.isUnloaded(world)) continue;
            int samples = Math.max(8, rope.segments());
            Vec3d delta = rope.pointB().subtract(rope.pointA());

            for (int sampleIndex = 0; sampleIndex < samples; sampleIndex++) {
                float normalizedSample = (float) sampleIndex / samples;
                Vec3d sampleStart = sampleRopeCurve(rope.pointA(), delta, rope.slack(), normalizedSample);
                Vec3d sampleEnd = sampleStart.subtract(origin);
                double projectionLength = sampleEnd.dotProduct(lookDirection);
                if (projectionLength <= 0) continue;
                Vec3d distanceReduction = sampleEnd.subtract(lookDirection.multiply(projectionLength));
                double angularDistance = distanceReduction.length() / projectionLength;
                if (angularDistance < closestDist) {
                    closestDist = angularDistance;
                    closest = rope;
                }
            }
        }
        return Optional.ofNullable(closest);
    }

    public static Vec3d sampleRopeCurve(Vec3d posA, Vec3d delta, float slack, float normalizedSampleIndex) {
        double sag = slack * normalizedSampleIndex * (normalizedSampleIndex - 1.0F);
        double x = posA.x + delta.x * normalizedSampleIndex;
        double y = posA.y + (delta.y > 0
                ? delta.y * normalizedSampleIndex * normalizedSampleIndex
                : delta.y - delta.y * (1.0F - normalizedSampleIndex) * (1.0F - normalizedSampleIndex));
        y += sag;
        double z = posA.z + delta.z * normalizedSampleIndex;
        return new Vec3d(x, y, z);
    }
}
