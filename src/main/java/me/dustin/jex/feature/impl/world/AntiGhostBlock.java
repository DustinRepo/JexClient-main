package me.dustin.jex.feature.impl.world;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.world.EventBreakBlock;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.Direction;

@Feat(name = "AntiGhostBlock", category = FeatureCategory.WORLD, description = "Prevent the game from creating ghost blocks.")
public class AntiGhostBlock extends Feature {

    @EventListener(events = {EventBreakBlock.class})
    public void breakB(EventBreakBlock eventBreakBlock) {
        NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, eventBreakBlock.getPos(), Direction.UP));
    }

}