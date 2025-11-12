package net.shirojr.nemuelch.item.custom.adminToolItem;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.item.util.ThirdPersonInvisible;

import java.util.Optional;
import java.util.UUID;

public class EntityTransportToolItem extends Item implements ThirdPersonInvisible {
    public EntityTransportToolItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        World world = user.getWorld();
        Identifier entityId = EntityType.getId(entity.getType());
        var entityNbt = entity.writeNbt(new NbtCompound());
        if (entity instanceof PlayerEntity) {
            if (!world.isClient()) {
                user.sendMessage(Text.translatable("item.nemuelch.entity_transport_tool_no_valid_entity"), false);
            }
            return ActionResult.FAIL;
        }
        if (!world.isClient) {
            user.sendMessage(Text.literal("Entity accepted"), false);
            NbtCompound toolNbt = user.getMainHandStack().getOrCreateNbt();
            toolNbt.putString("entityId", entityId.toString());
            toolNbt.put("entityNbt", entity.writeNbt(entityNbt));
            entity.discard();
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos positionClicked = context.getBlockPos().up();
        PlayerEntity user = context.getPlayer();
        if (context.getStack().hasNbt() && context.getStack().getNbt() != null) {
            World world = context.getWorld();
            NbtCompound nbt = context.getStack().getSubNbt("entityNbt");
            String entityId = context.getStack().getNbt().getString("entityId");
            Optional<EntityType<?>> storedEntity = EntityType.get(entityId);
            if (storedEntity.isEmpty()) {
                NeMuelch.LOGGER.warn("Entity was not present or not readable from EntityTransportationTool");
                return ActionResult.FAIL;
            }

            Entity entity = storedEntity.get().create(world);
            if (entity != null) {
                if (!world.isClient()) {
                    entity.readNbt(nbt);
                    entity.setUuid(UUID.randomUUID());
                    entity.setPos(positionClicked.getX(), positionClicked.getY(), positionClicked.getZ());
                    world.spawnEntity(entity);
                    entity.refreshPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
                    world.emitGameEvent(user, GameEvent.ENTITY_PLACE, positionClicked);
                    NeMuelch.LOGGER.info("created Entity from EntityType:{}", storedEntity.get());
                }
            } else {
                NeMuelch.LOGGER.error("Couldn't spawn entity from EntityTransportationTool");
                return ActionResult.FAIL;
            }
            return ActionResult.SUCCESS;
        }

        return ActionResult.FAIL;
    }
}
