package net.shirojr.nemuelch.entity.custom;

import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Equipment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.init.NeMuelchItems;
import net.shirojr.nemuelch.network.packet.DummyClearS2CPacket;
import net.shirojr.nemuelch.network.packet.DummyHitS2CPacket;
import net.shirojr.nemuelch.util.data.DamageAccumulator;
import net.shirojr.nemuelch.util.helper.EntityGroupMapper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class DummyCloseQuarterEntity extends LivingEntity implements DamageAccumulator.Callback {
    public static final Identifier LOOT_TABLE_ID = NeMuelch.getId("entities/dummy_cqc");
    public static final int BASE_ROCKING_DURATION = NeMuelchConfigInit.CONFIG.dummyEntityData.getBaseAnimationDuration();
    public static final int SHIELD_COOLDOWN = 100;

    public static final Function<DummyCloseQuarterEntity, Optional<Hand>> HAS_SHIELD_ITEM = entity -> {
        if (entity.getMainHandStack().getItem() instanceof ShieldItem) return Optional.of(Hand.MAIN_HAND);
        if (entity.getOffHandStack().getItem() instanceof ShieldItem) return Optional.of(Hand.OFF_HAND);
        return Optional.empty();
    };
    public static final Predicate<Entity> USED_SHIELD_BREAKING_ITEM = entity -> {
        if (!(entity instanceof LivingEntity livingEntity)) return false;
        return livingEntity.getMainHandStack().getItem() instanceof AxeItem;
    };

    private final DefaultedList<ItemStack> armorItems = DefaultedList.ofSize(4, ItemStack.EMPTY);
    private final DefaultedList<ItemStack> handItems = DefaultedList.ofSize(2, ItemStack.EMPTY);
    private final DamageAccumulator damageHandler;

    private EntityGroupMapper currentGroup;
    private int shieldCooldown;


    public DummyCloseQuarterEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
        this.currentGroup = EntityGroupMapper.DEFAULT;
        this.damageHandler = new DamageAccumulator(this);
    }

    public static DefaultAttributeContainer.Builder createBaseAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 1000)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                .add(EntityAttributes.GENERIC_ARMOR)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (hand != Hand.MAIN_HAND) return super.interact(player, hand);
        ItemStack stack = player.getMainHandStack();
        if (player.isSneaking() && stack.getItem() instanceof AxeItem) {
            if (!getWorld().isClient()) {
                this.kill();
                stack.damage(1, player, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
            }
            return ActionResult.SUCCESS;
        }
        if (player.getMainHandStack().isEmpty() && this.hasEquipment()) {
            if (this.getWorld() instanceof ServerWorld serverWorld) {
                this.dropInventory();
                serverWorld.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ARMOR_STAND_HIT,
                        SoundCategory.NEUTRAL, 1f, 1f);
            }
            return ActionResult.SUCCESS;
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
        if (isInvulnerableTo(source)) return false;
        if (this.blockedByShield(source)) {
            if (getWorld() instanceof ServerWorld serverWorld) {
                if (USED_SHIELD_BREAKING_ITEM.test(source.getAttacker())) {
                    this.setShieldCooldown(SHIELD_COOLDOWN);
                    serverWorld.playSound(null, this.getBlockPos(), SoundEvents.ITEM_SHIELD_BREAK, SoundCategory.NEUTRAL);
                } else {
                    if (source.getAttacker() instanceof PlayerEntity player) {
                        player.sendMessage(Text.translatable("entity.nemuelch.dummy_cqc.attack_blocked"), true);
                    }
                    serverWorld.playSound(null, this.getBlockPos(), SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.NEUTRAL);
                }
            }
            return false;
        }

        double hitFraction = 1f;
        Entity attacker = source.getAttacker();
        Entity damageSourceEntity = source.getSource();
        if (attacker instanceof LivingEntity && attacker.equals(damageSourceEntity)) {
            Vec3d eyePos = attacker.getEyePos();
            Vec3d endRaycastPos = eyePos.add(attacker.getRotationVec(1f).multiply(20));
            Optional<Vec3d> hitPos = this.getBoundingBox().raycast(eyePos, endRaycastPos);
            hitFraction = hitPos.map(vec3d -> (vec3d.y - this.getY()) / this.getHeight()).orElse(1.0);
        }
        if (hitFraction < 0.5) return false;

        if (!super.damage(source, amount)) return false;
        if (source.equals(getDamageSources().genericKill()) && amount >= Float.MAX_VALUE) return true;

        Vec3d hitDirection = null;
        if (damageSourceEntity != null && (!damageSourceEntity.equals(attacker))) {
            hitDirection = damageSourceEntity.getPos().subtract(this.getPos()).normalize();
        } else if (attacker != null) {
            hitDirection = attacker.getPos().subtract(this.getPos()).normalize();
        }
        float angleInRad = hitDirection != null ? (float) Math.atan2(hitDirection.z, hitDirection.x) : (float) Math.toRadians(getRandom().nextInt(360));
        amount = this.applyArmorToDamage(source, amount);
        amount = this.modifyAppliedDamage(source, amount);
        this.registerHit(amount, source.getName(), angleInRad, attacker != null && USED_SHIELD_BREAKING_ITEM.test(attacker));
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (getShieldCooldown() > 0) {
            this.setShieldCooldown(this.getShieldCooldown() - 1);
        }
        if (getWorld() instanceof ServerWorld serverWorld) {
            BlockPos posBelow = getBlockPos().down();
            if (this.age % 40 == 0) {
                if (!serverWorld.getBlockState(posBelow).isSolidBlock(serverWorld, posBelow)) {
                    this.kill();
                }
            }
            if (this.damageHandler.isEmpty()) {
                return;
            }
            DamageAccumulator.DamageEntry newest = this.damageHandler.getNewestDamage();
            if (newest == null) return;
            float elapsed = this.age - newest.age();
            if (elapsed / NeMuelchConfigInit.CONFIG.dummyEntityData.getDisplayDuration() >= 1f) {
                this.clearDamageEntries();
            }
        }
    }

    public DamageAccumulator getDamageHandler() {
        return this.damageHandler;
    }

    public void clearDamageEntries() {
        this.damageHandler.clear();
        if (this.getWorld() instanceof ServerWorld) {
            new DummyClearS2CPacket(this.getId()).send(this);
        }
    }

    public List<ItemStack> getEquippedStacks(boolean nonEmpty) {
        List<ItemStack> stacks = new ArrayList<>();
        if (!nonEmpty || !getMainHandStack().isEmpty()) {
            stacks.add(this.getMainHandStack());
        }
        if (!nonEmpty || !getOffHandStack().isEmpty()) {
            stacks.add(this.getOffHandStack());
        }
        for (ItemStack stack : this.getItemsEquipped()) {
            if (nonEmpty && stack.isEmpty()) continue;
            stacks.add(stack);
        }
        return stacks;
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

    @Override
    public boolean isBlocking() {
        if (HAS_SHIELD_ITEM.apply(this).isEmpty()) return false;
        return this.getShieldCooldown() <= 0;
    }

    @Override
    public boolean blockedByShield(DamageSource source) {
        Entity entity = source.getSource();
        boolean piercingProjectile = entity instanceof PersistentProjectileEntity persistentProjectileEntity && persistentProjectileEntity.getPierceLevel() > 0;
        return !source.isIn(DamageTypeTags.BYPASSES_SHIELD) && this.isBlocking() && !piercingProjectile;
    }

    public int getShieldCooldown() {
        return shieldCooldown;
    }

    public void setShieldCooldown(int shieldCooldown) {
        this.shieldCooldown = Math.max(0, shieldCooldown);
    }

    @Override
    public void damageShield(float amount) {
        super.damageShield(amount);
        Hand hand;
        if (getEquippedStack(EquipmentSlot.MAINHAND).getItem() instanceof ShieldItem) {
            hand = Hand.MAIN_HAND;
        } else if (getEquippedStack(EquipmentSlot.OFFHAND).getItem() instanceof ShieldItem) {
            hand = Hand.OFF_HAND;
        } else {
            return;
        }
        this.activeItemStack.damage(
                1 + MathHelper.floor(amount),
                this,
                entity -> entity.sendToolBreakStatus(hand)
        );
        this.setShieldCooldown(SHIELD_COOLDOWN);
    }

    public void registerHit(float damage, String damageType, float angleInRad, boolean canBreakShield) {
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            if (isBlocking()) {
                if (canBreakShield) {
                    this.damageShield(damage);
                    serverWorld.playSound(null, this.getBlockPos(), SoundEvents.ITEM_SHIELD_BREAK, SoundCategory.NEUTRAL);
                } else {
                    serverWorld.playSound(null, this.getBlockPos(), SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.NEUTRAL);
                }
                return;
            }
            this.damageHandler.addDamage(new DamageAccumulator.DamageEntry(
                    damage, damageType, angleInRad, this.age
            ));
            new DummyHitS2CPacket(this.getId(), damage, damageType, angleInRad).send(this);
        }
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
    public boolean canBreatheInWater() {
        return true;
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

    @Override
    public void onDamageAdded(List<DamageAccumulator.DamageEntry> newEntries) {
        DamageAccumulator.Callback.super.onDamageAdded(newEntries);
    }

    @Override
    public void onDamageCleared(List<DamageAccumulator.DamageEntry> oldEntries) {
        DamageAccumulator.Callback.super.onDamageCleared(oldEntries);
        if (this.getWorld() instanceof ServerWorld serverWorld && this.getHealth() < this.getMaxHealth()) {
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 20, true, true));
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 80, 10, true, true));
            serverWorld.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_GLOW_SQUID_AMBIENT, SoundCategory.NEUTRAL);
        }
    }
}
