package net.shirojr.nemuelch.item.custom.armorAndShieldItem;

import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.shirojr.nemuelch.item.NeMuelchItemGroup;
import net.shirojr.nemuelch.util.NeMuelchTags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class NeMuelchShield extends ShieldItem {

    public final ToolMaterial material;

    public NeMuelchShield(ToolMaterial material) {
        super(new Item.Settings().group(NeMuelchItemGroup.WARFARE)
                .maxCount(1)
                .maxDamage(250 + material.getDurability())
                .rarity(Rarity.RARE)
        );
        this.material = material;
        DispenserBlock.registerBehavior(this, ArmorItem.DISPENSER_BEHAVIOR);
    }

    @Override
    public UseAction getUseAction(ItemStack itemStack){
        return UseAction.BLOCK;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack){
        return 72000;
    }

    @Override
    public TypedActionResult<ItemStack> use (World world, PlayerEntity user, Hand hand){
        ItemStack itemStack = user.getStackInHand(hand);
        user.setCurrentHand(hand);
        return TypedActionResult.consume(itemStack);
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
//      Registry.ITEM.get(new Identifier(repIngredient)));
        return Registry.ITEM.getOrCreateEntry(Registry.ITEM.getKey(ingredient.getItem().asItem()).get()).isIn(NeMuelchTags.Items.SHIELD_REPAIR_MATERIAL);
    }
}
