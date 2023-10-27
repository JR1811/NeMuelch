package net.shirojr.nemuelch.util.helper;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.shirojr.nemuelch.NeMuelch;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

@SuppressWarnings({"unused"})
public class ExplosionHelper {
    private ExplosionHelper() {
        // Helper class is not instantiable
    }

    /**
     * @param origin          center of explosion
     * @param radius          range of explosion
     * @param blockExceptions BlockStates which are not removed
     * @param blastResistance blocks with blast resistance values higher than this won't be removed
     * @param involveEntities if explosion will move and damage nearby LivingEntities
     * @param entityDamage    amount of maximum damage dealt in HP to nearby LivingEntities
     * @return Information about changed and unchanged BlockStates and their BlockPos and
     * influenced Entities using {@link BlockAndEntityHandler BlockAndEntityHandler}.
     * Can be used for particle placement and other things, or may be left as is.
     */
    public static BlockAndEntityHandler explode(ServerWorld world, BlockPos origin, int radius,
                                                ExplosionShape explosionShape, @Nullable Integer extrusion,
                                                Predicate<BlockState> blockExceptions, int blastResistance,
                                                boolean involveEntities, float entityDamage) {
        BlockAndEntityHandler outputHandler = new BlockAndEntityHandler();
        HashMap<BlockPos, BlockState> explodedBlocks = new HashMap<>();
        BlockPos.Mutable currentPos = new BlockPos.Mutable();

        for (int x = -radius; x < radius; x++) {
            for (int y = -radius; y < radius; y++) {
                for (int z = -radius; z < radius; z++) {
                    if (!explosionShape.isInsideVolume(radius, extrusion, new Vec3i(x, y, z))) continue;

                    currentPos.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    BlockState currentBlockState = world.getBlockState(currentPos);
                    boolean isAir = currentBlockState.isAir();
                    boolean resistsBlast = currentBlockState.getBlock().getBlastResistance() > blastResistance;
                    boolean isExcluded = !blockExceptions.test(currentBlockState);

                    if (isAir || resistsBlast || isExcluded) {
                        outputHandler.addToUnaffectedBlockPosList(currentPos);
                        continue;
                    }

                    outputHandler.addToAffectedBlockStates(new BlockPos(currentPos), currentBlockState);
                    world.setBlockState(currentPos, Blocks.AIR.getDefaultState());
                }
            }
        }
        if (involveEntities) {
            Box box = new Box(new Vec3d(origin.getX() - radius, origin.getY() - radius, origin.getZ() - radius),
                    new Vec3d(origin.getX() + radius, origin.getY() + radius, origin.getZ() + radius));

            List<LivingEntity> possibleEntities = world.getEntitiesByClass(LivingEntity.class, box, livingEntity -> {
                if (!(livingEntity instanceof PlayerEntity player)) return true;
                return !player.getAbilities().creativeMode && !player.isSpectator();
            });

            for (LivingEntity entity : possibleEntities) {
                double squaredDistanceToEntity = entity.getPos().squaredDistanceTo(Vec3d.ofCenter(origin));
                double squaredRadius = radius * radius;
                if (squaredDistanceToEntity > squaredRadius) continue;

                double strength = Math.sqrt(squaredRadius - squaredDistanceToEntity);

                NeMuelch.devLogger("push strength: " + strength + " | distance: " + Math.sqrt(squaredDistanceToEntity));

                Vec3d originVector = new Vec3d(origin.getX(), origin.getY(), origin.getZ());
                Vec3d pushDirection = entity.getPos().subtract(originVector).normalize();
                Vec3d pushWithStrength = pushDirection.multiply(strength);

                entity.setVelocity(pushWithStrength.add(0, 0.35, 0));    // always add a bit of "up-movement"
                entity.velocityModified = true;

                if (entityDamage <= 0) entity.damage(DamageSource.MAGIC, entityDamage);
                else entity.heal(Math.abs(entityDamage));
            }
        }

        return outputHandler;
    }

