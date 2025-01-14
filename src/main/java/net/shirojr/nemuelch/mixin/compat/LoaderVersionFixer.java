package net.shirojr.nemuelch.mixin.compat;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.UUID;

@Pseudo
@Mixin(targets = {"me.fzzyhmstrs.amethyst_imbuement.registry.RegisterKeybindServer"})
public class LoaderVersionFixer {

    @Dynamic
    @WrapWithCondition(method = "<clinit>", at = @At(value = "FIELD", target = "Lme/fzzyhmstrs/amethyst_imbuement/registry/RegisterKeybindServer;veinMiners:Ljava/util/Map;"), remap = false)
    private static boolean PreventClientAssignmentCode(Map<UUID, Boolean> value) {
        return false;
    }
}