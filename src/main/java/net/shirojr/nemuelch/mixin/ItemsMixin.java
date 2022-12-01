package net.shirojr.nemuelch.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.shirojr.nemuelch.item.custom.extendedVanillaitem.StickItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Items.class)
public abstract class ItemsMixin {

    @Redirect(
            method = "<clinit>",
            slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=stick")),
            at = @At(value = "NEW", target = "Lnet/minecraft/item/Item;<init>", ordinal = 0)
    )
    private static Item NeMuelch$redirectStickItemRegistration(Item.Settings settings) {
        return new StickItem(settings);
    }
}
