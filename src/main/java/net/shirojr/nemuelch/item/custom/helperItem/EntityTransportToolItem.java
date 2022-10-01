package net.shirojr.nemuelch.item.custom.helperItem;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.shirojr.nemuelch.NeMuelch;

import java.util.UUID;

public class EntityTransportToolItem extends Item {
    public EntityTransportToolItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {

        World world = user.getWorld();
        Identifier entityId = EntityType.getId(entity.getType());
        var entityNbt = entity.writeNbt(new NbtCompound());

        if (!world.isClient) {

            user.sendMessage(new LiteralText("Entity accepted"), false);
            if (entity instanceof PlayerEntity) {

                user.sendMessage(new TranslatableText("item.nemuelch.entity_transport_tool_no_valid_entity"), false);
            }

            else {

                NbtCompound toolNbt = user.getMainHandStack().getOrCreateNbt();
                toolNbt.putString("entityId", entityId.toString());
                toolNbt.put("entityNbt", entity.writeNbt(entityNbt));

                entity.discard();
            }
        }

        return super.useOnEntity(stack, user, entity, hand);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {

        BlockPos positionClicked = context.getBlockPos().up();

        if (context.getStack().hasNbt()) {

            World world = context.getWorld();
            NbtCompound nbt = context.getStack().getSubNbt("entityNbt");
            String entityId = context.getStack().getNbt().getString("entityId");


            var entityType = EntityType.get(entityId).orElse(null);

            if (entityType != null)  {

                NeMuelch.LOGGER.info("creating Entity from EntityType:" + entityType);

                Entity entity = entityType.create(world);
                entity.readNbt(nbt);

                entity.setPos(positionClicked.getX(), positionClicked.getY(), positionClicked.getZ());
                world.spawnEntity(entity);

                world.emitGameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, positionClicked);

            }
            else NeMuelch.LOGGER.info("entityType is null");

        }

        return super.useOnBlock(context);
    }
}
