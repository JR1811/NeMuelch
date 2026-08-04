package net.shirojr.nemuelch.monster.type;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.monster.AbstractMonsterType;
import net.shirojr.nemuelch.monster.abilities.custom.*;
import net.shirojr.nemuelch.monster.abilities.data.VampireData;
import net.shirojr.nemuelch.monster.abilities.util.AbilityRegistrar;
import net.shirojr.nemuelch.monster.abilities.util.MonsterTypeData;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public class VampireMonsterType extends AbstractMonsterType {

    @Override
    public void initAbilities(PlayerEntity player, AbilityRegistrar registrar, @Nullable MonsterTypeData data) {
        if (!(data instanceof VampireData vampireData)) {
            throw new IllegalStateException("Wrong dynamic monster data on init of abilities");
        }
        registrar.add(new DrinkBloodAbility(vampireData, 100, 4.5f))
                .add(new BurnSelfAbility(player, BurnSelfAbility.CAUSES_VAMPIRE_BURN))
                .add(new DashAbility(60, 0, 1.2))
                .add(new MultiJumpAbility(5, -1, true))
                .add(new PassiveSpeedModifierAbility(player, 1.4))
                .add(new RiverWaterPunishmentAbility());
    }

    @Override
    public @Nullable MonsterTypeData createDynamicData(PlayerEntity player) {
        return new VampireData(VampireData.Rank.SCUM);
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
                    case "holy", "sun" -> 1.5f;
                    case "silver", "garlic", "athame" -> 1.2f;
                    default -> 1.0f;
                };
                severity += wordSeverity;
                break;
            }
        }

        return Math.min(severity, 5f);
    }

    public static Predicate<ItemStack> containsAntiVampireProperties() {
        List<String> bannedWords = List.of("silver", "holy", "sun", "light", "athame", "garlic");
        return stack -> {
            if (stack.isEmpty()) return false;
            for (String bannedWord : bannedWords) {
                if (Registries.ITEM.getId(stack.getItem()).getPath().contains(bannedWord)) return true;
                if (stack.isIn(NeMuelchTags.Items.ANTI_VAMPIRE)) return true;
                NbtCompound nbt = stack.getNbt();
                return nbt != null && nbt.contains(NeMuelchNbtKeys.ANTI_VAMPIRE) && nbt.getBoolean(NeMuelchNbtKeys.ANTI_VAMPIRE);
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
}
