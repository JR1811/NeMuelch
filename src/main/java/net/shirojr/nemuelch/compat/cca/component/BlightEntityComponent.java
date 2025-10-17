package net.shirojr.nemuelch.compat.cca.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.cca.NeMuelchComponents;
import net.shirojr.nemuelch.compat.cca.util.BlightType;

import java.util.Locale;

public interface BlightEntityComponent extends Component, AutoSyncedComponent, CommonTickingComponent {
    Identifier KEY = NeMuelch.getId("blight_entity");

    static BlightEntityComponent get(LivingEntity provider) {
        return NeMuelchComponents.BLIGHT_ENTITY.get(provider);
    }

    LivingEntity getProvider();

    Severity getSeverity(BlightType type);

    void setSeverity(BlightType type, Severity severity, boolean shouldSync);

    void clearSeverities(boolean shouldSync);

    default void sync() {
        NeMuelchComponents.BLIGHT_ENTITY.sync(getProvider());
    }

    enum Severity implements StringIdentifiable {
        NONE, LOW, MEDIUM, HIGH;

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
