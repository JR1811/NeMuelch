package net.shirojr.nemuelch.compat.cca.implementation;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.cca.NeMuelchComponents;
import net.shirojr.nemuelch.init.NeMuelchCustomRegistries;
import net.shirojr.nemuelch.monster.AbstractMonsterType;
import net.shirojr.nemuelch.monster.abilities.AbilityContainer;
import net.shirojr.nemuelch.monster.abilities.util.MonsterTypeData;
import net.shirojr.nemuelch.util.constants.NbtKeys;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unused")
public class MonsterComponent implements Component, ServerTickingComponent, AutoSyncedComponent {
    public static final Identifier KEY = NeMuelch.getId("monster");
    private final LivingEntity entity;
    @Nullable
    private AbstractMonsterType activeType;
    private final AbilityContainer abilities;
    @Nullable
    private MonsterTypeData data;

    public MonsterComponent(LivingEntity entity) {
        this.entity = entity;
        this.abilities = new AbilityContainer();
    }

    public static MonsterComponent get(LivingEntity entity) {
        return NeMuelchComponents.MONSTER.get(entity);
    }

    public Optional<AbstractMonsterType> getActiveType() {
        return Optional.ofNullable(activeType);
    }

    public void setActiveType(@Nullable AbstractMonsterType activeType) {
        AbstractMonsterType old = this.activeType;
        this.activeType = activeType;
        if (!Objects.equals(old, this.activeType)) {
            if (old != null) {
                old.onMonsterTypeLost(this.entity);
            }
            World world = this.entity.getWorld();
            MiscWorldComponent worldComponent = MiscWorldComponent.get(world);
            this.abilities.clear();
            this.data = null;
            if (this.activeType == null) {
                worldComponent.modifyMonsterTracker(uuids -> uuids.remove(entity.getUuid()));
            } else {
                this.activeType.onMonsterTypeGained(this.entity);
                if (this.entity instanceof PlayerEntity player) {
                    this.activeType.initAbilities(player, this.abilities, this.data);
                    this.data = this.activeType.createDynamicData(player);
                }
                worldComponent.modifyMonsterTracker(uuids -> uuids.add(entity.getUuid()));
            }
            this.sync();
        }
    }

    public AbilityContainer getAbilities() {
        return abilities;
    }

    @SuppressWarnings("unchecked")
    public <T extends MonsterTypeData> Optional<T> getData(Class<T> type) {
        return type.isInstance(this.data) ? Optional.of((T) this.data) : Optional.empty();
    }

    @Override
    public void serverTick() {
        if (!(this.entity instanceof ServerPlayerEntity player)) return;
        this.getAbilities().serverTick(player);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag) {
        this.abilities.clear();
        this.data = null;

        if (tag.contains(NbtKeys.MONSTER_TYPE)) {
            this.activeType = NeMuelchCustomRegistries.MONSTERS.get(Identifier.tryParse(tag.getString(NbtKeys.MONSTER_TYPE)));
            if (this.activeType != null && this.entity instanceof PlayerEntity player) {
                this.data = this.activeType.createDynamicData(player);
                this.activeType.initAbilities(player, this.abilities, this.data);
                this.abilities.readFromNbt(tag);
            }
        } else {
            this.activeType = null;
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag) {
        if (this.activeType != null) {
            Identifier id = NeMuelchCustomRegistries.MONSTERS.getId(this.activeType);
            if (id != null) {
                tag.putString(NbtKeys.MONSTER_TYPE, id.toString());
            } else {
                NeMuelch.LOGGER.warn("Stored Monster Type not found in Registry: {}", this.activeType);
                tag.remove(NbtKeys.MONSTER_TYPE);
            }
        } else {
            tag.remove(NbtKeys.MONSTER_TYPE);
        }
    }

    public void sync() {
        if (this.entity.getWorld().isClient()) return;
        NeMuelchComponents.MONSTER.sync(this.entity);
    }
}
