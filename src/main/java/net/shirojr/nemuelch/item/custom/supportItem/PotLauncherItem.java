package net.shirojr.nemuelch.item.custom.supportItem;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
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
                PotLauncherEntity entity = new PotLauncherEntity(serverWorld, Vec3d.ofCenter(context.getBlockPos()));
                //TODO: use ctor with rotation angles?
                serverWorld.spawnEntity(entity);
            }
        }
        return super.useOnBlock(context);
    }
}
