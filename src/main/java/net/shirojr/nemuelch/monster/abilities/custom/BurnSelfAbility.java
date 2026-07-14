package net.shirojr.nemuelch.monster.abilities.custom;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.monster.abilities.PassiveAbility;
import net.shirojr.nemuelch.util.constants.NbtKeys;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public class BurnSelfAbility extends PassiveAbility {
    public static final Predicate<ItemStack> CAUSES_VAMPIRE_BURN = stack -> {
        if (stack.isEmpty()) return false;
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains(NbtKeys.ANTI_VAMPIRE) && nbt.getBoolean(NbtKeys.ANTI_VAMPIRE)) {
            return true;
        }
        if (stack.isIn(NeMuelchTags.Items.ANTI_VAMPIRE)) {
            return true;
        }
        List<String> bannedWords = List.of("silver", "holy", "sun", "athame", "garlic");
        for (String bannedWord : bannedWords) {
            String itemPath = Registries.ITEM.getId(stack.getItem()).getPath();
            if (!itemPath.contains(bannedWord)) continue;
            return true;
        }
        return false;
    };

    private final Predicate<ItemStack> causesBurn;

    public BurnSelfAbility(ServerPlayerEntity provider, @NotNull Predicate<ItemStack> causesBurn) {
        super(provider);
        this.causesBurn = causesBurn;
    }

    protected void burnSelf() {
        this.provider.setOnFireFor(4);
    }

    @Override
    public void tickServer(ServerPlayerEntity player) {
        super.tickServer(player);
        ServerWorld serverWorld = player.getServerWorld();
        if (serverWorld == null) return;
        if (player.age % 40 != 0) return;
        if (player.getInventory().containsAny(this.causesBurn)) {
            this.burnSelf();
        }
        if (!isProtectedFromEnvironment(serverWorld, player)) {
            this.burnSelf();
        }
    }

    protected boolean isProtectedFromEnvironment(ServerWorld world, ServerPlayerEntity player) {
        if (player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) return true;
        if (world.isRaining() || world.isThundering()) return true;
        if (player.isSubmergedInWater()) return true;
        if (!world.isSkyVisible(player.getBlockPos())) return true;
        if (player.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) return false;

        int safetyStackCounter = 0;
        for (ItemStack armorStack : player.getArmorItems()) {
            if (armorStack.isEmpty()) continue;
            safetyStackCounter++;
            if (safetyStackCounter >= 2) return true;
        }
        return false;
    }

    @Override
    public void onPickedUpItem(PlayerEntity player, ItemEntity itemEntity) {
        if (this.causesBurn.test(itemEntity.getStack())) {
            this.burnSelf();
        }
    }
}
