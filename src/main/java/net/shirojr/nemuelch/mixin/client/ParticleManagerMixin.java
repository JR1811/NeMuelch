package net.shirojr.nemuelch.mixin.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.shirojr.nemuelch.block.util.HittableWithItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin implements ResourceReloader {
    @Shadow protected ClientWorld world;

    @Inject(method = "addBlockBreakingParticles(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)V", at = @At("HEAD"), cancellable = true)
    private void preventNoBoundingBoxCrash(BlockPos pos, Direction direction, CallbackInfo ci) {
        BlockState blockState = world.getBlockState(pos);
        if (blockState.getBlock() instanceof HittableWithItem) {
            ci.cancel();
        }
    }
}
