package net.shirojr.nemuelch.compat.yacl;

import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.shirojr.nemuelch.NeMuelch;

public class NeMuelchYaclConfig {
    public static ConfigClassHandler<NeMuelchYaclConfig> HANDLER = ConfigClassHandler.createBuilder(NeMuelchYaclConfig.class)
            .id(NeMuelch.getId("config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("nemuelch.json5"))
                    .appendGsonBuilder(GsonBuilder::setPrettyPrinting)
                    .setJson5(true)
                    .build()
            ).build();

    //@SerialEntry

}