    /**
     * Basic implementation of
     * {@link ExplosionHelper#explode(ServerWorld, BlockPos, int, ExplosionShape, Integer, Predicate, int, boolean, float) explodeSpherical()}
     *
     * @param origin Center of explosion
     * @param radius Radius of Explosion
     */
    public static void explodeSpherical(ServerWorld world, BlockPos origin, int radius) {
        Predicate<BlockState> isSafeFromExplosion = blockState -> {
            if (blockState.contains(Properties.WATERLOGGED)) return true;
            if (blockState.isOf(Blocks.BEDROCK)) return true;
            return true;
        };

        BlockAndEntityHandler blockAndEntityHandler = explode(world, origin, radius, ExplosionShape.SPHERE, null,
                isSafeFromExplosion, 10000, true, 8);
        HashMap<BlockPos, BlockState> explodedBlocks = blockAndEntityHandler.getAffectedBlockStates();

        world.playSound(null, origin, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 1f, 1f);
        explodedBlocks.forEach((pos, blockState) -> {
            boolean allowParticle = world.random.nextInt(2) < 1;
            if (allowParticle) {
                double xMovment = world.random.nextGaussian() - 0.5;
                double yMovement = world.random.nextGaussian() - 0.5;
                double zMovement = world.random.nextGaussian() - 0.5;

                world.spawnParticles(ParticleTypes.EXPLOSION, pos.getX(), pos.getY(), pos.getZ(), 1,
                        xMovment, yMovement, zMovement, 0.25);
            }
        });
    }

    enum ExplosionShape {
        SPHERE, CYLINDER, CUBE;

        boolean isInsideVolume(int radius, @Nullable Integer extrusion, Vec3i pos) {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();

            return switch (this) {
                case SPHERE -> x * x + y * y + z * z > radius * radius;
                case CYLINDER -> {
                    if (extrusion == null) yield false;
                    if (y < (-extrusion) || y > extrusion) yield false;
                    yield x * x + z * z > radius * radius;
                }
                case CUBE -> true;
            };
        }
    }

    /**
     * Class to keep track of, and handle the blocks and entities which are influenced by the custom explosion
     */
    static class BlockAndEntityHandler {
        private final HashMap<BlockPos, BlockState> affectedBlockStates;
        private final List<BlockPos> unaffectedBlockPosList;
        private final List<LivingEntity> targetedLivingEntities;

        //region constructors
        public BlockAndEntityHandler() {
            this.affectedBlockStates = new HashMap<>();
            this.unaffectedBlockPosList = new ArrayList<>();
            this.targetedLivingEntities = new ArrayList<>();
        }

        public BlockAndEntityHandler(HashMap<BlockPos, BlockState> affectedBlockStates,
                                     List<BlockPos> unaffectedBlockPosList, List<LivingEntity> targetedLivingEntities) {
            this.affectedBlockStates = affectedBlockStates;
            this.unaffectedBlockPosList = unaffectedBlockPosList;
            this.targetedLivingEntities = targetedLivingEntities;
        }
        //endregion

        //region accessor and mutator methods
        public void addToAffectedBlockStates(BlockPos pos, BlockState state) {
            this.affectedBlockStates.put(pos, state);
        }

        public void addToUnaffectedBlockPosList(BlockPos pos) {
            this.unaffectedBlockPosList.add(pos);
        }

        public void addToTargetedLivingEntities(LivingEntity entity) {
            this.targetedLivingEntities.add(entity);
        }

        public HashMap<BlockPos, BlockState> getAffectedBlockStates() {
            return this.affectedBlockStates;
        }

        public List<BlockPos> getUnaffectedBlockPosList() {
            return this.unaffectedBlockPosList;
        }

        public List<LivingEntity> getTargetedLivingEntities() {
            return this.targetedLivingEntities;
        }
        //endregion
    }
}
