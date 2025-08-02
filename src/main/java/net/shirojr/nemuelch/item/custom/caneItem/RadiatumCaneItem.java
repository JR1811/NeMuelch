package net.shirojr.nemuelch.item.custom.caneItem;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchSounds;

import java.util.Collection;

//TODO: add to pestcane station recipies

public class RadiatumCaneItem extends Item {
    private static final int ANIM_CAST = 0;
    private static final int USE_COOLDOWN_TICKS = 60;
    private static final String castController = "castController";

    public RadiatumCaneItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.playSound(NeMuelchSounds.ITEM_RADIATOR_ACTIVATION, 2f, 1f);
        user.getItemCooldownManager().set(this, USE_COOLDOWN_TICKS);

        if (world instanceof ServerWorld serverWorld) {
            Collection<ServerPlayerEntity> affectedPlayers = PlayerLookup.around(serverWorld, user.getPos(), 7);
            for (ServerPlayerEntity target : affectedPlayers) {
                if (target != user) {
                    int strength = 1;
                    double x = user.getX() - target.getX();
                    double z = user.getZ() - target.getZ();


                    Vec3d vec3d = target.getVelocity();
                    Vec3d vec3d2 = (new Vec3d(x, 0.0, z)).normalize().multiply(strength);

                    target.setVelocity(vec3d.x / 2.0 - vec3d2.x, (target.isOnGround() ? Math.min(0.4, vec3d.y / 2.0 + strength) : vec3d.y) + 0.4, vec3d.z / 2.0 - vec3d2.z);
                    target.velocityDirty = true;
                }
            }


            //user.setVelocity(user.get);
        }


        return super.use(world, user, hand);
    }
}
