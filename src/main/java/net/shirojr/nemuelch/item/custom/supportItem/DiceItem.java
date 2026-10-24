package net.shirojr.nemuelch.item.custom.supportItem;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.entity.custom.DiceEntity;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.item.util.InnerOuterColoring;

public class DiceItem extends Item implements InnerOuterColoring {
    public DiceItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.isSneaking()) {
            if (hand != Hand.MAIN_HAND) return super.use(world, user, hand);
            HitResult hitResult = user.raycast(user.isCreative() ? 5.0F : 4.5F, 0f, false);
            if (!hitResult.getType().equals(HitResult.Type.BLOCK)) return TypedActionResult.fail(stack);
            if (world instanceof ServerWorld serverWorld) {
                Vec3d spawnPos = hitResult.getPos();
                DiceEntity diceEntity = new DiceEntity(serverWorld, stack);
                diceEntity.setPosition(spawnPos);

                Box intersectionCheck = diceEntity.getBoundingBox().contract(1.0E-3);
                if (!serverWorld.isSpaceEmpty(intersectionCheck)) {
                    return TypedActionResult.fail(stack);
                }
                if (!serverWorld.getOtherEntities(diceEntity, intersectionCheck, dice -> dice instanceof DiceEntity).isEmpty()) {
                    return TypedActionResult.fail(stack);
                }

                serverWorld.spawnEntity(diceEntity);
                DiceItem.playPlaceSound(world, spawnPos);
                if (!user.isCreative()) stack.decrement(1);
            }
            return TypedActionResult.success(stack);
        }
        if (user.getMainHandStack().getItem() instanceof DiceItem && user.getOffHandStack().getItem() instanceof DiceItem) {
            if (hand != Hand.MAIN_HAND) return super.use(world, user, hand);
            playShakeSound(world, user.getPos());
            return TypedActionResult.success(stack);
        }
        return TypedActionResult.fail(stack);
    }

    public static void playShakeSound(World world, Vec3d pos) {
        if (!(world instanceof ServerWorld serverWorld)) return;
        float pitch = MathHelper.lerp(world.getRandom().nextFloat(), 0.8f, 1f);
        serverWorld.playSound(null, pos.x, pos.y, pos.z, NeMuelchSounds.DICE_SHAKE, SoundCategory.PLAYERS, 2f, pitch);
    }

    public static void playPickupSound(World world, Vec3d pos) {
        if (!(world instanceof ServerWorld serverWorld)) return;
        float pitch = MathHelper.lerp(world.getRandom().nextFloat(), 0.8f, 1f);
        serverWorld.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.PLAYERS, 2f, pitch);
    }

    public static void playPlaceSound(World world, Vec3d pos) {
        if (!(world instanceof ServerWorld serverWorld)) return;
        float pitch = MathHelper.lerp(world.getRandom().nextFloat(), 0.8f, 1f);
        serverWorld.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.PLAYERS, 2f, pitch);
    }
}
