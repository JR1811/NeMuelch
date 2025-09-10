package net.shirojr.nemuelch.datapack;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class RandomTickSpeedChanceDatapack implements SimpleSynchronousResourceReloadListener {
    public static final String DIRECTORY = "random_tick_speed_chances";
    public static final HashMap<Block, Float> BLOCK_CHANCES = new HashMap<>();

    @Override
    public Identifier getFabricId() {
        return NeMuelch.getId(DIRECTORY);
    }

    @Override
    public void reload(ResourceManager manager) {
        BLOCK_CHANCES.clear();
        var files = manager.findResources(DIRECTORY, filePath -> filePath.getPath().endsWith(".json") && filePath.getPath().contains(DIRECTORY));
        for (Map.Entry<Identifier, Resource> entry : files.entrySet()) {
            Identifier fileIdentifier = entry.getKey();
            try {
                InputStream inputStream = entry.getValue().getInputStream();
                JsonObject json = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
                for (var jsonEntry : json.entrySet()) {
                    Identifier blockIdentifier = Identifier.tryParse(jsonEntry.getKey());
                    Block block = Registries.BLOCK.get(blockIdentifier);
                    float chance = jsonEntry.getValue().getAsFloat();
                    if (Registries.BLOCK.getDefaultId().equals(blockIdentifier)) {
                        NeMuelch.LOGGER.warn("Block ID [{}] not recognized in {} datapack", blockIdentifier, fileIdentifier.getPath());
                        continue;
                    }
                    if (BLOCK_CHANCES.containsKey(block)) {
                        NeMuelch.LOGGER.warn("Block ID [{}] already present in datapack holder", blockIdentifier);
                        continue;
                    }
                    BLOCK_CHANCES.put(block, chance);
                }
            } catch (Exception e) {
                NeMuelch.LOGGER.error("{} not loaded", fileIdentifier, e);
            }
        }
    }
}
