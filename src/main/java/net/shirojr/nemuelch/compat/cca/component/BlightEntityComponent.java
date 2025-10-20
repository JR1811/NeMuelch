package net.shirojr.nemuelch.compat.cca.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.cca.NeMuelchComponents;
import net.shirojr.nemuelch.compat.cca.util.BlightType;

import java.util.Locale;
import java.util.Set;

public interface BlightEntityComponent extends Component, AutoSyncedComponent, CommonTickingComponent {
    Identifier KEY = NeMuelch.getId("blight_entity");

    static BlightEntityComponent get(LivingEntity provider) {
        return NeMuelchComponents.BLIGHT_ENTITY.get(provider);
    }

    LivingEntity getProvider();

    Severity getSeverity(BlightType type);

    void setSeverity(BlightType type, Severity severity, boolean disregardSeverityRanking, boolean shouldSync);

    void clearSeverities(boolean shouldSync);

    boolean isEmpty();

    default void sync() {
        if (!(getProvider().getWorld() instanceof ServerWorld)) return;
        NeMuelchComponents.BLIGHT_ENTITY.sync(getProvider());
    }

    enum Severity implements StringIdentifiable {
        NONE(0, 0),
        LOW(80, 0),
        MEDIUM(2000, 1),
        HIGH(20000, 3);

        private final int effectDuration;
        private final int effectAmplifier;

        Severity(int effectDuration, int effectAmplifier) {
            this.effectDuration = effectDuration;
            this.effectAmplifier = effectAmplifier;
        }

        public void onApplied(LivingEntity target, Set<BlightType> types) {
            for (BlightType type : types) {
                if (type.getEffect() != null) {
                    target.addStatusEffect(new StatusEffectInstance(type.getEffect(), getEffectDuration(), getEffectAmplifier(), true, true));
                }
            }
            if (target instanceof PlayerEntity player) {
                PlayerInventory inventory = player.getInventory();
                for (int i = 0; i < inventory.size(); i++) {
                    ItemStack stack = inventory.getStack(i);
                    if (stack.isEmpty()) continue;
                    BlightType.applyToStack(stack, types);
                }
            }
        }

        public void onCleared(LivingEntity target, Set<BlightType> types) {

        }

        public int getEffectDuration() {
            return effectDuration;
        }

        public int getEffectAmplifier() {
            return effectAmplifier;
        }

        @Override
        public String asString() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        public static Severity fromString(String name) {
            for (Severity entry : Severity.values()) {
                if (entry.asString().equals(name.toLowerCase(Locale.ROOT))) return entry;
            }
            throw new IllegalStateException("No such Blight Severity: " + name);
        }
    }
}
