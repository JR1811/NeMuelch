package net.shirojr.nemuelch.util.helper;

import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.ConfigInit;
import net.shirojr.nemuelch.item.custom.supportItem.WateringCanItem;
import org.jetbrains.annotations.Nullable;

public class WateringCanHelper {
    private WateringCanHelper() {
        // empty private ctor to avoid instantiating this util class
    }

    public static final String FILL_STATE_NBT_KEY = "watering_can.fillState";
    public static final String MAX_FILL_NBT_KEY = "watering_can.maxFill";
    public static final String SHOULD_FILL_NBT_KEY = "watering_can.shouldFill";

    public static void writeNbtFillState(ItemStack itemStack, int fillState) {
        itemStack.getOrCreateNbt().putInt(FILL_STATE_NBT_KEY, fillState);
    }

    public static int readNbtFillState(ItemStack itemStack) {
        if (!itemStack.getOrCreateNbt().contains(FILL_STATE_NBT_KEY)) return 0;
        return itemStack.getOrCreateNbt().getInt(FILL_STATE_NBT_KEY);
    }

    public static void writeNbtMaxFill(ItemStack itemStack, int fillState) {
        itemStack.getOrCreateNbt().putInt(MAX_FILL_NBT_KEY, fillState);
    }

    @Nullable
    public static ItemMaterial getItemMaterial(ItemStack itemStack) {
        for (var entry : ItemMaterial.values()) {
            if (itemStack.getItem() instanceof WateringCanItem wateringCanItem) {
                if (wateringCanItem.getItemMaterial() == entry) return entry;
            }
        }
        return null;
    }

    public static boolean useOnFertilizable(World world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        if (!(blockState.getBlock() instanceof Fertilizable fertilizable)) return false;

        if (((Fertilizable) blockState.getBlock()).isFertilizable(world, pos, blockState, world.isClient)) {
            if (world instanceof ServerWorld) {
                if (fertilizable.canGrow(world, world.random, pos, blockState)) {
                    fertilizable.grow((ServerWorld) world, world.random, pos, blockState);
                }
            }
            return true;
        }
        return false;
    }

    public enum ItemMaterial implements StringIdentifiable {
        COPPER("copper", ConfigInit.CONFIG.wateringCan.getCopper().getCapacity(), 2),
        IRON("iron", ConfigInit.CONFIG.wateringCan.getIron().getCapacity(), 4),
        GOLD("gold", ConfigInit.CONFIG.wateringCan.getGold().getCapacity(), 8),
        DIAMOND("diamond", ConfigInit.CONFIG.wateringCan.getDiamond().getCapacity(), 10);

        private final int capacity, range;
        private final String id;

        ItemMaterial(String id, int capacity, int range) {
            this.capacity = capacity;
            this.id = id;
            this.range = range;
        }

        public int getCapacity() {
            return this.capacity;
        }
        public int getRange() {
            return this.range;
        }

        @Override
        public String asString() {
            return this.id;
        }
    }
}
