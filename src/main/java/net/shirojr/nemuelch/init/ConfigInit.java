package net.shirojr.nemuelch.init;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.shirojr.nemuelch.config.NeMuelchConfig;

public class ConfigInit {

    public static NeMuelchConfig CONFIG = new NeMuelchConfig();

    public static void init() {

        AutoConfig.register(NeMuelchConfig.class, GsonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(NeMuelchConfig.class).getConfig();
    }
}
