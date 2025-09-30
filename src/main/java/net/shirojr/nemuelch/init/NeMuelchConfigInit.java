package net.shirojr.nemuelch.init;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.shirojr.nemuelch.config.NeMuelchConfig;

public class NeMuelchConfigInit {
    public static NeMuelchConfig CONFIG = new NeMuelchConfig();

    public static void initialize() {
        AutoConfig.register(NeMuelchConfig.class, Toml4jConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(NeMuelchConfig.class).getConfig();
    }
}
