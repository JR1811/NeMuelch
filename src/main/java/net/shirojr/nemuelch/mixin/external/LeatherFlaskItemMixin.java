package net.shirojr.nemuelch.mixin.external;

import net.dehydration.init.SoundInit;
import net.dehydration.item.Leather_Flask;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.init.NeMuelchItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.shirojr.nemuelch.item.custom.armorAndShieldItem.PortableBarrelItem.*;

// mixin into dehydration mod class
@Mixin(Leather_Flask.class)
public abstract class LeatherFlaskItemMixin extends Item {
    @Shadow(remap = false)
    public int addition;

    public LeatherFlaskItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "use", at = @At("HEAD")/*, remap = false*/, cancellable = true)
    private void nemuelch$use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> info) {
        ItemStack itemStack = user.getStackInHand(hand);
        ItemStack chestStack = user.getInventory().getArmorStack(2);
        NbtCompound flaskNbt = itemStack.getOrCreateNbt();
        NbtCompound barrelNbt = chestStack.getOrCreateNbt();
        HitResult hitResult = raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY);

        if (world.isClient && hitResult.getType() != HitResult.Type.BLOCK) {
            if (chestStack.getItem() == NeMuelchItems.PORTABLE_BARREL) {
                if (!isFlaskEmpty(itemStack) && !isPortableBarrelFull(chestStack)) {
                    user.playSound(SoundInit.EMPTY_FLASK_EVENT, 1f, 1f);
                } else if (!isFlaskFull(itemStack) && !isPortableBarrelEmpty(chestStack)) {
                    user.playSound(SoundInit.FILL_FLASK_EVENT, 1f, 1f);
                }
            }
            return;
        }

        if (hitResult.getType() != HitResult.Type.BLOCK && chestStack.getItem() == NeMuelchItems.PORTABLE_BARREL) {
            // fill up barrel from flask
            if (flaskNbt.getInt("leather_flask") > 0 && barrelNbt.getInt(NBT_KEY_FILL_STATUS) < NeMuelchConfigInit.CONFIG.portableBarrelMaxFill) {
                int oldFill = barrelNbt.getInt(NBT_KEY_FILL_STATUS);

                // handle fill status
                if (oldFill + flaskNbt.getInt("leather_flask") > NeMuelchConfigInit.CONFIG.portableBarrelMaxFill) {
                    flaskNbt.putInt("leather_flask", NeMuelchConfigInit.CONFIG.portableBarrelMaxFill - oldFill);
                    barrelNbt.putInt(NBT_KEY_FILL_STATUS, NeMuelchConfigInit.CONFIG.portableBarrelMaxFill);
                } else {
                    barrelNbt.putInt(NBT_KEY_FILL_STATUS, oldFill + flaskNbt.getInt("leather_flask"));
                    flaskNbt.putInt("leather_flask", 0);
                }

                // handle barrel water purity
                if (flaskNbt.getInt("purified_water") < barrelNbt.getInt(NBT_KEY_WATER_PURITY)) {
                    barrelNbt.putInt(NBT_KEY_WATER_PURITY, flaskNbt.getInt("purified_water"));
                }

                info.setReturnValue(TypedActionResult.consume(itemStack));
                return;
            }

            // fill up empty flask from barrel
            if (flaskNbt.getInt("leather_flask") == 0 && barrelNbt.getInt(NBT_KEY_FILL_STATUS) > 0) {
                int flaskFluidSize = 2 + addition;
                int oldFill = barrelNbt.getInt(NBT_KEY_FILL_STATUS);

                // handle fill status
                if (oldFill > flaskFluidSize) {
                    flaskNbt.putInt("leather_flask", flaskFluidSize);
                    barrelNbt.putInt(NBT_KEY_FILL_STATUS, oldFill - flaskFluidSize);
                } else {
                    flaskNbt.putInt("leather_flask", oldFill);
                    barrelNbt.putInt(NBT_KEY_FILL_STATUS, 0);
                }

                // handle flask water purity
                if (barrelNbt.getInt(NBT_KEY_WATER_PURITY) < flaskNbt.getInt("purified_water")) {
                    flaskNbt.putInt("purified_water", barrelNbt.getInt(NBT_KEY_WATER_PURITY));
                }

                info.setReturnValue(TypedActionResult.consume(itemStack));
            }
        }
    }

    @Shadow
    public static boolean isFlaskFull(ItemStack stack) {
        return false;
    }

    @Shadow
    public static boolean isFlaskEmpty(ItemStack stack) {
        return false;
    }
}
