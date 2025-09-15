package net.shirojr.nemuelch.block.util;

import net.minecraft.block.Block;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record Variation(String name, Block parentBlock, Identifier innerTexture, Identifier outerTexture,
                        Identifier rimTexture, @Nullable Identifier customParticleTexture,
                        List<TagKey<Block>> blockTags) {

    public Variation(String name, Block parentBlock, Identifier innerTexture, Identifier outerTexture,
                     Identifier rimTexture, @Nullable Identifier customParticleTexture, List<TagKey<Block>> blockTags) {
        this.name = name;
        this.parentBlock = parentBlock;
        this.innerTexture = Identifier.of(innerTexture.getNamespace(), "block/" + innerTexture.getPath());
        this.outerTexture = Identifier.of(outerTexture.getNamespace(), "block/" + outerTexture.getPath());
        this.rimTexture = Identifier.of(rimTexture.getNamespace(), "block/" + rimTexture.getPath());
        this.customParticleTexture = customParticleTexture == null ? null :
                Identifier.of(customParticleTexture.getNamespace(), "block/" + customParticleTexture.getPath());
        this.blockTags = blockTags;
    }

    public Variation(String name, Block parentBlock, Identifier innerTexture, Identifier outerTexture,
                     Identifier rimTexture, List<TagKey<Block>> blockTags) {
        this(name, parentBlock, innerTexture, outerTexture, rimTexture, null, blockTags);
    }

    public Variation(String name, Block parentBlock, Identifier singleTexture, List<TagKey<Block>> blockTags) {
        this(name, parentBlock, singleTexture, singleTexture, singleTexture, blockTags);
    }

    public Variation(String name, Block parentBlock, Identifier singleTexture, Identifier particleTexture, List<TagKey<Block>> blockTags) {
        this(name, parentBlock, singleTexture, singleTexture, singleTexture, particleTexture, blockTags);
    }

    @SafeVarargs
    public static List<TagKey<Block>> getStoneTags(TagKey<Block>... extraTags) {
        List<TagKey<Block>> blockTags = new ArrayList<>();
        blockTags.add(BlockTags.PICKAXE_MINEABLE);
        blockTags.addAll(Arrays.asList(extraTags));
        return blockTags;
    }
}
