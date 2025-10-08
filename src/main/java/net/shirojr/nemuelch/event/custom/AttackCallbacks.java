package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.shirojr.nemuelch.compat.cca.component.BlightChunkComponent;
import net.shirojr.nemuelch.compat.cca.component.GeneralMonsterComponent;
import net.shirojr.nemuelch.compat.cca.util.BlightType;
import net.shirojr.nemuelch.monster.AbstractMonsterType;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

public class AttackCallbacks implements AttackEntityCallback, AttackBlockCallback, PlayerBlockBreakEvents.After {
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

    @Override
    public void afterBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
        if (!(world instanceof ServerWorld serverWorld)) return;
        Optional<BlightChunkComponent> blightChunkComponent = BlightChunkComponent.maybeGet(serverWorld.getChunk(pos));
        if (blightChunkComponent.isEmpty()) return;
        BlightChunkComponent component = blightChunkComponent.get();
        if (component.isEmpty()) return;
        component.getBlightsOfPos(pos).forEach(type -> type.getActions().get().onBlockBroken(
                serverWorld, component.getTimeOfFirstInitializedBlight(), pos, player)
        );
        if (component.isBlighted(pos, BlightType.AIRBORNE)) return;
        component.clearPos(pos, Set.of());
    }
}
