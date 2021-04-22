package me.dustin.jex.feature.impl.player;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.load.impl.IMinecraft;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;

@Feat(name = "FastPlace", category = FeatureCategory.PLAYER, description = "Place fast.")
public class FastPlace extends Feature {

    @EventListener(events = {EventPlayerPackets.class})
    public void run(EventPlayerPackets event) {
        if (event.getMode() == EventPlayerPackets.Mode.PRE) {
            ((IMinecraft) Wrapper.INSTANCE.getMinecraft()).setRightClickDelayTimer(0);
        }
    }

    @Override
    public void onDisable() {
        ((IMinecraft) Wrapper.INSTANCE.getMinecraft()).setRightClickDelayTimer(4);
        super.onDisable();
    }

}