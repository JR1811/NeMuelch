package net.shirojr.nemuelch.item.custom.castAndMagicItem;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.shirojr.nemuelch.entity.NeMuelchEntities;
import net.shirojr.nemuelch.entity.custom.OnionEntity;
import net.shirojr.nemuelch.init.ConfigInit;
import net.shirojr.nemuelch.item.materials.OnionWandMaterial;

import java.util.Random;

public class OnionWandItem extends ToolItem {
    public OnionWandItem(Settings settings) {
        super(OnionWandMaterial.INSTANCE, settings);
    }

    // usage cool down of the item
    private static final int USE_COOLDOWN_TICKS = 600;
    // count of entities summoned by one use
    private static final int MIN_COUNT = ConfigInit.CONFIG.onionEntitySummonableAmountMin;
    private static final int MAX_COUNT = ConfigInit.CONFIG.onionEntitySummonableAmountMax + 1; // bound is exclusive

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        Random random = world.random;

        if (!world.isClient()) {
            for (int i = 0; i < world.random.nextInt(MIN_COUNT, MAX_COUNT); i++) {

                OnionEntity onion = new OnionEntity(world, user.getUuid());
                onion.setPos(user.getX() + random.nextDouble(-5,5), user.getY() + 1, user.getZ() + random.nextDouble(-5, 5));

                world.spawnEntity(onion);
            }

            user.getItemCooldownManager().set(this, USE_COOLDOWN_TICKS);
            itemStack.damage(1, user, player -> player.sendToolBreakStatus(hand));
        }

        return TypedActionResult.success(itemStack, world.isClient());
    }

}

