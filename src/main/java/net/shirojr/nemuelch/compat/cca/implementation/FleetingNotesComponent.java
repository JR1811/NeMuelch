package net.shirojr.nemuelch.compat.cca.implementation;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.cca.NeMuelchComponents;
import net.shirojr.nemuelch.compat.cca.util.FleetingNoteData;
import net.shirojr.nemuelch.init.NemuelchGameRules;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class FleetingNotesComponent implements Component, AutoSyncedComponent, CommonTickingComponent {
    public static final Identifier KEY = NeMuelch.getId("fleeting_notes");

    private final World provider;
    private final List<FleetingNoteData.Positioned> notes;

    public FleetingNotesComponent(World world) {
        this.provider = world;
        this.notes = new ArrayList<>();
    }

    public static FleetingNotesComponent get(World world) {
        return NeMuelchComponents.FLEETING_NOTES.get(world);
    }

    public List<FleetingNoteData.Positioned> getUnsyncedData() {
        return Collections.unmodifiableList(this.notes);
    }

    public boolean isEmpty() {
        return this.notes.isEmpty();
    }

    public boolean isAnyInRenderDistance(Vec3d viewPos) {
        for (FleetingNoteData.Positioned note : this.notes) {
            if (note.isOutsideOfRenderDistance(viewPos)) continue;
            return true;
        }
        return false;
    }

    public void modifyData(boolean shouldSync, Consumer<List<FleetingNoteData.Positioned>> data) {
        data.accept(this.notes);
        if (shouldSync) this.sync();
    }

    @Override
    public void tick() {
        Iterator<FleetingNoteData.Positioned> it = this.notes.iterator();
        boolean shouldSync = false;
        while (it.hasNext()) {
            FleetingNoteData.Positioned note = it.next();
            if (note.data().isMarkedForRemoval()) {
                it.remove();
                shouldSync = true;
            } else note.data().tick();
        }
        if (shouldSync) this.sync();
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound nbt) {
        this.notes.clear();
        NbtList notesNbt = nbt.getList("notes", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < notesNbt.size(); i++) {
            NbtCompound noteNbt = notesNbt.getCompound(i);
            this.notes.add(FleetingNoteData.Positioned.fromNbt(noteNbt));
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound nbt) {
        NbtList notesListNbt = new NbtList();
        for (var entry : this.notes) {
            NbtCompound noteNbt = new NbtCompound();
            entry.toNbt(noteNbt);
            notesListNbt.add(noteNbt);
        }
        nbt.put("notes", notesListNbt);
    }

    public void sync() {
        NeMuelchComponents.FLEETING_NOTES.sync(this.provider);
    }


    public static class PlayerLeftFleetingNote {
        public static final float VISIBLE_DISTANCE = 5f;
        public static final float VISIBLE_ANGLE = 20f;

        private PlayerLeftFleetingNote() {
        }

        public static List<Text> getLeaveText(Entity entity, boolean hideName) {
            List<Text> output = new ArrayList<>();
            MutableText line = Text.empty();
            String key = "info.nemuelch.entity_left";
            if (hideName) {
                key += ".no_name";
                line.append(Text.translatable(key));
            } else {
                line.append(Text.translatable(key, entity.getName()));
            }
            output.add(line);
            return output;
        }

        public static int getDuration(ServerWorld world) {
            return world.getGameRules().get(NemuelchGameRules.PLAYER_LEFT_FLEETING_NOTE_DURATION).get();
        }

        public static void create(ServerPlayerEntity leavingPlayer, ServerWorld world) {
            if (!leavingPlayer.getServerWorld().getGameRules().getBoolean(NemuelchGameRules.PLAYER_LEFT_FLEETING_NOTES)) {
                return;
            }
            Vec3d pos = leavingPlayer.getPos();
            boolean hideName = world.getGameRules().getBoolean(NemuelchGameRules.PLAYER_LEFT_FLEETING_NOTE_HIDE_NAME);
            FleetingNotesComponent component = FleetingNotesComponent.get(world);
            component.modifyData(true, data ->
                    data.add(
                            new FleetingNoteData.Positioned(
                                    pos,
                                    new FleetingNoteData(getDuration(world), VISIBLE_DISTANCE, VISIBLE_ANGLE, getLeaveText(leavingPlayer, hideName))
                            )
                    )
            );
        }
    }
}
