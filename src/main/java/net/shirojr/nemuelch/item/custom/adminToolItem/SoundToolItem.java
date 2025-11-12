package net.shirojr.nemuelch.item.custom.adminToolItem;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.item.util.ThirdPersonInvisible;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SoundToolItem extends Item implements ThirdPersonInvisible {
    public static final String SOUND_NBT_KEY = "SelectedSound";
    public static final String VOLUME_NBT_KEY = "Volume";
    public static final String PITCH_NBT_KEY = "Pitch";
    public static final String POS_NBT_KEY = "Pos";
    public static final String TARGET_NBT_KEY = "Target";

    public SoundToolItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        getSound(stack).ifPresent(soundEvent -> tooltip.add(Text.translatable("item.nemuelch.sound_tool.tooltip.sound", soundEvent.getId())));
        getVolume(stack).ifPresent(volume -> tooltip.add(Text.translatable("item.nemuelch.sound_tool.tooltip.volume", volume)));
        getPitch(stack).ifPresent(pitch -> tooltip.add(Text.translatable("item.nemuelch.sound_tool.tooltip.pitch", pitch)));
        getPos(stack).ifPresent(pos -> {
            BlockPos blockPos = BlockPos.ofFloored(pos);
            tooltip.add(Text.translatable("item.nemuelch.sound_tool.tooltip.pos", blockPos.getX(), blockPos.getY(), blockPos.getZ()));
        });
        getTargetUuid(stack).ifPresent(uuid -> tooltip.add(Text.translatable("item.nemuelch.sound_tool.tooltip.target", uuid)));

        getSound(stack).ifPresent(soundEvent -> tooltip.add(Text.empty()));

        if (Screen.hasShiftDown()) {
            tooltip.add(Text.translatable("item.nemuelch.sound_tool.tooltip"));
        } else {
            tooltip.add(Text.translatable("item.nemuelch.tooltip.expand.line2"));
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        // maybe add screen interaction? Nah, im too lazy
        if (playSoundOrNotify(world, user, hand)) {
            return TypedActionResult.success(stack);
        }
        return TypedActionResult.fail(stack);
    }

    public static boolean playSoundOrNotify(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        SoundEvent sound = getSound(stack).orElse(null);
        if (sound == null) {
            player.sendMessage(Text.translatable("item.nemuelch.sound_tool.error"), true);
            return false;
        }
        Vec3d pos = getPos(stack).orElse(player.getPos());
        if (world instanceof ServerWorld serverWorld) {
            pos = getTarget(serverWorld, stack).map(Entity::getPos).orElse(pos);
        }
        float volume = getVolume(stack).orElse(1f);
        float pitch = getPitch(stack).orElse(1f);
        world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), sound, SoundCategory.NEUTRAL, volume, pitch);
        return true;
    }

    @Nullable
    public static SoundEvent getSound(Identifier identifier) {
        return Registries.SOUND_EVENT.get(identifier);
    }

    public static Optional<SoundEvent> getSound(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(SOUND_NBT_KEY)) return Optional.empty();
        return Optional.ofNullable(getSound(Identifier.tryParse(nbt.getString(SOUND_NBT_KEY))));
    }

    public static void setSound(ItemStack stack, @Nullable SoundEvent sound) {
        if (sound == null) {
            if (stack.getNbt() != null) stack.getNbt().remove(SOUND_NBT_KEY);
            return;
        }
        Identifier identifier = Registries.SOUND_EVENT.getId(sound);
        if (identifier == null) return;
        stack.getOrCreateNbt().putString(SOUND_NBT_KEY, identifier.toString());
    }

    public static Optional<Float> getVolume(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(VOLUME_NBT_KEY)) return Optional.empty();
        return Optional.of(nbt.getFloat(VOLUME_NBT_KEY));
    }

    public static void setVolume(ItemStack stack, @Nullable Float volume) {
        if (volume == null) {
            if (stack.getNbt() != null) stack.getNbt().remove(VOLUME_NBT_KEY);
            return;
        }
        stack.getOrCreateNbt().putFloat(VOLUME_NBT_KEY, volume);
    }

    public static Optional<Float> getPitch(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(PITCH_NBT_KEY)) return Optional.empty();
        return Optional.of(nbt.getFloat(PITCH_NBT_KEY));
    }

    public static void setPitch(ItemStack stack, @Nullable Float pitch) {
        if (pitch == null) {
            if (stack.getNbt() != null) stack.getNbt().remove(PITCH_NBT_KEY);
            return;
        }
        stack.getOrCreateNbt().putFloat(PITCH_NBT_KEY, pitch);
    }

    public static Optional<Vec3d> getPos(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(POS_NBT_KEY)) return Optional.empty();
        NbtCompound posNbt = nbt.getCompound(POS_NBT_KEY);
        Vec3d pos = new Vec3d(posNbt.getDouble("x"), posNbt.getDouble("y"), posNbt.getDouble("z"));
        return Optional.of(pos);
    }

    public static void setPos(ItemStack stack, @Nullable Vec3d pos) {
        if (pos == null) {
            if (stack.getNbt() != null) stack.getNbt().remove(POS_NBT_KEY);
            return;
        }
        NbtCompound posNbt = new NbtCompound();
        posNbt.putDouble("x", pos.getX());
        posNbt.putDouble("y", pos.getY());
        posNbt.putDouble("z", pos.getZ());
        stack.getOrCreateNbt().put(POS_NBT_KEY, posNbt);
    }

    public static Optional<UUID> getTargetUuid(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(TARGET_NBT_KEY)) return Optional.empty();
        return Optional.ofNullable(nbt.getUuid(TARGET_NBT_KEY));
    }

    public static Optional<Entity> getTarget(ServerWorld world, ItemStack stack) {
        return getTargetUuid(stack).map(world::getEntity);
    }

    public static void setTarget(ItemStack stack, @Nullable Entity entity) {
        if (entity == null) {
            if (stack.getNbt() != null) stack.getNbt().remove(TARGET_NBT_KEY);
            return;
        }
        stack.getOrCreateNbt().putUuid(TARGET_NBT_KEY, entity.getUuid());
    }
}
