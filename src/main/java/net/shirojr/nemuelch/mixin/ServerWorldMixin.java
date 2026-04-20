package net.shirojr.nemuelch.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.shirojr.nemuelch.compat.cca.implementation.OccasionsWorldComponent;
import net.shirojr.nemuelch.occasion.OccasionEntry;
import net.shirojr.nemuelch.occasion.util.OccasionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Credits:
 * <a href="https://github.com/Globox1997/RpgDifficulty/blob/1.21/src/main/java/net/rpgdifficulty/mixin/ServerWorldMixin.java">RPGDifficulty</a>
 * from <a href="https://github.com/Globox1997">Globox1998</a>
 */
@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    @Inject(method = "spawnEntity(Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"))
    private void spawnReinforcedEntities(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        ServerWorld world = (ServerWorld) (Object) this;
        OccasionsWorldComponent worldComponent = OccasionsWorldComponent.get(world);
        for (OccasionEntry entry : worldComponent.getUnsyncedActiveOccasions()) {
            OccasionType occasion = entry.getType();
            occasion.modifyEntitySpawn(world, entity);
        }
    }
}
