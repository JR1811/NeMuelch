package net.shirojr.nemuelch.item.custom.adminToolItem;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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

    public static ItemStack create(Identifier sequence, @Nullable Double maxRange, @Nullable Double minRange,
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

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) return TypedActionResult.pass(stack);
        if (!nbt.contains(SEQUENCE_NBT_KEY)) return TypedActionResult.pass(stack);

        Identifier sequence = Identifier.tryParse(nbt.getString(SEQUENCE_NBT_KEY));

        if (world instanceof ServerWorld serverWorld) {
            double maxRange = nbt.contains(MAX_RANGE_NBT_KEY) ? nbt.getDouble(MAX_RANGE_NBT_KEY) : -1;
            double minFalloffRange = nbt.contains(MIN_RANGE_NBT_KEY) ? nbt.getDouble(MIN_RANGE_NBT_KEY) : 0;
            Vec3d originPos = NbtUtil.vec3dFromNbt(nbt, ORIGIN_POS_NBT_KEY);
            UUID originEntity = nbt.contains(ORIGIN_ENTITY_NBT_KEY) ? nbt.getUuid(ORIGIN_ENTITY_NBT_KEY) : null;
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
                if (sqDistance < minFalloffRange * minFalloffRange) {
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
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) return;
        String identifier = nbt.getString(SEQUENCE_NBT_KEY);
        if (identifier == null) return;
        tooltip.add(Text.literal("Sequence: [%s]".formatted(identifier)));
        double maxRange = nbt.contains(MAX_RANGE_NBT_KEY) ? nbt.getDouble(MAX_RANGE_NBT_KEY) : -1;
        if (maxRange != -1) {
            tooltip.add(Text.literal("Max Range: [%s]".formatted(maxRange)));
        }
    }
}
