package net.shirojr.nemuelch.init;

import net.minecraft.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.monster.AbstractMonsterType;
import net.shirojr.nemuelch.monster.type.DryadMonsterType;
import net.shirojr.nemuelch.monster.type.VampireMonsterType;
import net.shirojr.nemuelch.monster.type.WerwolfMonsterType;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public interface NeMuelchMonsterTypes {
    List<AbstractMonsterType> ALL = new ArrayList<>();

    DryadMonsterType DRYAD = register("dryad", new DryadMonsterType());
    VampireMonsterType VAMPIRE = register("vampire", new VampireMonsterType());
    WerwolfMonsterType WERWOLF = register("werwolf", new WerwolfMonsterType());

    private static <T extends AbstractMonsterType> T register(String name, T entry) {
        T registeredEntry = Registry.register(NeMuelchCustomRegistries.MONSTERS, NeMuelch.getId(name), entry);
        ALL.add(registeredEntry);
        return registeredEntry;
    }

    static void initialize() {
        // static initialisation
    }
}
