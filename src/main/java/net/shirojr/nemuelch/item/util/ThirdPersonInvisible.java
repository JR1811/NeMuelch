package net.shirojr.nemuelch.item.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.shirojr.nemuelch.init.NeMuelchTags;

public interface ThirdPersonInvisible {
    static boolean isInvisible(ItemStack stack) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) return false;
        if (!GameruleCache.INSTANCE.get()) {
            if (stack.getItem() instanceof ThirdPersonInvisible) return true;
            return stack.isIn(NeMuelchTags.Items.BLOCK_THIRD_PERSON_RENDERING);
        }
        return false;
    }

    class GameruleCache {
        public static final GameruleCache INSTANCE = new GameruleCache(true);

        private boolean value;

        public GameruleCache(boolean value) {
            this.value = value;
        }

        public boolean get() {
            return value;
        }

        public void set(boolean value) {
            this.value = value;
        }
    }
}
