package net.shirojr.nemuelch.item.custom.caneItem;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.sound.NeMuelchSounds;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.network.GeckoLibNetwork;
import software.bernie.geckolib3.network.ISyncable;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Stream;

//TODO: add to pestcane station recipies

public class RadiatumCaneItem extends Item implements IAnimatable, ISyncable {
    public AnimationFactory factory = new AnimationFactory(this);
    private static final int ANIM_CAST = 0;
    private static final int USE_COOLDOWN_TICKS = 60;
    private static final String castController = "castController";


    public RadiatumCaneItem(Settings settings) {
        super(settings);
        GeckoLibNetwork.registerSyncable(this);
    }


    //region animation
    private <E extends Item & IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.radiatumcane.spin", true));

        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController(this, castController,20, this::predicate));

    }

    @Override
    public AnimationFactory getFactory() { return this.factory; }
    //endregion

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.playSound(NeMuelchSounds.ITEM_RADIATOR_ACTIVATION, 2f, 1f);
        user.getItemCooldownManager().set(this, USE_COOLDOWN_TICKS);

        if (!world.isClient()) {
            final int id = GeckoLibUtil.guaranteeIDForStack(user.getStackInHand(hand), (ServerWorld) world);

            GeckoLibNetwork.syncAnimation(user, this, id, ANIM_CAST);   // sync for caster
            for (PlayerEntity otherPlayer : PlayerLookup.tracking(user)) {
                GeckoLibNetwork.syncAnimation(otherPlayer, this, id, ANIM_CAST);    // sync for involved players
            }
        }

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

    @Override
    public void onAnimationSync(int id, int state) {
        if (state == ANIM_CAST) {

            //FIXME: animation seems to overlap and doesn't apply correctly
            @SuppressWarnings("rawtypes")
            final AnimationController controller = GeckoLibUtil.getControllerForID(this.factory, id, castController);

            if (controller.getAnimationState() != AnimationState.Stopped) {
                controller.setAnimation(new AnimationBuilder().addAnimation("animation.radiatumcane.charge", false));
                controller.markNeedsReload();
                //controller.setAnimation(new AnimationBuilder().addAnimation("animation.radiatumcane.spin", true));
            }

        }
    }
}
