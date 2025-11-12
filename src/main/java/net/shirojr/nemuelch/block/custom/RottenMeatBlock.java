package net.shirojr.nemuelch.block.custom;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.shirojr.nemuelch.block.entity.custom.RottenMeatBlockEntity;
import net.shirojr.nemuelch.compat.cca.component.RottenMeatDigestionComponent;
import net.shirojr.nemuelch.init.NeMuelchBlockEntities;
import net.shirojr.nemuelch.init.NeMuelchParticles;
import net.shirojr.nemuelch.init.NeMuelchProperties;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.network.util.NetworkIdentifiers;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

@SuppressWarnings({"deprecation"})
public class RottenMeatBlock extends BlockWithEntity {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final IntProperty STAGE = NeMuelchProperties.ROTTEN_MEAT_STAGE;

    public RottenMeatBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(
                this.getDefaultState()
                        .with(FACING, Direction.NORTH)
                        .with(STAGE, 0)
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING, STAGE);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = super.getPlacementState(ctx);
        if (state == null) return null;
        state = state.with(FACING, ctx.getHorizontalPlayerFacing()).with(STAGE, 0);
        return state;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    /**
     * Use {@link #jumpStartBlockEntity(World, BlockPos, BlockState, ItemStack, boolean)} instead if the BlockEntity should be
     * created with an ItemStack to begin with
     *
     * @return <code>null</code> if invalid because of wrong BlockStates
     */
    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        if (state.get(STAGE) == 0) return null;
        return new RottenMeatBlockEntity(pos, state);
    }

    @Nullable
    public static RottenMeatBlockEntity jumpStartBlockEntity(World world, BlockPos pos, BlockState state, ItemStack jumpStartStack, boolean overwriteStage) {
        if (world.getBlockEntity(pos) != null) return null;
        if (overwriteStage) {
            world.setBlockState(pos, state.with(STAGE, 1));
        } else {
            if (!state.contains(STAGE) || state.get(STAGE) < 1) {
                return null;
            }
        }
        return new RottenMeatBlockEntity(pos, state, jumpStartStack.copy());
    }

    @Override
    public void onStateReplaced(BlockState oldState, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (oldState.isOf(this) && !newState.isOf(this) && world instanceof ServerWorld serverWorld) {
            RottenMeatDigestionComponent.get(world, pos)
                    .ifPresent(component -> ItemScatterer.spawn(serverWorld, pos, component.getDigestionStacks()));
        }

        super.onStateReplaced(oldState, world, pos, newState, moved);

        if (newState.contains(STAGE) && oldState.contains(STAGE)) {
            if (oldState.get(STAGE) == 0 && newState.get(STAGE) != 0) {
                ItemStack itemStack = RottenMeatDigestionComponent.getFoodOnTop(world, pos, Box.from(new BlockBox(pos.up())))
                        .map(ItemEntity::getStack).orElse(ItemStack.EMPTY);
                BlockEntity blockEntity = jumpStartBlockEntity(world, pos, newState, itemStack, false);
                if (blockEntity != null) {
                    world.addBlockEntity(blockEntity);
                }
            }
            if (oldState.get(STAGE) != 0 && newState.get(STAGE) == 0) {
                world.removeBlockEntity(pos);
            }
        }
    }

    /**
     * Ticked in {@link net.shirojr.nemuelch.compat.cca.component.RottenMeatDigestionComponent RottenMeatDigestionComponent}
     */
    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, NeMuelchBlockEntities.ROTTEN_MEAT, (world1, pos, state1, blockEntity) -> {
        });
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        super.onSteppedOn(world, pos, state, entity);
        Random random = world.getRandom();
        double speed = entity.getVelocity().horizontalLengthSquared();
        if (entity instanceof ClientPlayerEntity) {
            if (world instanceof ClientWorld clientWorld) {
                if (entity.age % 10 == 0 && speed > 0.01) {
                    float pitch = MathHelper.lerp(random.nextFloat(), 0.6f, 0.8f);
                    world.playSound(entity.getX(), entity.getY(), entity.getZ(), NeMuelchSounds.SQUIRT, SoundCategory.BLOCKS, 1f, pitch, true);
                    spawnParticles(100, 1, pos, clientWorld, random);
                }
            }
        }
        if (world instanceof ServerWorld serverWorld) {
            if (entity.age % 10 == 0 && speed > 0.01) {
                float pitch = MathHelper.lerp(random.nextFloat(), 0.6f, 0.8f);
                serverWorld.playSound(null, pos.getX(), pos.getY(), pos.getZ(), NeMuelchSounds.SQUIRT, SoundCategory.BLOCKS, 1f, pitch);
            }
            if (entity instanceof LivingEntity livingEntity && !livingEntity.isUndead()) {
                if (entity.age % 60 < 5 && serverWorld.getRandom().nextFloat() < 0.05) {
                    livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 60, 0));
                    world.playSound(null, livingEntity.getBlockPos(), SoundEvents.ENTITY_EVOKER_FANGS_ATTACK, SoundCategory.BLOCKS, 1f, 0.75f);
                    spawnParticles(100, 1, pos, serverWorld);
                }
            }
        }
        if (state.contains(STAGE) && state.get(STAGE) == 0) {
            if (entity instanceof ItemEntity itemEntity && RottenMeatDigestionComponent.canDigest(itemEntity.getStack())) {
                jumpStartBlockEntity(world, pos, state, itemEntity.getStack(), true);
            }
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (!(world instanceof ClientWorld clientWorld)) return;
        int outerParticleRange = 5;
        int innerParticleRange = 1;

        int outerParticleAmount = 1;
        int innerParticleAmount = 5;

        int enclosingBlocksAmount = enclosingBlocks(
                world, pos,
                countableState -> countableState.isOf(this),
                blockerState -> blockerState.getFluidState().isIn(FluidTags.WATER)
        );

        if (enclosingBlocksAmount > 3) {
            spawnParticles(innerParticleAmount, innerParticleRange, pos, clientWorld, random);
        }
        if (enclosingBlocksAmount > 1) {
            spawnParticles(outerParticleAmount, outerParticleRange, pos, clientWorld, random);
        }
    }

    public static void spawnParticles(int amount, int range, BlockPos pos, ClientWorld world, Random random) {
        BlockPos.Mutable mutable = pos.mutableCopy();
        int originX = pos.getX();
        int originY = pos.getY();
        int originZ = pos.getZ();

        for (int i = 0; i < amount; i++) {
            mutable.set(
                    originX + MathHelper.nextInt(random, -range, range),
                    originY - random.nextInt(range),
                    originZ + MathHelper.nextInt(random, -range, range)
            );
            BlockState blockState = world.getBlockState(mutable);
            if (blockState.isFullCube(world, mutable)) continue;

            world.addParticle(
                    NeMuelchParticles.ROTTEN_MEAT_AIR,
                    mutable.getX() + random.nextDouble(),
                    mutable.getY() + random.nextDouble(),
                    mutable.getZ() + random.nextDouble(),
                    0.0,
                    0.8,
                    0.0
            );
        }
    }

    public static void spawnParticles(int amount, int range, BlockPos pos, ServerWorld world) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(amount);
        buf.writeVarInt(range);
        buf.writeLong(pos.asLong());

        for (ServerPlayerEntity target : PlayerLookup.tracking(world, pos)) {
            ServerPlayNetworking.send(target, NetworkIdentifiers.SPAWN_ROTTEN_PARTICLE, PacketByteBufs.copy(buf));
        }
    }

    private int enclosingBlocks(World world, BlockPos pos, Predicate<BlockState> stateMatcher, Predicate<BlockState> blocker) {
        int count = 0;
        for (Direction direction : Direction.values()) {
            BlockState state = world.getBlockState(pos.offset(direction));
            if (blocker.test(state)) return 0;
            if (!stateMatcher.test(state)) continue;
            count++;
        }
        return count;
    }
}
