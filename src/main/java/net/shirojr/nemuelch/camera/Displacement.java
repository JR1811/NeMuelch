package net.shirojr.nemuelch.camera;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.network.util.NetworkUtil;
import org.joml.Vector3f;

import java.util.Objects;

/**
 * Stores Information about {@link Vec3d Position} and rotations (yaw, pitch, roll) and provides utility for
 * interpolation, using {@link Easing}, and modification
 */
@SuppressWarnings("unused")
public class Displacement {
    public static final Displacement DEFAULT = new Displacement();
    public static final float EQUALITY_TOLERANCE = 1e-6f;

    private Vec3d position;
    private float yaw;
    private float pitch;
    private float roll;

    // region constructors
    public Displacement(Vec3d position, float yaw, float pitch, float roll) {
        this.position = position;
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
    }

    public Displacement(Vec3d position, Vector3f rotations) {
        this(position, rotations.x, rotations.y, rotations.z);
    }

    public Displacement(Vec3d position) {
        this(position, 0, 0, 0);
    }

    public Displacement(Vector3f rotations) {
        this(rotations.x, rotations.y, rotations.z);
    }

    public Displacement(float yaw, float pitch, float roll) {
        this(Vec3d.ZERO, yaw, pitch, roll);
    }

    public Displacement(float yaw, float pitch) {
        this(yaw, pitch, 0);
    }

    public Displacement() {
        this(Vec3d.ZERO);
    }
    // endregion

    // region getter / setter
    public Vec3d getPosition() {
        return position;
    }

    public void setPosition(Vec3d position) {
        this.position = position;
    }

    public Vector3f getRotations() {
        return new Vector3f(yaw, pitch, roll);
    }

    public void setRotations(Vector3f rotations) {
        setYaw(rotations.x);
        setPitch(rotations.y);
        setRoll(rotations.z);
    }

    public void setRotations(float yaw, float pitch) {
        setYaw(yaw);
        setPitch(pitch);
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = MathHelper.wrapDegrees(yaw);
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = MathHelper.clamp(pitch, -90, 90);
    }

    public float getRoll() {
        return roll;
    }

    public void setRoll(float roll) {
        this.roll = MathHelper.wrapDegrees(roll);
    }

    public Vec3d getForwardVector() {
        return Vec3d.fromPolar(this.pitch, this.yaw);
    }

    // endregion

    public static Displacement scaleToIntensity(double normalizedIntensity, Displacement maxDisplacement) {
        Vec3d scaledPos = new Vec3d(
                MathHelper.lerp(normalizedIntensity, 0, maxDisplacement.getPosition().x),
                MathHelper.lerp(normalizedIntensity, 0, maxDisplacement.getPosition().y),
                MathHelper.lerp(normalizedIntensity, 0, maxDisplacement.getPosition().z)
        );
        float scaledYaw = MathHelper.lerp((float) normalizedIntensity, 0, maxDisplacement.getYaw());
        float scaledPitch = MathHelper.lerp((float) normalizedIntensity, 0, maxDisplacement.getPitch());
        float scaledRoll = MathHelper.lerp((float) normalizedIntensity, 0, maxDisplacement.getRoll());

        return new Displacement(scaledPos, scaledYaw, scaledPitch, scaledRoll);
    }

    public void add(Displacement other) {
        addPosition(other);
        addRotation(other);
    }

    public Displacement addAndCreate(Displacement other) {
        return new Displacement(
                this.position.add(other.position),
                this.yaw + other.yaw,
                this.pitch + other.pitch,
                this.roll + other.roll
        );
    }

    public void addPosition(Displacement other) {
        this.setPosition(this.getPosition().add(other.getPosition()));
    }

    public void addRotation(Displacement other) {
        this.setYaw(this.getYaw() + other.getYaw());
        this.setPitch(this.getPitch() + other.getPitch());
        this.setRoll(this.getRoll() + other.getRoll());
    }

    public Displacement subtract(Displacement other) {
        return new Displacement(
                this.position.subtract(other.position),
                this.yaw - other.yaw,
                this.pitch - other.pitch,
                this.roll - other.roll
        );
    }

