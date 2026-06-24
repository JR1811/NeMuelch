package net.shirojr.nemuelch.item.custom.weaponry;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.init.NemuelchGameRules;
import net.shirojr.nemuelch.mixin.access.PersistentProjectileEntityAccess;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NeMuelchShieldItem extends ShieldItem {
    public NeMuelchShieldItem(Settings settings) {
        super(settings);
    }

    public static boolean isShieldItem(ItemStack stack) {
        return stack.isOf(Items.SHIELD) || stack.isIn(ConventionalItemTags.SHIELDS) || stack.getItem() instanceof NeMuelchShieldItem;
    }

    @SuppressWarnings("unused")
    public int getCooldownDuration(LivingEntity user, ItemStack shieldStack, boolean shortCooldown) {
        return shortCooldown ? 5 : 50;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return FabricLoader.getInstance().isDevelopmentEnvironment() ? 40 : 10;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        TypedActionResult<ItemStack> original = super.use(world, user, hand);
        if (user.isSneaking() && !user.isOnGround() && user.getVelocity().y > 0) {
            if (world instanceof ServerWorld serverWorld) {
                if (serverWorld.getGameRules().getBoolean(NemuelchGameRules.ALLOW_BUCKLER_SHIELD_DASH)) {
                    Vec3d newVelocity = user.getRotationVec(1).multiply(0.5).add(0, 0.3, 0);
                    user.addVelocity(newVelocity);
                    user.velocityDirty = true;
                    sendVelocityUpdatePacket(user);
                    user.getItemCooldownManager().set(this, getCooldownDuration(user, user.getStackInHand(hand), false));
                    user.getActiveItem().damage(10, user, player -> player.sendToolBreakStatus(player.getActiveHand()));
                    user.clearActiveItem();
                    serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, SoundCategory.NEUTRAL, 1f, 1f);
                    return TypedActionResult.success(user.getStackInHand(hand));
                }
            }
        }
        return original;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack finishedStack = super.finishUsing(stack, world, user);
        if (user instanceof PlayerEntity player) {
            player.getItemCooldownManager().set(stack.getItem(), getCooldownDuration(player, stack, false));
        }
        return finishedStack;
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return this.getTranslationKey();
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        // super.appendTooltip(stack, world, tooltip, context);
    }

    public static boolean blockedByCustomShield(LivingEntity user, DamageSource source) {
        Entity entity = source.getSource();
        boolean isPiercingProjectile = entity instanceof PersistentProjectileEntity persistentProjectileEntity && persistentProjectileEntity.getPierceLevel() > 0;
        if (user.getActiveItem().getItem() instanceof NeMuelchShieldItem shieldItem) {
            if (!source.isIn(DamageTypeTags.BYPASSES_SHIELD) && shieldItem.isBlocking(user) && !isPiercingProjectile) {
                Vec3d sourcePos = source.getPosition();
                if (sourcePos != null) {
                    Vec3d lookDir = user.getRotationVec(1.0F);
                    Vec3d attackDirection = sourcePos.subtract(user.getPos()).normalize();
                    attackDirection = new Vec3d(attackDirection.x, 0.0, attackDirection.z);
                    return attackDirection.dotProduct(lookDir) >= 0.0;
                }
            }
        }
        return false;
    }

    public boolean isBlocking(LivingEntity user) {
        ItemStack activeStack = user.getActiveItem();
        if (user.isUsingItem() && !activeStack.isEmpty()) {
            Item item = activeStack.getItem();
            if (item.getUseAction(activeStack) != UseAction.BLOCK) return false;
            int windUpDuration = 5;
            return item.getMaxUseTime(activeStack) - user.getItemUseTime() >= windUpDuration;
        } else {
            return false;
        }
    }

    /**
     * Use this only for direct actions. Indirect and / or tick-based modifications might get overwritten after this
     * method has been called (e.g. {@link #onBlockingPersistentProjectile(LivingEntity, PersistentProjectileEntity) onBlockingPersistentProjectile})
     */
    public void onSuccessfulBLock(LivingEntity user, DamageSource source, float blockedDamageAmount) {
        if (source.getSource() instanceof LivingEntity attacker && !attacker.getType().isIn(NeMuelchTags.EntityTypes.BUCKLER_SHIELD_KNOCKBACK_IMMUNE)) {
            attacker.takeKnockback(
                    MathHelper.clamp(blockedDamageAmount, 0, 50) / 50 * 5,
                    MathHelper.sin(user.getYaw() * (float) (Math.PI / 180.0)),
                    -MathHelper.cos(user.getYaw() * (float) (Math.PI / 180.0))
            );
            if (user.getWorld() instanceof ServerWorld serverWorld) {
                user.getActiveItem().damage(5, user, p -> p.sendToolBreakStatus(p.getActiveHand()));
                sendVelocityUpdatePacket(attacker);
                if (user instanceof PlayerEntity player) {
                    player.getItemCooldownManager().set(this, getCooldownDuration(player, user.getActiveItem(), false));
                }

                serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(), NeMuelchSounds.IMPACT_HEAVY, SoundCategory.NEUTRAL, 2f, 1f);
            }
        }
    }

    public void onBlockingPersistentProjectile(LivingEntity user, PersistentProjectileEntity projectileEntity) {
        World world = projectileEntity.getWorld();
        projectileEntity.setVelocity(projectileEntity.getVelocity().multiply(-0.1).add(0, 0.7, 0));
        projectileEntity.setYaw(projectileEntity.getYaw() + 180.0F);
        projectileEntity.prevYaw += 180.0F;

        if (world instanceof ServerWorld serverWorld) {
            if (projectileEntity.getVelocity().lengthSquared() < 1.0E-7) {
                if (projectileEntity.pickupType == PersistentProjectileEntity.PickupPermission.ALLOWED) {
                    projectileEntity.dropStack(((PersistentProjectileEntityAccess) projectileEntity).getAsItemStack(), 0.1F);
                }
                projectileEntity.discard();
            } else if (user instanceof PlayerEntity player && player.getActiveItem().getItem() instanceof NeMuelchShieldItem shieldItem) {
                projectileEntity.pickupType = PersistentProjectileEntity.PickupPermission.ALLOWED;
                player.getItemCooldownManager().set(this, shieldItem.getCooldownDuration(player, player.getActiveItem(), true));
                player.clearActiveItem();
            }
            sendVelocityUpdatePacket(projectileEntity);
            serverWorld.playSound(null, user.getX(), user.getEyeY(), user.getZ(), NeMuelchSounds.RICOCHET, SoundCategory.NEUTRAL, 2f, 1f);
        }
    }

    public static void sendVelocityUpdatePacket(Entity velocityUpdater) {
        PlayerLookup.tracking(velocityUpdater).forEach(player ->
                player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(velocityUpdater))
        );
        if (velocityUpdater instanceof ServerPlayerEntity player) {
            player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
        }
    }
}
