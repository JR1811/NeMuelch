package net.shirojr.nemuelch.mixin;

import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Random;

@Mixin(NetherWartBlock.class)
public abstract class NetherWartBlockMixin extends PlantBlock implements Fertilizable {
    public NetherWartBlockMixin(Settings settings) {
        super(settings);
    }

    @Override
    public boolean isFertilizable(BlockView world, BlockPos pos, BlockState state, boolean isClient) {
        return NeMuelchConfigInit.CONFIG.fertilizableNetherWarts;
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return NeMuelchConfigInit.CONFIG.fertilizableNetherWarts;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        int newAgeValue = getAge(state) + getGrowthAmount(world);

        if (newAgeValue > getMaxAge()) {
            newAgeValue = getMaxAge();
        }

        world.setBlockState(pos, this.withAge(newAgeValue), Block.NOTIFY_LISTENERS);
    }


    @Unique
    protected int getAge(BlockState state) {
        return state.get(this.getAgeProperty());
    }

    @Unique
    public int getMaxAge() {
        return NetherWartBlock.field_31199;
    }

    @Unique
    public IntProperty getAgeProperty() {
        return NetherWartBlock.AGE;
    }

    @Unique
    public BlockState withAge(int age) {
        return this.getDefaultState().with(this.getAgeProperty(), age);
    }

    @Unique
    protected int getGrowthAmount(World world) {
        return MathHelper.nextInt(world.random, 0, getMaxAge() - 1);
    }
}
