package net.shirojr.nemuelch.entity.misc;

import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.Random;

public class DiceRollPlan {
    public final EnumMap<Phase, Integer> phases = new EnumMap<>(Phase.class);
    public float height, easePower;
    public final Vector3f sameSideRollingAxis = new Vector3f();
    public int extraTurns;
    public int direction;

    public static DiceRollPlan create(int seed) {
        Random random = new Random(seed);
        DiceRollPlan plan = new DiceRollPlan();

        plan.phases.put(Phase.RISE, 10 + random.nextInt(9));
        plan.phases.put(Phase.ROTATION_START, random.nextInt(plan.phases.get(Phase.RISE) / 2 + 1));
        plan.phases.put(Phase.ROTATION_END, plan.phases.get(Phase.ROTATION_START) + 30 + random.nextInt(44));
        plan.phases.put(Phase.FALL_START, plan.phases.get(Phase.ROTATION_END) + 3 + random.nextInt(6));
        plan.phases.put(Phase.END, plan.phases.get(Phase.FALL_START) + 12 + random.nextInt(9));

        plan.height = 0.8f + random.nextFloat() * 0.4f;
        plan.easePower = 2f + random.nextFloat() * 2f;
        randomAxis(random, plan.sameSideRollingAxis);
        plan.extraTurns = 2 + random.nextInt(3);
        plan.direction = random.nextBoolean() ? 1 : -1;

        return plan;
    }

    private static float progress(float start, float end, float t) {
        return end <= start ? 1f : MathHelper.clamp((t - start) / (end - start), 0f, 1f);
    }

    public float rollingProgress(float t) {
        return progress(phases.get(Phase.ROTATION_START), phases.get(Phase.ROTATION_END), t);
    }

    public float heightAt(float t) {
        float rise = 1f - (float) Math.pow(1f - progress(0, phases.get(Phase.RISE), t), easePower);
        float fall = (float) Math.pow(progress(phases.get(Phase.FALL_START), phases.get(Phase.END), t), easePower);
        return height * rise * (1f - fall);
    }

    public Quaternionf rotationAt(float t, Quaternionf from, Quaternionf to, Quaternionf out) {
        float progressEased = 1f - (float) Math.pow(1f - this.rollingProgress(t), easePower);
        float angle = direction * (float) (Math.PI * 2) * extraTurns * (1f - progressEased);
        Quaternionf spin = new Quaternionf().rotationAxis(angle, sameSideRollingAxis);

        from.slerp(to, progressEased, out);
        return spin.mul(out, out);
    }

    private static void randomAxis(Random r, Vector3f out) {
        out.set((float) r.nextGaussian(), (float) r.nextGaussian(), (float) r.nextGaussian());
        if (out.lengthSquared() < 1e-4f) {
            out.set(1, 0, 0);
        }
        out.normalize();
    }

    public boolean isFinished(int ticks) {
        return ticks > this.phases.get(Phase.END);
    }


    public enum Phase {
        RISE, ROTATION_START, ROTATION_END, FALL_START, END
    }
}
