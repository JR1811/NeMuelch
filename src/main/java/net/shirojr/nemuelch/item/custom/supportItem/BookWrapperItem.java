package net.shirojr.nemuelch.item.custom.supportItem;

import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.util.constants.NbtKeys;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BookWrapperItem extends Item {
    public static final int STORABLE_ITEMS_AMOUNT = 1;

    public BookWrapperItem(Settings settings) {
        super(settings);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BRUSH;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 200;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return;
        BlockHitResult raycast = raycast(world, player, RaycastContext.FluidHandling.NONE);
        if (raycast.getType().equals(HitResult.Type.MISS)) {
            stopUsage(player, stack, NbtKeys.STARTED_ON_CANDLE);
            return;
        }
        BlockState targetState = world.getBlockState(raycast.getBlockPos());
        if (!targetState.isIn(NeMuelchTags.Blocks.SIGIL_COLOR_BLOCKS)) {
            stopUsage(player, stack, NbtKeys.STARTED_ON_CANDLE);
        }
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !(user instanceof PlayerEntity)) return super.finishUsing(stack, world, user);
        if (!(world instanceof ServerWorld serverWorld)) return super.finishUsing(stack, world, user);
        if (nbt.contains(NbtKeys.STARTED_ON_CANDLE)) {
            BlockPos targetPos = BlockPos.fromLong(nbt.getLong(NbtKeys.STARTED_ON_CANDLE));
            nbt.remove(NbtKeys.STARTED_ON_CANDLE);
            BlockState candleState = world.getBlockState(targetPos);
            MapColor mapColor = candleState.getMapColor(world, targetPos);
            int renderColor = mapColor.getRenderColor(MapColor.Brightness.HIGH);
            equipSigil(serverWorld, stack, user, renderColor);
        }
        return stack;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        ItemStack stack = context.getStack();
        BlockPos blockPos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        if (player == null) return super.useOnBlock(context);
        World world = player.getWorld();
        if (!(world instanceof ServerWorld)) return super.useOnBlock(context);
        BlockState targetState = world.getBlockState(blockPos);
        if (targetState.isIn(NeMuelchTags.Blocks.SIGIL_COLOR_BLOCKS)) {
            if (targetState.contains(Properties.LIT) && !targetState.get(Properties.LIT)) return ActionResult.PASS;
            stack.getOrCreateNbt().putLong(NbtKeys.STARTED_ON_CANDLE, blockPos.asLong());
        } else {
            NbtCompound nbt = stack.getNbt();
            if (nbt != null) nbt.remove(NbtKeys.STARTED_ON_CANDLE);
        }
        return ActionResult.PASS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return super.use(world, user, hand);
    }

    @Override
    public boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        NbtCompound oldNbt = oldStack.copy().getNbt();
        NbtCompound newNbt = newStack.copy().getNbt();
        if (oldNbt != null && !oldNbt.contains(NbtKeys.SIGIL)) {
            return newNbt != null && newNbt.contains(NbtKeys.SIGIL);
        }

        return false;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("item.nemuelch.book_wrapper.tooltip.desc1"));
        for (ItemStack contentStack : getContent(stack, false)) {
            tooltip.add(contentStack.getName());
        }
    }

    public static void stopUsage(LivingEntity entity, ItemStack stoppedStack, @Nullable String removeNbt) {
        if (!entity.isUsingItem()) return;
        if (!entity.getMainHandStack().equals(stoppedStack)) return;
        entity.stopUsingItem();
        if (removeNbt != null && stoppedStack.getNbt() != null) {
            stoppedStack.getNbt().remove(removeNbt);
        }
    }

    public static void equipSigil(ServerWorld world, ItemStack stack, LivingEntity entity, int color) {
        stack.getOrCreateNbt().putInt(NbtKeys.SIGIL, color);
        world.playSound(null, entity.getBlockPos(), SoundEvents.BLOCK_SLIME_BLOCK_STEP, SoundCategory.PLAYERS, 1f, 1f);
    }

    public static void equipStrip(ServerWorld world, ItemStack stack, LivingEntity entity, int color) {
        stack.getOrCreateNbt().putInt(NbtKeys.STRIP, color);
        world.playSound(null, entity.getBlockPos(), SoundEvents.ENTITY_LEASH_KNOT_PLACE, SoundCategory.PLAYERS, 1f, 1f);
    }

    public static void equipWrapper(ServerWorld world, ItemStack stack, LivingEntity entity, int color) {
        stack.getOrCreateNbt().putInt(NbtKeys.WRAPPER, color);
        world.playSound(null, entity.getBlockPos(), SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 1f, 1f);
    }

    @SuppressWarnings("ConstantValue")
    public static void addContent(ItemStack wrapperStack, ItemStack... contentStacks) {
        NbtCompound nbt = wrapperStack.getNbt();
        NbtList contentList = new NbtList();
        if (nbt != null && nbt.contains(NbtKeys.STORED_ITEMS)) {
            NbtList existingList = nbt.getList(NbtKeys.STORED_ITEMS, NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < Math.max(0, STORABLE_ITEMS_AMOUNT - 1); i++) {
                if (i >= existingList.size() - 1) break;
                contentList.add(existingList.get(i));
            }
        }
        for (ItemStack contentStack : contentStacks) {
            if (!contentStack.isIn(NeMuelchTags.Items.BOOK_WRAPPER_CONTENT)) continue;
            NbtCompound contentStackNbt = new NbtCompound();
            contentStack.writeNbt(contentStackNbt);
            contentList.add(contentStackNbt);
        }
        wrapperStack.getOrCreateNbt().put(NbtKeys.STORED_ITEMS, contentList);
    }

    public static Set<ItemStack> getContent(ItemStack wrappedStack, boolean removeFromWrapper) {
        HashSet<ItemStack> result = new HashSet<>();
        NbtCompound nbt = wrappedStack.getNbt();
        if (nbt == null) return result;
        for (NbtElement nbtElement : nbt.getList(NbtKeys.STORED_ITEMS, NbtElement.COMPOUND_TYPE)) {
            result.add(ItemStack.fromNbt((NbtCompound) nbtElement));
        }
        if (removeFromWrapper) {
            nbt.remove(NbtKeys.STORED_ITEMS);
        }
        return result;
    }
}
