package net.shirojr.nemuelch.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.entity.custom.LeashedEntity;
import net.shirojr.nemuelch.util.duck.Leashable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityLeashingMixin extends PlayerEntity implements Leashable {
    @Unique
    private Entity leashHolder;

    @Unique
    private LeashedEntity leashedSelf;

    private ServerPlayerEntityLeashingMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Override
    public ActionResult neMuelch$interact(PlayerEntity player, Hand hand) {
        return this.interact(player, hand);
    }

    @Override
    public Entity neMuelch$getLeashHolder() {
        return leashHolder;
    }
}
