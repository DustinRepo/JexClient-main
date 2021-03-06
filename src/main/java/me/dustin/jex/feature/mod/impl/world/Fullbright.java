package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;

@Feature.Manifest(name = "Fullbright", category = Feature.Category.WORLD, description = "Goodbye, darkness. You were never my friend.")
public class Fullbright extends Feature {


    @EventListener(events = {EventTick.class})
    private void tick(EventTick eventTick) {
        if (Wrapper.INSTANCE.getOptions() == null)
            return;
        double gamma = Wrapper.INSTANCE.getOptions().gamma;
        if (!getState()) {
            if (gamma > 1)
                Wrapper.INSTANCE.getOptions().gamma -= 0.5f;
            else {
                super.onDisable();
            }
        } else {
            if (gamma < 10) {
                Wrapper.INSTANCE.getOptions().gamma += 0.5f;
            }
        }
    }

    @Override
    public void onDisable() {
    }
}
