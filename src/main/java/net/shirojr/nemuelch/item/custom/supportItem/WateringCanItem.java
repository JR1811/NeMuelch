package net.shirojr.nemuelch.item.custom.supportItem;

import net.minecraft.block.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.shirojr.nemuelch.block.NeMuelchBlocks;
import net.shirojr.nemuelch.util.helper.WateringCanHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WateringCanItem extends BlockItem {
    private WateringCanHelper.ItemMaterial itemMaterial;

    public WateringCanItem(Block block, Settings settings, WateringCanHelper.ItemMaterial itemMaterial) {
        super(block, settings);
        this.itemMaterial = itemMaterial;
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack itemStack = new ItemStack(this);
        WateringCanHelper.writeNbtFillState(itemStack, 0);
        WateringCanHelper.writeNbtMaxFill(itemStack, this.itemMaterial.getCapacity());
        return itemStack;
    }

    public WateringCanHelper.ItemMaterial getItemMaterial() {
        return this.itemMaterial;
    }

    public void setItemMaterial(WateringCanHelper.ItemMaterial material) {
        this.itemMaterial = material;
    }


    public void onCraft(ItemStack stack, World world, PlayerEntity player) {
        if (!stack.hasNbt()) {
            NbtCompound tags = new NbtCompound();
            tags.putInt(WateringCanHelper.FILL_STATE_NBT_KEY, 0);
            tags.putInt(WateringCanHelper.MAX_FILL_NBT_KEY, this.itemMaterial.getCapacity());
            stack.setNbt(tags);
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getPlayer() == null) return super.useOnBlock(context);

        World world = context.getWorld();
        BlockPos oldBlockPos = context.getBlockPos();
        BlockPos newBlockPos = oldBlockPos.offset(context.getSide());
        BlockState blockState = world.getBlockState(oldBlockPos);

        if (context.getPlayer().isSneaking()) {
            ItemPlacementContext placementContext = new ItemPlacementContext(context);
            if (!placementContext.canPlace()) return ActionResult.PASS;
            if (world.getBlockState(oldBlockPos).getBlock() instanceof PlantBlock) {
                world.breakBlock(oldBlockPos, true);
                newBlockPos = oldBlockPos;
            }
            if (!world.isClient()) {
                boolean stateHasProperty = world.getBlockState(newBlockPos).contains(Properties.WATERLOGGED);
                boolean waterloggedState = stateHasProperty ? world.getBlockState(newBlockPos).get(Properties.WATERLOGGED) :
                        world.getBlockState(newBlockPos).getBlock().equals(Blocks.WATER);

                @SuppressWarnings("DataFlowIssue") // WATERING_CAN has WATERLOGGED blockstate
                BlockState wateringCanBlockState = NeMuelchBlocks.WATERING_CAN.getPlacementState(placementContext)
                        .with(Properties.WATERLOGGED, waterloggedState);

                world.setBlockState(newBlockPos, wateringCanBlockState, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
                NeMuelchBlocks.WATERING_CAN.onPlaced(world, newBlockPos, wateringCanBlockState, context.getPlayer(), context.getStack());
                if (!context.getPlayer().getAbilities().creativeMode) context.getStack().decrement(1);
            }
            return ActionResult.SUCCESS;
        }

        if (WateringCanHelper.readNbtFillState(context.getStack()) <= 0) {
            context.getPlayer().sendMessage(new TranslatableText("chat.nemuelch.watering_can.empty"), true);
            return ActionResult.PASS;
        }

        if (!(world.getBlockState(oldBlockPos).getBlock() instanceof Fertilizable) &&
                !(world.getBlockState(newBlockPos).getBlock() instanceof FluidBlock)) {
            return ActionResult.PASS;
        }

        if (!world.isClient()) {
            WateringCanHelper.writeNbtFillState(context.getStack(), WateringCanHelper.readNbtFillState(context.getStack()) - 1);
            world.playSound(null, context.getPlayer().getBlockPos(), SoundEvents.BLOCK_WATER_AMBIENT, SoundCategory.PLAYERS, 2f, 1f);
        }

        if (WateringCanHelper.useOnFertilizable(world, oldBlockPos)) {
            if (!world.isClient) {
                world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, oldBlockPos, 0);
            }
            return ActionResult.success(world.isClient);
        }
        boolean isSolidFullSquare = blockState.isSideSolidFullSquare(world, oldBlockPos, context.getSide());
        if (isSolidFullSquare && BoneMealItem.useOnGround(context.getStack(), world, newBlockPos, context.getSide())) {
            if (!world.isClient) {
                world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, newBlockPos, 0);
            }
            return ActionResult.success(world.isClient);
        }
        return ActionResult.PASS;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(new LiteralText("[" + WateringCanHelper.readNbtFillState(stack) + "/" + WateringCanHelper.getItemMaterial(stack).getCapacity() + "]"));
        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableText("item.nemuelch.watering_can.tooltip.shift1"));
            tooltip.add(new TranslatableText("item.nemuelch.watering_can.tooltip.shift2"));
        } else {
            tooltip.add(new TranslatableText("item.nemuelch.tooltip.expand.line1"));
            tooltip.add(new TranslatableText("item.nemuelch.tooltip.expand.line2"));
        }
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        if (!isIn(group)) return;
        stacks.add(new ItemStack(this));
    }
}
