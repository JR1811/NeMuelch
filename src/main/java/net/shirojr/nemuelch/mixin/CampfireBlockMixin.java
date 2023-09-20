package net.shirojr.nemuelch.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.ConfigInit;
import net.shirojr.nemuelch.util.NeMuelchTags;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CampfireBlock.class)
public abstract class CampfireBlockMixin extends BlockWithEntity {

    @Shadow @Final public static BooleanProperty LIT;

    protected CampfireBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "onUse", at = @At(value = "HEAD"), cancellable = true)
    private void nemuelch$lightUpWithTorch(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> info) {
        if (!ConfigInit.CONFIG.campfireUtilities || !(world instanceof ServerWorld)) return;
        if (state.get(LIT)) return;
        if(hit.getSide() != Direction.UP) return;

        if (Registry.ITEM.getOrCreateEntry(Registry.ITEM.getKey(player.getStackInHand(hand).getItem().asItem())
                .get()).isIn(NeMuelchTags.Items.CAMPFIRE_IGNITER)) {

            player.getStackInHand(hand).decrement(1);
            world.setBlockState(pos, state.with(LIT, true), Block.NOTIFY_ALL);

            info.setReturnValue(ActionResult.success(world.isClient()));
            world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.PLAYERS, 2f, 1f);
        }
    }
}