package net.shirojr.nemuelch.mixin;

import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.init.NeMuelchItems;
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

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "findRespawnPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BedBlock;isBedWorking(Lnet/minecraft/world/World;)Z"), cancellable = true)
    private static void nemuelch$applyCustomCoordinatesRespawnPosition(ServerWorld world, BlockPos pos, float angle,
                                                                       boolean forced, boolean alive,
                                                                       CallbackInfoReturnable<Optional<Vec3d>> info) {
        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();

        boolean customBedRespawn = NeMuelchConfigInit.CONFIG.useCustomBedRespawnLocation;
        Vec3d spawnLocation = NeMuelchConfigInit.CONFIG.respawnLocation.add(0.5, 0.1, 0.5);

        if (customBedRespawn && block instanceof BedBlock && BedBlock.isBedWorking(world)) {
            info.setReturnValue(Optional.of(spawnLocation));
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
        if (itemInMainHand.isEmpty()) return i;
        Collection<EntityAttributeModifier> knockBackAttributes = itemInMainHand.getAttributeModifiers(EquipmentSlot.MAINHAND).get(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
        if (knockBackAttributes.isEmpty()) return i;

        Iterator<EntityAttributeModifier> iterator = knockBackAttributes.iterator();
        if (iterator.hasNext()) {
            EntityAttributeModifier entityAttributeModifier = iterator.next();
            double knockBackValue = entityAttributeModifier.getValue();

            return i + (int) knockBackValue;
        }
        return i;
    }


    // Shield breaking source: https://github.com/chronosacaria/MCDungeonsWeapons
    @Shadow
    public abstract void incrementStat(Stat<?> stat);

    @Shadow
    public abstract ItemCooldownManager getItemCooldownManager();

    @Shadow
    public abstract void remove(RemovalReason reason);

    @Inject(method = "damageShield", at = @At("HEAD"))
    private void nemuelch$damageNeMuelchShield(float amount, CallbackInfo info) {
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
}