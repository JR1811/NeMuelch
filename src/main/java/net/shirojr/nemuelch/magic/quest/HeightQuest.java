package net.shirojr.nemuelch.magic.quest;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.magic.Quest;
import net.shirojr.nemuelch.magic.Reward;

public class HeightQuest extends Quest {
    private final double maxY, minY;

    public HeightQuest(String name, Reward reward, double minY, double maxY) {
        super(name, reward);
        this.minY = minY;
        this.maxY = maxY;
    }

    @Override
    public Identifier getIdentifier() {
        return new Identifier(NeMuelch.MOD_ID, "height_quest");
    }

    public static HeightQuest below(String name, Reward reward, World world, double maxY) {
        return new HeightQuest(name, reward, world.getBottomY(), maxY);
    }

    public static HeightQuest above(String name, Reward reward, World world, double minxY) {
        return new HeightQuest(name, reward, minxY, world.getTopY());
    }

    public boolean isInRange(Vec3d currentPos) {
        return currentPos.getY() > this.minY && currentPos.getY() < this.maxY;
    }

    @Override
    public void checkProgress(LivingEntity entity) {
        if (isInRange(entity.getPos())) {
            this.markCompleted();
        }
        super.checkProgress(entity);
    }
}
