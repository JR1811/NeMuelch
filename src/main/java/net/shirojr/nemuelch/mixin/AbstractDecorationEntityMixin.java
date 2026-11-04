package net.shirojr.nemuelch.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;
import net.shirojr.nemuelch.compat.cca.util.FoodModificationEntry;
import net.shirojr.nemuelch.item.misc.FoodItemStackModificator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractDecorationEntity.class)
public abstract class AbstractDecorationEntityMixin extends Entity {
    private AbstractDecorationEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void addItemFrameFoodModificationParticles(CallbackInfo ci) {
        AbstractDecorationEntity entity = (AbstractDecorationEntity) (Object) this;
        if (!(entity instanceof ItemFrameEntity frameEntity)) return;
        if (!this.getWorld().isClient) return;
        if (this.age <= 200) return;
        if (this.age % 4 != 0 || this.random.nextFloat() > 0.6f) return;

        ItemStack stack = frameEntity.getHeldItemStack();
        if (stack.isEmpty()) return;
        Object2IntOpenHashMap<FoodModificationEntry> modifications = FoodItemStackModificator.getFoodModificationData(stack);
        if (modifications == null || modifications.isEmpty()) return;

        int color = FoodModificationEntry.getColor(modifications.keySet());
        float r = (color >> 16 & 0xFF) / 255f;
        float g = (color >> 8 & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        this.getWorld().addParticle(ParticleTypes.ENTITY_EFFECT,
                this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                this.getY() + (this.random.nextDouble() - 0.5) * 0.5,
                this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                r, g, b);
    }
}
