package net.shirojr.nemuelch.entity.custom.projectile;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.*;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.block.entity.custom.DropPotBlockEntity;
import net.shirojr.nemuelch.entity.damage.DropPotDamageSource;
import net.shirojr.nemuelch.init.NeMuelchBlocks;
import net.shirojr.nemuelch.init.NeMuelchEntities;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.item.custom.supportItem.DropPotBlockItem;
import net.shirojr.nemuelch.util.constants.NetworkIdentifiers;
import net.shirojr.nemuelch.util.helper.SoundInstanceHelper;
import net.shirojr.nemuelch.util.logger.LoggerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DropPotEntity extends ProjectileEntity {
    public static final int RENDER_DISTANCE = 300, MAX_IDLE_TICKS = 120;

    @Nullable
    private UUID userUuid;
    private static final TrackedData<Integer> COLOR = DataTracker.registerData(DropPotEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(DropPotBlockEntity.SLOT_SIZE, ItemStack.EMPTY);

    private int idleTicks = 0;

    public DropPotEntity(EntityType<DropPotEntity> dropPotEntityEntityType, World world) {
        super(dropPotEntityEntityType, world);
    }

    public DropPotEntity(World world, Vec3d pos, Vec3d velocity) {
        this(NeMuelchEntities.DROP_POT, world);
        this.setPosition(pos);
        this.setVelocity(velocity);
        this.velocityDirty = true;
    }

    public DropPotEntity(World world, Vec3d pos, Vec3d velocity, DefaultedList<ItemStack> inventory) {
        this(world, pos, velocity);
        if (inventory == null) return;
        for (int i = 0; i < this.getInventory().size(); i++) {
            this.getInventory().set(i, inventory.get(i));
        }
    }

    public DropPotEntity(World world, Vec3d pos, Vec3d velocity, ItemStack dropPotStack) {
        this(world, pos, velocity);
        if (!(dropPotStack.getItem() instanceof DropPotBlockItem)) {
            throw new IllegalArgumentException("DropPotEntity was initialized with a non-DropPot ItemStack");
        }
        DefaultedList<ItemStack> inventory = DropPotBlockItem.getInventory(dropPotStack);
        if (inventory.size() != this.getInventory().size()) {
            throw new IllegalArgumentException("DropPot Item Inventory size didn't match Entity Inventory");
        }
        for (int i = 0; i < this.getInventory().size(); i++) {
            this.getInventory().set(i, inventory.get(i));
        }
    }

    public DropPotEntity(World world, @NotNull Entity user) {
        this(NeMuelchEntities.DROP_POT, world);
        this.userUuid = user.getUuid();

        BlockPos.Mutable posWalker = user.getBlockPos().mutableCopy();
        for (int i = 0; i < 2; i++) {
            posWalker.move(Direction.DOWN);
            if (!world.getBlockState(posWalker).isAir()) break;
        }
        this.setPosition(Vec3d.ofCenter(posWalker));
        this.setVelocity(user.getVelocity());
        this.setNoGravity(false);
        this.velocityDirty = true;
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.playSound(null, user.getBlockPos(), NeMuelchSounds.POT_RELEASE, SoundCategory.PLAYERS, 5f, 1f);
        }
    }

    public DropPotEntity(World world, @NotNull Entity user, List<ItemStack> inventory) {
        this(world, user);
        if (inventory != null) {
            for (int i = 0; i < this.getInventory().size(); i++) {
                this.getInventory().set(i, inventory.get(i));
            }
        }
    }

    public DefaultedList<ItemStack> getInventory() {
        return inventory;
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(COLOR, -1);
    }

    @Override
    public void tick() {
        super.tick();
        Vec3d potVelocity = this.getVelocity();
        if (potVelocity.normalize().equals(Vec3d.ZERO)) {
            this.idleTicks++;
        }
        if (this.idleTicks >= MAX_IDLE_TICKS && world instanceof ServerWorld serverWorld) {
            this.onLanded(serverWorld, this.getBlockPos());
            this.discard();
            return;
        }
        HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
        this.onCollision(hitResult);
        this.updateRotation();

        this.setVelocity(potVelocity.multiply(0.99F));
        if (!this.hasNoGravity()) {
            this.setVelocity(this.getVelocity().add(0.0, -getFallingSpeed(), 0.0));
            this.velocityDirty = true;
        }
        this.move(MovementType.SELF, this.getVelocity());
    }

    private float getFallingSpeed() {
        return this.isSubmergedInWater() ? 0.005f : 0.04f;
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (this.world instanceof ServerWorld serverWorld) {
            LoggerUtil.devLogger(String.valueOf(this.getVelocity().length()));
            onLanded(serverWorld, blockHitResult.getBlockPos().offset(blockHitResult.getSide()));
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (entityHitResult.getEntity().getUuid().equals(this.userUuid) && this.age < 60) return;
        if (this.world instanceof ServerWorld serverWorld) {
            LoggerUtil.devLogger(String.valueOf(this.getVelocity().length()));
            BlockPos.Mutable posOnGround = entityHitResult.getEntity().getBlockPos().mutableCopy();
            int groundClearance = 5;
            for (int i = 0; i < groundClearance; i++) {
                if (!world.getBlockState(posOnGround.down()).canPlaceAt(world, posOnGround.down())) {
                    break;
                }
                posOnGround.move(Direction.DOWN);
                if (i == groundClearance - 1) {
                    posOnGround = entityHitResult.getEntity().getBlockPos().mutableCopy();
                }
            }
            onLanded(serverWorld, posOnGround.toImmutable());
            Entity user = getUser(this.getWorld());

            float damageRange = (float) MathHelper.clamp(this.getVelocity().length(), 0.5, 2.5);
            float normalizedDamage = (damageRange - 0.5f) / 2.0f;
            entityHitResult.getEntity().damage(DropPotDamageSource.create(user), MathHelper.lerp(normalizedDamage, 1, 20));
        }
        if (!this.world.isClient()) {
            this.discard();
        }
        //speed length = 0.5 - 2.5 (and above)
    }

    private void onLanded(ServerWorld world, BlockPos pos) {
        boolean shouldBreak = this.getVelocity().length() > 0.4;
        for (ItemStack stack : inventory) {
            if (stack.isIn(ItemTags.WOOL) || stack.isIn(ItemTags.CARPETS)) {
                shouldBreak = false;
            }
        }
        if (world.getBlockState(pos.down()).isIn(BlockTags.WOOL) || world.getBlockState(pos).isIn(BlockTags.CARPETS)) {
            shouldBreak = false;
        }

        SoundEvent landingSound = shouldBreak ? NeMuelchSounds.POT_HIT : NeMuelchSounds.POT_LAND;
        int particleAmount = shouldBreak ? 20 : 5;

        world.playSound(null, pos, landingSound, SoundCategory.BLOCKS, 3f, 1f);
        for (int i = 0; i < particleAmount; i++) {
            world.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    this.getPos().getX() + 0.5 + random.nextDouble() / 3.0 * (double) (random.nextBoolean() ? 1 : -1),
                    this.getPos().getY() + random.nextDouble(),
                    this.getPos().getZ() + 0.5 + random.nextDouble() / 3.0 * (double) (random.nextBoolean() ? 1 : -1),
                    1,
                    0.0, 0.07, 0.0,
                    0.2
            );
        }
        if (shouldBreak) {
            boolean canIgniteTnt = false;
            List<ItemStack> throwablePotionStacks = new ArrayList<>();
            int tntStacks = 0;
            for (int i = 0; i < this.inventory.size(); i++) {
                ItemStack stack = this.inventory.get(i);
                if (stack.isIn(NeMuelchTags.Items.IGNITES_POTS)) canIgniteTnt = true;
                if (stack.getItem() instanceof ThrowablePotionItem) {
                    throwablePotionStacks.add(stack.copy());
                    this.inventory.set(i, ItemStack.EMPTY);
                }
                if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof TntBlock) {
                    tntStacks++;
                    this.inventory.set(i, ItemStack.EMPTY);
                }
            }
            for (ItemStack stack : throwablePotionStacks) {
                Vec3d unitDirection = this.getVelocity().multiply(1, 0, 1).normalize();
                double maxAngle = Math.toRadians(30);
                double randomAngle = (world.getRandom().nextDouble() * 2 - 1) * maxAngle;
                double x = unitDirection.x * Math.cos(randomAngle) - unitDirection.z * Math.sin(randomAngle);
                double y = Math.abs(this.getVelocity().getY());
                double z = unitDirection.x * Math.sin(randomAngle) + unitDirection.z * Math.cos(randomAngle);
                unitDirection = new Vec3d(x, y, z).multiply(0.3);

                PotionEntity potionEntity;
                if (getUser(world) instanceof LivingEntity attacker) {
                    potionEntity = new PotionEntity(world, attacker);
                } else {
                    potionEntity = new PotionEntity(world, pos.getX(), pos.getY(), pos.getZ());
                }
                potionEntity.setItem(stack);
                potionEntity.setVelocity(unitDirection.multiply(this.getVelocity().length()));
                potionEntity.setPosition(this.getPos());
                world.spawnEntity(potionEntity);
            }
            if (canIgniteTnt) {
                for (int i = 0; i < tntStacks; i++) {
                    Vec3d unitDirection = this.getVelocity().multiply(1, 0, 1).normalize();
                    double maxAngle = Math.toRadians(30);
                    double randomAngle = (world.getRandom().nextDouble() * 2 - 1) * maxAngle;
                    double x = unitDirection.x * Math.cos(randomAngle) - unitDirection.z * Math.sin(randomAngle);
                    double y = Math.abs(this.getVelocity().getY());
                    double z = unitDirection.x * Math.sin(randomAngle) + unitDirection.z * Math.cos(randomAngle);
                    unitDirection = new Vec3d(x, y, z).multiply(0.3);

                    TntEntity tntEntity;
                    if (getUser(world) instanceof LivingEntity attacker) {
                        tntEntity = new TntEntity(world, this.getX(), this.getY(), this.getZ(), attacker);
                    } else {
                        tntEntity = new TntEntity(EntityType.TNT, world);
                    }
                    tntEntity.setVelocity(unitDirection.multiply(this.getVelocity().length()));
                    tntEntity.setPosition(this.getPos());
                    tntEntity.setFuse(80);
                    world.spawnEntity(tntEntity);
                }
            }
            ItemScatterer.spawn(world, pos.up(), this.inventory);
        } else {
            world.setBlockState(pos, NeMuelchBlocks.DROP_POT.getDefaultState());
            if (world.getBlockEntity(pos) instanceof DropPotBlockEntity blockEntity) {
                blockEntity.replaceContent(this.inventory);
            }
        }
        this.discard();
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeIdentifier(SoundInstanceHelper.DROP_POT.getIdentifier());
        buf.writeVarInt(this.getId());
        ServerPlayNetworking.send(player, NetworkIdentifiers.START_SOUND_INSTANCE_S2C, buf);
    }

    @Nullable
    public Entity getUser(World world) {
        if (!(world instanceof ServerWorld serverWorld) || this.userUuid == null) return null;
        return serverWorld.getEntity(this.userUuid);
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.BLOCKS;
    }

    @Override
    public boolean isCollidable() {
        return true;
    }


    @Override
    public boolean collidesWith(Entity other) {
        return super.collidesWith(other);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("userUuid")) {
            this.userUuid = nbt.getUuid("userUuid");
        }
        if (nbt.contains("motion", 9)) {
            NbtList motion = nbt.getList("motion", NbtElement.DOUBLE_TYPE);
            if (motion.size() == 3) {
                Vec3d motionVector = new Vec3d(motion.getDouble(0), motion.getDouble(1), motion.getDouble(2));
                this.setVelocity(motionVector);
                this.velocityDirty = true;
            }
        }
        Inventories.readNbt(nbt, this.inventory);
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.userUuid != null) {
            nbt.putUuid("userUuid", this.userUuid);
        }
        NbtList motion = new NbtList();
        motion.add(NbtDouble.of(this.getVelocity().getX()));
        motion.add(NbtDouble.of(this.getVelocity().getY()));
        motion.add(NbtDouble.of(this.getVelocity().getZ()));
        nbt.put("motion", motion);
        Inventories.writeNbt(nbt, this.inventory);
    }
}
