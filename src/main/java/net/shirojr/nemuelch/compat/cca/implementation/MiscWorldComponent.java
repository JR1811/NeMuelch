package net.shirojr.nemuelch.compat.cca.implementation;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.cca.NeMuelchComponents;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.UUID;

public class MiscWorldComponent implements Component, AutoSyncedComponent {
    public static final Identifier KEY = NeMuelch.getId("misc_world");

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final World world;
    private final HashSet<UUID> artificialOccasionEntities = new HashSet<>();

    public MiscWorldComponent(World world) {
        this.world = world;
    }

    public static MiscWorldComponent get(World world) {
        return NeMuelchComponents.MISC_WORLD.get(world);
    }

    public HashSet<UUID> getArtificialOccasionEntities() {
        return artificialOccasionEntities;
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound nbt) {
        this.artificialOccasionEntities.clear();
        NbtList occasionEntitiesNbtList = nbt.getList("occasionEntities", NbtElement.STRING_TYPE);
        for (int i = 0; i < occasionEntitiesNbtList.size(); i++) {
            UUID entitiyUuid = UUID.fromString(occasionEntitiesNbtList.getString(i));
            this.artificialOccasionEntities.add(entitiyUuid);
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound nbt) {
        NbtList entitiesNbt = new NbtList();
        for (UUID entityUuid : this.artificialOccasionEntities) {
            entitiesNbt.add(NbtString.of(entityUuid.toString()));
        }
        nbt.put("occasionEntities", entitiesNbt);
    }

    public void sync() {
        NeMuelchComponents.MISC_WORLD.sync(this.world);
    }
}
