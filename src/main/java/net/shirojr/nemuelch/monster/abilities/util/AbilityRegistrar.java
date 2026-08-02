package net.shirojr.nemuelch.monster.abilities.util;

import net.shirojr.nemuelch.monster.abilities.Ability;

import java.util.Optional;
import java.util.function.Consumer;

public interface AbilityRegistrar {
    <T extends Ability> Optional<T> get(Class<T> type);

    <T extends Ability> AbilityRegistrar add(T entry);

    <T extends Ability> void modify(Class<T> type, Consumer<T> modifier);

    void clear();
}
