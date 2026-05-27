package net.shirojr.nemuelch.particle.data;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.util.StringIdentifiable;
import net.shirojr.nemuelch.init.NeMuelchParticleTypes;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public record SwipeParticleEffect(int color, float pitch, float yaw, Direction direction) implements ParticleEffect {
    public static final Codec<SwipeParticleEffect> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("color").forGetter(SwipeParticleEffect::color),
                    Codec.FLOAT.fieldOf("pitch").forGetter(SwipeParticleEffect::pitch),
                    Codec.FLOAT.fieldOf("yaw").forGetter(SwipeParticleEffect::yaw),
                    Codec.STRING.fieldOf("direction").xmap(Direction::fromString, Direction::asString).forGetter(SwipeParticleEffect::direction)
            ).apply(instance, SwipeParticleEffect::new)
    );

    public static final ParticleEffect.Factory<SwipeParticleEffect> FACTORY = new Factory<>() {
        @Override
        public SwipeParticleEffect read(ParticleType<SwipeParticleEffect> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            int color = reader.readInt();
            reader.expect(' ');
            float pitch = (float) reader.readDouble();
            reader.expect(' ');
            float yaw = (float) reader.readDouble();
            Direction direction = Direction.fromCommandStringReading(reader, true);

            return new SwipeParticleEffect(color, pitch, yaw, direction);
        }

        @Override
        public SwipeParticleEffect read(ParticleType<SwipeParticleEffect> type, PacketByteBuf buf) {
            return new SwipeParticleEffect(buf.readVarInt(), buf.readFloat(), buf.readFloat(), Direction.fromPacketByteBuf(buf));
        }
    };

    @SuppressWarnings("unused")
    public static Codec<SwipeParticleEffect> getCodec(ParticleType<SwipeParticleEffect> type) {
        return CODEC;
    }

    @Override
    public ParticleType<?> getType() {
        return this.direction.getParticleType();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(color);
        buf.writeFloat(pitch);
        buf.writeFloat(yaw);
        this.direction.toPacketByteBuf(buf);
    }

    @Override
    public String asString() {
        return String.format(Locale.ROOT, "%s %d %f %f %s",
                Registries.PARTICLE_TYPE.getId(this.getType()),
                this.color, this.pitch, this.yaw, this.direction.asString()
        );
    }

    public enum Direction implements StringIdentifiable {
        UP("up", NeMuelchParticleTypes.SWIPE_UP),
        DOWN("down", NeMuelchParticleTypes.SWIPE_DOWN);

        public static final Map<String, Direction> ENTRIES = Arrays.stream(values())
                .collect(Collectors.toMap(Direction::asString, direction -> direction));

        private final String name;
        private final ParticleType<SwipeParticleEffect> particleType;

        Direction(String name, ParticleType<SwipeParticleEffect> particleType) {
            this.name = name;
            this.particleType = particleType;
        }

        @Override
        public String asString() {
            return this.name;
        }

        public static Direction fromString(String input) {
            Direction direction = ENTRIES.get(input);
            if (direction == null) throw new IllegalArgumentException("Invalid swipe direction: '%s'".formatted(input));
            return direction;
        }

        public ParticleType<SwipeParticleEffect> getParticleType() {
            return particleType;
        }

        public static Direction fromPacketByteBuf(PacketByteBuf buf) {
            String input = buf.readString();
            return fromString(input);
        }

        public void toPacketByteBuf(PacketByteBuf buf) {
            buf.writeString(this.asString());
        }

        public static Direction fromCommandStringReading(StringReader reader, boolean isLast) throws CommandSyntaxException {
            reader.expect(' ');
            String input = isLast ? reader.readUnquotedString() : reader.readStringUntil(' ');
            return fromString(input);
        }
    }
}
