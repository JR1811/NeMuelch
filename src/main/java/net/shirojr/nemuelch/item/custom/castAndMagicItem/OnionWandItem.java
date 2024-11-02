package net.shirojr.nemuelch.item.custom.castAndMagicItem;

import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.entity.NeMuelchEntities;
import net.shirojr.nemuelch.init.ConfigInit;
import net.shirojr.nemuelch.item.materials.OnionWandMaterial;

import java.util.Random;

public class OnionWandItem extends ToolItem {
    public OnionWandItem(Settings settings) {
        super(OnionWandMaterial.INSTANCE, settings);
    }

    private static final int USE_COOLDOWN_TICKS = 600;
    private static final int MIN_SPAWN_COUNT = ConfigInit.CONFIG.onion.getToolData().getMinAmountSpawn();
    private static final int MAX_SPAWN_COUNT = ConfigInit.CONFIG.onion.getToolData().getMaxAmountSpawn() + 1;

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        Random random = world.random;

        if (world instanceof ServerWorld serverWorld) {
            for (int i = 0; i < world.random.nextInt(MIN_SPAWN_COUNT, MAX_SPAWN_COUNT); i++) {
                int spawnAttempts = 10;
                BlockPos.Mutable pos = new BlockPos.Mutable(
                        user.getX() + random.nextDouble(-5, 5),
                        user.getY() + 1,
                        user.getZ() + random.nextDouble(-5, 5)
                );
                while (!serverWorld.getBlockState(pos).isAir() && spawnAttempts >= 0) {
                    pos.set(
                            user.getX() + random.nextDouble(-5, 5),
                            user.getY() + 1,
                            user.getZ() + random.nextDouble(-5, 5)
                    );
                    spawnAttempts--;
                }
                if (spawnAttempts <= 0) continue;
                NeMuelchEntities.ONION.spawn(serverWorld, null, null, user, pos, SpawnReason.BREEDING, false, false);
            }

            user.getItemCooldownManager().set(this, USE_COOLDOWN_TICKS);
            itemStack.damage(1, user, player -> player.sendToolBreakStatus(hand));
        }

        return TypedActionResult.success(itemStack, world.isClient());
    }

}

