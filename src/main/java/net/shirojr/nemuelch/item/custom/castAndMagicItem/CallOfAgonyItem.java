package net.shirojr.nemuelch.item.custom.castAndMagicItem;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.revive.ReviveMain;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.NeMuelchClient;
import net.shirojr.nemuelch.effect.NeMuelchEffects;
import net.shirojr.nemuelch.sound.NeMuelchSounds;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class CallOfAgonyItem extends Item {
    private boolean successfulCast = true;  //TODO: implement unlocking behavior

    public CallOfAgonyItem(Settings settings) {
        super(settings);
    }

    /*
    - setVelocity in a radius as knockback (look up e.g. CrookedCrooks mod)
    - particles in the vicinity
     */

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        if (world.isClient()) {
            if (successfulCast) return TypedActionResult.success(itemStack);
            return TypedActionResult.pass(itemStack);
        }

        else {
            if (successfulCast) {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 100, 0, true, false));
                user.addStatusEffect(new StatusEffectInstance(NeMuelchEffects.LEVITATING_ABSOLUTION, 80, 0, true, false));
                user.addStatusEffect(new StatusEffectInstance(NeMuelchEffects.SHIELDING_SKIN, 100, 0, true, false));
                MinecraftClient.getInstance().particleManager.addEmitter(user, ParticleTypes.ASH, 70);

                //				this.client.particleManager.addEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);

                List<Entity> targets = world.getOtherEntities(user, Box.of(user.getPos(), 11, 6, 11));
                targets.forEach(entity -> {
                    //if (entity.isPlayer()) {
                        // 20% chance
                        if (world.random.nextInt(0, 9) < 2) {
                            ((LivingEntity) entity).addStatusEffect(new StatusEffectInstance(NeMuelchEffects.PLAYTHING_OF_THE_UNSEEN_DEITY, 70, 1, true, false));
                            ((LivingEntity) entity).addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 140, 1, true, false));
                        }
                        else {
                            double strength = 1.5;
                            double x =  user.getX() - entity.getX();
                            //double x = MathHelper.sin(user.getYaw() * 0.017453292F);
                            double z = user.getZ() - entity.getZ();
                            //double z = -MathHelper.cos(user.getYaw() * 0.017453292F);

                            entity.velocityDirty = true;
                            Vec3d vec3d = entity.getVelocity();
                            Vec3d vec3d2 = (new Vec3d(x, 0.0, z)).normalize().multiply(strength);
                            entity.setVelocity(vec3d.x / 2.0 - vec3d2.x, (entity.isOnGround() ? Math.min(0.4, vec3d.y / 2.0 + strength) : vec3d.y) + 0.4, vec3d.z / 2.0 - vec3d2.z);
                            entity.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 2f, 1f);

                            PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
                            passedData.writeBlockPos(entity.getBlockPos());
                            passedData.writeEnumConstant(NeMuelchClient.ParticlePacketType.ITEM_CALLOFAGONY_KNOCKBACK);

                            // sending network packets to the player clients (see also NeMuelchClient)
                            Stream<PlayerEntity> watchingPlayers = PlayerStream.watching(entity);  //FIXME: doesn't involve caster???

                            watchingPlayers.forEach(player -> ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, NeMuelch.PLAY_PARTICLE_PACKET_ID, passedData));

                            //FIXME: also add NBT for floating casters to catch if floating players are entering the world without having the effect
                        }
                    //}


                });

                world.playSound(null, user.getX(), user.getY(), user.getZ(),
                        NeMuelchSounds.ITEM_RUNE, SoundCategory.PLAYERS, 1f, 1f);

                itemStack.decrement(1);
                return super.use(world, user, hand);
            }
        }

        return TypedActionResult.pass(itemStack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if(Screen.hasShiftDown()) {
            if (false)   //TODO: implement knowledge system
            {
                tooltip.add(new TranslatableText("item.nemuelch.call_of_agony.description1"));
                tooltip.add(new TranslatableText("item.nemuelch.call_of_agony.description2"));
                tooltip.add(new TranslatableText("item.nemuelch.call_of_agony.description3"));
            }
            else {
                tooltip.add(new TranslatableText("item.nemuelch.rune.unknown"));
            }
        }
        else {
            tooltip.add(new TranslatableText("item.nemuelch.rune"));
            tooltip.add(new TranslatableText("item.nemuelch.tooltip.expand.line2"));
        }
    }
}
