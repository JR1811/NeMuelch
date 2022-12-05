package net.shirojr.nemuelch.mixin;

import net.dehydration.item.Leather_Flask;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.init.ConfigInit;
import net.shirojr.nemuelch.item.NeMuelchItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.shirojr.nemuelch.item.custom.armorItem.PortableBarrelItem.NBT_KEY_FILL_STATUS;
import static net.shirojr.nemuelch.item.custom.armorItem.PortableBarrelItem.NBT_KEY_WATER_PURITY;

// mixin into dehydration mod class
@Mixin(Leather_Flask.class)
public class LeatherFlaskItemMixin extends Item {
    public LeatherFlaskItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> info) {
        ItemStack itemStack = user.getStackInHand(hand);
        ItemStack chestStack = user.getInventory().getArmorStack(2);
        HitResult hitResult = raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY);

        // condition for:
        // 1. user doesn't aim at a block (including water source)
        // 2. flask is not empty
        // 3. user wears a portable barrel
        // 4. portable barrel is not full
        if (hitResult.getType() != HitResult.Type.BLOCK &&
                itemStack.getOrCreateNbt().getInt("leather_flask") > 0 &&
                chestStack.getItem() == NeMuelchItems.PORTABLE_BARREL &&
                chestStack.getOrCreateNbt().getInt(NBT_KEY_FILL_STATUS) < ConfigInit.CONFIG.portableBarrelMaxFill)
        {
            int oldFill = chestStack.getOrCreateNbt().getInt(NBT_KEY_FILL_STATUS);
            NbtCompound nbt = chestStack.getOrCreateNbt();
            nbt.putInt(NBT_KEY_FILL_STATUS, oldFill + itemStack.getOrCreateNbt().getInt("leather_flask"));

            // handle purity water in tank
            if (chestStack.getOrCreateNbt().getInt(NBT_KEY_WATER_PURITY) == 2) {
                if (itemStack.getOrCreateNbt().getInt("purified_water") != 2) {
                    nbt.putInt(NBT_KEY_WATER_PURITY, itemStack.getOrCreateNbt().getInt("purified_water"));
                }
            }

        info.setReturnValue(TypedActionResult.consume(itemStack)); //FIXME: throws some kind of network error
        }
    }
}
