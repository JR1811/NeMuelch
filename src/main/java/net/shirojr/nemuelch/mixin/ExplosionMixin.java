package net.shirojr.nemuelch.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.shirojr.nemuelch.compat.cca.implementation.ExplosionRefillerComponent;
import net.shirojr.nemuelch.compat.cca.util.BlockSnapshot;
import net.shirojr.nemuelch.compat.cca.util.ExplosionRefillerEntry;
import net.shirojr.nemuelch.util.duck.Restorable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
public abstract class ExplosionMixin implements Restorable {
    @Shadow
    @Final
    private World world;
    @Shadow
    @Final
    private ObjectArrayList<BlockPos> affectedBlocks;

    @Unique
    private boolean isRestorable = false;

    @Inject(
            method = "affectWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Util;shuffle(Lit/unimi/dsi/fastutil/objects/ObjectArrayList;Lnet/minecraft/util/math/random/Random;)V"
            )
    )
    private void queueRefillerEntry(boolean particles, CallbackInfo ci) {
        if (!this.isRestorable || this.affectedBlocks.isEmpty() || world == null) return;
        ObjectArrayList<BlockSnapshot> blocks = new ObjectArrayList<>();
        for (BlockPos affectedBlock : this.affectedBlocks) {
            BlockState state = world.getBlockState(affectedBlock);
            if (state.isAir()) continue;
            blocks.add(new BlockSnapshot(affectedBlock, state));
        }
        ExplosionRefillerEntry entry = new ExplosionRefillerEntry(world.getTime(), blocks);
        if (entry.isEmpty()) return;
        ExplosionRefillerComponent component = ExplosionRefillerComponent.get(world);
        component.addEntry(entry);
    }

    @Override
    public void nemuelch$setRestorable() {
        this.isRestorable = true;
    }
}
