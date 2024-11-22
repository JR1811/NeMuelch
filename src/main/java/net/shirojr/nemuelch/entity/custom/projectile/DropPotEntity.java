package net.shirojr.nemuelch.entity.custom.projectile;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.nbt.NbtCompound;
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
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.NeMuelchBlocks;
import net.shirojr.nemuelch.block.entity.DropPotBlockEntity;
import net.shirojr.nemuelch.entity.NeMuelchEntities;
import net.shirojr.nemuelch.entity.damage.DropPotDamageSource;
import net.shirojr.nemuelch.network.NeMuelchS2CPacketHandler;
import net.shirojr.nemuelch.sound.NeMuelchSounds;
import net.shirojr.nemuelch.util.helper.SoundInstanceHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class DropPotEntity extends ProjectileEntity {
    public static final int RENDER_DISTANCE = 300;
    public static final float FALLING_ACCELERATION = 0.04f;


    @Nullable
    private UUID userUuid;
    private static final TrackedData<Integer> COLOR = DataTracker.registerData(DropPotEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(DropPotBlockEntity.SLOT_SIZE, ItemStack.EMPTY);

    public DropPotEntity(World world) {
        super(NeMuelchEntities.DROP_POT, world);
    }

    public DropPotEntity(World world, @NotNull Entity user) {
        this(world);
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
        if (inventory == null) return;
        for (int i = 0; i < this.inventory.size(); i++) {
            if (i > inventory.size() - 1) break;
            this.inventory.set(i, inventory.get(i));
        }
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(COLOR, -1);
    }

    @Override
    public void tick() {
        super.tick();
        Vec3d potVelocity = this.getVelocity();
        HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
        this.onCollision(hitResult);
        this.updateRotation();

        this.setVelocity(potVelocity.multiply(0.99F));
        if (!this.hasNoGravity()) {
            this.setVelocity(this.getVelocity().add(0.0, -FALLING_ACCELERATION, 0.0));
            // this.setPosition(d, e, f);
            this.velocityDirty = true;
            NeMuelch.LOGGER.info(String.valueOf(this.getVelocity().length()));
        }
        this.move(MovementType.SELF, this.getVelocity());
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (this.world instanceof ServerWorld serverWorld) {
            NeMuelch.devLogger(String.valueOf(this.getVelocity().length()));
            onLanded(serverWorld, blockHitResult.getBlockPos().up());
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (entityHitResult.getEntity().getUuid().equals(this.userUuid) && this.age < 60) return;
        if (this.world instanceof ServerWorld serverWorld) {
            NeMuelch.devLogger(String.valueOf(this.getVelocity().length()));
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
        boolean shouldBreak = this.getVelocity().length() > 0.6;
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
            List<ItemStack> throwablePotions = new ArrayList<>();
            for (int i = 0; i < this.inventory.size(); i++) {
                ItemStack stack = this.inventory.get(i);
                if (stack.getItem() instanceof ThrowablePotionItem) {
                    throwablePotions.add(stack.copy());
                    this.inventory.set(i, ItemStack.EMPTY);
                }
            }
            for (ItemStack stack  : throwablePotions) {
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
        ServerPlayNetworking.send(player, NeMuelchS2CPacketHandler.START_SOUND_INSTANCE_CHANNEL, buf);
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
        Inventories.readNbt(nbt, this.inventory);
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.userUuid != null) {
            nbt.putUuid("userUuid", this.userUuid);
        }
        Inventories.writeNbt(nbt, this.inventory);
    }
}
