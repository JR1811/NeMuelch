package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import net.shirojr.nemuelch.compat.cca.component.monster.GeneralMonsterComponent;
import net.shirojr.nemuelch.monster.AbstractMonsterType;
import org.jetbrains.annotations.Nullable;

public class UseEvents implements UseEntityCallback, UseBlockCallback {
    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        GeneralMonsterComponent monsterComponent = GeneralMonsterComponent.get(player);
        for (AbstractMonsterType entry : monsterComponent.getActiveMonsterTypes()) {
            entry.getAbilities().onInteractBlock(player, world, hand, hitResult);
        }
        return ActionResult.PASS;
    }

    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        GeneralMonsterComponent monsterComponent = GeneralMonsterComponent.get(player);
        for (AbstractMonsterType entry : monsterComponent.getActiveMonsterTypes()) {
            entry.getAbilities().onInteractEntity(player, world, hand, entity, hitResult);
        }
        return ActionResult.PASS;
    }
}
