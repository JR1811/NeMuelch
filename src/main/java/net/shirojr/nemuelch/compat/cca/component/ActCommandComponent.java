package net.shirojr.nemuelch.compat.cca.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.cca.NeMuelchComponents;

public interface ActCommandComponent extends Component {
    Identifier KEY = NeMuelch.getId("act_command");

    static ActCommandComponent get(PlayerEntity provider) {
        return NeMuelchComponents.ACT_COMMAND.get(provider);
    }

    boolean enabledStalkMode();

    void setStalkMode(boolean enableStalk);
}
