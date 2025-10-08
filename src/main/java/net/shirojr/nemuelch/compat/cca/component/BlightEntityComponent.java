package net.shirojr.nemuelch.compat.cca.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;

public interface BlightEntityComponent extends Component, AutoSyncedComponent, CommonTickingComponent {
    Identifier KEY = NeMuelch.getId("blight_entity");


}
