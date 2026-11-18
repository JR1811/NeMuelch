package net.shirojr.nemuelch.item.custom.castAndMagicItem;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchParticleTypes;
import net.shirojr.nemuelch.init.NeMuelchStatusEffects;
import net.shirojr.nemuelch.network.util.NetworkIdentifiers;
import net.shirojr.nemuelch.util.helper.SoundInstanceHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ArtifactItem extends Item {
    public ArtifactItem(Settings settings) {
        super(settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if (!(entity instanceof ServerPlayerEntity player)) return;
        if (stack.getNbt() == null || !stack.getNbt().contains("shouldPlay")) {
            stack.getOrCreateNbt().putBoolean("shouldPlay", selected);
        }

        if (!selected && !stack.getOrCreateNbt().getBoolean("shouldPlay")) {
            stack.getOrCreateNbt().putBoolean("shouldPlay", true);
        }

        if (selected && stack.getNbt() != null && stack.getNbt().getBoolean("shouldPlay")) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeIdentifier(SoundInstanceHelper.WHISPERS.getIdentifier());
            buf.writeVarInt(player.getId());
            ServerPlayNetworking.send(player, NetworkIdentifiers.START_SOUND_INSTANCE_S2C, buf);
            stack.getOrCreateNbt().putBoolean("shouldPlay", false);
        }
    }

    @Override
    public boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        if (oldStack.getNbt() == null || newStack.getNbt() == null) return true;
        NbtCompound oldNbt = oldStack.getNbt().copy();
        oldNbt.remove("shouldPlay");
        NbtCompound newNbt = newStack.getNbt().copy();
        newNbt.remove("shouldPlay");
        return !oldNbt.equals(newNbt);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        if (!Screen.hasShiftDown()) {
            tooltip.add(Text.translatable("item.nemuelch.blocked_book_artifact.tooltip.desc1"));
            tooltip.add(Text.translatable("item.nemuelch.tooltip.expand.line2"));
        } else {
            tooltip.add(Text.translatable("item.nemuelch.blocked_book_artifact.unknown1"));
            tooltip.add(Text.translatable("item.nemuelch.blocked_book_artifact.unknown2"));
        }
    }

    public void onItemEntityKilled(ItemEntity itemEntity, DamageSource source, @Nullable Entity owner) {
        if (owner instanceof ServerPlayerEntity player) {
            if (source.isIn(DamageTypeTags.IS_FIRE) || source.isIn(DamageTypeTags.IS_EXPLOSION)) {
                player.addStatusEffect(
                        new StatusEffectInstance(
                                NeMuelchStatusEffects.EXECUTION, 20 * 60 * 10, 94,
                                false, false, true
                        )
                );
                ServerWorld serverWorld = player.getServerWorld();
                Vec3d pos = itemEntity.getPos();
                spawnParticleRing(serverWorld, pos.add(0, 1.5, 0), 24, 2);
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void spawnParticleRing(ServerWorld world, Vec3d center, int amount, double radius) {
        for (int i = 0; i < amount; i++) {
            double angle = (2 * Math.PI) * i / amount;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;

            world.spawnParticles(NeMuelchParticleTypes.MIASMA_RED,
                    x, center.y + 0.4, z, 1, 0, 0, 0, 0);
        }
    }
}
