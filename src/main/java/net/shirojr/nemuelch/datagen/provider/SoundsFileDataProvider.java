package net.shirojr.nemuelch.datagen.provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundEntry;
import net.minecraft.client.sound.SoundEntryDeserializer;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.floatprovider.ConstantFloatProvider;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.datagen.util.SoundEntryJsonSerializer;
import net.shirojr.nemuelch.datagen.util.SoundOptions;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class SoundsFileDataProvider implements DataProvider {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(SoundEntry.class, new SoundEntryDeserializer())
            .registerTypeAdapter(SoundEntry.class, new SoundEntryJsonSerializer())
            .setPrettyPrinting()
            .create();

    private final DataOutput.PathResolver pathResolver;
    private final CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture;
    private final String modId;
    @Nullable
    private final Identifier existingSoundsFile;
    private final Map<String, SoundEntryBuilder> entries = new LinkedHashMap<>();

    public SoundsFileDataProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture,
                                  String modId, @Nullable Identifier existingSoundsFile) {
        this.pathResolver = output.getResolver(DataOutput.OutputType.RESOURCE_PACK, "");
        this.registryLookupFuture = registryLookupFuture;
        this.modId = modId;
        this.existingSoundsFile = existingSoundsFile;
    }

    public SoundsFileDataProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture,
                                  Identifier existingSoundsFile) {
        this(output, registryLookupFuture, NeMuelch.MOD_ID, existingSoundsFile);
    }

    @SuppressWarnings("unused")
    public SoundsFileDataProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture) {
        this(output, registryLookupFuture, NeMuelch.MOD_ID, null);
    }

    protected abstract void generate(RegistryWrapper.WrapperLookup registryLookup);

    protected void createSimpleEntryFromId(Identifier id) {
        createEntry(id.getPath())
                .sound(id.toString())
                .subtitle("sound.%s.%s".formatted(id.getNamespace(), id.getPath()));
    }

    protected SoundEntryBuilder createEntry(String id) {
        SoundEntryBuilder builder = new SoundEntryBuilder();
        entries.put(id, builder);
        return builder;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        return registryLookupFuture.thenCompose(wrapperLookup -> {
            entries.clear();
            generate(wrapperLookup);

            Map<String, SoundEntry> merged = readExistingEntries();
            for (Map.Entry<String, SoundEntryBuilder> entry : entries.entrySet()) {
                SoundEntry generated = entry.getValue().build();
                SoundEntry existing = merged.get(entry.getKey());
                if (existing != null) {
                    if (!GSON.toJsonTree(existing, SoundEntry.class).equals(GSON.toJsonTree(generated, SoundEntry.class))) {
                        System.err.println("[" + modId + "] sounds.json: '" + entry.getKey()
                                + "' is defined both manually and by datagen with different content. Keeping the manual entry.");
                    }
                } else {
                    merged.put(entry.getKey(), generated);
                }
            }

            JsonObject root = new JsonObject();
            merged.keySet().stream().sorted()
                    .forEach(id -> root.add(id, GSON.toJsonTree(merged.get(id), SoundEntry.class)));

            Path path = pathResolver.resolveJson(Identifier.of(modId, "sounds"));
            return DataProvider.writeToPath(writer, root, path);
        });
    }

    private Map<String, SoundEntry> readExistingEntries() {
        Map<String, SoundEntry> map = new LinkedHashMap<>();
        String resourcePath;
        if (existingSoundsFile != null) {
            resourcePath = "assets/%s/%s".formatted(existingSoundsFile.getNamespace(), existingSoundsFile.getPath());
        } else {
            resourcePath = "assets/%s/sounds.json".formatted(modId);
        }
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                return map;
            }
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                for (String key : root.keySet()) {
                    map.put(key, GSON.fromJson(root.get(key), SoundEntry.class));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No existing sounds.json file found", e);
        }
        return map;
    }

    @Override
    public String getName() {
        return "sounds.json file generation";
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class SoundEntryBuilder {
        private boolean replace = false;
        private String subtitle;
        private final List<Sound> sounds = new ArrayList<>();

        public SoundEntryBuilder replace(boolean replace) {
            this.replace = replace;
            return this;
        }

        public SoundEntryBuilder subtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        public SoundEntryBuilder sound(String name) {
            return this.sounds(List.of(name));
        }

        public SoundEntryBuilder sounds(Collection<String> names) {
            for (String name : names) {
                this.sounds.add(new Sound(name, ConstantFloatProvider.create(1.0f), ConstantFloatProvider.create(1.0f),
                        1, Sound.RegistrationType.FILE, false, false, 16));
            }
            return this;
        }

        public SoundEntryBuilder sound(String name, Consumer<SoundOptions> config) {
            SoundOptions options = new SoundOptions(name);
            config.accept(options);
            sounds.add(options.build());
            return this;
        }

        private SoundEntry build() {
            return new SoundEntry(sounds, replace, subtitle);
        }
    }
}
