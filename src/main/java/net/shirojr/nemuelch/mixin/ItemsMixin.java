package net.shirojr.nemuelch.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.item.custom.extendedVanillaitem.StickItem;
import net.shirojr.nemuelch.item.custom.supportItem.OminousHeartItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Items.class)
public abstract class ItemsMixin {

    //@Inject(method = "<clinit>", at = @At("HEAD"))
    //@Redirect(method = "<clinit>", at = @At(value = "CONSTANT", args = "stringValue=stick"))

    @Redirect(
            method = "<clinit>",
            slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=stick")),
            at = @At(value = "NEW", target = "Lnet/minecraft/item/Item;<init>", ordinal = 0)
    )
    private static Item NeMuelch$redirectStickItemRegistration(Item.Settings settings) {

        NeMuelch.LOGGER.info("yup, that mixin is working... i guess....");
        return new StickItem(settings);
    }
}
