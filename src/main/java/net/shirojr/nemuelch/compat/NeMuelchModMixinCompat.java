package net.shirojr.nemuelch.compat;

import net.shirojr.nemuelch.compat.revive.ReviveCompat;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class NeMuelchModMixinCompat implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
/*        if (mixinClassName.contains("ClientPlayerInteractionManagerMixin") && !ReviveCompat.IS_INSTALLED) return false;
        if (mixinClassName.contains("ServerPlayNetworkHandlerMixin") && !ReviveCompat.IS_INSTALLED) return false;*/
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
