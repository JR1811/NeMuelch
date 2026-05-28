package net.shirojr.nemuelch.occasion.type;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.cca.implementation.MiscWorldComponent;
import net.shirojr.nemuelch.compat.satin.NeMuelchShaderManager;
import net.shirojr.nemuelch.network.packet.WorldRendererReloadS2CPacket;
import net.shirojr.nemuelch.network.util.NetworkIdentifiers;
import net.shirojr.nemuelch.occasion.OccasionEntry;
import net.shirojr.nemuelch.occasion.util.EntityStrengthener;
import net.shirojr.nemuelch.occasion.util.OccasionGrade;
import net.shirojr.nemuelch.occasion.util.OccasionType;
import net.shirojr.nemuelch.util.duck.Generation;
import org.joml.Vector4f;

import java.util.*;
import java.util.function.Predicate;

public final class CrimsonPhase extends OccasionType {
    private final long defaultDuration;
    private final int defaultTransitionDuration;

    public CrimsonPhase(long defaultDuration, int defaultTransitionDuration) {
        this.defaultDuration = defaultDuration;
        this.defaultTransitionDuration = defaultTransitionDuration;
    }

    @Override
    public Text getName() {
        return Text.translatable("occasion.nemuelch.crimson_phase");
    }

    @Override
    public List<Text> getDescription() {
        List<Text> result = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            result.add(Text.translatable("occasion.nemuelch.crimson_phase.desc" + i));
        }
        return result;
    }

    @Override
    public Predicate<OccasionType> excludeOther() {
        return super.excludeOther();
    }

    @Override
    public OccasionGrade getGrade() {
        return OccasionGrade.DANGEROUS;
    }

    @Override
    public void onStart(World world, OccasionEntry entry) {
        if (world instanceof ServerWorld serverWorld) {
            MinecraftServer server = serverWorld.getServer();
            for (ServerPlayerEntity target : PlayerLookup.all(server)) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeVarInt(NeMuelchShaderManager.getOrdinal(NeMuelchShaderManager.CRIMSON_PHASE));
                buf.writeFloat(1f);
                buf.writeVarInt(defaultTransitionDuration());
                ServerPlayNetworking.send(target, NetworkIdentifiers.SHADER_TRANSITION_START, buf);
            }
        }
    }

    @Override
    public void onActiveTick(World world, OccasionEntry entry) {

    }

    @Override
    public void onFinish(World world, OccasionEntry entry) {
        if (world instanceof ServerWorld serverWorld) {
            MinecraftServer server = serverWorld.getServer();
            for (ServerPlayerEntity target : PlayerLookup.all(server)) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeVarInt(NeMuelchShaderManager.getOrdinal(NeMuelchShaderManager.CRIMSON_PHASE));
                buf.writeFloat(0f);
                buf.writeVarInt(defaultTransitionDuration());
                ServerPlayNetworking.send(target, NetworkIdentifiers.SHADER_TRANSITION_START, buf);
            }

            new WorldRendererReloadS2CPacket().send(PlayerLookup.all(server));

            for (UUID entityUuid : MiscWorldComponent.get(serverWorld).getArtificialOccasionEntities()) {
                Entity entity = serverWorld.getEntity(entityUuid);
                if (entity != null) {
                    entity.playSound(SoundEvents.BLOCK_CONDUIT_DEACTIVATE, 1f, 0.8f);
                    entity.discard();
                }
            }
        }
    }

    @Override
    public void onPlayerJoinedWorldWhileActive(ServerPlayerEntity player, OccasionEntry entry) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(NeMuelchShaderManager.getOrdinal(NeMuelchShaderManager.CRIMSON_PHASE));
        buf.writeFloat(1f);
        ServerPlayNetworking.send(player, NetworkIdentifiers.SHADER_INTENSITY_SETTER, buf);
    }

    @Override
    public void onPlayerLeftWorldWhileActive(ServerPlayerEntity player, OccasionEntry entry) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(NeMuelchShaderManager.getOrdinal(NeMuelchShaderManager.CRIMSON_PHASE));
        ServerPlayNetworking.send(player, NetworkIdentifiers.SHADER_CLEAR, buf);
    }

    @Override
    public Optional<Vector4f> getSunColor(World world, OccasionEntry entry) {
        return Optional.of(new Vector4f(0.8f, .2f, .4f, 0.4f));
    }

    @Override
    public Optional<Identifier> getMoonSprite(World world, OccasionEntry entry) {
        return Optional.of(NeMuelch.getId("textures/environment/moon_crimson_phase.png"));
    }

    @Override
    public Optional<Float> getSunSize(World world, OccasionEntry entry) {
        return Optional.of(10f);
    }

    @Override
    public OptionalDouble getMoonSize(World world, OccasionEntry entry) {
        return OptionalDouble.of(15f);
    }

    @Override
    public long defaultDuration() {
        return defaultDuration;
    }

    public int defaultTransitionDuration() {
        return defaultTransitionDuration;
    }

    @Override
    public void modifyEntitySpawn(ServerWorld world, Entity entity) {
        super.modifyEntitySpawn(world, entity);
        if (entity instanceof HostileEntity hostileEntity) {
            EntityStrengthener.modifyBaseAttributeIfPresent(
                    hostileEntity.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED),
                    operand -> operand * 1.7
            );

            EntityStrengthener.modifyBaseAttributeIfPresent(
                    hostileEntity.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED),
                    operand -> operand * 1.4
            );

            EntityStrengthener.modifyBaseAttributeIfPresent(
                    hostileEntity.getAttributeInstance(EntityAttributes.GENERIC_ARMOR),
                    operand -> operand + 15
            );

            EntityStrengthener.modifyBaseAttributeIfPresent(
                    hostileEntity.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE),
                    operand -> operand * 3
            );

            EntityStrengthener.modifyBaseAttributeIfPresent(
                    hostileEntity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH),
                    operand -> operand * 3
            );
            hostileEntity.setHealth(hostileEntity.getMaxHealth());

        } else if (entity instanceof PersistentProjectileEntity projectileEntity && projectileEntity.getOwner() instanceof MobEntity) {
            projectileEntity.setDamage(projectileEntity.getDamage() * 2);
        }
    }

    @Override
    public void afterEntityKill(ServerWorld world, Entity attacker, LivingEntity killedEntity) {
        if (!(killedEntity instanceof Generation killedGenerationHolder)) return;
        int killedGeneration = killedGenerationHolder.nemuelch$getGeneration();
        if (Generation.getMaxGeneration(world) <= killedGeneration) return;
        if (attacker instanceof ServerPlayerEntity player) {
            if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
                if (player.isCreative() || player.isSpectator()) {
                    return;
                }
            }
        }
        Random random = world.getRandom();
        EntityType<?> killedType = killedEntity.getType();

        Vec3d lookDir = attacker.getRotationVec(1f);
        Vec3d behindDir = lookDir.negate();
        double maxDeviation = Math.toRadians(40);
        double deviation = (random.nextDouble() * 2 - 1) * maxDeviation;
        double distance = 20 + (random.nextDouble() * 10);
        double cos = Math.cos(deviation);
        double sin = Math.sin(deviation);

        double devX = behindDir.x * cos - behindDir.z * sin;
        double devZ = behindDir.x * sin + behindDir.z * cos;

        Vec3d spawnCenter = attacker.getPos().add(devX * distance, 0, devZ * distance);
        int swarmSize = random.nextInt(10);
        HashSet<UUID> newEntities = new HashSet<>();
        for (int i = 0; i < swarmSize; i++) {
            double spread = 10;
            double x = spawnCenter.x + (random.nextDouble() * 2 - 1) * spread;
            double z = spawnCenter.z + (random.nextDouble() * 2 - 1) * spread;
            double y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, (int) x, (int) z);
            Entity newEntity = killedType.spawn(world, BlockPos.ofFloored(x, y, z), SpawnReason.MOB_SUMMONED);
            if (newEntity instanceof Generation newGenerationHolder) {
                newGenerationHolder.nemuelch$setGeneration(killedGeneration + 1);
                newEntities.add(newEntity.getUuid());
            }
        }
        MiscWorldComponent.get(world).getArtificialOccasionEntities().addAll(newEntities);
    }

    @Override
    public int getModifiedXp(int original, LivingEntity entity, int generation) {
        int maxDegradeGeneration = 5;
        float normalizedGeneration = 1 - MathHelper.clamp(generation / maxDegradeGeneration, 0, 1);
        return (int) (original - (original * normalizedGeneration));
    }

    @Override
    public OptionalInt getGlobalWaterColor(BlockRenderView world, BlockPos pos) {
        return OptionalInt.of(0x6B0F1A);
    }

    @Override
    public OptionalInt getFogWaterColor(ClientWorld world) {
        return OptionalInt.of(0x6B0F1A);
    }

    @Override
    public OptionalDouble getEntitySoundPitch(double original) {
        return OptionalDouble.of(original * 0.5);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CrimsonPhase) obj;
        return this.defaultDuration == that.defaultDuration &&
                this.defaultTransitionDuration == that.defaultTransitionDuration;
    }

    @Override
    public int hashCode() {
        return Objects.hash(defaultDuration, defaultTransitionDuration);
    }

    @Override
    public String toString() {
        return "CrimsonPhase[" +
                "defaultDuration=" + defaultDuration + ", " +
                "defaultTransitionDuration=" + defaultTransitionDuration + ']';
    }

}
