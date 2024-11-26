package net.shirojr.nemuelch.item.custom.supportItem;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.entity.custom.PotLauncherEntity;

public class PotLauncherItem extends Item {
    public PotLauncherItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockState state = world.getBlockState(context.getBlockPos());
        if (Block.isFaceFullSquare(state.getCollisionShape(world, context.getBlockPos()), Direction.UP)) {
            if (world instanceof ServerWorld serverWorld) {
                PotLauncherEntity entity;
                Vec3d spawnLocation = Vec3d.of(context.getBlockPos().up());
                if (context.getPlayer() == null) {
                    entity = new PotLauncherEntity(serverWorld, spawnLocation);
                } else {
                    entity = new PotLauncherEntity(
                            serverWorld, spawnLocation,
                            new EulerAngle(context.getPlayer().getPitch(), context.getPlayer().getYaw(), context.getPlayer().getRoll())
                    );
                }
                serverWorld.spawnEntity(entity);
            }
            return ActionResult.SUCCESS;
        }
        return super.useOnBlock(context);
    }
}
