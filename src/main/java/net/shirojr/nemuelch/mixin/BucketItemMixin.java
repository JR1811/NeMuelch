package net.shirojr.nemuelch.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.init.ConfigInit;
import net.shirojr.nemuelch.item.NeMuelchItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import static net.shirojr.nemuelch.item.custom.armorItem.PortableBarrelItem.NBT_KEY_FILL_STATUS;
import static net.shirojr.nemuelch.item.custom.armorItem.PortableBarrelItem.NBT_KEY_WATER_PURITY;

@Mixin(BucketItem.class)
public class BucketItemMixin extends Item {
    private static int bucketFillAmount = 5;

    public BucketItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/TypedActionResult;pass(Ljava/lang/Object;)Lnet/minecraft/util/TypedActionResult;", ordinal = 0), cancellable = true)
    private void NeMuelch$UseEmptyBucket(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> info) {
        if (world.isClient) info.cancel();

        ItemStack itemStack = user.getStackInHand(hand);
        ItemStack chestStack = user.getInventory().getArmorStack(2);

        if (chestStack.getItem() == NeMuelchItems.PORTABLE_BARREL) {
            // check if portable barrel has any custom nbt data
            if (!chestStack.hasNbt()) {
                NbtCompound nbt = chestStack.getOrCreateNbt();
                nbt.putInt(NBT_KEY_FILL_STATUS, 0);
                nbt.putInt(NBT_KEY_WATER_PURITY, 2);    // initialize with pure quality
            }

            if (user.getStackInHand(hand).getItem() == Items.WATER_BUCKET) {
                // ckeck if the fill status is below the max fill capacity
                if (chestStack.getOrCreateNbt().getInt(NBT_KEY_FILL_STATUS) < ConfigInit.CONFIG.portableBarrelMaxFill) {
                    int oldFill = chestStack.getOrCreateNbt().getInt(NBT_KEY_FILL_STATUS);

                    NbtCompound nbt = chestStack.getOrCreateNbt();
                    nbt.putInt(NBT_KEY_FILL_STATUS, oldFill + bucketFillAmount);
                    nbt.putInt(NBT_KEY_WATER_PURITY, 0);    // set to dirty water

                    // clean-up for overfilled barrel
                    if (chestStack.getOrCreateNbt().getInt(NBT_KEY_FILL_STATUS) > ConfigInit.CONFIG.portableBarrelMaxFill) {
                        nbt.putInt(NBT_KEY_FILL_STATUS, ConfigInit.CONFIG.portableBarrelMaxFill);
                    }

                    user.getStackInHand(hand).decrement(1);
                    user.giveItemStack(new ItemStack(Items.BUCKET));
                    user.playSound(SoundEvents.ITEM_BUCKET_EMPTY, 1f, 1f);
                    info.setReturnValue(TypedActionResult.success(itemStack));  //FIXME: this doesn't return so it still continues with the next part of the method... WHY ???? ISN' THAT A CANCEL ???????? fml...
                }

            }

            // remove water from tank
            else if (user.getStackInHand(hand).getItem() == Items.BUCKET &&
                    chestStack.getOrCreateNbt().getInt(NBT_KEY_FILL_STATUS) >= bucketFillAmount) {

                int oldFill = chestStack.getOrCreateNbt().getInt(NBT_KEY_FILL_STATUS);

                NbtCompound nbt = chestStack.getOrCreateNbt();
                nbt.putInt(NBT_KEY_FILL_STATUS, oldFill - bucketFillAmount);  // charge of one bucket
                if (chestStack.getOrCreateNbt().getInt(NBT_KEY_FILL_STATUS) == 0) nbt.putInt(NBT_KEY_WATER_PURITY, 2);  // bucket is pure when empty

                user.getStackInHand(hand).decrement(1);
                user.giveItemStack(new ItemStack(Items.WATER_BUCKET));
                user.playSound(SoundEvents.ITEM_BUCKET_EMPTY, 1f, 1f);
                info.setReturnValue(TypedActionResult.success(itemStack));
            } else {
                info.setReturnValue(TypedActionResult.pass(itemStack));
            }
        }

        else {
            info.setReturnValue(TypedActionResult.pass(itemStack));
        }
    }
}


// because of this bs not returning / canceling i had to use a lot of else and else ifs now... -.-