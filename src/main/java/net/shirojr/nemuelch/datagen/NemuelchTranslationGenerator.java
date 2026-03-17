package net.shirojr.nemuelch.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.util.VariationHolder;
import net.shirojr.nemuelch.init.*;
import net.shirojr.nemuelch.item.custom.block.CrateBlockItem;
import net.shirojr.nemuelch.item.custom.castAndMagicItem.MiasmaItem;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NemuelchTranslationGenerator extends FabricLanguageProvider {
    public NemuelchTranslationGenerator(FabricDataOutput dataOutput) {
        super(dataOutput);
    }

    @Override
    public void generateTranslations(TranslationBuilder builder) {
        builder.add(NeMuelchEntities.POT_LAUNCHER, cleanString(Registries.ENTITY_TYPE.getId(NeMuelchEntities.POT_LAUNCHER), false));
        builder.add(NeMuelchEntities.DROP_POT, cleanString(Registries.ENTITY_TYPE.getId(NeMuelchEntities.DROP_POT), false));
        builder.add(NeMuelchEntities.SLIME_ITEM, "Slime");
        builder.add(NeMuelchEntities.ARKADUSCANE_PROJECTILE, cleanString(Registries.ENTITY_TYPE.getId(NeMuelchEntities.ARKADUSCANE_PROJECTILE), false));

        builder.add(NeMuelchItems.POT_LAUNCHER, cleanString(Registries.ITEM.getId(NeMuelchItems.POT_LAUNCHER), false));
        builder.add(NeMuelchItems.POT_LAUNCHER_LEGS, cleanString(Registries.ITEM.getId(NeMuelchItems.POT_LAUNCHER_LEGS), false));
        builder.add(NeMuelchItems.POT_LAUNCHER_DEEPSLATE_BASKET, cleanString(Registries.ITEM.getId(NeMuelchItems.POT_LAUNCHER_DEEPSLATE_BASKET), false));
        builder.add(NeMuelchItems.POT_LAUNCHER_LOADER, cleanString(Registries.ITEM.getId(NeMuelchItems.POT_LAUNCHER_LOADER), false));
        builder.add(NeMuelchItems.BOOK_WRAPPER, cleanString(Registries.ITEM.getId(NeMuelchItems.BOOK_WRAPPER), false));
        builder.add(NeMuelchItems.LARD, cleanString(Registries.ITEM.getId(NeMuelchItems.LARD), false));
        builder.add(NeMuelchItems.SOAP, cleanString(Registries.ITEM.getId(NeMuelchItems.SOAP), false));
        builder.add(NeMuelchItems.CREATIVE_SOAP, cleanString(Registries.ITEM.getId(NeMuelchItems.CREATIVE_SOAP), false));
        builder.add(NeMuelchItems.SOUND_TOOL, cleanString(Registries.ITEM.getId(NeMuelchItems.SOUND_TOOL), false));
        builder.add(NeMuelchItems.DISPLACEMENT_TOOL, "Camera Displacement Tool");
        builder.add(NeMuelchItems.MEAT_LUMP, cleanString(Registries.ITEM.getId(NeMuelchItems.MEAT_LUMP), false));
        builder.add(NeMuelchItems.COOKED_MEAT_LUMP, cleanString(Registries.ITEM.getId(NeMuelchItems.COOKED_MEAT_LUMP), false));
        builder.add(NeMuelchItems.ROTTEN_MEAT_LUMP, cleanString(Registries.ITEM.getId(NeMuelchItems.ROTTEN_MEAT_LUMP), false));
        for (CrateBlockItem crate : NeMuelchItems.CRATES) {
            builder.add(crate, cleanString(Registries.ITEM.getId(crate), false));
        }
        builder.add(NeMuelchItems.GLOVE, "Glove");
        builder.add("tooltip.nemuelch.glove.line_1", "Hits taken: ");

        builder.add(NeMuelchEnchantments.CURSE_OF_THE_BARE, cleanString(Registries.ENCHANTMENT.getId(NeMuelchEnchantments.CURSE_OF_THE_BARE), false));

        builder.add(NeMuelchBlocks.ROTTEN_MEAT, cleanString(Registries.BLOCK.getId(NeMuelchBlocks.ROTTEN_MEAT), false));
        builder.add(NeMuelchBlocks.ROTTEN_TREE_LOG, cleanString(Registries.BLOCK.getId(NeMuelchBlocks.ROTTEN_TREE_LOG), false));
        builder.add(NeMuelchBlocks.ROTTEN_TREE_SAPLING, cleanString(Registries.BLOCK.getId(NeMuelchBlocks.ROTTEN_TREE_SAPLING), false));
        builder.add(NeMuelchBlocks.ADVANCED_FOG, cleanString(Registries.BLOCK.getId(NeMuelchBlocks.ADVANCED_FOG), false));

        builder.add("sound." + NeMuelchSounds.SQUIRT.getId().toTranslationKey(), "Something Squirted");
        builder.add("sound." + NeMuelchSounds.EATING_CRUNCHY.getId().toTranslationKey(), "Crunchy Eating");
        builder.add("sound." + NeMuelchSounds.EATING_DIGESTION.getId().toTranslationKey(), "Digestion Growled");
        builder.add("sound." + NeMuelchSounds.HUMAN_GROWL.getId().toTranslationKey(), "Growl");
        builder.add("sound." + NeMuelchSounds.HIT_DEITY.getId().toTranslationKey(), "Hit by something which does not exist");
        builder.add("sound." + NeMuelchSounds.ANCIENT_CREATURE_CALL_1.getId().toTranslationKey(), "Something ancient called");
        builder.add("sound." + NeMuelchSounds.PULL_UP.getId().toTranslationKey(), "Pull");
        builder.add("sound." + NeMuelchSounds.CRYPTIC_CHANT_01.getId().toTranslationKey(), "Who Are you?");
        builder.add("sound." + NeMuelchSounds.CRYPTIC_POEM_01.getId().toTranslationKey(), "Where is the Captain?");
        builder.add("sound." + NeMuelchSounds.CHILD_GIGGLE.getId().toTranslationKey(), "A! ?Happy? Child?");
        builder.add("sound." + NeMuelchSounds.CHILD_LAUGH_1.getId().toTranslationKey(), "A! ?Laughing? Child?");
        builder.add("sound." + NeMuelchSounds.CHILD_LAUGH_2.getId().toTranslationKey(), "A! ?Laughing? Child?");

        builder.add(NeMuelchStatusEffects.DEFERRED_HEALTH, "Imminent Health");
        builder.add(NeMuelchStatusEffects.DEFERRED_DAMAGE, "Imminent Damage");
        builder.add(NeMuelchStatusEffects.EXECUTION, "Execution");
        builder.add(NeMuelchStatusEffects.REBOUND, "Rebound");

        builder.add("death.attack.execution", "%s was executed");
        builder.add("death.attack.execution.item", "%s was executed by %s using %s");
        builder.add("death.attack.execution.player", "%s was executed by %s");

        for (Potion potion : NeMuelchPotions.ALL_POTIONS) {
            String potionKey = potion.finishTranslationKey("item.minecraft.potion.effect.");
            builder.add(potionKey, "Potion of " + cleanString(Registries.POTION.getId(potion), false));
            String splashPotionKey = potion.finishTranslationKey("item.minecraft.splash_potion.effect.");
            builder.add(splashPotionKey, "Splash Potion of " + cleanString(Registries.POTION.getId(potion), false));
            String lingeringPotionKey = potion.finishTranslationKey("item.minecraft.lingering_potion.effect.");
            builder.add(lingeringPotionKey, "Lingering Potion of " + cleanString(Registries.POTION.getId(potion), false));
            String tippedArrowKey = potion.finishTranslationKey("item.minecraft.tipped_arrow.effect.");
            builder.add(tippedArrowKey, "Arrow of " + cleanString(Registries.POTION.getId(potion), false));
        }

        for (VariationHolder variationHolder : NeMuelchBlocks.VARIATION_BLOCKS) {
            Identifier identifier = Registries.BLOCK.getId(variationHolder.getBlock());
            builder.add(variationHolder.getBlock(), cleanString(identifier, false));
        }

        for (MiasmaItem miasmaItem : NeMuelchItems.MIASMA_ITEMS) {
            builder.add(miasmaItem, cleanString(Registries.ITEM.getId(miasmaItem), true));
        }

        try {
            Path existingFilePath = dataOutput.getModContainer().findPath("assets/%s/lang/en_us.existing.json".formatted(NeMuelch.MOD_ID)).orElseThrow();
            builder.add(existingFilePath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to add existing language file!", e);
        }
    }

    public static String cleanString(@Nullable Identifier identifier, boolean reverse) {
        if (identifier == null) throw new NullPointerException("Not a valid Identifier for clean String Translation");
        List<String> input = List.of(identifier.getPath().split("/"));
        List<String> words = Arrays.asList(input.get(input.size() - 1).split("_"));
        return cleanMergedString(words, reverse);
    }

    public static String cleanMergedString(List<String> input, boolean reverse) {
        List<String> words = new ArrayList<>(input);
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
