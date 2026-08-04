package net.shirojr.nemuelch.compat.cca.implementation;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.cca.NeMuelchComponents;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import net.shirojr.nemuelch.util.helper.PlayerLookupUtil;
import net.shirojr.nemuelch.util.helper.Vec3dHelper;
import org.jetbrains.annotations.NotNull;

public class ProjectileRicochetComponent implements Component {
    public static final Identifier KEY = NeMuelch.getId("ricochet");
    private final ProjectileEntity entity;

    private int maxRicochetSession;
    private int ricochetsLeft;

    public ProjectileRicochetComponent(ProjectileEntity entity) {
        this.entity = entity;
    }

    public static ProjectileRicochetComponent get(ProjectileEntity projectile) {
        return NeMuelchComponents.RICOCHET.get(projectile);
    }

    public void setRicochetsLeft(int ricochetsLeft) {
        int old = this.ricochetsLeft;
        this.ricochetsLeft = Math.max(0, ricochetsLeft);
        if (old < this.ricochetsLeft) {
            this.maxRicochetSession = this.ricochetsLeft;
        }
        this.sync();
    }

    public void decrementRicochets() {
        this.setRicochetsLeft(this.getRicochetsLeft() - 1);
    }

    public int getRicochetsLeft() {
        return ricochetsLeft;
    }

    public int getMaxRicochetSession() {
        return maxRicochetSession;
    }

    public void handleReflection(BlockHitResult blockHitResult) {
        if (!(this.entity.getWorld() instanceof ServerWorld serverWorld)) return;
        Direction hitSide = blockHitResult.getSide();
        Vec3d incomingVelocity = this.entity.getVelocity();
        Vec3d reflectedVelocity = Vec3dHelper.reflect(incomingVelocity, hitSide);
        this.entity.setVelocity(reflectedVelocity.multiply(0.4));
        this.entity.velocityDirty = true;
        PlayerLookupUtil.trackingAndSelf(this.entity).forEach(player -> player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(this.entity)));
        this.decrementRicochets();
        float pitch = MathHelper.lerp((float) this.getRicochetsLeft() / this.getMaxRicochetSession(), 0.6f, 0.8f);
        serverWorld.playSound(null, this.entity.getX(), this.entity.getY(), this.entity.getZ(), NeMuelchSounds.RICOCHET, SoundCategory.NEUTRAL, 1f, pitch);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag) {
        if (tag.contains(NeMuelchNbtKeys.RICOCHET)) {
            this.ricochetsLeft = tag.getInt(NeMuelchNbtKeys.RICOCHET);
        }
        if (tag.contains(NeMuelchNbtKeys.RICOCHET_MAX_SESSION)) {
            this.maxRicochetSession = tag.getInt(NeMuelchNbtKeys.RICOCHET_MAX_SESSION);
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag) {
        tag.putInt(NeMuelchNbtKeys.RICOCHET, this.getRicochetsLeft());
        tag.putInt(NeMuelchNbtKeys.RICOCHET_MAX_SESSION, this.getMaxRicochetSession());
    }

    public void sync() {
        if (this.entity.getWorld().isClient()) return;
        NeMuelchComponents.RICOCHET.sync(this.entity);
    }
}
