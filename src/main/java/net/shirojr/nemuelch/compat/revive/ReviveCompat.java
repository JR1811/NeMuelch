package net.shirojr.nemuelch.compat.revive;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.init.ConfigInit;
import net.shirojr.nemuelch.util.NeMuelchTags;

public class ReviveCompat {
    public static final String MOD_ID = "revive";
    public static final boolean IS_INSTALLED = FabricLoader.getInstance().isModLoaded(MOD_ID);

    public static boolean shouldOpenBodyScreen(Entity target, PlayerEntity user, Hand hand) {
        if (!(target instanceof LivingEntity livingEntity)) return true;
        if (!livingEntity.isDead()) return true;
        ItemStack stack = user.getStackInHand(hand);
        // if (user.getItemCooldownManager().isCoolingDown(stack.getItem())) return true;
        return !(stack.getItem() instanceof ShovelItem) && !stack.isIn(NeMuelchTags.Items.PULL_BODY_TOOLS);
    }

    public static boolean pullBody(World world, Entity target, PlayerEntity user, Hand hand) {
        if (shouldOpenBodyScreen(target, user, hand)) return false;
        NeMuelch.devLogger("targetPlayer is player and is dead");

        if (world instanceof ServerWorld serverWorld) {
            NeMuelch.devLogger("applying operations on server side: " + world);
            ItemStack stack = user.getStackInHand(hand);
            Vec3d pull = user.getPos().subtract(target.getPos());
            pull.subtract(user.getRotationVector());

            /*target.setVelocity(
                    pull.getX() * ConfigInit.CONFIG.pullBodyFeature.getVelocity().getHorizontal(),
                    ConfigInit.CONFIG.pullBodyFeature.getVelocity().getVertical(),
                    pull.getZ() * ConfigInit.CONFIG.pullBodyFeature.getVelocity().getHorizontal()
            );*/
            target.setVelocity(5, 5, 5);
            target.velocityModified = true;
            stack.damage(ConfigInit.CONFIG.pullBodyFeature.getTool().getDamage(),
                    user, p -> p.sendToolBreakStatus(user.getActiveHand()));
            user.getItemCooldownManager().set(stack.getItem(), ConfigInit.CONFIG.pullBodyFeature.getTool().getCooldown());

            serverWorld.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.BLOCK_HONEY_BLOCK_BREAK, SoundCategory.PLAYERS,
                    2f, 1f);
        }
        return true;
    }
}