package net.shirojr.nemuelch.item.custom.weaponry;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.shirojr.nemuelch.block.custom.RottenMeatBlock;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.item.util.ItemCallbacks;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;

public class ChainedMaceItem extends AxeItem implements ItemCallbacks {
    public static final String LOADED_BLOCK_NBT_KEY = "loadedBlock";
    public static final HashMap<TagKey<Block>, BiConsumer<LivingEntity, LivingEntity>> POST_HIT_ACTIONS = new HashMap<>();
    private static final UUID DAMAGE_MODIFIER_UUID = UUID.fromString("706f4d10-08fd-4b58-998b-fd1ab5d204d6");
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("bd10134f-72de-4be0-9cfe-5a8f5f1b3235");

    public ChainedMaceItem(ToolMaterial toolMaterial, float attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
        POST_HIT_ACTIONS.put(NeMuelchTags.Blocks.CHAINED_MACE_HUNGER, (user, target) -> {
            if (!(user.getWorld() instanceof ServerWorld serverWorld)) return;
            if (serverWorld.getRandom().nextFloat() > 0.2) return;
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 80, 1));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 40, 2));
            Vec3d pos = user.getPos();
            serverWorld.playSound(
                    null,
                    pos.x, pos.y, pos.z,
                    NeMuelchSounds.SQUIRT, SoundCategory.NEUTRAL,
                    2f, 1f
            );
            RottenMeatBlock.spawnParticles(100, 2, BlockPos.ofFloored(pos), serverWorld);
        });
        POST_HIT_ACTIONS.put(NeMuelchTags.Blocks.CHAINED_MACE_BURN, (user, target) -> {
            if (!(user.getWorld() instanceof ServerWorld serverWorld)) return;
            if (serverWorld.getRandom().nextFloat() > 0.2) return;

            if (serverWorld.getRandom().nextFloat() > 0.3) {
                target.setOnFireFor(5);
            } else {
                Hand handWithMace = getHandWithMace(user);
                if (handWithMace != null && !wearsFireProtection(user, handWithMace)) {
                    user.setOnFireFor(5);
                }
            }
        });
        POST_HIT_ACTIONS.put(NeMuelchTags.Blocks.CHAINED_MACE_DEATH, (user, target) -> {
            if (!(user.getWorld() instanceof ServerWorld serverWorld)) return;
            target.kill();
            Vec3d pos = user.getPos();
            serverWorld.playSound(
                    null,
                    pos.x, pos.y, pos.z,
                    SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.NEUTRAL,
                    2f, 1f
            );
        });
        POST_HIT_ACTIONS.put(NeMuelchTags.Blocks.CHAINED_MACE_POISON, (user, target) -> {
            if (!(user.getWorld() instanceof ServerWorld serverWorld)) return;
            if (serverWorld.getRandom().nextFloat() > 0.3f) return;
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 80, 1));
        });
        POST_HIT_ACTIONS.put(NeMuelchTags.Blocks.CHAINED_MACE_WITHER, (user, target) -> {
            if (!(user.getWorld() instanceof ServerWorld serverWorld)) return;
            if (serverWorld.getRandom().nextFloat() > 0.3f) return;
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60, 1));
        });
    }

    public static Optional<Block> getLoadedBlock(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(LOADED_BLOCK_NBT_KEY)) return Optional.empty();
        Identifier blockId = Identifier.tryParse(nbt.getString(LOADED_BLOCK_NBT_KEY));
        if (Registries.BLOCK.getDefaultId().equals(blockId)) return Optional.empty();
        return Optional.of(Registries.BLOCK.get(blockId));
    }

    public static boolean isBlockLoaded(ItemStack stack, BlockItem block) {
        return getLoadedBlock(stack).map(loadedBlock -> block.equals(loadedBlock.asItem())).orElse(false);
    }

    public static void setLoadedBlock(@Nullable World world, ItemStack stack, @Nullable BlockItem blockItem) {
        if (blockItem == null) {
            stack.removeSubNbt(LOADED_BLOCK_NBT_KEY);
            return;
        }
        Block block = blockItem.getBlock();
        Optional<Block> oldBlock = getLoadedBlock(stack);
        if (oldBlock.isPresent() && oldBlock.get().equals(block)) return;
        if (world != null && !block.getDefaultState().isFullCube(world, BlockPos.ORIGIN)) {
            return;
        }
        stack.getOrCreateNbt().putString(LOADED_BLOCK_NBT_KEY, Registries.BLOCK.getId(block).toString());
    }

    public static boolean wearsFireProtection(LivingEntity entity, Hand hand) {
        return entity.getStackInHand(Hand.values()[(hand.ordinal() + 1) % Hand.values().length]).isIn(NeMuelchTags.Items.GLOVES);
    }

    @Nullable
    public Hand getHandWithMace(LivingEntity entity) {
        if (entity.getMainHandStack().isOf(this)) return Hand.MAIN_HAND;
        if (entity.getOffHandStack().isOf(this)) return Hand.OFF_HAND;
        return null;
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        getLoadedBlock(stack).ifPresent(block -> {
            for (var entry : POST_HIT_ACTIONS.entrySet()) {
                if (block.getDefaultState().isIn(entry.getKey())) {
                    entry.getValue().accept(attacker, target);
                }
            }
        });
        return super.postHit(stack, target, attacker);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState state = world.getBlockState(blockPos);
        if (!state.isIn(NeMuelchTags.Blocks.CHAINED_MACE_BLACKLIST) && state.getBlock().asItem() instanceof BlockItem blockItem) {
            ItemStack stack = context.getStack();
            Optional<Block> oldBlock = getLoadedBlock(stack);
            setLoadedBlock(world, stack, blockItem);
            Optional<Block> newBlock = getLoadedBlock(stack);
            if (oldBlock.isEmpty() || (newBlock.isPresent() && !oldBlock.get().equals(newBlock.get()))) {
                PlayerEntity player = context.getPlayer();
                if (player != null) {
                    player.getItemCooldownManager().set(stack.getItem(), 80);
                    player.resetLastAttackedTicks();
                    if (world instanceof ServerWorld) {
                        if (!player.isCreative()) {
                            world.breakBlock(blockPos, false, player);
                            oldBlock.ifPresent(block -> player.getInventory().offerOrDrop(block.asItem().getDefaultStack()));
                            stack.damage(1, player, p -> p.sendToolBreakStatus(context.getHand()));
                        }
                        world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, blockPos, Block.getRawIdFromState(state));
                    }
                } else {
                    world.breakBlock(blockPos, false);
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        }
        return super.useOnBlock(context);
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        return super.postMine(stack, world, state, pos, miner);
    }

    @Override
    public boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        NbtCompound oldNbt = oldStack.copy().getNbt();
        NbtCompound newNbt = newStack.copy().getNbt();
        if (oldNbt == null || newNbt == null) return super.allowNbtUpdateAnimation(player, hand, oldStack, newStack);
        oldNbt.remove(LOADED_BLOCK_NBT_KEY);
        newNbt.remove(LOADED_BLOCK_NBT_KEY);
        return !oldNbt.equals(newNbt);
    }

    @Override
    public void onItemEntityDestroyed(ItemEntity entity) {
        super.onItemEntityDestroyed(entity);
        if (entity.getWorld() instanceof ServerWorld serverWorld) {
            getLoadedBlock(entity.getStack()).ifPresent(block -> ItemScatterer.spawn(serverWorld, entity.getX(), entity.getY(), entity.getZ(), block.asItem().getDefaultStack()));
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        getLoadedBlock(stack).ifPresent(block -> {
            MutableText line = Text.translatable("tooltip.nemuelch.chained_mace.loaded_block");
            line.append(Text.translatable(block.getTranslationKey()));
            tooltip.add(line);
        });
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(super.getAttributeModifiers(stack, slot));
        if (slot.equals(EquipmentSlot.MAINHAND)) {
            getLoadedBlock(stack).ifPresent(block -> {
                float normalizedHardness = MathHelper.clamp(block.getHardness(), 0.0f, 5.0f) / 5.0f;
                double damageBonus = MathHelper.lerp(normalizedHardness, 0, 15);
                double speedPenalty = -MathHelper.lerp(normalizedHardness, 1.0, 1.7);
                builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(
                        DAMAGE_MODIFIER_UUID, "Chained Mace Attack Damage", damageBonus, EntityAttributeModifier.Operation.ADDITION
                ));
                builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(
                        SPEED_MODIFIER_UUID, "Chained Mace Attack Speed", speedPenalty, EntityAttributeModifier.Operation.ADDITION
                ));
            });
        }
        return builder.build();
    }

    @Override
    public <T extends LivingEntity> void nemuelch$onBroken(T user, ItemStack stack) {
        if (!(user.getWorld() instanceof ServerWorld serverWorld)) return;
        getLoadedBlock(stack).ifPresent(
                block -> {
                    serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.ENTITY_GLOW_ITEM_FRAME_ADD_ITEM, SoundCategory.NEUTRAL, 1f, 1f);
                    ItemScatterer.spawn(serverWorld, user.getX(), user.getY(), user.getZ(), block.asItem().getDefaultStack());
                }
        );
    }
}
