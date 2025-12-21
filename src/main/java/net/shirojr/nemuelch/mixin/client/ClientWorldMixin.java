package net.shirojr.nemuelch.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.shirojr.nemuelch.init.NeMuelchItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
    @ModifyExpressionValue(method = "getBlockParticle", at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"))
    private boolean addInvisibleBlockBillboardSpriteRendering(boolean original, @Local Item itemInHand) {
        return original || itemInHand.equals(NeMuelchItems.ADVANCED_FOG);
    }
}
