package net.shirojr.nemuelch.compat.cca.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

public record RespawnLocation(Identifier identifier, BlockPos position, RegistryKey<World> dimension,
                              HashSet<UUID> assignedEntities) {
    public static final RespawnLocation DEFAULT = new RespawnLocation(
            NeMuelch.getId("config_default"),
            BlockPos.ofFloored(NeMuelchConfigInit.CONFIG.defaultRespawnLocation),
            World.OVERWORLD
    );

    public RespawnLocation(Identifier identifier, BlockPos position, RegistryKey<World> dimension) {
        this(identifier, position, dimension, new HashSet<>());
    }

    public void toNbt(NbtCompound nbt) {
        NbtCompound locationNbt = new NbtCompound();
        locationNbt.putString("id", identifier.toString());
        locationNbt.putLong("position", position.asLong());
        locationNbt.putString("dimension", dimension.getValue().toString());
        NbtList assignedNbtList = new NbtList();
        for (UUID uuid : assignedEntities) {
            NbtCompound assignedNbt = new NbtCompound();
            assignedNbt.putUuid("entity", uuid);
            assignedNbtList.add(assignedNbt);
        }
        locationNbt.put("assigned", assignedNbtList);
        nbt.put("respawnLocation", locationNbt);
    }

    @Nullable
    public static RespawnLocation fromNbt(NbtCompound nbt) {
        if (!nbt.contains("respawnLocation")) return null;
        NbtCompound locationNbt = nbt.getCompound("respawnLocation");
        Identifier id = Identifier.tryParse(locationNbt.getString("id"));
        BlockPos location = BlockPos.fromLong(locationNbt.getLong("position"));
        RegistryKey<World> world = RegistryKey.of(RegistryKeys.WORLD, Identifier.tryParse(locationNbt.getString("dimension")));
        HashSet<UUID> assignedEntities = new HashSet<>();
        for (NbtElement nbtElement : locationNbt.getList("assigned", NbtElement.COMPOUND_TYPE)) {
            UUID entry = ((NbtCompound) nbtElement).getUuid("entity");
            assignedEntities.add(entry);
        }
        return new RespawnLocation(id, location, world, assignedEntities);
    }

    @Nullable
    public ServerWorld getWorld(MinecraftServer server) {
        return server.getWorld(dimension);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return Objects.equals(identifier, ((RespawnLocation) obj).identifier);
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }
}
