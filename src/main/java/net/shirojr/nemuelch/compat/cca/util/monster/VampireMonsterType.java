package net.shirojr.nemuelch.compat.cca.util.monster;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.init.NeMuelchTags;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class VampireMonsterType extends AbstractMonsterType {
    public static final Identifier IDENTIFIER = NeMuelch.getId("vampire");

    public static final float SPECIALISATION_GAIN_FACTOR = 0.5f;
    public static final float SPECIALISATION_DECAY_OTHER_FACTOR = 0.98f;

    private float animalSpecialization;
    private float monsterSpecialization;
    private float humanoidSpecialization;

    private Rank rank;

    private int drinkCooldownTicks; // TODO: better cooldown with higher ranks

    public VampireMonsterType(LivingEntity provider, float animal, float monster, float humanoid) {
        super(IDENTIFIER, provider, 0f);

        float total = animal + monster + humanoid;
        this.animalSpecialization = animal / total;
        this.monsterSpecialization = monster / total;
        this.humanoidSpecialization = humanoid / total;

        this.drinkCooldownTicks = 0;
    }

    // region Getters & Setters
    public float getAnimalSpecialization() {
        return animalSpecialization;
    }

    public float getMonsterSpecialization() {
        return monsterSpecialization;
    }

    public float getHumanoidSpecialization() {
        return humanoidSpecialization;
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }
    // endregion

    public SubType calculateSubType() {
        float dominant = Math.max(animalSpecialization, Math.max(monsterSpecialization, humanoidSpecialization));
        float specialisationThreshold = 0.5f;
        float hybridThreshold = 0.3f;

        if (dominant < specialisationThreshold) {
            return SubType.BALANCED;
        }
        if (animalSpecialization == dominant) {
            if (monsterSpecialization > humanoidSpecialization && monsterSpecialization > hybridThreshold) {
                return SubType.APEX_PREDATOR;
            } else if (humanoidSpecialization > hybridThreshold) {
                return SubType.PRIMAL_MASTER;
            }
            return SubType.BEAST_LORD;
        }
        if (monsterSpecialization == dominant) {
            if (humanoidSpecialization > animalSpecialization && humanoidSpecialization > hybridThreshold) {
                return SubType.DARK_SOVEREIGN;
            } else if (animalSpecialization > hybridThreshold) {
                return SubType.APEX_PREDATOR;
            }
            return SubType.SHADOW_HUNTER;
        }
        if (monsterSpecialization > animalSpecialization && monsterSpecialization > hybridThreshold) {
            return SubType.DARK_SOVEREIGN;
        } else if (animalSpecialization > hybridThreshold) {
            return SubType.PRIMAL_MASTER;
        }
        return SubType.BLOOD_NOBLE;
    }

    public float calculateSpecialisationGain(BloodSource source, float yield) {
        float baseGain = yield * SPECIALISATION_GAIN_FACTOR;
        float dominantSpecialisation = switch (source) {
            case ANIMAL -> getAnimalSpecialization();
            case MONSTER -> getMonsterSpecialization();
            default -> getHumanoidSpecialization();
        };
        float diminishingFactor = 1 - (dominantSpecialisation * 0.5f);
        return baseGain * diminishingFactor;
    }

    public void addSpecialization(BloodSource source, float amount) {
        switch (source) {
            case ANIMAL -> {
                animalSpecialization += amount;
                monsterSpecialization *= SPECIALISATION_DECAY_OTHER_FACTOR;
                humanoidSpecialization *= SPECIALISATION_DECAY_OTHER_FACTOR;
            }
            case MONSTER -> {
                monsterSpecialization += amount;
                animalSpecialization *= SPECIALISATION_DECAY_OTHER_FACTOR;
                humanoidSpecialization *= SPECIALISATION_DECAY_OTHER_FACTOR;
            }
            case HUMANOID -> {
                humanoidSpecialization += amount;
                animalSpecialization *= SPECIALISATION_DECAY_OTHER_FACTOR;
                monsterSpecialization *= SPECIALISATION_DECAY_OTHER_FACTOR;
            }
        }
        float total = animalSpecialization + monsterSpecialization + humanoidSpecialization;
        animalSpecialization /= total;
        monsterSpecialization /= total;
        humanoidSpecialization /= total;
    }

    private int calculateSuspicion(LivingEntity target, World world, BloodSource source) {
        int result = source.getSuspicion();
        if (world.isDay()) result *= 2;
        List<LivingEntity> witnesses = world.getEntitiesByClass(
                LivingEntity.class,
                target.getBoundingBox().expand(16),
                entity -> entity != target && entity.canSee(target)
        );
        result += witnesses.size() * 3;
        return result;
    }

    public boolean shouldTargetDie(LivingEntity target, float bloodTaken) {
        float deathChance = BloodSource.get(target).getDeathChance();

        float killControl = this.rank.getKillControlFactor();
        float yieldMultiplier = 1.0f + (bloodTaken * 2f);

        float finalDeathChance = Math.min(0.85f, deathChance * deathChance * yieldMultiplier);
        return provider.getRandom().nextFloat() < finalDeathChance;
    }

    private BloodSuckResult handleAntiVampireFeedback(LivingEntity target, List<String> consequences) {
        List<ItemStack> targetInventory = getTargetInventory(target);
        float severity = calculateAntiVampireSeverity(targetInventory);
        float rankResistance = this.getRank().getAntiVampireResistance();
        float damage = 4.0f * severity * rankResistance;

        if (!this.provider.getWorld().isClient()) {
            this.provider.damage(this.provider.getDamageSources().magic(), damage);
            this.provider.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, (int) (300 * severity), 1));
        }

        consequences.add("§cThe blessed items sear your undead flesh!");
        return new BloodSuckResult(
                -0.02f * severity,
                0f,
                BloodSource.get(target),
                (int) (30 * severity),
                false,
                consequences
        );
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
        BloodSource bloodSource = BloodSource.get(target);
        float baseYield = bloodSource.getBaseYield();
        //bloodSource.get
        // TODO: continue here
        return null;
    }


    /*public boolean drinkBlood(LivingEntity target, float normalizedAmount, int cooldownTicks) {
        float clampedAmount = MathHelper.clamp(normalizedAmount, 0f, 1f);
        float lerpedToTargetHealth = MathHelper.lerp(clampedAmount, 0, target.getHealth());

        List<ItemStack> inventory = new ArrayList<>();
        if (target instanceof HostileEntity hostileTarget) {
            for (ItemStack itemStack : hostileTarget.getItemsEquipped()) {
                inventory.add(itemStack);
            }
            if (containsAntiVampireProperties(inventory)) {
                provider.damage(provider.getDamageSources().magic(), lerpedToTargetHealth);
            } else {
                hostileTarget.damage(provider.getDamageSources().magic(), lerpedToTargetHealth);
                //TODO: add new blood amount

            }
            hostileTarget.setAttacker(provider);
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

        this.drinkCooldownTicks = cooldownTicks;

        if (provider.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.playSound(null, target.getBlockPos(), NeMuelchSounds.BLOOD_SUCK, SoundCategory.PLAYERS, 1f, 1f);
        }
        return true;
    }

    public boolean drinkBlood(ItemStack stack) {

        return true;
    }*/

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
                return nbt != null && nbt.contains("anti_vampire") && nbt.getBoolean("anti_vampire");
            }
            return false;
        };
    }

    @Override
    public void onMonsterTypeGainedDominance(LivingEntity provider) {
        super.onMonsterTypeGainedDominance(provider);
        this.playSoundForProvider(SoundEvents.ENTITY_BAT_LOOP, SoundCategory.PLAYERS, provider.getPos(), 1f, 0.8f);
    }

    @Override
    public void onMonsterTypeLostDominance(LivingEntity provider) {
        super.onMonsterTypeLostDominance(provider);
    }

    @Override
    public void serverTick() {
        if (this.drinkCooldownTicks > 0) {
            this.drinkCooldownTicks--;
        }
    }

    @Override
    protected void writeCustomNbt(NbtCompound nbt) {

    }

    @Override
    protected void readCustomNbt(NbtCompound nbt) {

    }

    public enum SubType {
        BALANCED(0.33f, 0.33f, 0.34f, "Balanced"),
        BEAST_LORD(0.7f, 0.2f, 0.1f, "Animal-focused"),
        SHADOW_HUNTER(0.2f, 0.7f, 0.1f, "Monster-focused"),
        BLOOD_NOBLE(0.1f, 0.2f, 0.7f, "Humanoid-focused"),
        APEX_PREDATOR(0.5f, 0.5f, 0f, "Animal/Monster hybrid"),
        DARK_SOVEREIGN(0f, 0.5f, 0.5f, "Monster/Humanoid hybrid"),
        PRIMAL_MASTER(0.5f, 0f, 0.5f, "Animal/Humanoid hybrid");

        private final float animalAffinity;
        private final float monsterAffinity;
        private final float humanoidAffinity;
        private final String description;

        SubType(float animal, float monster, float humanoid, String description) {
            this.animalAffinity = animal;
            this.monsterAffinity = monster;
            this.humanoidAffinity = humanoid;
            this.description = description;
        }

        public float getAnimalAffinity() {
            return animalAffinity;
        }

        public float getMonsterAffinity() {
            return monsterAffinity;
        }

        public float getHumanoidAffinity() {
            return humanoidAffinity;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum BloodSource {
        ANIMAL(0.03f, 5, 0.4f, "Passive creatures"),
        MONSTER(0.08f, 2, 0.3f, "Hostile entities"),
        HUMANOID(0.1f, 25, 0.2f, "Humanoids"),
        PLAYER(0.12f, 40, 0.05f, "Players");

        private final float baseYield;
        private final int suspicion;
        private final float deathChance;
        private final String description;

        BloodSource(float baseYield, int suspicion, float deathChance, String description) {
            this.baseYield = baseYield;
            this.suspicion = suspicion;
            this.deathChance = deathChance;
            this.description = description;
        }

        public static BloodSource get(LivingEntity target) {
            if (target instanceof AnimalEntity || target instanceof AmbientEntity) {
                return ANIMAL;
            }
            if (target instanceof Monster || target instanceof SlimeEntity) {
                return MONSTER;
            }
            if (target instanceof PlayerEntity) {
                return PLAYER;
            }
            return HUMANOID;
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

        public String getDescription() {
            return description;
        }
    }

    public enum Rank {
        SCUM(0.05f, 15f, 1.5f, 1.0f),
        PEASANT(0.1f, 17.5f, 1.2f, 0.9f),
        SERVANT(0.4f, 20f, 1.0f, 0.7f),
        KING(0.8f, 25f, 0.6f, 0.5f),
        EMPEROR(0.9f, 35f, 0.3f, 0.3f),
        GOD(1f, 60f, 0.05f, 0.2f);

        private final float upperBloodThreshold;
        private final float damageMultiplier;
        private final float killControlFactor;
        private final float antiVampireResistance;

        Rank(float upperBloodThreshold, float damageMultiplier, float killControlFactor, float antiVampireResistance) {
            this.upperBloodThreshold = upperBloodThreshold;
            this.damageMultiplier = damageMultiplier;
            this.killControlFactor = killControlFactor;
            this.antiVampireResistance = antiVampireResistance;
        }

        public float getUpperBloodThreshold() {
            return upperBloodThreshold;
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
            while (highestMatch.getNext().upperBloodThreshold <= normalizedBlood) {
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
