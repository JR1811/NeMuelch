package net.shirojr.nemuelch.entity.custom;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Equipment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.network.packet.DummyHitS2CPacket;
import net.shirojr.nemuelch.util.helper.EntityGroupMapper;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class DummyCloseQuarterEntity extends LivingEntity {
    public static final int BASE_ROCKING_DURATION = 20 * 3;

    private final DefaultedList<ItemStack> armorItems = DefaultedList.ofSize(4, ItemStack.EMPTY);
    private final DefaultedList<ItemStack> handItems = DefaultedList.ofSize(2, ItemStack.EMPTY);

    private DummyHitS2CPacket clientHitData;
    private int clientHitAge;
    private EntityGroupMapper currentGroup;


    public DummyCloseQuarterEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
        this.currentGroup = EntityGroupMapper.DEFAULT;
        this.clientHitAge = -1;
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (player.isSneaking()) {
            if (this.getWorld() instanceof ServerWorld serverWorld) {
                this.dropInventory();
                serverWorld.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ARMOR_STAND_HIT,
                        SoundCategory.NEUTRAL, 1f, 1f);
            }
            return ActionResult.SUCCESS;
        }
        if (stack.getItem() instanceof Equipment equipment && canEquip(stack)) {
            equipStack(equipment.getSlotType(), stack);
            return ActionResult.SUCCESS;
        }
        return super.interact(player, hand);
    }

    @Override
    protected void dropInventory() {
        super.dropInventory();
        for (ItemStack stack : handItems) {
            if (stack.isEmpty()) continue;
            ItemScatterer.spawn(getWorld(), this.getX(), this.getY(), this.getZ(), stack);
        }
        this.handItems.clear();
        for (ItemStack stack : armorItems) {
            if (stack.isEmpty()) continue;
            ItemScatterer.spawn(getWorld(), this.getX(), this.getY(), this.getZ(), stack);
        }
        this.armorItems.clear();
    }

    @Override
    public boolean canEquip(ItemStack stack) {
        EquipmentSlot equipmentSlot = MobEntity.getPreferredEquipmentSlot(stack);
        return this.getEquippedStack(equipmentSlot).isEmpty();
    }

    @Override
    public EntityGroup getGroup() {
        return currentGroup.getGroup();
    }

    public void sendHit(float damage, float angleInRad) {
        if (getWorld() instanceof ServerWorld) {
            new DummyHitS2CPacket(this.getId(), damage, angleInRad).send(this);
        }
    }

    @Nullable
    public DummyHitS2CPacket getClientHitData() {
        return clientHitData;
    }

    public void registerClientHitData(@Nullable DummyHitS2CPacket clientHitData) {
        if (!getWorld().isClient()) return;
        this.clientHitData = clientHitData;
        this.setClientHitAge(this.age);
    }

    public void resetClientHitData() {
        this.clientHitData = null;
        this.clientHitAge = -1;
    }

    public int getClientHitAge() {
        return clientHitAge;
    }

    public void setClientHitAge(int clientHitAge) {
        if (clientHitAge == -1) {
            this.clientHitAge = -1;
            return;
        }
        this.clientHitAge = Math.max(clientHitAge, 0);
    }

    @Override
    public void tickMovement() {

    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (!super.damage(source, amount)) return false;
        double hitFraction = 0;
        Vec3d hitDirection;
        Entity attacker = source.getAttacker();
        if (attacker == null) return false;
        Entity damageSourceEntity = source.getSource();

        if (attacker instanceof LivingEntity) {
            Vec3d eyePos = attacker.getEyePos();
            Vec3d endRaycastPos = eyePos.add(attacker.getRotationVec(1f).multiply(20));
            Optional<Vec3d> hitPos = this.getBoundingBox().raycast(eyePos, endRaycastPos);
            if (hitPos.isPresent()) {
                hitFraction = (hitPos.get().y - this.getY()) / this.getHeight();
            }
        }
        if (damageSourceEntity != null && !damageSourceEntity.equals(source.getAttacker())) {
            double impactY = damageSourceEntity.getY();
            hitFraction = (impactY - this.getY()) / this.getHeight();
        }
        if (hitFraction < 0.5) return false;


        if (damageSourceEntity != null && !damageSourceEntity.equals(attacker)) {
            hitDirection = damageSourceEntity.getVelocity().normalize().negate();
        } else {
            hitDirection = attacker.getPos().subtract(this.getPos()).normalize();
        }
        float angleInRad = (float) Math.atan2(hitDirection.z, hitDirection.x);
        this.sendHit(amount, angleInRad);

        NeMuelch.LOGGER.info("Hit Dummy with Amount {} at local HitFraction {} rotated with {} deg", amount, hitFraction, Math.toDegrees(angleInRad));
        return true;
    }

    public static DefaultAttributeContainer.Builder createBaseAttributes() {
        return DefaultAttributeContainer.builder()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, FabricLoader.getInstance().isDevelopmentEnvironment() ? 1000 : 500)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                .add(EntityAttributes.GENERIC_ARMOR)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return this.armorItems;
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return switch (slot.getType()) {
            case HAND -> this.handItems.get(slot.getEntitySlotId());
            case ARMOR -> this.armorItems.get(slot.getEntitySlotId());
        };
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
        switch (slot.getType()) {
            case HAND:
                this.onEquipStack(slot, this.handItems.set(slot.getEntitySlotId(), stack), stack);
                break;
            case ARMOR:
                this.onEquipStack(slot, this.armorItems.set(slot.getEntitySlotId(), stack), stack);
        }
    }

    @Override
    public Arm getMainArm() {
        return Arm.RIGHT;
    }

    @Override
    public boolean shouldDropXp() {
        return false;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        this.handItems.clear();
        if (nbt.contains("HandItems")) {
            Inventories.readNbt(nbt.getCompound("HandItems"), this.handItems);
        }

        this.armorItems.clear();
        if (nbt.contains("ArmorItems")) {
            Inventories.readNbt(nbt.getCompound("ArmorItems"), this.armorItems);
        }

        if (nbt.contains("EntityGroup")) {
            this.currentGroup = EntityGroupMapper.get(nbt.getString("EntityGroup"));
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound handItemsNbt = new NbtCompound();
        Inventories.writeNbt(handItemsNbt, this.handItems);
        nbt.put("HandItems", handItemsNbt);

        NbtCompound armorItemsNbt = new NbtCompound();
        Inventories.writeNbt(armorItemsNbt, this.armorItems);
        nbt.put("ArmorItems", armorItemsNbt);

        nbt.putString("EntityGroup", this.currentGroup.asString());

        return super.writeNbt(nbt);
    }
}
