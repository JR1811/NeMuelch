package net.shirojr.nemuelch.init;

import com.mojang.brigadier.arguments.ArgumentType;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.command.argument.EasingArgumentType;
import net.shirojr.nemuelch.compat.cca.util.BlightType;

public class NeMuelchArgumentTypes {
    static {
        register("blight_type", BlightType.ArgumentType.class, ConstantArgumentSerializer.of(BlightType.ArgumentType::blightType));
        register("easing", EasingArgumentType.class, ConstantArgumentSerializer.of(EasingArgumentType::easing));
    }

    @SuppressWarnings("SameParameterValue")
    private static <A extends ArgumentType<?>, T extends ArgumentSerializer.ArgumentTypeProperties<A>> void register(
            String name,
            Class<? extends A> clazz,
            ArgumentSerializer<A, T> serializer) {
        ArgumentTypeRegistry.registerArgumentType(NeMuelch.getId(name), clazz, serializer);
    }

    public static void initialize() {
        // static initialisation
    }
}
