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
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class MiscWorldComponent implements Component, AutoSyncedComponent {
    public static final Identifier KEY = NeMuelch.getId("misc_world");

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final World world;
    private final HashSet<UUID> artificialOccasionEntities = new HashSet<>();
    private final HashSet<UUID> monsterTracker = new HashSet<>();

    public MiscWorldComponent(World world) {
        this.world = world;
    }

    public static MiscWorldComponent get(World world) {
        return NeMuelchComponents.MISC_WORLD.get(world);
    }

    public HashSet<UUID> getArtificialOccasionEntities() {
        return artificialOccasionEntities;
    }

    public Set<UUID> getMonsterTracker() {
        return Collections.unmodifiableSet(this.monsterTracker);
    }

    public void modifyMonsterTracker(Consumer<HashSet<UUID>> monsterTrackerProvider) {
        HashSet<UUID> old = new HashSet<>(this.monsterTracker);
        monsterTrackerProvider.accept(this.monsterTracker);
        if (!Objects.equals(this.monsterTracker, old)) {
            this.sync();
        }
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound nbt) {
        if (nbt.contains(NeMuelchNbtKeys.OCCASION_ENTITIES)) {
            this.artificialOccasionEntities.clear();
            NbtList occasionEntitiesNbtList = nbt.getList(NeMuelchNbtKeys.OCCASION_ENTITIES, NbtElement.STRING_TYPE);
            for (int i = 0; i < occasionEntitiesNbtList.size(); i++) {
                UUID entitiyUuid = UUID.fromString(occasionEntitiesNbtList.getString(i));
                this.artificialOccasionEntities.add(entitiyUuid);
            }
        }
        if (nbt.contains(NeMuelchNbtKeys.ACTIVE_MONSTERS)) {
            NbtList activeMonstersNbt = nbt.getList(NeMuelchNbtKeys.ACTIVE_MONSTERS, NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < activeMonstersNbt.size(); i++) {
                NbtCompound entryNbt = activeMonstersNbt.getCompound(i);

            }
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound nbt) {
        NbtList occasionEntities = new NbtList();
        for (UUID entityUuid : this.artificialOccasionEntities) {
            occasionEntities.add(NbtString.of(entityUuid.toString()));
        }
        nbt.put(NeMuelchNbtKeys.OCCASION_ENTITIES, occasionEntities);

    }

    public void sync() {
        NeMuelchComponents.MISC_WORLD.sync(this.world);
    }
}
