package net.shirojr.nemuelch.monster.type;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.monster.AbstractMonsterType;
import net.shirojr.nemuelch.monster.abilities.custom.DrinkBloodAbility;
import net.shirojr.nemuelch.util.constants.NbtKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public class VampireMonsterType extends AbstractMonsterType implements DrinkBloodAbility.BloodDrinker {
    private long consumedBlood;
    private Rank rank;

    @Override
    public long getConsumedBlood() {
        return this.consumedBlood;
    }

    @Override
    public void setConsumedBlood(long consumedBlood) {
        this.consumedBlood = Math.min(Math.max(consumedBlood, 0), this.getBloodIntakeCapacity());
    }

    @Override
    public void addConsumedBlood(long consumedBlood) {
        this.setConsumedBlood(this.getConsumedBlood() + consumedBlood);
    }

    @Override
    public long getBloodIntakeCapacity() {
        return this.getRank().getBloodIntakeCapacity();
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    @Override
    public void onDrankBlood(ServerPlayerEntity user, LivingEntity target) {

    }


    private BloodSuckResult handleAntiVampireFeedback(LivingEntity target, List<String> consequences) {
        List<ItemStack> targetInventory = getTargetInventory(target);
        float severity = calculateAntiVampireSeverity(targetInventory);
        float rankResistance = this.getRank().getAntiVampireResistance();
        float damage = 4.0f * severity * rankResistance;
/*
        if (!this.provider.getWorld().isClient()) {
            this.provider.damage(this.provider.getDamageSources().magic(), damage);
            this.provider.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, (int) (300 * severity), 1));
        }*/

        consequences.add("§cThe blessed items sear your undead flesh!");
        Optional<BloodSource> bloodSource = BloodSource.get(target);
        return bloodSource.map(source -> new BloodSuckResult(
                -0.02f * severity,
                0f,
                source,
                (int) (30 * severity),
                false,
                consequences
        )).orElse(null);
    }

    private float calculateAntiVampireSeverity(List<ItemStack> inventory) {
        float severity = 0f;
        List<String> bannedWords = List.of("silver", "holy", "sun", "athame", "garlic");
        for (ItemStack stack : inventory) {
            if (stack.isEmpty()) continue;
            NbtCompound nbt = stack.getNbt();
            if (nbt != null && nbt.contains("anti_vampire") && nbt.getBoolean("anti_vampire")) {
                severity += 2f;
                continue;
            }
            if (stack.isIn(NeMuelchTags.Items.ANTI_VAMPIRE)) {
                severity += 2f;
                continue;
            }
            for (String bannedWord : bannedWords) {
                String itemPath = Registries.ITEM.getId(stack.getItem()).getPath();
                if (!itemPath.contains(bannedWord)) continue;
                float wordSeverity = switch (bannedWord) {
                    case "holy", "athame" -> 1.5f;
                    case "silver", "garlic", "sun" -> 1.2f;
                    default -> 1.0f;
                };
                severity += wordSeverity;
                break;
            }
        }

        return Math.min(severity, 5f);
    }

    public BloodSuckResult attemptBloodSuck(LivingEntity target) {
        List<String> consequences = new ArrayList<>();
        List<ItemStack> targetInventory = getTargetInventory(target);
        boolean hasAntiVampireItemStacks = false;
        for (ItemStack itemStack : targetInventory) {
            if (!containsAntiVampireProperties().test(itemStack)) continue;
            hasAntiVampireItemStacks = true;
            break;
        }
        if (hasAntiVampireItemStacks) {
            return handleAntiVampireFeedback(target, consequences);
        }
        float baseYield = BloodSource.get(target).map(BloodSource::getBaseYield).orElse(0f);
        //bloodSource.get
        // TODO: continue here
        return null;
    }

    private static List<ItemStack> getTargetInventory(LivingEntity target) {
        List<ItemStack> inventory = new ArrayList<>();
        if (target instanceof HostileEntity hostileTarget) {
            for (ItemStack itemStack : hostileTarget.getItemsEquipped()) {
                inventory.add(itemStack);
            }
        } else if (target instanceof MobEntity mobTarget) {
            for (ItemStack itemStack : mobTarget.getItemsEquipped()) {
                inventory.add(itemStack);
            }
        } else if (target instanceof ServerPlayerEntity playerTarget) {
            for (int i = 0; i < 9; i++) {
                ItemStack hotbarStack = playerTarget.getInventory().main.get(i);
                inventory.add(hotbarStack);
            }
            inventory.add(playerTarget.getMainHandStack());
            inventory.add(playerTarget.getOffHandStack());
            inventory.addAll(playerTarget.getInventory().armor);
        }
        return inventory;
    }

    public static Predicate<ItemStack> containsAntiVampireProperties() {
        List<String> bannedWords = List.of("silver", "holy", "sun", "light", "athame", "garlic");
        return stack -> {
            if (stack.isEmpty()) return false;
            for (String bannedWord : bannedWords) {
                if (Registries.ITEM.getId(stack.getItem()).getPath().contains(bannedWord)) return true;
                if (stack.isIn(NeMuelchTags.Items.ANTI_VAMPIRE)) return true;
                NbtCompound nbt = stack.getNbt();
                return nbt != null && nbt.contains(NbtKeys.ANTI_VAMPIRE) && nbt.getBoolean(NbtKeys.ANTI_VAMPIRE);
            }
            return false;
        };
    }

    @Override
    public void onMonsterTypeGained(LivingEntity provider) {
        super.onMonsterTypeGained(provider);
        this.playSoundForProvider(provider, SoundEvents.ENTITY_BAT_LOOP, SoundCategory.PLAYERS, provider.getPos(), 1f, 0.8f);
    }

    @Override
    public void onMonsterTypeLost(LivingEntity provider) {
        super.onMonsterTypeLost(provider);
    }

    @Override
    protected void writeCustomNbt(NbtCompound nbt) {

    }

    @Override
    protected void readCustomNbt(NbtCompound nbt) {

    }

    public enum BloodSource {
        ANIMAL(0.03f, 5, 0.4f),
        MONSTER(0.08f, 2, 0.3f),
        HUMANOID(0.1f, 25, 0.2f),
        PLAYER(0.12f, 40, 0.05f);

        private final float baseYield;
        private final int suspicion;
        private final float deathChance;

        BloodSource(float baseYield, int suspicion, float deathChance) {
            this.baseYield = baseYield;
            this.suspicion = suspicion;
            this.deathChance = deathChance;
        }

        public static Optional<BloodSource> get(LivingEntity target) {
            if (target instanceof AnimalEntity || target instanceof AmbientEntity || target.getType().isIn(NeMuelchTags.EntityTypes.MONSTER_FOOD_SOURCE_ANIMAL)) {
                return Optional.of(ANIMAL);
            }
            if (target instanceof Monster || target instanceof SlimeEntity || target.getType().isIn(NeMuelchTags.EntityTypes.MONSTER_FOOD_SOURCE_MONSTER)) {
                return Optional.of(MONSTER);
            }
            if (target instanceof PlayerEntity || target.getType().isIn(NeMuelchTags.EntityTypes.MONSTER_FOOD_SOURCE_PLAYER)) {
                return Optional.of(PLAYER);
            }
            if (target.getType().isIn(NeMuelchTags.EntityTypes.MONSTER_FOOD_SOURCE_HUMANOID))
                return Optional.of(HUMANOID);
            return Optional.empty();
        }

        public float getBaseYield() {
            return baseYield;
        }

        public int getSuspicion() {
            return suspicion;
        }

        public float getDeathChance() {
            return deathChance;
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public enum Rank {
        SCUM(FluidConstants.DROPLET * 3, 15f, 1.5f, 1.0f),
        PEASANT(FluidConstants.DROPLET * 50, 17.5f, 1.2f, 0.9f),
        SERVANT(FluidConstants.DROPLET * 1000, 20f, 1.0f, 0.7f),
        KING(FluidConstants.BUCKET, 25f, 0.6f, 0.5f),
        EMPEROR(FluidConstants.BUCKET * 20, 35f, 0.3f, 0.3f),
        GOD(FluidConstants.BUCKET * 200, 60f, 0.05f, 0.2f);

        private final long bloodIntakeCapacity;
        private final float damageMultiplier;
        private final float killControlFactor;
        private final float antiVampireResistance;

        Rank(long bloodIntakeCapacity, float damageMultiplier, float killControlFactor, float antiVampireResistance) {
            this.bloodIntakeCapacity = bloodIntakeCapacity;
            this.damageMultiplier = damageMultiplier;
            this.killControlFactor = killControlFactor;
            this.antiVampireResistance = antiVampireResistance;
        }

        public long getBloodIntakeCapacity() {
            return bloodIntakeCapacity;
        }

        public float getDamageMultiplier() {
            return damageMultiplier;
        }

        public float getKillControlFactor() {
            return killControlFactor;
        }

        public float getAntiVampireResistance() {
            return antiVampireResistance;
        }

        public static Rank get(float normalizedBlood) {
            Rank highestMatch = SCUM;
            while (highestMatch.getNext().bloodIntakeCapacity <= normalizedBlood) {
                highestMatch = highestMatch.getNext();
                if (highestMatch.equals(GOD)) {
                    break;
                }
            }
            return highestMatch;
        }

        public Rank getBefore() {
            int indexBefore = Math.max(this.ordinal() - 1, 0);
            return Rank.values()[indexBefore];
        }

        public Rank getNext() {
            int nextIndex = Math.min(this.ordinal() + 1, Rank.values().length - 1);
            return Rank.values()[nextIndex];
        }
    }

    public record BloodSuckResult(float bloodGained, float specializationGained, BloodSource sourceType,
                                  int suspicionGained, boolean entityKilled, List<String> consequences) {
    }
}
