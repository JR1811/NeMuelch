package net.shirojr.nemuelch.compat.cca.implementation;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.NeMuelchComponents;
import net.shirojr.nemuelch.compat.cca.util.FoodModificationEntry;
import net.shirojr.nemuelch.item.misc.FoodItemStackModificator;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FoodModificationComponent implements Component, ServerTickingComponent, AutoSyncedComponent {
    public static final Identifier KEY = NeMuelch.getId("food_modification");
    private final PlayerEntity player;

    private final Object2IntLinkedOpenHashMap<FoodModificationEntry> activeEntries = new Object2IntLinkedOpenHashMap<>();

    public FoodModificationComponent(PlayerEntity player) {
        this.player = player;
    }

    public static FoodModificationComponent get(LivingEntity entity) {
        return NeMuelchComponents.FOOD_MODIFICATION.get(entity);
    }

    public void consumeFood(ItemStack stack) {
        Object2IntOpenHashMap<FoodModificationEntry> entries = FoodItemStackModificator.getFoodModificationData(stack);
        if (entries == null) return;
        entries.forEach(this::addEntry);
    }

    public void addEntry(@Nullable FoodModificationEntry entry, int delay) {
        if (entry == null) return;
        int newDelay = Math.max(0, delay);
        var iterator = this.activeEntries.object2IntEntrySet().iterator();
        while (iterator.hasNext()) {
            var existingEntry = iterator.next();
            if (!existingEntry.getKey().potionId().equals(entry.potionId())) continue;
            if (existingEntry.getIntValue() <= newDelay) return;
            iterator.remove();
            break;
        }
        this.activeEntries.put(entry, Math.max(0, delay));
        this.sync();
    }

    public void clearActiveModifiers() {
        this.activeEntries.clear();
        this.sync();
    }

    public int size() {
        return this.activeEntries.size();
    }

    public boolean isEmpty() {
        return this.activeEntries.isEmpty();
    }

    @Override
    public void serverTick() {
        if (this.activeEntries.isEmpty() || !this.player.isAlive()) return;
        boolean changed = false;
        var iterator = this.activeEntries.object2IntEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Object2IntMap.Entry<FoodModificationEntry> entry = iterator.next();
            int remaining = entry.getIntValue() - 1;
            if (remaining <= 0) {
                entry.getKey().applyEffects(this.player);
                iterator.remove();
                changed = true;
            } else {
                entry.setValue(remaining);
            }
        }
        if (changed) {
            this.sync();
        }
    }

    public List<Text> printAllEntries() {
        List<Text> result = new ArrayList<>();

        for (var entry : this.activeEntries.object2IntEntrySet()) {
            if (entry.getIntValue() <= 0) continue;
            MutableText entryText = Text.empty();
            FoodModificationEntry data = entry.getKey();
            Potion potion = Registries.POTION.get(data.potionId());
            if (Registries.POTION.getId(potion).equals(Registries.POTION.getDefaultId())) continue;

            ItemStack stack = PotionUtil.setPotion(new ItemStack(Items.POTION), potion);
            Text name = stack.getName().copy().styled(style -> style.withColor(PotionUtil.getColor(potion)));
            entryText.append(name);

            MutableText timeText = Text.literal(" in " + (entry.getIntValue() / 20) + " seconds");
            entryText.append(timeText);

            result.add(entryText);
        }
        return result;
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound nbt) {
        this.activeEntries.clear();
        if (nbt.contains(NeMuelchNbtKeys.ACTIVE_FOOD_MODIFICATION)) {
            NbtList entriesNbt = nbt.getList(NeMuelchNbtKeys.ACTIVE_FOOD_MODIFICATION, NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < entriesNbt.size(); i++) {
                NbtCompound entryNbt = entriesNbt.getCompound(i);
                FoodModificationEntry entry = FoodModificationEntry.fromNbt(entryNbt.getCompound(NeMuelchNbtKeys.FOOD_MODIFICATION_ENTRY));
                if (entry == null) continue;
                int delay = entryNbt.getInt(NeMuelchNbtKeys.DELAY);
                this.activeEntries.put(entry, delay);
            }
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound nbt) {
        if (!this.activeEntries.isEmpty()) {
            NbtList activeEntriesNbt = new NbtList();
            for (var entry : this.activeEntries.object2IntEntrySet()) {
                NbtCompound entryNbt = new NbtCompound();
                entryNbt.put(NeMuelchNbtKeys.FOOD_MODIFICATION_ENTRY, entry.getKey().toNbt());
                entryNbt.putInt(NeMuelchNbtKeys.DELAY, entry.getIntValue());
                activeEntriesNbt.add(entryNbt);
            }
            nbt.put(NeMuelchNbtKeys.ACTIVE_FOOD_MODIFICATION, activeEntriesNbt);
        }
    }

    public void sync() {
        if (!(this.player.getWorld() instanceof ServerWorld)) return;
        NeMuelchComponents.FOOD_MODIFICATION.sync(this.player);
    }
}
