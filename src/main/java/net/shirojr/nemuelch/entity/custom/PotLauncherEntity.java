package net.shirojr.nemuelch.entity.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.world.World;
import net.shirojr.nemuelch.entity.NeMuelchEntities;
import org.jetbrains.annotations.Nullable;

public class PotLauncherEntity extends AbstractDecorationEntity {
    private static final EulerAngle DEFAULT_ROTATION = new EulerAngle(1.0F, 0.0F, 1.0F);

    private static final TrackedData<EulerAngle> ROTATIONS = DataTracker.registerData(PotLauncherEntity.class, TrackedDataHandlerRegistry.ROTATION);

    public PotLauncherEntity(World world) {
        super(NeMuelchEntities.POT_LAUNCHER, world);
    }

    public PotLauncherEntity(World world, BlockPos pos) {
        this(world);
        this.setPosition(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(ROTATIONS, DEFAULT_ROTATION);
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this, this.getType(), 0, this.getDecorationBlockPos());
    }

    @Override
    public int getWidthPixels() {
        return 16;
    }

    @Override
    public int getHeightPixels() {
        return 16;
    }

    @Override
    public boolean shouldRender(double distance) {
        double d = 16.0;
        d *= 64.0 * getRenderDistanceMultiplier();
        return distance < d * d;
    }

    @Override
    public void onBreak(@Nullable Entity entity) {

    }

    @Override
    public void onPlace() {

    }
}
