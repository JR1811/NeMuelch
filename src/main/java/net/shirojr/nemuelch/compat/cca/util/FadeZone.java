package net.shirojr.nemuelch.compat.cca.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import net.shirojr.nemuelch.util.helper.Vec3dHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record FadeZone(@NotNull Identifier identifier, Vec3d center, double minRadius, double maxRadius,
                       boolean inverted, Set<UUID> targets) {
    public FadeZone {
        if (minRadius >= maxRadius) {
            throw new IllegalArgumentException("Min Radius of FadeZone needs to be smaller than Max Radius");
        }
        targets = Set.copyOf(targets);
    }

    public FadeZone(Identifier identifier, Vec3d center, double maxRadius, boolean inverted, HashSet<UUID> targets) {
        this(identifier, center, 0, maxRadius, inverted, targets);
    }

    public FadeZone asGlobal() {
        return new FadeZone(identifier, center, minRadius, maxRadius, inverted, new HashSet<>());
    }

    public boolean isGlobal() {
        return this.targets.isEmpty();
    }

    public double getNormalizedFade(Vec3d pos) {
        double sqDistance = pos.distanceTo(this.center);
        double norm = 1 - MathHelper.clamp((sqDistance - this.minRadius) / (this.maxRadius - this.minRadius), 0, 1);
        return this.inverted ? 1 - norm : norm;
    }

    @Nullable
    public static FadeZone fromNbt(NbtCompound nbt) {
        NbtList targetsNbt = nbt.getList(NeMuelchNbtKeys.TARGETS, NbtElement.STRING_TYPE);
        HashSet<UUID> targets = new HashSet<>();
        for (int i = 0; i < targetsNbt.size(); i++) {
            targets.add(UUID.fromString(targetsNbt.getString(i)));
        }
        Identifier id = Identifier.tryParse(nbt.getString(NeMuelchNbtKeys.IDENTIFIER));
        if (id == null) return null;
        return new FadeZone(
                id,
                Vec3dHelper.fromNbt(nbt),
                nbt.getDouble(NeMuelchNbtKeys.MIN_RADIUS),
                nbt.getDouble(NeMuelchNbtKeys.MAX_RADIUS),
                nbt.getBoolean(NeMuelchNbtKeys.INVERTED),
                targets
        );
    }

    public void toNbt(NbtCompound nbt) {
        nbt.putString(NeMuelchNbtKeys.IDENTIFIER, this.identifier.toString());
        Vec3dHelper.toNbt(nbt, this.center);
        nbt.putDouble(NeMuelchNbtKeys.MIN_RADIUS, this.minRadius);
        nbt.putDouble(NeMuelchNbtKeys.MAX_RADIUS, this.maxRadius);
        nbt.putBoolean(NeMuelchNbtKeys.INVERTED, this.inverted);

        NbtList targetsNbt = new NbtList();
        for (UUID target : this.targets) {
            targetsNbt.add(NbtString.of(target.toString()));
        }
        nbt.put(NeMuelchNbtKeys.TARGETS, targetsNbt);
    }
}