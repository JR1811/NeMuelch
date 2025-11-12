package net.shirojr.nemuelch.compat.cca.component;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.custom.RottenMeatBlock;
import net.shirojr.nemuelch.block.entity.custom.RottenMeatBlockEntity;
import net.shirojr.nemuelch.compat.cca.NeMuelchComponents;
import net.shirojr.nemuelch.init.NeMuelchProperties;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.init.NemuelchGameRules;
import net.shirojr.nemuelch.util.logger.LoggerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RottenMeatDigestionComponent implements Component, ServerTickingComponent, AutoSyncedComponent {
    public static final Identifier KEY = NeMuelch.getId("rotten_meat_digestion");

    public final RottenMeatBlockEntity provider;
    public final Box digestionArea;

    public static final int MAX_INTAKE_COOLDOWN = 80;
    public static final int MAX_DIGESTION_SIZE = 5;

    public static final String JUMP_START_NBT_KEY = "JumpStartCompleted";
    public static final String DIGESTION_STACKS_NBT_KEY = "DigestionStacks";
    public static final String DIGESTION_TICK_NBT_KEY = "DigestionTick";
    public static final String INTAKE_COOLDOWN_NBT_KEY = "IntakeCooldown";
    public static final String REWARD_SCORE_NBT_KEY = "RewardScore";

    public static final String LOOT_TABLE_FILE_BASE_NAME = "custom/rotten_meat_digestion";

    private boolean jumpStartComplete;

    private final DefaultedList<ItemStack> digestionStacks;
    private int digestionTick;
    private int intakeCooldown;
    private float digestionRewardTier;

    public RottenMeatDigestionComponent(RottenMeatBlockEntity blockEntity) {
        this.provider = blockEntity;
        this.jumpStartComplete = false;
        this.digestionArea = Box.from(new BlockBox(getProvider().getPos().up()));
        this.digestionStacks = DefaultedList.ofSize(MAX_DIGESTION_SIZE, ItemStack.EMPTY);
        this.digestionTick = 0;
        this.intakeCooldown = 0;
        this.digestionRewardTier = 0;
    }

    public static RottenMeatDigestionComponent get(RottenMeatBlockEntity blockEntity) {
        return NeMuelchComponents.ROTTEN_MEAT_DIGESTION.get(blockEntity);
    }

    public static Optional<RottenMeatDigestionComponent> get(World world, BlockPos pos) {
        return NeMuelchComponents.ROTTEN_MEAT_DIGESTION.maybeGet(world.getBlockEntity(pos));
    }

    public RottenMeatBlockEntity getProvider() {
        return provider;
    }

    public World getWorld() {
        return provider.getWorld();
    }

    public static boolean canDigest(ItemStack stack) {
        return CompostingChanceRegistry.INSTANCE.get(stack.getItem()) > 0;
    }

    public int getNonEmptyDigestionStackSize() {
        int count = 0;
        for (ItemStack digestionStack : this.digestionStacks) {
            if (digestionStack.isEmpty()) continue;
            count++;
        }
        return count;
    }

    public boolean isDigestionFull() {
        for (ItemStack digestionStack : this.digestionStacks) {
            if (digestionStack.isEmpty()) return false;
        }
        return true;
    }

    /**
     * For actual Inventory modification use e.g. {@link #addToDigestion(ItemStack, boolean)} or {@link #digestLatestStack()}.
     * Otherwise, you have to manually {@link #sync()} the component
     */
    public DefaultedList<ItemStack> getDigestionStacks() {
        return digestionStacks;
    }

    public boolean addToDigestion(ItemStack newStack, boolean considerCooldown) {
        if (isIntakeCoolingDown() && considerCooldown) return false;
        if (isDigesting() || this.getDigestionRewardTier() > 0) return false;
        boolean modified = false;
        ItemStack remainingStack = newStack.copy();

        // first pass for possible merges
        for (ItemStack entry : this.digestionStacks) {
            if (entry.isEmpty()) continue;
            if (ItemStack.canCombine(entry, remainingStack)) {
                int spaceAvailable = entry.getMaxCount() - entry.getCount();
                int transferAmount = Math.min(spaceAvailable, remainingStack.getCount());
                if (transferAmount > 0) {
                    entry.increment(transferAmount);
                    remainingStack.decrement(transferAmount);
                    modified = true;
                    if (remainingStack.isEmpty()) {
                        break;
                    }
                }
            }
        }

        // second pass for filling empty slots with left-overs
        if (!remainingStack.isEmpty()) {
            for (int i = 0; i < this.digestionStacks.size(); i++) {
                ItemStack entryStack = this.digestionStacks.get(i);
                if (!entryStack.isEmpty()) continue;
                this.digestionStacks.set(i, remainingStack.copy());
                modified = true;
                break;
            }
        }

        if (modified) {
            this.setIntakeCooldown(MAX_INTAKE_COOLDOWN);
            if (getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.playSound(null, provider.getPos(), NeMuelchSounds.EATING_CRUNCHY, SoundCategory.BLOCKS, 2f, 0.8f);
            }
            if (isDigestionFull()) {
                this.setDigestionTick(getMaxDigestionDuration(), true);
            }
            return true;
        }
        return false;
    }

    public ItemStack digestLatestStack() {
        ItemStack removedStack = null;
        for (int i = 0; i < this.digestionStacks.size(); i++) {
            ItemStack stack = this.digestionStacks.get(i);
            if (stack.isEmpty()) continue;
            removedStack = stack.copy();
            this.digestionStacks.set(i, ItemStack.EMPTY);
            break;
        }
        if (removedStack == null) return ItemStack.EMPTY;
        if (getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.playSound(null, provider.getPos(), NeMuelchSounds.EATING_DIGESTION, SoundCategory.BLOCKS, 2f, 0.8f);
            RottenMeatBlock.spawnParticles(10, 1, provider.getPos(), serverWorld);
        }
        sync();
        return removedStack;
    }

    public int getMaxDigestionDuration() {
        if (getWorld() == null || getWorld().getGameRules() == null) return 3000;
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) return 100;
        return getWorld().getGameRules().getInt(NemuelchGameRules.MEAT_BLOCK_DIGESTION_DURATION);
    }

    public int getDigestionTick() {
        return digestionTick;
    }

    public boolean isDigesting() {
        return this.digestionTick > 0;
    }

    public void setDigestionTick(int digesting, boolean shouldSync) {
        this.digestionTick = MathHelper.clamp(digesting, 0, getMaxDigestionDuration());
        if (shouldSync) {
            sync();
        }
    }

    public int getIntakeCooldown() {
        return intakeCooldown;
    }

    public void setIntakeCooldown(int intakeCooldown) {
        this.intakeCooldown = Math.min(intakeCooldown, MAX_INTAKE_COOLDOWN);
        if (this.intakeCooldown <= 0 || this.intakeCooldown == MAX_INTAKE_COOLDOWN) {
            sync();
        }
    }

    public boolean isIntakeCoolingDown() {
        return this.getIntakeCooldown() > 0;
    }

    public float getDigestionRewardTier() {
        return digestionRewardTier;
    }

    public void setDigestionRewardTier(float digestionRewardTier) {
        this.digestionRewardTier = digestionRewardTier;
        this.sync();
    }

    public static Optional<ItemEntity> getFoodOnTop(World world, BlockPos pos, Box searchArea) {
        if (world.getBlockState(pos.up()).isFullCube(world, pos)) return Optional.empty();

        List<ItemEntity> resultList = new ArrayList<>();
        world.collectEntitiesByType(
                TypeFilter.instanceOf(ItemEntity.class),
                searchArea,
                itemEntity -> canDigest(itemEntity.getStack()),
                resultList,
                1
        );
        return resultList.isEmpty() ? Optional.empty() : Optional.of(resultList.get(0));
    }

    public static int getDigestionRewardTier(ItemConvertible item) {
        float chance = CompostingChanceRegistry.INSTANCE.get(item);
        if (chance >= 1.0f) return 4;
        if (chance >= 0.85f) return 3;
        if (chance >= 0.65f) return 2;
        if (chance >= 0.5f) return 1;
        return 0;
    }

    public static List<ItemStack> getRewards(ServerWorld serverWorld, float tier, RottenMeatBlockEntity blockEntity, @Nullable LivingEntity entity) {
        int maxedTier = 20;
        float rewardQuality = MathHelper.lerp(tier / maxedTier, 0f, 3f);
        String lootDataName = LOOT_TABLE_FILE_BASE_NAME;
        if (rewardQuality >= 2f) {
            lootDataName += "_high";
        } else if (rewardQuality >= 1f) {
            lootDataName += "_mid";
        } else if (rewardQuality >= 0.5f) {
            lootDataName += "_low";
        } else {
            return List.of();
        }
        Identifier identifier = NeMuelch.getId(lootDataName);
        LootTable lootTable = serverWorld.getServer().getLootManager().getLootTable(identifier);
        LoggerUtil.devLogger("Generating loot for: " + identifier + " [%s / %s]".formatted(tier, maxedTier));

        LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder(serverWorld)
                .add(LootContextParameters.ORIGIN, blockEntity.getPos().toCenterPos());
        if (entity != null) {
            builder = builder.add(LootContextParameters.THIS_ENTITY, entity);
        }
        return lootTable.generateLoot(builder.build(LootContextTypes.CHEST));
    }

    private boolean dropRewards(ServerWorld serverWorld) {
        BlockPos dropPos = getProvider().getPos().up();
        List<ItemStack> rewards = getRewards(serverWorld, this.getDigestionRewardTier(), getProvider(), null);
        for (ItemStack rewardStack : rewards) {
            ItemScatterer.spawn(serverWorld, dropPos.getX(), dropPos.getY(), dropPos.getZ(), rewardStack.copy());
        }
        if (!rewards.isEmpty()) {
            serverWorld.playSound(null, dropPos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1f, 0.7f);
            return true;
        } else {
            serverWorld.playSound(null, dropPos, SoundEvents.ENTITY_VILLAGER_HURT, SoundCategory.BLOCKS, 1f, 0.5f);
            return false;
        }
    }

    private void resetDigestion() {
        this.setDigestionRewardTier(0f);
        this.setDigestionTick(0, true);
        if (getNonEmptyDigestionStackSize() > 0) {
            DefaultedList<ItemStack> stacks = this.getDigestionStacks();
            for (int i = 0; i < stacks.size(); i++) {
                ItemStack stack = stacks.get(i);
                if (stack.isEmpty()) continue;
                stacks.set(i, ItemStack.EMPTY);
            }
        }
        getWorld().setBlockState(getProvider().getPos(), getProvider().getCachedState().with(RottenMeatBlock.STAGE, 0), Block.NOTIFY_LISTENERS);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean finishProcessAndReset(ServerWorld serverWorld) {
        if (this.dropRewards(serverWorld)) {
            serverWorld.playSound(null, provider.getPos(), NeMuelchSounds.SQUIRT, SoundCategory.BLOCKS, 1f, 0.5f);
            this.resetDigestion();
            return true;
        }
        return false;
    }

    private void updateBlockState() {
        BlockState currentState = getProvider().getCachedState();
        if (!currentState.contains(RottenMeatBlock.STAGE)) return;

        int currentStage = currentState.get(RottenMeatBlock.STAGE);
        float progress = 1f - ((float) getDigestionTick() / getMaxDigestionDuration());
        progress = MathHelper.clamp(progress, 0f, 1f);
        float stageFloat = 1f + (progress * (NeMuelchProperties.MAX_ROTTEN_MEAT_STAGE - 1));
        int targetStage = Math.min(MathHelper.floor(stageFloat), NeMuelchProperties.MAX_ROTTEN_MEAT_STAGE);

        if (targetStage != currentStage) {
            BlockState newState = currentState.with(RottenMeatBlock.STAGE, targetStage);
            getWorld().setBlockState(getProvider().getPos(), newState, Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    public void serverTick() {
        if (!this.jumpStartComplete && !getProvider().getJumpStartStack().isEmpty()) {
            if (addToDigestion(provider.clearJumpStartStack(), false)) {
                getProvider().clearJumpStartStack();
                this.jumpStartComplete = true;
            }
        }

        Supplier<Optional<ItemEntity>> cachedFoodOnTop = Suppliers.memoize(() -> getFoodOnTop(getWorld(), provider.getPos(), this.digestionArea));

        if (this.isIntakeCoolingDown()) {
            this.setIntakeCooldown(this.getIntakeCooldown() - 1);
        } else if (!isDigestionFull() && cachedFoodOnTop.get().isPresent()) {
            ItemEntity itemEntity = cachedFoodOnTop.get().get();
            if (addToDigestion(itemEntity.getStack(), true)) {
                if (getWorld() instanceof ServerWorld serverWorld) {
                    float pitch = MathHelper.lerp(serverWorld.getRandom().nextFloat(), 0.7f, 0.9f);
                    serverWorld.playSound(null, provider.getPos(), SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.BLOCKS, 1f, pitch);
                    RottenMeatBlock.spawnParticles(10, 1, provider.getPos(), serverWorld);
                    itemEntity.discard();
                }
            }
        }

        if (this.isDigesting()) {
            float digestionTimeProgress = (float) this.getDigestionTick() / getMaxDigestionDuration();
            float digestionStackProgress = (float) getNonEmptyDigestionStackSize() / MAX_DIGESTION_SIZE;
            if (digestionTimeProgress < digestionStackProgress) {
                ItemStack digestedStack = this.digestLatestStack();
                int rewardTier = getDigestionRewardTier(digestedStack.getItem());
                float normalizedStackSize = (float) digestedStack.getCount() / digestedStack.getMaxCount();
                this.setDigestionRewardTier(this.getDigestionRewardTier() + (rewardTier * normalizedStackSize));
            }
            this.setDigestionTick(this.getDigestionTick() - 1, false);
            this.updateBlockState();
            if (!this.isDigesting()) {
                this.sync();
            }
        }
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        if (nbt.contains(JUMP_START_NBT_KEY)) {
            boolean jumpStartCompleted = nbt.getBoolean(JUMP_START_NBT_KEY);
            if (jumpStartCompleted) this.jumpStartComplete = true;
        }

        if (nbt.contains(DIGESTION_STACKS_NBT_KEY)) {
            Inventories.readNbt(nbt.getCompound(DIGESTION_STACKS_NBT_KEY), this.digestionStacks);
        }

        if (nbt.contains(DIGESTION_TICK_NBT_KEY)) {
            this.setDigestionTick(nbt.getInt(DIGESTION_TICK_NBT_KEY), false);
        }

        if (nbt.contains(INTAKE_COOLDOWN_NBT_KEY)) {
            this.setIntakeCooldown(nbt.getInt(INTAKE_COOLDOWN_NBT_KEY));
        }

        if (nbt.contains(REWARD_SCORE_NBT_KEY)) {
            this.digestionRewardTier = nbt.getFloat(REWARD_SCORE_NBT_KEY);
        }
    }

    @Override
    public void writeToNbt(NbtCompound nbt) {
        NbtCompound stacksNbt = new NbtCompound();
        nbt.putBoolean(JUMP_START_NBT_KEY, this.jumpStartComplete);

        Inventories.writeNbt(stacksNbt, this.digestionStacks);
        nbt.put(DIGESTION_STACKS_NBT_KEY, stacksNbt);

        nbt.putInt(DIGESTION_TICK_NBT_KEY, this.getDigestionTick());

        nbt.putInt(INTAKE_COOLDOWN_NBT_KEY, this.getIntakeCooldown());

        nbt.putFloat(REWARD_SCORE_NBT_KEY, this.digestionRewardTier);
    }

    public void sync() {
        getProvider().markDirty();
        NeMuelchComponents.ROTTEN_MEAT_DIGESTION.sync(getProvider());
    }
}
