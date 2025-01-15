package net.shirojr.nemuelch.item.custom.armorAndShieldItem;

import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchItemGroups;
import net.shirojr.nemuelch.init.NeMuelchTags;

public class NeMuelchShield extends ShieldItem {

    public final ToolMaterial material;

    public NeMuelchShield(ToolMaterial material) {
        super(new Settings().group(NeMuelchItemGroups.WARFARE)
                .maxCount(1)
                .maxDamage(1250 + material.getDurability())
                .rarity(Rarity.RARE)
        );
        this.material = material;
        DispenserBlock.registerBehavior(this, ArmorItem.DISPENSER_BEHAVIOR);
    }

    @Override
    public UseAction getUseAction(ItemStack itemStack) {
        return UseAction.BLOCK;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack) {
        return 72000;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
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
