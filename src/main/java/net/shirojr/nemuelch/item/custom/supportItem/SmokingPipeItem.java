package net.shirojr.nemuelch.item.custom.supportItem;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.inventory.HandInventory;
import net.shirojr.nemuelch.item.util.FirstPersonInvisible;
import net.shirojr.nemuelch.item.util.ThirdPersonInvisible;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SmokingPipeItem extends Item implements ThirdPersonInvisible, FirstPersonInvisible {
    public static final String LIT_NBT_KEY = "LIT";
    public static final String IN_USE_NBT_KEY = "InUse";
    public static final String FILLING_NBT_KEY = "Filling";
    public static final String SELECTED_FILLING_NBT_KEY = "SelectedFilling";

    public static int INHALE_MAX_DURATION = 200;
    public static int BURN_DURATION = 2000;

    private final int maxEffectsCount;
    private final boolean poisonsOnOverusage;

    public SmokingPipeItem(Settings settings, int maxEffectsCount, boolean poisonsOnOverusage) {
        super(settings);
        this.maxEffectsCount = maxEffectsCount;
        this.poisonsOnOverusage = poisonsOnOverusage;
    }

    public int getMaxEffectsCount() {
        return maxEffectsCount;
    }

    public boolean poisonsOnOverusage() {
        return poisonsOnOverusage;
    }

    public static int getLitTime(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(LIT_NBT_KEY)) return 0;
        return nbt.getInt(LIT_NBT_KEY);
    }

    public boolean isLit(ItemStack stack) {
        return getLitTime(stack) > 0;
    }

    public void setLitTime(@Nullable Entity user, ItemStack stack, int litTime) {
        NbtCompound nbt = stack.getOrCreateNbt();
        int oldTime = 0;
        if (nbt.contains(LIT_NBT_KEY)) {
            oldTime = nbt.getInt(LIT_NBT_KEY);
        }
        nbt.putInt(LIT_NBT_KEY, litTime);
        if (user != null && user.getWorld() instanceof ServerWorld serverWorld) {
            if (oldTime < litTime) {
                serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                        NeMuelchSounds.KINDLE, SoundCategory.NEUTRAL, 1f, 1f);
            } else if (litTime == 0) {
                this.setInUse(stack, false);
                serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                        SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.NEUTRAL, 1f, 1f);
            }
        }
    }

    public boolean isInUse(ItemStack stack) {
        return stack.getNbt() != null && stack.getNbt().contains(IN_USE_NBT_KEY) && stack.getNbt().getBoolean(IN_USE_NBT_KEY);
    }

    public void setInUse(ItemStack stack, boolean inUse) {
        stack.getOrCreateNbt().putBoolean(IN_USE_NBT_KEY, inUse);
    }

    public List<StatusEffectInstance> getFilling(ItemStack stack) {
        List<StatusEffectInstance> instances = new ArrayList<>();
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(FILLING_NBT_KEY)) return instances;
        NbtList fillingNbtList = nbt.getList(FILLING_NBT_KEY, NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < fillingNbtList.size(); i++) {
            NbtCompound fillingNbt = fillingNbtList.getCompound(i);
            instances.add(StatusEffectInstance.fromNbt(fillingNbt));
        }
        return instances;
    }

    public boolean setFilling(ItemStack stack, List<StatusEffectInstance> instances) {
        if (instances.size() > getMaxEffectsCount()) return false;
        NbtList fillingNbtList = new NbtList();
        for (StatusEffectInstance instance : instances) {
            fillingNbtList.add(instance.writeNbt(new NbtCompound()));
        }
        stack.getOrCreateNbt().put(FILLING_NBT_KEY, fillingNbtList);
        return true;
    }

    public boolean isFull(ItemStack stack) {
        return getFilling(stack).size() >= getMaxEffectsCount();
    }

    public boolean isEmpty(ItemStack stack) {
        return getFilling(stack).isEmpty();
    }

    public static void setSelectedEffect(ItemStack stack, StatusEffect effect) {
        Identifier id = Registries.STATUS_EFFECT.getId(effect);
        if (id == null) throw new IllegalStateException("StatusEffect not found in Registry: %s".formatted(effect.toString()));
        stack.getOrCreateNbt().putString(SELECTED_FILLING_NBT_KEY, id.toString());
    }

    @Nullable
    public static StatusEffect getSelectedEffect(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) return null;
        return Registries.STATUS_EFFECT.get(Identifier.tryParse(nbt.getString(SELECTED_FILLING_NBT_KEY)));
    }

    public int getDuration(ItemStack stack) {
        StatusEffect selectedEffect = getSelectedEffect(stack);
        if (selectedEffect == null) return 0;
        for (StatusEffectInstance activeEffectInstance : getFilling(stack)) {
            if (activeEffectInstance.getEffectType().equals(selectedEffect)) {
                return INHALE_MAX_DURATION;
            }
        }
        return 0;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        if (!isLit(stack)) return 0;
        return getDuration(stack);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        HandInventory handInventory = new HandInventory(user);
        if (hand.equals(Hand.MAIN_HAND)) {
            ItemStack stack = handInventory.getMainHandStack();
            if (user.isSneaking() && !isEmpty(stack)) {
                setFilling(stack, new ArrayList<>());
                setLitTime(user, stack, 0);
                setInUse(stack, false);
                return TypedActionResult.success(stack);
            }
            if (!isLit(stack)) {
                if (handInventory.getOffHandStack().isIn(NeMuelchTags.Items.CAMPFIRE_IGNITER)) {
                    if (!user.isCreative()) {
                        handInventory.getOffHandStack().decrement(1);
                    }
                    setLitTime(user, stack, BURN_DURATION);
                    return TypedActionResult.success(stack);
                }
                return TypedActionResult.pass(stack);
            } else {
                List<StatusEffectInstance> filling = getFilling(stack);
                if (filling.isEmpty()) return super.use(world, user, hand);
                StatusEffectInstance statusEffectInstance = filling.get(user.getRandom().nextInt(filling.size()));
                setSelectedEffect(stack, statusEffectInstance.getEffectType());
                setInUse(stack, true);
            }
        }
        return super.use(world, user, hand);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        int litTime = getLitTime(stack);
        if (litTime > 0) {
            setLitTime(entity, stack, litTime - 1);
        }
        if (isInUse(stack) && !selected) {
            setInUse(stack, false);
        }
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!isLit(stack)) {
            setInUse(stack, false);
            user.clearActiveItem();
            return;
        }
        super.usageTick(world, user, stack, remainingUseTicks);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        setInUse(stack, false);
        double normalizedUsageDuration = 1 - ((double) remainingUseTicks / getMaxUseTime(stack));
        if (isLit(stack)) {

        }
        super.onStoppedUsing(stack, world, user, remainingUseTicks);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        setInUse(stack, false);
        if (poisonsOnOverusage()) {
            if (world instanceof ServerWorld serverWorld) {
                int duration = getMaxUseTime(stack) * 2;
                StatusEffectInstance poisonInstance = user.getStatusEffect(StatusEffects.POISON);
                if (poisonInstance != null) {
                    duration += poisonInstance.getDuration();
                }
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, duration));
                serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(), NeMuelchSounds.COUGH, SoundCategory.PLAYERS, 2f, 1);
            }
        } else {
            this.onStoppedUsing(stack, world, user, 0);
        }
        return super.finishUsing(stack, world, user);
    }

    @Override
    public boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        NbtCompound oldNbt = oldStack.copy().getNbt();
        NbtCompound newNbt = newStack.copy().getNbt();
        if (oldNbt == null || newNbt == null) {
            return super.allowNbtUpdateAnimation(player, hand, oldStack, newStack);
        }
        if (oldNbt.contains(LIT_NBT_KEY) && newNbt.contains(LIT_NBT_KEY)) {
            if ((oldNbt.getInt(LIT_NBT_KEY) == 0) == (newNbt.getInt(LIT_NBT_KEY) == 0)) {
                oldNbt.remove(LIT_NBT_KEY);
                newNbt.remove(LIT_NBT_KEY);
            }
        }
        oldNbt.remove(IN_USE_NBT_KEY);
        newNbt.remove(IN_USE_NBT_KEY);
        oldNbt.remove(SELECTED_FILLING_NBT_KEY);
        newNbt.remove(SELECTED_FILLING_NBT_KEY);
        return !oldNbt.equals(newNbt);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        List<StatusEffectInstance> filling = getFilling(stack);
        if (filling.isEmpty()) return;
        tooltip.add(Text.translatable("tooltip.nemuelch.smoking_pipe.filling"));
        for (StatusEffectInstance instance : filling) {
            MutableText line = Text.literal(" ");
            line.append(Text.translatable(instance.getTranslationKey()).formatted(Formatting.GRAY));
            tooltip.add(line);
        }
    }

    @Override
    public boolean isInFirstPersonInvisibleState(ItemStack stack) {
        return isInUse(stack);
    }

    @Override
    public boolean isInThirdPersonInvisibleState(ItemStack stack) {
        return isInUse(stack);
    }
}
