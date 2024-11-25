package net.shirojr.nemuelch.entity.custom;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.entity.NeMuelchEntities;

public class PotLauncherEntity extends LivingEntity {
    public PotLauncherEntity(World world) {
        super(NeMuelchEntities.POT_LAUNCHER, world);
    }

    public PotLauncherEntity(World world, Vec3d pos) {
        this(world);
        this.setPosition(pos);
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return null;
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return null;
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {

    }

    @Override
    public Arm getMainArm() {
        return null;
    }
}
