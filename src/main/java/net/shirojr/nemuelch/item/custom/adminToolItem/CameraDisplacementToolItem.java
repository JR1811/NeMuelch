package net.shirojr.nemuelch.item.custom.adminToolItem;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchItems;
import net.shirojr.nemuelch.item.util.ThirdPersonInvisible;
import net.shirojr.nemuelch.network.util.NetworkIdentifiers;
import net.shirojr.nemuelch.util.helper.NbtUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CameraDisplacementToolItem extends Item implements ThirdPersonInvisible {
    public static final String SEQUENCE_NBT_KEY = "Sequence";
    public static final String MAX_RANGE_NBT_KEY = "MaxRange";
    public static final String MIN_RANGE_NBT_KEY = "MinRange";
    public static final String ORIGIN_POS_NBT_KEY = "OriginPos";
    public static final String ORIGIN_ENTITY_NBT_KEY = "OriginEntity";

    public CameraDisplacementToolItem(Settings settings) {
        super(settings);
    }

    //region getter & setter
    public static Optional<Identifier> getSequence(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(SEQUENCE_NBT_KEY)) return Optional.empty();
        return Optional.ofNullable(Identifier.tryParse(nbt.getString(SEQUENCE_NBT_KEY)));
    }

    public static void setSequence(ItemStack stack, @Nullable Identifier sequence) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(SEQUENCE_NBT_KEY)) {
            return;
        }
        if (sequence == null) {
            nbt.remove(SEQUENCE_NBT_KEY);
            return;
        }
        nbt.putString(SEQUENCE_NBT_KEY, sequence.toString());
    }

    public static Optional<Double> getMaxRange(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(MAX_RANGE_NBT_KEY)) return Optional.empty();
        return Optional.of(nbt.getDouble(MAX_RANGE_NBT_KEY));
    }

    public static void setMaxRange(ItemStack stack, @Nullable Double maxRange) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(MAX_RANGE_NBT_KEY)) {
            return;
        }
        if (maxRange == null) {
            nbt.remove(MAX_RANGE_NBT_KEY);
            return;
        }
        nbt.putDouble(MAX_RANGE_NBT_KEY, maxRange);
    }

    public static Optional<Double> getMinRange(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(MIN_RANGE_NBT_KEY)) return Optional.empty();
        return Optional.of(nbt.getDouble(MIN_RANGE_NBT_KEY));
    }

    public static void setMinRange(ItemStack stack, @Nullable Double minRange) {
        NbtCompound nbt = stack.getNbt();
        if (minRange == null) {
            if (nbt != null) nbt.remove(MIN_RANGE_NBT_KEY);
            return;
        }
        if (nbt == null) {
            nbt = stack.getOrCreateNbt();
        }
        nbt.putDouble(MIN_RANGE_NBT_KEY, minRange);
    }

    public static Optional<Vec3d> getOriginPos(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(ORIGIN_POS_NBT_KEY)) return Optional.empty();
        return Optional.ofNullable(NbtUtil.vec3dFromNbt(nbt, ORIGIN_POS_NBT_KEY));
    }

    public static void setOriginPos(ItemStack stack, @Nullable Vec3d pos) {
        NbtCompound nbt = stack.getNbt();
        if (pos == null) {
            if (nbt != null) nbt.remove(ORIGIN_POS_NBT_KEY);
            return;
        }
        if (nbt == null) {
            nbt = stack.getOrCreateNbt();
        }
        NbtUtil.vec3dToNbt(nbt, ORIGIN_POS_NBT_KEY, pos);
    }

    public static Optional<UUID> getOriginEntity(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(ORIGIN_ENTITY_NBT_KEY)) return Optional.empty();
        return Optional.ofNullable(nbt.getUuid(ORIGIN_ENTITY_NBT_KEY));
    }

    public static void setOriginEntity(ItemStack stack, @Nullable UUID uuid) {
        NbtCompound nbt = stack.getNbt();
        if (uuid == null) {
            if (nbt != null) nbt.remove(ORIGIN_ENTITY_NBT_KEY);
            return;
        }
        if (nbt == null) {
            nbt = stack.getOrCreateNbt();
        }
        nbt.putUuid(ORIGIN_ENTITY_NBT_KEY, uuid);
    }
    //endregion


    public static ItemStack createWithData(Identifier sequence, @Nullable Double maxRange, @Nullable Double minRange,
                                           @Nullable Vec3d originPos, @Nullable UUID originEntity) {
        ItemStack stack = NeMuelchItems.DISPLACEMENT_TOOL.getDefaultStack();
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putString(SEQUENCE_NBT_KEY, sequence.toString());
        if (maxRange != null) {
            nbt.putDouble(MAX_RANGE_NBT_KEY, maxRange);
        }
        if (minRange != null) {
            nbt.putDouble(MIN_RANGE_NBT_KEY, minRange);
        }
        if (originPos != null) {
            NbtUtil.vec3dToNbt(nbt, ORIGIN_POS_NBT_KEY, originPos);
        }
        if (originEntity != null) {
            nbt.putUuid(ORIGIN_ENTITY_NBT_KEY, originEntity);
        }
        return stack;
    }

    @SuppressWarnings("unused")
    public static void clearData(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) return;
        setSequence(stack, null);
        setMaxRange(stack, null);
        setMinRange(stack, null);
        setOriginPos(stack, null);
        setOriginEntity(stack, null);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) return TypedActionResult.pass(stack);
        Identifier sequence = getSequence(stack).orElse(null);
        if (sequence == null) return TypedActionResult.pass(stack);

        if (world instanceof ServerWorld serverWorld) {
            double maxRange = getMaxRange(stack).orElse(-1.);
            double minFalloffRange = getMinRange(stack).orElse(0.);
            Vec3d originPos = getOriginPos(stack).orElse(null);
            UUID originEntity = getOriginEntity(stack).orElse(null);
            if (originPos == null) {
                Entity entity = serverWorld.getEntity(originEntity);
                if (entity != null) originPos = entity.getPos();
            }
            if (originPos == null) {
                originPos = user.getPos();
            }

            StringBuilder nameCollector = new StringBuilder();

            for (ServerPlayerEntity target : PlayerLookup.all(serverWorld.getServer())) {
                double intensity;
                double sqDistance = target.squaredDistanceTo(originPos);

                if (maxRange > 0 && sqDistance > maxRange * maxRange) {
                    continue;
                }
                if (sqDistance < minFalloffRange * minFalloffRange || maxRange == -1) {
                    intensity = 1;
                } else {
                    double distance = Math.sqrt(sqDistance);
                    double adjustedDistance = distance - minFalloffRange;
                    double adjustedMaxRange = maxRange - minFalloffRange;
                    intensity = 1 - (adjustedDistance / adjustedMaxRange);
                }
                if (intensity <= 0) continue;

                if (!nameCollector.isEmpty()) {
                    nameCollector.append(", ");
                }
                nameCollector.append(target.getName().getString());

                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeIdentifier(sequence);
                buf.writeDouble(intensity);
                ServerPlayNetworking.send(target, NetworkIdentifiers.CAMERA_DISPLACEMENT_SEQUENCE_START_SCALED, buf);
            }
            user.sendMessage(Text.literal("Applied Camera Displacement to " + nameCollector), false);
        }

        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        Identifier sequence = getSequence(stack).orElse(null);
        if (sequence != null) {
            getSequence(stack).ifPresent(identifier -> {
                tooltip.add(Text.translatable("item.nemuelch.displacement_tool.tooltip.sequence", identifier));
                tooltip.add(Text.empty());
            });
            getMinRange(stack).ifPresent(min -> tooltip.add(Text.translatable("item.nemuelch.displacement_tool.tooltip.min", min)));
            getMaxRange(stack).ifPresent(max -> tooltip.add(Text.translatable("item.nemuelch.displacement_tool.tooltip.max", max)));
            getOriginPos(stack).ifPresent(pos -> tooltip.add(Text.translatable("item.nemuelch.displacement_tool.tooltip.pos", pos.x, pos.y, pos.z)));
            getOriginEntity(stack).ifPresent(uuid -> tooltip.add(Text.translatable("item.nemuelch.displacement_tool.tooltip.target", uuid)));
            tooltip.add(Text.empty());
        }

        if (Screen.hasShiftDown()) {
            tooltip.add(Text.translatable("item.nemuelch.displacement_tool.tooltip"));
        } else {
            tooltip.add(Text.translatable("item.nemuelch.tooltip.expand.line2"));
        }
    }
}
