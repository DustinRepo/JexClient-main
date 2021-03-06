package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;

@Feature.Manifest(name = "AntiHunger", category = Feature.Category.PLAYER, description = "Lose less hunger while sprinting.")
public class AntiHunger extends Feature {

    @EventListener(events = {EventPlayerPackets.class})
    public void run(EventPlayerPackets event) {
        if (event.getMode() == EventPlayerPackets.Mode.PRE) {
            if (Wrapper.INSTANCE.getLocalPlayer() == null)
                return;
            if (Wrapper.INSTANCE.getLocalPlayer().prevY == Wrapper.INSTANCE.getLocalPlayer().getY())
                event.setOnGround(false);
        }
    }

}
