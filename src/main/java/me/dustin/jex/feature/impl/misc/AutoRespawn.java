package me.dustin.jex.feature.impl.misc;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;

@Feat(name = "AutoRespawn", category = FeatureCategory.MISC, description = "Respawn without having to click anything.")
public class AutoRespawn extends Feature {

    @EventListener(events = {EventPlayerPackets.class})
    public void run(EventPlayerPackets eventPlayerPackets) {
        if (!Wrapper.INSTANCE.getLocalPlayer().isAlive())
            Wrapper.INSTANCE.getLocalPlayer().requestRespawn();
    }

}