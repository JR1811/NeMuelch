package net.shirojr.nemuelch.compat.cca.implementation;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.cca.NeMuelchComponents;
import net.shirojr.nemuelch.compat.cca.util.RopeData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class RopesComponent implements Component, AutoSyncedComponent {
    public static final Identifier KEY = NeMuelch.getId("ropes");

    private final List<RopeData> ropes;
    private final World world;

    public RopesComponent(World world) {
        this.world = world;
        this.ropes = new ArrayList<>();
    }

    public static RopesComponent get(World world) {
        return NeMuelchComponents.ROPES.get(world);
    }

    public World getProvider() {
        return this.world;
    }

    public List<RopeData> getRopes() {
        return Collections.unmodifiableList(this.ropes);
    }

    public void modifyRopes(boolean sync, Consumer<List<RopeData>> entries) {
        entries.accept(this.ropes);
        if (sync) this.sync();
    }

    public boolean isEmpty() {
        return this.ropes.isEmpty();
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound nbt) {
        this.ropes.clear();
        NbtList ropesNbt = nbt.getList("ropes", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < ropesNbt.size(); i++) {
            this.ropes.add(RopeData.fromNbt(ropesNbt.getCompound(i)));
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound nbt) {
        NbtList ropesNbt = new NbtList();
        for (RopeData rope : this.ropes) {
            NbtCompound ropeNbt = new NbtCompound();
            rope.toNbt(ropeNbt);
            ropesNbt.add(ropeNbt);
        }
        nbt.put("ropes", ropesNbt);
    }

    public void sync() {
        NeMuelchComponents.ROPES.sync(this.world);
    }
}
