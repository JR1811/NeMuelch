package net.shirojr.nemuelch.item.custom.castAndMagicItem;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.shirojr.nemuelch.util.constants.NetworkIdentifiers;
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
            tooltip.add(new TranslatableText("item.nemuelch.blocked_book_artifact.tooltip.desc1"));
            tooltip.add(new TranslatableText("item.nemuelch.tooltip.expand.line2"));
        } else {
            tooltip.add(new TranslatableText("item.nemuelch.blocked_book_artifact.unknown1"));
            tooltip.add(new TranslatableText("item.nemuelch.blocked_book_artifact.unknown2"));
        }
    }
}
