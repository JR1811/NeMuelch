package net.shirojr.nemuelch.item.misc;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.Potion;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import net.shirojr.nemuelch.compat.cca.util.FoodModificationEntry;
import net.shirojr.nemuelch.init.NeMuelchGameRules;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import org.jetbrains.annotations.Nullable;

public class FoodItemStackModificator {
    private static final double SUCCESS_DECAY_PER_ENTRY = 0.5;

    public static void mix(ItemStack stack, FoodModificationEntry entry, int dormantDuration) {
        NbtCompound nbt = stack.getOrCreateNbt();
        NbtList foodModificationNbtList = new NbtList();
        if (nbt.contains(NeMuelchNbtKeys.FOOD_MODIFICATIONS)) {
            NbtList nbtList = nbt.getList(NeMuelchNbtKeys.FOOD_MODIFICATIONS, NbtElement.COMPOUND_TYPE);
            foodModificationNbtList.addAll(nbtList);
        }
        NbtCompound entryNbt = new NbtCompound();
        entryNbt.put(NeMuelchNbtKeys.FOOD_MODIFICATION_ENTRY, entry.toNbt());
        entryNbt.putInt(NeMuelchNbtKeys.DELAY, dormantDuration);
        foodModificationNbtList.add(entryNbt);
        nbt.put(NeMuelchNbtKeys.FOOD_MODIFICATIONS, foodModificationNbtList);
    }

    public static boolean contains(ItemStack stack, Potion potion) {
        Object2IntOpenHashMap<FoodModificationEntry> entries = getFoodModificationData(stack);
        if (entries == null) return false;
        for (FoodModificationEntry entry : entries.keySet()) {
            if (entry.getPotion().equals(potion)) return true;
        }
        return false;
    }

    @Nullable
    public static Object2IntOpenHashMap<FoodModificationEntry> getFoodModificationData(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(NeMuelchNbtKeys.FOOD_MODIFICATIONS)) return null;
        Object2IntOpenHashMap<FoodModificationEntry> result = new Object2IntOpenHashMap<>();
        NbtList nbtList = nbt.getList(NeMuelchNbtKeys.FOOD_MODIFICATIONS, NbtElement.COMPOUND_TYPE);

        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound entryNbt = nbtList.getCompound(i);
            int delay = entryNbt.getInt(NeMuelchNbtKeys.DELAY);
            FoodModificationEntry entry = FoodModificationEntry.fromNbt(entryNbt.getCompound(NeMuelchNbtKeys.FOOD_MODIFICATION_ENTRY));
            result.put(entry, delay);
        }
        return result;
    }

    public static int getFoodModificationCount(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(NeMuelchNbtKeys.FOOD_MODIFICATIONS)) return 0;
        return nbt.getList(NeMuelchNbtKeys.FOOD_MODIFICATIONS, NbtElement.COMPOUND_TYPE).size();
    }

    public static double getNormalizedAlchemistsSuccessRate(ItemStack stack, ServerWorld world, BlockPos modificationOrigin) {
        GameRules gameRules = world.getGameRules();
        int maxModifications = gameRules.getInt(NeMuelchGameRules.FOOD_MODIFICATION_MAX_APPLIED);
        int preAppliedModifications = getFoodModificationCount(stack);
        if (preAppliedModifications >= maxModifications) return 0;

        double successRate = gameRules.get(NeMuelchGameRules.FOOD_MODIFICATION_BASE_SUCCESS_RATE).get();
        int checkRange = gameRules.getInt(NeMuelchGameRules.ALCHEMISTS_SUCCESS_RANGE_CHECK);
        // if (checkRange <= 0) return MathHelper.clamp(successRate, 0, 1);
        if (checkRange > 0) {
            double lowImpactChance = gameRules.get(NeMuelchGameRules.ALCHEMISTS_SUCCESS_LOW_IMPACT_CHANCE).get();
            double midImpactChance = gameRules.get(NeMuelchGameRules.ALCHEMISTS_SUCCESS_MID_IMPACT_CHANCE).get();
            double highImpactChance = gameRules.get(NeMuelchGameRules.ALCHEMISTS_SUCCESS_HIGH_IMPACT_CHANCE).get();

            for (BlockPos entryPos : BlockPos.iterateOutwards(modificationOrigin, checkRange, checkRange, checkRange)) {
                BlockState entryState = world.getBlockState(entryPos);
                if (entryState.isIn(NeMuelchTags.Blocks.ALCHEMISTS_SUCCESS_LOW_IMPACT)) successRate += lowImpactChance;
                if (entryState.isIn(NeMuelchTags.Blocks.ALCHEMISTS_SUCCESS_MID_IMPACT)) successRate += midImpactChance;
                if (entryState.isIn(NeMuelchTags.Blocks.ALCHEMISTS_SUCCESS_HIGH_IMPACT)) successRate += highImpactChance;
                if (successRate >= 1) break;
            }
        }

        // successRate *= (maxModifications - preAppliedModifications) / (double) maxModifications;
        successRate = MathHelper.clamp(successRate, 0, 1) * Math.pow(SUCCESS_DECAY_PER_ENTRY, preAppliedModifications);
        return MathHelper.clamp(successRate, 0, 1);
    }
}
