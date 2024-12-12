package net.shirojr.nemuelch.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.entity.EntityLike;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.entity.custom.PotLauncherEntity;
import net.shirojr.nemuelch.init.ConfigInit;
import net.shirojr.nemuelch.util.NeMuelchTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements Nameable, EntityLike, CommandOutput {
    @Shadow
    public World world;

    /**
     * Implementation of Body Pull feature
     *
     * @param user Player who is about to pull the body
     * @param hand Hand, which is used to drag the body
     **/
    @Inject(method = "interactAt", at = @At(value = "HEAD"), cancellable = true)
    private void nemuelch$interactAt(PlayerEntity user, Vec3d hitPos, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        Entity entity = (Entity) (Object) this;
        ItemStack stack = user.getStackInHand(hand);

        if (!(entity instanceof ServerPlayerEntity targetPlayer)) return;
        if (user.getItemCooldownManager().isCoolingDown(stack.getItem())) return;


        NeMuelch.devLogger("not on cooldown");

        boolean isTool = stack.getItem() instanceof ShovelItem || stack.isIn(NeMuelchTags.Items.PULL_BODY_TOOLS);
        if (!isTool) return;
        if (!targetPlayer.isDead()) return;

        NeMuelch.devLogger("targetPlayer is player and is dead");

        if (!user.getWorld().isClient()) {
            NeMuelch.devLogger("applying operations on server side: " + world);
            Vec3d pull = user.getPos().subtract(targetPlayer.getPos());
            pull.subtract(user.getRotationVector());

            targetPlayer.setVelocity(
                    pull.getX() * ConfigInit.CONFIG.pullBodyFeature.getVelocity().getHorizontal(),
                    ConfigInit.CONFIG.pullBodyFeature.getVelocity().getVertical(),
                    pull.getZ() * ConfigInit.CONFIG.pullBodyFeature.getVelocity().getHorizontal()
            );
            targetPlayer.velocityModified = true;

            stack.damage(ConfigInit.CONFIG.pullBodyFeature.getTool().getDamage(),
                    user, p -> p.sendToolBreakStatus(user.getActiveHand()));
            user.getItemCooldownManager().set(stack.getItem(), ConfigInit.CONFIG.pullBodyFeature.getTool().getCooldown());

            ServerWorld world = (ServerWorld) user.getWorld();
            world.playSound(null, targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(),
                    SoundEvents.BLOCK_HONEY_BLOCK_BREAK, SoundCategory.PLAYERS,
                    2f, 1f);
        }

        cir.setReturnValue(ActionResult.SUCCESS);
    }

    @Inject(method = "updatePassengerPosition(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity$PositionUpdater;)V", at = @At("HEAD"), cancellable = true)
    private void updateDropPotLauncherPassengerPosition(Entity passenger, Entity.PositionUpdater positionUpdater, CallbackInfo ci) {
        if (!(((Entity) (Object) this) instanceof PotLauncherEntity entity)) return;
        if (!entity.hasPassenger(passenger)) return;

        double pitchInRad = Math.toRadians(entity.getAngles().getPitch());
        double yawInRad = Math.toRadians(entity.getAngles().getYaw());
        double distance = 1.5;
        Vec3d offset = new Vec3d(0, 0.7, 0);

        double x = distance * Math.cos(pitchInRad) * Math.sin(yawInRad);    // might need to be inverted distance too
        double y = distance * Math.sin(pitchInRad);
        double z = -distance * Math.cos(pitchInRad) * Math.cos(yawInRad);

        Vec3d newPosition = new Vec3d(x, y, z).add(entity.getPos()).add(offset);
        positionUpdater.accept(passenger, newPosition.x, newPosition.y, newPosition.z);
        ci.cancel();
    }
}
