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

@Mixin(BucketItem.class)
public class BucketItemMixin extends Item {
    public BucketItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/TypedActionResult;pass(Ljava/lang/Object;)Lnet/minecraft/util/TypedActionResult;", ordinal = 0), cancellable = true)
    private void NeMuelch$UseEmptyBucket(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> info) {

        NeMuelch.LOGGER.info("Im a bucket which is executed!");
        ItemStack itemStack = user.getStackInHand(hand);
        ItemStack chestStack = user.getInventory().getArmorStack(2);

        if (chestStack.getItem() == NeMuelchItems.PORTABLE_BARREL && user.getStackInHand(hand).getItem() == Items.WATER_BUCKET) {
            if (!chestStack.hasNbt()) {
                NbtCompound nbt = chestStack.getOrCreateNbt();
                nbt.putInt(NBT_KEY_FILL_STATUS, 0);
            }

            if (chestStack.getOrCreateNbt().getInt(NBT_KEY_FILL_STATUS) < ConfigInit.CONFIG.portableBarrelMaxFill) {
                int oldFill = chestStack.getOrCreateNbt().getInt(NBT_KEY_FILL_STATUS);

                NbtCompound nbt = chestStack.getOrCreateNbt();
                nbt.putInt(NBT_KEY_FILL_STATUS, oldFill + 5);  // charge of one bucket

                // clean-up for overfilled barrel
                if (chestStack.getOrCreateNbt().getInt(NBT_KEY_FILL_STATUS) > ConfigInit.CONFIG.portableBarrelMaxFill) {
                    nbt.putInt(NBT_KEY_FILL_STATUS, ConfigInit.CONFIG.portableBarrelMaxFill);
                }

                user.setStackInHand(hand, new ItemStack(Items.BUCKET));
                user.playSound(SoundEvents.ITEM_BUCKET_EMPTY,1f, 1f);
                info.setReturnValue(TypedActionResult.success(itemStack));
            }
        }
        // the logic stuff
        info.setReturnValue(TypedActionResult.pass(itemStack));
    }
}