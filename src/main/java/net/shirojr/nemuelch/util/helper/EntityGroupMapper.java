package net.shirojr.nemuelch.util.helper;

import net.minecraft.entity.EntityGroup;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import net.shirojr.nemuelch.init.NeMuelchTags;

import java.util.Locale;

public enum EntityGroupMapper implements StringIdentifiable {
    DEFAULT(EntityGroup.DEFAULT, NeMuelchTags.Items.DUMMY_CLEAR, Formatting.WHITE),
    UNDEAD(EntityGroup.UNDEAD, NeMuelchTags.Items.DUMMY_UNDEAD, Formatting.GREEN),
    ARTHROPOD(EntityGroup.ARTHROPOD, NeMuelchTags.Items.DUMMY_ARTHROPOD, Formatting.DARK_GRAY),
    ILLAGER(EntityGroup.ILLAGER, NeMuelchTags.Items.DUMMY_ILLAGER, Formatting.RED),
    AQUATIC(EntityGroup.AQUATIC, NeMuelchTags.Items.DUMMY_AQUATIC, Formatting.AQUA);

    private final EntityGroup group;
    private final TagKey<Item> markerItem;
    private final Formatting textFormatting;

    EntityGroupMapper(EntityGroup group, TagKey<Item> markerItem, Formatting formatting) {
        this.group = group;
        this.markerItem = markerItem;
        this.textFormatting = formatting;
    }

    public static EntityGroupMapper of(EntityGroup group) {
        for (EntityGroupMapper entry : EntityGroupMapper.values()) {
            if (entry.getGroup().equals(group)) return entry;
        }
        throw new IllegalStateException("No such Entity Group was found in the Mapper: %s".formatted(group));
    }

    public static EntityGroupMapper get(String name) {
        for (EntityGroupMapper entry : EntityGroupMapper.values()) {
            if (entry.asString().equals(name.toLowerCase(Locale.ROOT))) {
                return entry;
            }
        }
        return DEFAULT;
    }

    public EntityGroup getGroup() {
        return group;
    }

    public TagKey<Item> getMarkerItem() {
        return markerItem;
    }

    public Formatting getTextFormatting() {
        return textFormatting;
    }

    @Override
    public String asString() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
