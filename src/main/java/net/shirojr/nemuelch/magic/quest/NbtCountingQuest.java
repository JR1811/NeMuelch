package net.shirojr.nemuelch.magic.quest;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.magic.Reward;
import net.shirojr.nemuelch.magic.quest.type.CountingQuest;

public class NbtCountingQuest extends CountingQuest {
    private final NbtCompound nbt;

    public NbtCountingQuest(String name, Reward reward, int completionAmount, NbtCompound nbt) {
        super(name, reward, completionAmount);
        this.nbt = nbt;
    }

    @Override
    public Identifier getIdentifier() {
        return new Identifier(NeMuelch.MOD_ID, "nbt_comparison_quest");
    }

    public boolean canAdd(NbtCompound incomingNbt) {
        return incomingNbt.equals(this.nbt);
    }
}
