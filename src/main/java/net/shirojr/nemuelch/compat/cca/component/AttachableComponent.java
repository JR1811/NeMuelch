package net.shirojr.nemuelch.compat.cca.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.NeMuelchComponents;
import net.shirojr.nemuelch.util.helper.AttachableHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public interface AttachableComponent extends Component {
    Identifier KEY = NeMuelch.getId("attachable");

    static AttachableComponent get(Entity entity) {
        return NeMuelchComponents.ATTACHABLE.get(entity);
    }

    Entity getProvider();

    void setAttachedEntity(@Nullable Entity attachedUuid);

    Entity getAttachedEntity();

    Entity getSelf();

    default void snap(ServerWorld world, @Nullable Entity other, Consumer<Entity> otherAfterSnap) {
        AttachableHelper.detachBoth(this, Optional.ofNullable(other).map(AttachableComponent::get).orElse(null));
        if (this instanceof Entity self) {
            Vec3d pos = self.getPos();
            world.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                    SoundEvents.ENTITY_LEASH_KNOT_BREAK, SoundCategory.NEUTRAL, 2f, 1f);
        }
        otherAfterSnap.accept(other);
    }

    void sync();
}
