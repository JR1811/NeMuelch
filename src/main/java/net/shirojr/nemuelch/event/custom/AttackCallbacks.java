package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.shirojr.nemuelch.compat.cca.component.monster.GeneralMonsterComponent;
import net.shirojr.nemuelch.monster.AbstractMonsterType;
import org.jetbrains.annotations.Nullable;

public class AttackCallbacks implements AttackEntityCallback, AttackBlockCallback {
    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        GeneralMonsterComponent monsterComponent = GeneralMonsterComponent.get(player);
        for (AbstractMonsterType entry : monsterComponent.getActiveMonsterTypes()) {
            entry.getAbilities().onAttackOther(player, world, hand, entity, hitResult);
        }
        return ActionResult.PASS;
    }

    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {
        GeneralMonsterComponent monsterComponent = GeneralMonsterComponent.get(player);
        for (AbstractMonsterType entry : monsterComponent.getActiveMonsterTypes()) {
            entry.getAbilities().onAttackBlock(player, world, hand, pos, direction);
        }
        return ActionResult.PASS;
    }
}
