package net.shirojr.nemuelch.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Deque;
import java.util.List;

@Mixin(ToastManager.class)
public abstract class ToastManagerMixin {
    @Unique
    private static final List<SystemToast.Type> SYSTEM_FAILURE_TOAST_TYPES = List.of(
            SystemToast.Type.PACK_LOAD_FAILURE,
            SystemToast.Type.WORLD_ACCESS_FAILURE,
            SystemToast.Type.PACK_COPY_FAILURE
    );

    @WrapOperation(method = "add", at = @At(value = "INVOKE", target = "Ljava/util/Deque;add(Ljava/lang/Object;)Z"))
    private boolean clearUnnecessaryToasts(Deque<Toast> instance, Object toast, Operation<Boolean> original) {
        if (!NeMuelchConfigInit.CONFIG.hideUnnecessaryToasts || !(toast instanceof Toast)) {
            return original.call(instance, toast);
        }
        if (toast instanceof SystemToast systemToast) {
            if (SYSTEM_FAILURE_TOAST_TYPES.contains(systemToast.getType())) {
                return original.call(instance, toast);
            }
        }
        return false;
    }
}
