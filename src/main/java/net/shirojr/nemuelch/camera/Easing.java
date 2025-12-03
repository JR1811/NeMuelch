package net.shirojr.nemuelch.camera;

import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.MathHelper;
import net.shirojr.nemuelch.NeMuelch;
import org.apache.commons.lang3.function.TriFunction;

import java.util.List;
import java.util.Locale;

/**
 * @implNote Easing with <code>overshoot</code> in the name may exceed expected normalized values (0-1)
 */
public enum Easing implements StringIdentifiable {
    LINEAR(Displacement::lerp),
    SMOOTH_STEP((delta, start, end) -> {
        double smoothStep = MathHelper.clamp(delta, 0.0, 1.0);
        smoothStep = smoothStep * smoothStep * (3.0 - 2.0 * smoothStep);
        return Displacement.lerp(smoothStep, start, end);
    }),
    EASE_IN((delta, start, end) ->
            Displacement.lerp(delta * delta, start, end)
    ),
    EASE_OUT((delta, start, end) -> {
        double easedDelta = 1.0 - Math.pow(1.0 - delta, 2.0);
        return Displacement.lerp(easedDelta, start, end);
    }),
    EASE_IN_OUT((delta, start, end) -> {
        double easedDelta;
        if (delta < 0.5) easedDelta = 2.0 * delta * delta;
        else easedDelta = 1.0 - Math.pow(-2.0 * delta + 2.0, 2.0) / 2.0;
        return Displacement.lerp(easedDelta, start, end);
    }),
    OVERSHOOT_BOUNCE((delta, start, end) ->
            Displacement.lerp(bounceOut(delta), start, end)
    ),
    OVERSHOOT_SPRING((delta, start, end) ->
            Displacement.lerp(springOut(delta), start, end)
    ),
    EASE_POWER_QUADRATIC((delta, start, end) ->
            easePower(delta, start, end, 2.0)
    ),
    EASE_POWER_CUBIC((delta, start, end) ->
            easePower(delta, start, end, 3.0)
    ),
    OSCILLATE_SMOOTH((delta, start, end) -> {
        double oscillated = Math.sin(delta * Math.PI * 2.0); // Full cycle
        Displacement amplitude = end.subtract(start);
        return start.addAndCreate(amplitude.multiply(oscillated));
    }),
    OSCILLATE_MULTI((delta, start, end) -> {
        double cycles = 3.0;    //TODO: make variable amount!
        double oscillated = Math.sin(delta * Math.PI * cycles);
        Displacement amplitude = end.subtract(start);
        return start.addAndCreate(amplitude.multiply(oscillated));
    }),
    OSCILLATE_DAMPED((delta, start, end) -> {
        double cycles = 3.0;    //TODO: make variable amount!
        double damping = Math.pow(1.0 - delta, 2.0);
        double oscillated = Math.sin(delta * Math.PI * cycles) * damping;
        Displacement amplitude = end.subtract(start);
        Displacement result = start.addAndCreate(amplitude.multiply(oscillated));
        NeMuelch.LOGGER.info("Delta: {}, Oscillated: {}, Result yaw/pitch: {}/{}", delta, oscillated, result.getYaw(), result.getPitch());
        return result;
    });

    public static final com.mojang.serialization.Codec<Easing> CODEC = StringIdentifiable.createCodec(Easing::values);
    public static final List<Easing> OSCILLATORS = List.of(OSCILLATE_SMOOTH, OSCILLATE_MULTI, OSCILLATE_DAMPED);

    private final TriFunction<Double, Displacement, Displacement, Displacement> function;

    Easing(TriFunction<Double, Displacement, Displacement, Displacement> function) {
        this.function = function;
    }

    public Displacement interpolate(double delta, Displacement start, Displacement end) {
        return this.function.apply(delta, start, end);
    }

    // ----------------- [ Other Easing ] -----------------

    public static Displacement easePower(double delta, Displacement start, Displacement end, double power) {
        double easedDelta = Math.pow(delta, power);
        return Displacement.lerp(easedDelta, start, end);
    }

    // ----------------- [ Util ] -----------------

    private static double bounceOut(double delta) {
        if (delta < 1.0 / 2.75) {
            return 7.5625 * delta * delta;
        } else if (delta < 2.0 / 2.75) {
            delta -= 1.5 / 2.75;
            return 7.5625 * delta * delta + 0.75;
        } else if (delta < 2.5 / 2.75) {
            delta -= 2.25 / 2.75;
            return 7.5625 * delta * delta + 0.9375;
        } else {
            delta -= 2.625 / 2.75;
            return 7.5625 * delta * delta + 0.984375;
        }
    }

    private static double springOut(double delta) {
        if (delta == 0.0 || delta == 1.0) return delta;
        double oscillationFrequency = 0.3;
        double phaseShift = oscillationFrequency / 4.0;
        return Math.pow(2.0, -10.0 * delta) * Math.sin((delta - phaseShift) * (2.0 * Math.PI) / oscillationFrequency) + 1.0;
    }

    @Override
    public String asString() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
