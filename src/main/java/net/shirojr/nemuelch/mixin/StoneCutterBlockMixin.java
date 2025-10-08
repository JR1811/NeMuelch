package net.shirojr.nemuelch.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StonecutterBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.util.logger.LoggerUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StonecutterBlock.class)
public abstract class StoneCutterBlockMixin extends Block {
    @Shadow
    @Final
    public static DirectionProperty FACING;

    public StoneCutterBlockMixin(Settings settings) {
        super(settings);
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        if (world.isClient() || !NeMuelchConfigInit.CONFIG.stoneCutterDamage) return;
        if (entity instanceof PlayerEntity player && (player.isSpectator() || player.isCreative())) return;
        Vec3d velocityInfluence = new Vec3d(0.45, 0.45, 0.45);
        Vec3d stoneCutterInfluence = switch (state.get(FACING)) {
            case EAST -> new Vec3d(0.0, 1.0, -1.0);
            case SOUTH -> new Vec3d(1.0, 1.0, 0.0);
            case WEST -> new Vec3d(0.0, 1.0, 1.0);
            case NORTH -> new Vec3d(-1.0, 1.0, 0.0);
            default -> new Vec3d(0.0, 1.0, 0.0);
        };

        if (entity instanceof ServerPlayerEntity playerEntity) {
            Vec3d newVelocity = entity.getVelocity().add(stoneCutterInfluence.multiply(velocityInfluence));
            LoggerUtil.devLogger("Direction: " + state.get(FACING));
            playerEntity.setVelocity(newVelocity);
            playerEntity.velocityModified = true;

            playerEntity.damage(world.getDamageSources().generic(), 6.0f);
            playerEntity.sendMessage(Text.translatable("chat.nemuelch.standing_on_stonecutter"), true);
        }
        if (entity instanceof ItemEntity itemEntity) {
            itemEntity.discard();
        }
        super.onSteppedOn(world, pos, state, entity);
    }
}