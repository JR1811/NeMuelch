package net.shirojr.nemuelch.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.init.ConfigInit;
import net.shirojr.nemuelch.item.NeMuelchItems;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

import static net.shirojr.nemuelch.item.custom.armorItem.PortableBarrelItem.*;

@Mixin(BucketItem.class)
public class BucketItemMixin extends Item {
    private static int bucketFillAmount = 5;

    public BucketItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "use",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/util/TypedActionResult;pass(Ljava/lang/Object;)Lnet/minecraft/util/TypedActionResult;",
                    ordinal = 0),
            cancellable = true)
    private void NeMuelch$UseEmptyBucket(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> info) {
        ItemStack itemStack = user.getStackInHand(hand);
        ItemStack chestStack = user.getInventory().getArmorStack(2);
        HitResult hitResult = raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY);

        if (world.isClient && hitResult.getType() != HitResult.Type.BLOCK) {
            if (chestStack.getItem() == NeMuelchItems.PORTABLE_BARREL) {

                if (itemStack.getItem() == Items.WATER_BUCKET &&
                        chestStack.getOrCreateNbt().getInt(NBT_KEY_FILL_STATUS) < ConfigInit.CONFIG.portableBarrelMaxFill) {

                    user.playSound(SoundEvents.ITEM_BUCKET_EMPTY, 1f, 1f);
                }
                if (itemStack.getItem() == Items.BUCKET &&
                        chestStack.getOrCreateNbt().getInt(NBT_KEY_FILL_STATUS) >= bucketFillAmount) {

                    user.playSound(SoundEvents.ITEM_BUCKET_FILL, 1f, 1f);
                }
            }
            return;
        }

        if (chestStack.getItem() == NeMuelchItems.PORTABLE_BARREL && hitResult.getType() != HitResult.Type.BLOCK) {
            // check if portable barrel has any custom nbt data
            if (!chestStack.hasNbt()) {
                NbtCompound nbt = chestStack.getOrCreateNbt();
                nbt.putInt(NBT_KEY_FILL_STATUS, 0);
                nbt.putInt(NBT_KEY_WATER_PURITY, 2);    // initialize with pure quality
            }

            if (user.getStackInHand(hand).getItem() == Items.WATER_BUCKET) {
                if (!isPortableBarrelFull(chestStack)) {
                    int oldFill = chestStack.getOrCreateNbt().getInt(NBT_KEY_FILL_STATUS);

                    NbtCompound nbt = chestStack.getOrCreateNbt();
                    nbt.putInt(NBT_KEY_FILL_STATUS, oldFill + bucketFillAmount);
                    nbt.putInt(NBT_KEY_WATER_PURITY, 0);    // set to dirty water

                    // clean-up for overfilled barrel
                    if (nbt.getInt(NBT_KEY_FILL_STATUS) > ConfigInit.CONFIG.portableBarrelMaxFill) {
                        nbt.putInt(NBT_KEY_FILL_STATUS, ConfigInit.CONFIG.portableBarrelMaxFill);
                    }

                    user.getStackInHand(hand).decrement(1);
                    user.giveItemStack(new ItemStack(Items.BUCKET));
                    info.setReturnValue(TypedActionResult.success(itemStack));
                    return;
                }
            }

            // remove water from tank
            if (user.getStackInHand(hand).getItem() == Items.BUCKET &&
                    chestStack.getOrCreateNbt().getInt(NBT_KEY_FILL_STATUS) >= bucketFillAmount) {

                int oldFill = chestStack.getOrCreateNbt().getInt(NBT_KEY_FILL_STATUS);

                NbtCompound nbt = chestStack.getOrCreateNbt();
                nbt.putInt(NBT_KEY_FILL_STATUS, oldFill - bucketFillAmount);  // charge of one bucket

                if (isPortableBarrelEmpty(chestStack)) {
                    nbt.putInt(NBT_KEY_WATER_PURITY, 2);
                }

                user.getStackInHand(hand).decrement(1);
                user.giveItemStack(new ItemStack(Items.WATER_BUCKET));
                info.setReturnValue(TypedActionResult.success(itemStack));
                return;
            }
        }

        info.setReturnValue(TypedActionResult.pass(itemStack));

    }
}


// because of this bs not returning / canceling i had to use a lot of else and else ifs now... -.-