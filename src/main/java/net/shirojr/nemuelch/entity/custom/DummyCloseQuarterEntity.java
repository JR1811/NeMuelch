package net.shirojr.nemuelch.entity.custom;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Equipment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.init.NeMuelchItems;
import net.shirojr.nemuelch.network.packet.DummyHitS2CPacket;
import net.shirojr.nemuelch.util.data.DamageAccumulator;
import net.shirojr.nemuelch.util.helper.EntityGroupMapper;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class DummyCloseQuarterEntity extends LivingEntity {
    public static final Identifier LOOT_TABLE_ID = NeMuelch.getId("entities/dummy_cqc");
    public static final int BASE_ROCKING_DURATION = NeMuelchConfigInit.CONFIG.dummyEntityData.getBaseAnimationDuration();

    private final DefaultedList<ItemStack> armorItems = DefaultedList.ofSize(4, ItemStack.EMPTY);
    private final DefaultedList<ItemStack> handItems = DefaultedList.ofSize(2, ItemStack.EMPTY);

    private final DamageAccumulator damageHandler;
    private EntityGroupMapper currentGroup;


    public DummyCloseQuarterEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
        this.currentGroup = EntityGroupMapper.DEFAULT;
        this.damageHandler = new DamageAccumulator();
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
    public ActionResult interact(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (player.isSneaking()) {
            if (stack.getItem() instanceof AxeItem) {
                if (!getWorld().isClient()) {
                    this.kill();
                }
                return ActionResult.SUCCESS;
            }
            if (this.hasEquipment()) {
                if (this.getWorld() instanceof ServerWorld serverWorld) {
                    this.dropInventory();
                    serverWorld.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ARMOR_STAND_HIT,
                            SoundCategory.NEUTRAL, 1f, 1f);
                }
                return ActionResult.SUCCESS;
            }
        }
        for (EntityGroupMapper group : EntityGroupMapper.values()) {
            if (stack.isIn(group.getMarkerItem()) && !this.getGroup().equals(group.getGroup())) {
                this.setCurrentGroup(group);
                if (!player.isCreative()) {
                    stack.decrement(1);
                }
                player.sendMessage(Text.translatable("entity.nemuelch.dummy_cqc.set_group", group.name()), true);
                return ActionResult.SUCCESS;
            }
        }
        if (stack.getItem() instanceof Equipment equipment && canEquip(stack)) {
            this.equipStack(equipment.getSlotType(), stack);
            return ActionResult.SUCCESS;
        }
        return super.interact(player, hand);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (!super.damage(source, amount)) return false;
        if (source.equals(getDamageSources().genericKill()) && amount >= Float.MAX_VALUE) return true;
        double hitFraction = 0;
        Entity attacker = source.getAttacker();
        Entity damageSourceEntity = source.getSource();

        if (attacker instanceof LivingEntity) {
            Vec3d eyePos = attacker.getEyePos();
            Vec3d endRaycastPos = eyePos.add(attacker.getRotationVec(1f).multiply(20));
            Optional<Vec3d> hitPos = this.getBoundingBox().raycast(eyePos, endRaycastPos);
            if (hitPos.isPresent()) {
                hitFraction = (hitPos.get().y - this.getY()) / this.getHeight();
            }
        } else {
            hitFraction = 1f;
        }
        if (damageSourceEntity != null && !damageSourceEntity.equals(source.getAttacker())) {
            double impactY = damageSourceEntity.getY();
            hitFraction = (impactY - this.getY()) / this.getHeight();
        }
        if (hitFraction < 0.5) return false;

        Vec3d hitDirection = null;
        if (damageSourceEntity != null && (!damageSourceEntity.equals(attacker))) {
            hitDirection = damageSourceEntity.getVelocity().normalize().negate();
        } else if (attacker != null) {
            hitDirection = attacker.getPos().subtract(this.getPos()).normalize();
        }
        float angleInRad = hitDirection != null ? (float) Math.atan2(hitDirection.z, hitDirection.x) : (float) Math.toRadians(getRandom().nextInt(360));
        this.sendHit(amount, angleInRad);
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld() instanceof ServerWorld serverWorld) {
            BlockPos posBelow = getBlockPos().down();
            if (this.age % 40 == 0) {
                if (!serverWorld.getBlockState(posBelow).isSolidBlock(serverWorld, posBelow)) {
                    this.kill();
                }
            }
            return;
        } else if (damageHandler.isEmpty()) {
            return;
        }

        DamageAccumulator.DamageEntry newest = damageHandler.getNewestDamage();
        if (newest == null) return;
        float elapsed = this.age - newest.age();
        if (elapsed / NeMuelchConfigInit.CONFIG.dummyEntityData.getDisplayDuration() >= 1f) {
            this.resetClientHitData();
        }
    }

    public DamageAccumulator getDamageHandler() {
        return damageHandler;
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

    public boolean hasEquipment() {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!this.getEquippedStack(slot).equals(ItemStack.EMPTY)) return true;
        }
        return false;
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

    public void setCurrentGroup(EntityGroupMapper group) {
        this.currentGroup = group;
    }

    public void sendHit(float damage, float angleInRad) {
        if (getWorld() instanceof ServerWorld) {
            new DummyHitS2CPacket(this.getId(), damage, angleInRad).send(this);
        }
    }

    public void receiveClientHitData(@Nullable DummyHitS2CPacket clientHitData) {
        if (!getWorld().isClient()) return;
        if (clientHitData == null) {
            return;
        }
        this.damageHandler.getDamages().add(new DamageAccumulator.DamageEntry(
                clientHitData.damage(),
                clientHitData.angleInRad(),
                this.age
        ));
    }

    public void resetClientHitData() {
        this.damageHandler.clear();
    }

    @Override
    public void tickMovement() {
        // super.tickMovement();
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
    public boolean isPushable() {
        return false;
    }

    @Override
    public @Nullable ItemStack getPickBlockStack() {
        return NeMuelchItems.DUMMY.getDefaultStack().copy();
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
