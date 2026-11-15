package net.shirojr.nemuelch.compat.cca.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public record FoodModificationEntry(Identifier potionId, @Nullable UUID source) {
    @Nullable
    public static FoodModificationEntry create(Potion potion, @Nullable LivingEntity source) {
        Identifier id = Registries.POTION.getId(potion);
        if (id.equals(Registries.POTION.getDefaultId())) return null;
        return new FoodModificationEntry(id, source == null ? null : source.getUuid());
    }

    public Potion getPotion() {
        return Registries.POTION.get(this.potionId);
    }

    public int getColor() {
        return PotionUtil.getColor(this.getPotion());
    }

    public static int getColor(Collection<FoodModificationEntry> entries) {
        List<StatusEffectInstance> instances = new ArrayList<>();
        entries.forEach(entry -> instances.addAll(entry.getPotion().getEffects()));
        return PotionUtil.getColor(instances);
    }

    @Nullable
    public LivingEntity getSource(ServerWorld serverWorld) {
        if (this.source == null) return null;
        Entity entity = serverWorld.getEntity(this.source);
        return entity instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    public void applyEffects(LivingEntity target) {
        if (!(target.getWorld() instanceof ServerWorld serverWorld)) return;
        LivingEntity source = getSource(serverWorld);
        this.getPotion().getEffects().forEach(instance -> {
            StatusEffectInstance modifiedInstance = new StatusEffectInstance(
                    instance.getEffectType(),
                    (int) (instance.getDuration() * 0.75), instance.getAmplifier(),
                    instance.isAmbient(), false, instance.shouldShowIcon()
            );
            if (source == null) target.addStatusEffect(new StatusEffectInstance(modifiedInstance));
            else target.addStatusEffect(new StatusEffectInstance(modifiedInstance), source);
        });
        if (target instanceof ServerPlayerEntity player) {
            player.sendMessage(Text.translatable("info.nemuelch.food_modification.apply_effects"), true);
            serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                    NeMuelchSounds.EATING_DIGESTION, SoundCategory.PLAYERS, 2f, 0.85f);
        }
    }

    public NbtCompound toNbt() {
        NbtCompound entryNbt = new NbtCompound();
        entryNbt.putString(NeMuelchNbtKeys.POTION, this.potionId.toString());
        if (this.source != null) entryNbt.putUuid(NeMuelchNbtKeys.SOURCE, this.source);
        return entryNbt;
    }

    @Nullable
    public static FoodModificationEntry fromNbt(NbtCompound nbt) {
        Identifier id = Identifier.tryParse(nbt.getString(NeMuelchNbtKeys.POTION));
        if (id == null) return null;
        UUID source = nbt.contains(NeMuelchNbtKeys.SOURCE) ? nbt.getUuid(NeMuelchNbtKeys.SOURCE) : null;
        return new FoodModificationEntry(id, source);
    }
}
