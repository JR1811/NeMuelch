package net.shirojr.nemuelch.entity.custom;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.shirojr.nemuelch.entity.misc.DiceRollPlan;
import net.shirojr.nemuelch.init.NeMuelchEntities;
import net.shirojr.nemuelch.item.custom.supportItem.DiceItem;
import net.shirojr.nemuelch.item.util.InnerOuterColoring;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import net.shirojr.nemuelch.util.helper.PlayerLookupUtil;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class DiceEntity extends Entity {
    private static final TrackedData<ItemStack> DICE_STACK = DataTracker.registerData(DiceEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final TrackedData<Boolean> IS_RESTING = DataTracker.registerData(DiceEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> SIDES = DataTracker.registerData(DiceEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> SEED = DataTracker.registerData(DiceEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Quaternionf> FROM_ROT = DataTracker.registerData(DiceEntity.class, TrackedDataHandlerRegistry.QUATERNIONF);
    private static final TrackedData<Quaternionf> TO_ROT = DataTracker.registerData(DiceEntity.class, TrackedDataHandlerRegistry.QUATERNIONF);
    private static final Int2ObjectOpenHashMap<Vector3f> FACE_NORMALS = new Int2ObjectOpenHashMap<>(
            new int[]{1, 2, 3, 4, 5, 6},
            new Vector3f[]{
                    new Vector3f(0, 1, 0),
                    new Vector3f(0, 0, 1),
                    new Vector3f(1, 0, 0),
                    new Vector3f(-1, 0, 0),
                    new Vector3f(0, 0, -1),
                    new Vector3f(0, -1, 0)
            }
    );
    private static final int SERVER_TICK_GRACE = 10;


    private int rollTicks = -1;
    private int result = 1;
    private float yaw = 0f;

    private int rollPlanSeed;
    private DiceRollPlan rollPlan;


    public DiceEntity(EntityType<?> type, World world) {
        super(type, world);
        this.setNoGravity(true);
    }

    public DiceEntity(World world, int sides) {
        this(NeMuelchEntities.DICE, world);
        this.setSides(sides);
    }

    public DiceEntity(World world, ItemStack diceStack) {
        this(world, 6);
        this.setItemStack(diceStack);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(DICE_STACK, ItemStack.EMPTY);
        this.dataTracker.startTracking(IS_RESTING, true);
        this.dataTracker.startTracking(SIDES, 6);
        this.dataTracker.startTracking(SEED, 0);
        this.dataTracker.startTracking(FROM_ROT, restRotation(1, 0));
        this.dataTracker.startTracking(TO_ROT, restRotation(1, 0));
    }

    @Nullable
    public ItemStack getItemStack() {
        ItemStack stack = this.dataTracker.get(DICE_STACK);
        return stack.isEmpty() ? null : stack;
    }

    public void setItemStack(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            this.dataTracker.set(DICE_STACK, ItemStack.EMPTY.copy());
        } else {
            this.dataTracker.set(DICE_STACK, stack.copyWithCount(1));
        }
    }

    public int getSides() {
        return this.getDataTracker().get(SIDES);
    }

    public void setSides(int sides) {
        if (this.getSides() == sides) return;
        this.dataTracker.set(SIDES, sides);
    }

    public int getRollTicks() {
        return rollTicks;
    }

    public Quaternionf getFromRot() {
        return this.getDataTracker().get(FROM_ROT);
    }

    public Quaternionf getToRot() {
        return this.getDataTracker().get(TO_ROT);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isResting() {
        return this.getDataTracker().get(IS_RESTING);
    }

    public void setResting(boolean resting) {
        this.dataTracker.set(IS_RESTING, resting);
    }

    public DiceRollPlan getRollPlan() {
        int seed = this.dataTracker.get(SEED);
        if (this.rollPlan == null || this.rollPlanSeed != seed) {
            this.rollPlanSeed = seed;
            this.rollPlan = DiceRollPlan.create(seed);
        }
        return this.rollPlan;
    }

    public static Quaternionf restRotation(int result, float yaw) {
        Quaternionf faceUp = new Quaternionf().rotationTo(FACE_NORMALS.get(result), new Vector3f(0, 1, 0));
        return new Quaternionf().rotationY(yaw).mul(faceUp);
    }

    public int getResult() {
        return result;
    }

    @Override
    public boolean canHit() {
        return true;
    }

    @Override
    protected float getEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return dimensions.height / 2f;
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (player.isSneaking()) {
            this.dropAsItem();
            return ActionResult.SUCCESS;
        }
        if (!this.isResting()) return ActionResult.PASS;
        World world = this.getWorld();
        if (!world.isClient()) {
            this.startRoll(world.getRandom().nextInt(this.getSides()) + 1);
        }
        return ActionResult.success(world.isClient());
    }

    public void dropAsItem() {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;
        ItemStack storedStack = this.getItemStack();

        if (storedStack != null && !storedStack.isEmpty()) {
            ItemScatterer.spawn(serverWorld, this.getX(), this.getY(), this.getZ(), storedStack.copyWithCount(1));
            DiceItem.playPickupSound(serverWorld, this.getPos());
        }
        this.discard();
    }

    @SuppressWarnings("unused")
    public void onAttacked(PlayerEntity player, Hand hand, @Nullable EntityHitResult hitResult) {
        this.dropAsItem();
    }

    public void startRoll(int targetSide) {
        if (!this.isResting()) return;

        Quaternionf from = restRotation(this.result, this.yaw);
        this.result = MathHelper.clamp(targetSide, 1, this.getSides());
        Quaternionf to = restRotation(this.result, this.yaw);

        this.dataTracker.set(FROM_ROT, from);
        this.dataTracker.set(TO_ROT, to);
        this.dataTracker.set(SEED, this.random.nextInt());

        this.rollTicks = 0;
        this.setResting(false);
    }

    private List<DiceEntity> getDiceNearby(@Nullable Predicate<DiceEntity> filter, double searchSize, boolean includeSelf) {
        Predicate<DiceEntity> predicate = dice -> {
            if (!includeSelf && dice == this) return false;
            return filter == null || filter.test(dice);
        };
        return new ArrayList<>(
                this.getWorld().getEntitiesByType(NeMuelchEntities.DICE, this.getBoundingBox().expand(searchSize), predicate)
        );
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isTouchingWater()) {
            this.dropAsItem();
            return;
        }
        if (!getWorld().isClient() && this.age % 5 == 0) {
            BlockPos supportPos = BlockPos.ofFloored(this.getPos().add(0, -1.0E-3, 0));
            BlockState supportBlockState = getWorld().getBlockState(supportPos);
            if (supportBlockState.isAir() || !supportBlockState.getFluidState().isEmpty()) {
                this.dropAsItem();
                return;
            }
        }

        if (this.rollTicks < 0) return;
        this.rollTicks++;
        if (this.getWorld() instanceof ServerWorld) {
            if (this.random.nextFloat() < 0.2 && !this.getDiceNearby(null, 2, false).isEmpty()) {
                DiceItem.playShakeSound(this.getWorld(), this.getPos());
            }
            if (this.getRollPlan().isFinished(this.rollTicks + SERVER_TICK_GRACE)) {
                this.finish();
            }
        } else {
            if (this.getRollPlan().isFinished(this.rollTicks)) {
                this.rollTicks = -1;
            }
        }
    }

    public void finish() {
        this.rollTicks = -1;
        this.setResting(true);
        if (!(this.getWorld() instanceof ServerWorld)) return;
        int restingSum = this.getDiceNearby(diceEntity ->
                                diceEntity.isResting() && InnerOuterColoring.matchingColors(
                                        diceEntity.getItemStack(),
                                        this.getItemStack()),
                        5,
                        true
                )
                .stream().mapToInt(DiceEntity::getResult).sum();
        PlayerLookupUtil.trackingAndSelf(this).forEach(player ->
                player.sendMessage(Text.translatable("info.nemuelch.dice.result", this.result, restingSum), true)
        );
        /*boolean isHighScore = true;
        for (DiceEntity diceEntity : serverWorld.getEntitiesByType(NeMuelchEntities.DICE, new Box(BlockPos.ofFloored(this.getEyePos())), DiceEntity::isResting)) {
            if (diceEntity.getResult() != this.getSides()) {
                isHighScore = false;
                break;
            }
        }*/
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (!this.getWorld().isClient()) return;
        if (data.equals(IS_RESTING) && !this.isResting()) {
            this.rollTicks = 0;
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains(NeMuelchNbtKeys.STACK)) {
            this.setItemStack(ItemStack.fromNbt(nbt.getCompound(NeMuelchNbtKeys.STACK)));
        }
        if (nbt.contains(NeMuelchNbtKeys.SIDES)) {
            this.setSides(nbt.getInt(NeMuelchNbtKeys.SIDES));
        }
        if (nbt.contains(NeMuelchNbtKeys.RESULT)) {
            this.result = nbt.getInt(NeMuelchNbtKeys.RESULT);
        }
        if (nbt.contains(NeMuelchNbtKeys.YAW)) {
            this.yaw = nbt.getFloat(NeMuelchNbtKeys.YAW);
        }
        this.dataTracker.set(FROM_ROT, restRotation(this.result, this.yaw));
        this.dataTracker.set(TO_ROT, restRotation(this.result, this.yaw));
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        ItemStack storedStack = this.getItemStack();
        if (storedStack == null) nbt.remove(NeMuelchNbtKeys.STACK);
        else nbt.put(NeMuelchNbtKeys.STACK, storedStack.writeNbt(new NbtCompound()));
        nbt.putInt(NeMuelchNbtKeys.SIDES, this.getSides());
        nbt.putInt(NeMuelchNbtKeys.RESULT, this.result);
        nbt.putFloat(NeMuelchNbtKeys.YAW, this.yaw);
    }
}
