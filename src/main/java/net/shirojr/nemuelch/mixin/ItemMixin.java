package net.shirojr.nemuelch.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.init.ConfigInit;
import net.shirojr.nemuelch.util.NeMuelchTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin implements ItemConvertible {

    @Inject(method = "useOnEntity", at = @At("HEAD"), cancellable = true)
    public void nemuelch$pullBody(ItemStack stack, PlayerEntity user, LivingEntity target, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (user.getItemCooldownManager().isCoolingDown(stack.getItem()))
            return;

        boolean isTool = stack.getItem() instanceof ShovelItem || stack.isIn(NeMuelchTags.Items.PULL_BODY_TOOLS);
        if (!isTool) return;
        if (!(target instanceof PlayerEntity targetPlayer) || !target.isDead())
            return;

        if (!user.getWorld().isClient()) {
            Vec3d pull = user.getPos().subtract(target.getPos());
            pull.subtract(user.getRotationVector());

            targetPlayer.setVelocity(
                    pull.getX() * ConfigInit.CONFIG.pullBodyHorizontal, ConfigInit.CONFIG.pullBodyVertical,
                    pull.getZ() * ConfigInit.CONFIG.pullBodyHorizontal
            );
            targetPlayer.velocityModified = true;

            stack.damage(ConfigInit.CONFIG.pullToolDamage, user, p -> p.sendToolBreakStatus(user.getActiveHand()));
            user.getItemCooldownManager().set(stack.getItem(), ConfigInit.CONFIG.pullToolCooldown);

            ServerWorld world = (ServerWorld) user.getWorld();
            NeMuelch.devLogger(String.valueOf(world));
            world.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.BLOCK_HONEY_BLOCK_BREAK, SoundCategory.PLAYERS,
                    2f, 1f);
        }

        cir.setReturnValue(ActionResult.SUCCESS);
    }
}
