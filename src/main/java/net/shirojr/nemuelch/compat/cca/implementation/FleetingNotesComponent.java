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
import net.shirojr.nemuelch.util.helper.NbtUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class FleetingNotesComponent implements Component, AutoSyncedComponent, CommonTickingComponent {
    public static final Identifier KEY = NeMuelch.getId("fleeting_notes");

    private final World provider;
    private final HashMap<Vec3d, FleetingNoteData> fleetingNotes;

    public FleetingNotesComponent(World world) {
        this.provider = world;
        this.fleetingNotes = new HashMap<>();
    }

    public static FleetingNotesComponent get(World world) {
        return NeMuelchComponents.FLEETING_NOTES.get(world);
    }

    public Map<Vec3d, FleetingNoteData> getUnsyncedData() {
        return Collections.unmodifiableMap(this.fleetingNotes);
    }

    public boolean isEmpty() {
        return this.fleetingNotes.isEmpty();
    }

    public void modifyData(boolean shouldSync, Consumer<HashMap<Vec3d, FleetingNoteData>> data) {
        data.accept(this.fleetingNotes);
        if (shouldSync) this.sync();
    }

    @Override
    public void tick() {
        Iterator<FleetingNoteData> it = this.fleetingNotes.values().iterator();
        boolean shouldSync = false;
        while (it.hasNext()) {
            FleetingNoteData note = it.next();
            if (note.isMarkedForRemoval()) {
                it.remove();
                shouldSync = true;
            } else note.tick();
        }
        if (shouldSync) this.sync();
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound nbt) {
        this.fleetingNotes.clear();
        NbtList notesNbt = nbt.getList("notes", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < notesNbt.size(); i++) {
            NbtCompound noteNbt = notesNbt.getCompound(i);
            Vec3d pos = NbtUtil.vec3dFromNbt(noteNbt, "pos");
            FleetingNoteData data = FleetingNoteData.fromNbt(noteNbt);
            this.fleetingNotes.put(pos, data);
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound nbt) {
        NbtList notesListNbt = new NbtList();
        for (var entry : this.fleetingNotes.entrySet()) {
            NbtCompound noteNbt = new NbtCompound();
            NbtUtil.vec3dToNbt(noteNbt, "pos", entry.getKey());
            entry.getValue().toNbt(noteNbt);
            notesListNbt.add(noteNbt);
        }
        nbt.put("notes", notesListNbt);
    }

    public void sync() {
        NeMuelchComponents.FLEETING_NOTES.sync(this.provider);
    }


    public static class PlayerLeftFleetingNote {
        public static final float VISIBLE_DISTANCE = 20f;

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
            Vec3d pos = leavingPlayer.getPos();
            boolean hideName = world.getGameRules().getBoolean(NemuelchGameRules.PLAYER_LEFT_FLEETING_NOTE_HIDE_NAME);
            FleetingNotesComponent component = FleetingNotesComponent.get(world);
            component.modifyData(true, data ->
                    data.put(pos, new FleetingNoteData(getDuration(world), VISIBLE_DISTANCE, getLeaveText(leavingPlayer, hideName)))
            );
        }
    }
}
