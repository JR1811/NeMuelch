package net.shirojr.nemuelch.item.custom.supportItem;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.ConfigInit;
import net.shirojr.nemuelch.sound.NeMuelchSounds;

public class OminousHeartItem extends Item {

    private int tickCount = 0;

    public OminousHeartItem(Settings settings) {
        super(settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient) return;

        if (tickCount % 60 == 0) {
            entity.playSound(NeMuelchSounds.ITEM_OMINOUS_HEART,
                    ConfigInit.CONFIG.ominousHartVolume,
                    ConfigInit.CONFIG.ominousHartPitch);
        }
        tickCount ++;
    }
}
