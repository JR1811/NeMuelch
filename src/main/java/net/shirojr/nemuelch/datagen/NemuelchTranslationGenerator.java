package net.shirojr.nemuelch.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.init.NeMuelchEntities;
import net.shirojr.nemuelch.init.NeMuelchItems;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NemuelchTranslationGenerator extends FabricLanguageProvider {
    public NemuelchTranslationGenerator(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    public void generateTranslations(TranslationBuilder builder) {
        builder.add(NeMuelchEntities.POT_LAUNCHER, cleanString(Registry.ENTITY_TYPE.getId(NeMuelchEntities.POT_LAUNCHER), false));
        builder.add(NeMuelchEntities.DROP_POT, cleanString(Registry.ENTITY_TYPE.getId(NeMuelchEntities.DROP_POT), false));
        builder.add(NeMuelchEntities.SLIME_ITEM, "Slime");
        builder.add(NeMuelchEntities.ARKADUSCANE_PROJECTILE, cleanString(Registry.ENTITY_TYPE.getId(NeMuelchEntities.ARKADUSCANE_PROJECTILE), false));
        builder.add(NeMuelchEntities.ONION, cleanString(Registry.ENTITY_TYPE.getId(NeMuelchEntities.ONION), false));

        builder.add(NeMuelchItems.POT_LAUNCHER, cleanString(Registry.ITEM.getId(NeMuelchItems.POT_LAUNCHER), false));
        builder.add(NeMuelchItems.POT_LAUNCHER_LEGS, cleanString(Registry.ITEM.getId(NeMuelchItems.POT_LAUNCHER_LEGS), false));
        builder.add(NeMuelchItems.POT_LAUNCHER_DEEPSLATE_BASKET, cleanString(Registry.ITEM.getId(NeMuelchItems.POT_LAUNCHER_DEEPSLATE_BASKET), false));
        builder.add(NeMuelchItems.POT_LAUNCHER_LOADER, cleanString(Registry.ITEM.getId(NeMuelchItems.POT_LAUNCHER_LOADER), false));


        try {
            Path existingFilePath = dataGenerator.getModContainer()
                    .findPath("assets/%s/lang/en_us.existing.json".formatted(NeMuelch.MOD_ID)).get();
            builder.add(existingFilePath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to add existing language file!", e);
        }
    }

    public static String cleanString(Identifier identifier, boolean reverse) {
        List<String> input = List.of(identifier.getPath().split("/"));
        List<String> words = Arrays.asList(input.get(input.size() - 1).split("_"));
        if (reverse) Collections.reverse(words);
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            char capitalized = Character.toUpperCase(word.charAt(0));
            output.append(capitalized).append(word.substring(1));
            if (i < words.size() - 1) {
                output.append(" ");
            }
        }
        return output.toString();
    }
}
