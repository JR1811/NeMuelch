package net.shirojr.nemuelch.compat.statement;

import net.minecraft.block.Blocks;
import net.minecraft.state.property.BooleanProperty;
import virtuoel.statement.api.StateRefresher;

public class StatementPropertyRegistry {
    public static final BooleanProperty IS_PATH = BooleanProperty.of("is_path");

    static {
        StateRefresher.INSTANCE.addBlockProperty(Blocks.SAND, IS_PATH, false);
    }

    public static void initialize() {
        // static initialisation
    }
}
