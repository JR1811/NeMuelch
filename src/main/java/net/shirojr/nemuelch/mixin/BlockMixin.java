package net.shirojr.nemuelch.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Block.class)
public abstract class BlockMixin {
    @Shadow
    public abstract BlockState getDefaultState();

    /*@Inject(method = "getPlacementState", at = @At("HEAD"), cancellable = true)
    private void getSandPathPlacementState(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> cir) {
        BlockState defaultState = getDefaultState();
        if (!defaultState.isOf(Blocks.SAND)) return;
    }*/
}