    public Displacement multiply(double scalar) {
        return new Displacement(
                this.position.multiply(scalar),
                (float) (this.yaw * scalar),
                (float) (this.pitch * scalar),
                (float) (this.roll * scalar)
        );
    }

    public void reset() {
        this.setRotations(new Vector3f(0, 0, 0));
        this.setPosition(Vec3d.ZERO);
    }

    public static Displacement lerp(double delta, Displacement start, Displacement end) {
        Vec3d newPos = start.equals(end) ? start.getPosition() : new Vec3d(
                MathHelper.lerp(delta, start.getPosition().x, end.getPosition().x),
                MathHelper.lerp(delta, start.getPosition().y, end.getPosition().y),
                MathHelper.lerp(delta, start.getPosition().z, end.getPosition().z)
        );
        Vector3f newRotations = start.equals(end) ? start.getRotations() : new Vector3f(
                MathHelper.lerpAngleDegrees((float) delta, start.getYaw(), end.getYaw()),
                MathHelper.lerpAngleDegrees((float) delta, start.getPitch(), end.getPitch()),
                MathHelper.lerpAngleDegrees((float) delta, start.getRoll(), end.getRoll())
        );
        return new Displacement(newPos, newRotations);
    }

    public void toPacketByteBuf(PacketByteBuf buf) {
        NetworkUtil.writeVec3d(buf, getPosition());
        NetworkUtil.writeVector3f(buf, getRotations());
    }

    public static Displacement fromPacketByteBuf(PacketByteBuf buf) {
        Vec3d pos = NetworkUtil.readVec3d(buf);
        Vector3f rotation = NetworkUtil.readVector3f(buf);
        return new Displacement(pos, rotation);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Displacement other = (Displacement) obj;
        if (Math.abs(this.yaw - other.yaw) >= EQUALITY_TOLERANCE) return false;
        if (Math.abs(this.pitch - other.pitch) >= EQUALITY_TOLERANCE) return false;
        if (Math.abs(this.roll - other.roll) >= EQUALITY_TOLERANCE) return false;
        if (this.position == null) {
            return other.position == null;
        }
        if (other.position == null) {
            return false;
        }
        return this.position.squaredDistanceTo(other.position) < EQUALITY_TOLERANCE * EQUALITY_TOLERANCE;
    }

    @Override
    public int hashCode() {
        double hashScale = 1.0 / EQUALITY_TOLERANCE;
        int posHash = position != null ?
                Objects.hash(
                        Math.round(position.x * hashScale) / hashScale,
                        Math.round(position.y * hashScale) / hashScale,
                        Math.round(position.z * hashScale) / hashScale
                ) : 0;

        return Objects.hash(
                posHash,
                Math.round(yaw * hashScale) / hashScale,
                Math.round(pitch * hashScale) / hashScale,
                Math.round(roll * hashScale) / hashScale
        );
    }

    public static void toNbt(NbtCompound nbt, String key, Displacement displacement) {
        NbtCompound displacementNbt = new NbtCompound();

        NbtCompound posNbt = new NbtCompound();
        posNbt.putDouble("x", displacement.position.x);
        posNbt.putDouble("y", displacement.position.y);
        posNbt.putDouble("z", displacement.position.z);
        displacementNbt.put("pos", posNbt);

        displacementNbt.putFloat("yaw", displacement.yaw);
        displacementNbt.putFloat("pitch", displacement.pitch);
        displacementNbt.putFloat("roll", displacement.roll);

        nbt.put(key, displacementNbt);
    }

    public static Displacement fromNbt(NbtCompound nbt, String key) {
        NbtCompound displacementNbt = nbt.getCompound(key);

        NbtCompound posNbt = displacementNbt.getCompound("pos");
        Vec3d pos = new Vec3d(
                posNbt.getDouble("x"),
                posNbt.getDouble("y"),
                posNbt.getDouble("z")
        );

        float yaw = displacementNbt.getFloat("yaw");
        float pitch = displacementNbt.getFloat("pitch");
        float roll = displacementNbt.getFloat("roll");

        return new Displacement(pos, yaw, pitch, roll);
    }
}
