package net.shirojr.nemuelch.mixin;

import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import net.revive.accessor.PlayerEntityAccessor;
import net.shirojr.nemuelch.init.ConfigInit;
import net.shirojr.nemuelch.item.NeMuelchItems;
import net.shirojr.nemuelch.item.custom.armorAndShieldItem.NeMuelchShield;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Consumer;


@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {


    public PlayerEntityMixin(World world, PlayerEntity player) {
        super(EntityType.PLAYER, world);
    }

    @Inject(method = "findRespawnPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BedBlock;isBedWorking(Lnet/minecraft/world/World;)Z"), cancellable = true)
    private static void nemuelch$applyCustomCoordinatesRespawnPosition(ServerWorld world, BlockPos pos, float angle, boolean forced, boolean alive, CallbackInfoReturnable<Optional<Vec3d>> info) {
        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();

        boolean customBedRespawn = ConfigInit.CONFIG.useCustomBedRespawnLocation;

        double x = ConfigInit.CONFIG.respawnLocationX;
        double y = ConfigInit.CONFIG.respawnLocationY;
        double z = ConfigInit.CONFIG.respawnLocationZ;

        if (customBedRespawn && block instanceof BedBlock && BedBlock.isBedWorking(world)) {

            info.setReturnValue(Optional.of(new Vec3d(x + 0.5, y + 0.1, z + 0.5)));
            //info.cancel();
        }
    }

    @Shadow
    public abstract ItemStack getEquippedStack(EquipmentSlot slot);

    /**
     * Thanks to 🕊 Aquaglyph 🕊#7209 on the fabric discord for helping out with the KnockBack mixin <br>
     * check {@link net.shirojr.nemuelch.item.custom.caneItem.PestcaneItem#getAttributeModifiers(EquipmentSlot)  PestcaneItem}
     */
    @ModifyVariable(method = "attack(Lnet/minecraft/entity/Entity;)V", at = @At(value = "LOAD", target = "Lnet/minecraft/enchantment/EnchantmentHelper;getKnockback(Lnet/minecraft/entity/LivingEntity;)I", id = "i"))
    public int nemuelch$applyDefaultKnockbackFromStack(int i) {
        ItemStack itemInMainHand = this.getEquippedStack(EquipmentSlot.MAINHAND);

        if (!itemInMainHand.isEmpty()) {

            Collection<EntityAttributeModifier> knockBackAttributes = itemInMainHand.getAttributeModifiers(EquipmentSlot.MAINHAND).get(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);

            if (knockBackAttributes.size() > 0) {

                Iterator<EntityAttributeModifier> iterator = knockBackAttributes.iterator();

                if (iterator.hasNext()) {
                    EntityAttributeModifier entityAttributeModifier = iterator.next();
                    double knockBackValue = entityAttributeModifier.getValue();

                    return i + (int) knockBackValue;
                }
            }
        }

        return i;
    }


    // Shield breaking source: https://github.com/chronosacaria/MCDungeonsWeapons
    @Shadow
    public abstract void incrementStat(Stat<?> stat);

    @Shadow
    public abstract ItemCooldownManager getItemCooldownManager();

    @Inject(method = "damageShield", at = @At("HEAD"))
    private void nemuelch$damageNeMuelchShield(float amount, CallbackInfo info) {
        LivingEntity player = (LivingEntity) (Object) this;

        if (this.activeItemStack.getItem() instanceof NeMuelchShield) {
            if (!this.world.isClient) {
                this.incrementStat(Stats.USED.getOrCreateStat(this.activeItemStack.getItem()));
            }

            if (amount >= 3.0F) {
                int i = 1 + MathHelper.floor(amount);
                Hand hand = this.getActiveHand();
                this.activeItemStack.damage(i, this, (Consumer<LivingEntity>) ((playerEntity) -> playerEntity.sendToolBreakStatus(hand)));
                if (this.activeItemStack.isEmpty()) {
                    if (hand == Hand.MAIN_HAND) {
                        this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    } else {
                        this.equipStack(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                    }

                    this.activeItemStack = ItemStack.EMPTY;
                    this.playSound(SoundEvents.ITEM_SHIELD_BREAK, 0.8F, 0.8F + this.world.random.nextFloat() * 0.4F);
                }
            }
        }
    }

    @Inject(method = "disableShield", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ItemCooldownManager;set(Lnet/minecraft/item/Item;I)V"))
    public void nemuelch$disableNeMuelchShield(boolean sprinting, CallbackInfo ci) {
        this.getItemCooldownManager().set(NeMuelchItems.FORTIFIED_SHIELD.asItem(), 300);
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void nemuelch$dragBody(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        LivingEntity player = ((LivingEntity) (Object) this);
        if (!(entity instanceof PlayerEntity targetEntity)) return;
        if (!((PlayerEntityAccessor) targetEntity).canRevive()) return;
        if (!player.isSneaking() && player.getActiveItem().getItem() instanceof ShovelItem) return;

        Vec3f pull = new Vec3f(player.getVelocity().multiply(0.1, 1.0, 0.1));
        targetEntity.addVelocity(pull.getX(), pull.getY(), pull.getZ());

        player.getActiveItem().damage(10, player, p -> p.sendToolBreakStatus(player.getActiveHand()));

        cir.setReturnValue(ActionResult.SUCCESS);
    }
}