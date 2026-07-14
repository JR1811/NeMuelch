package net.shirojr.nemuelch.monster.abilities.custom;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.monster.abilities.ActiveAbility;
import net.shirojr.nemuelch.util.constants.NbtKeys;
import net.shirojr.nemuelch.util.helper.PlayerLookupUtil;

public class MultiJumpAbility extends ActiveAbility {
    private int maxJumps;
    private int jumpsLeft;
    private boolean redirectsVelocity;

    public MultiJumpAbility(int cooldown, int availableJumps, boolean redirectsVelocity) {
        super(cooldown);
        this.maxJumps = availableJumps;
        this.jumpsLeft = this.maxJumps;
        this.redirectsVelocity = redirectsVelocity;
    }

    public void setJumpsLeft(int jumpsLeft) {
        if (this.maxJumps == -1) {
            this.jumpsLeft = -1;
            return;
        }
        this.jumpsLeft = Math.max(0, jumpsLeft);
    }

    public int getJumpsLeft() {
        return jumpsLeft;
    }

    public boolean canMultiJump() {
        if (this.isOnCooldown()) return false;
        return this.getJumpsLeft() > 0 || this.getJumpsLeft() == -1;
    }

    public void onMultiJumped(ServerPlayerEntity player) {
        this.setJumpsLeft(this.getJumpsLeft() - 1);
        player.fallDistance = 0f;
        if (this.redirectsVelocity) {
            Vec3d velocity = player.getVelocity();
            double horizontalSpeed = velocity.horizontalLength();
            Vec3d lookDir = player.getRotationVec(1f);
            Vec3d lookDirFlat = new Vec3d(lookDir.x, 0, lookDir.z).normalize();
            Vec3d newVelocity = lookDirFlat.multiply(horizontalSpeed).add(0, velocity.y, 0);

            player.setVelocity(newVelocity);
            player.velocityDirty = true;
            PlayerLookupUtil.trackingAndSelf(player).forEach(target ->
                    target.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player))
            );
        }
        player.getServerWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PHANTOM_FLAP, SoundCategory.PLAYERS, 1f, 0.85f);
    }

    public boolean canReset() {
        return this.jumpsLeft != this.maxJumps || this.isOnCooldown();
    }

    public void reset() {
        this.jumpsLeft = this.maxJumps;
        this.clearCooldown();
    }

    @Override
    public void fromNbt(NbtCompound nbt) {
        super.fromNbt(nbt);
        if (nbt.contains(NbtKeys.MAX_JUMPS)) {
            this.maxJumps = nbt.getInt(NbtKeys.MAX_JUMPS);
        }
        if (nbt.contains(NbtKeys.JUMPS_LEFT)) {
            this.jumpsLeft = nbt.getInt(NbtKeys.JUMPS_LEFT);
        }
        if (nbt.contains(NbtKeys.REDIRECTS_VELOCITY)) {
            this.redirectsVelocity = nbt.getBoolean(NbtKeys.REDIRECTS_VELOCITY);
        }
    }

    @Override
    public void toNbt(NbtCompound nbt) {
        super.toNbt(nbt);
        nbt.putInt(NbtKeys.MAX_JUMPS, this.maxJumps);
        nbt.putInt(NbtKeys.JUMPS_LEFT, this.jumpsLeft);
        nbt.putBoolean(NbtKeys.REDIRECTS_VELOCITY, this.redirectsVelocity);
    }
}
