package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventAttackEntity;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

@Feature.Manifest(name = "Criticals", category = Feature.Category.COMBAT, description = "Automatically deal critical strikes when attacking.")
public class Criticals extends Feature {

    @Op(name = "Living Only")
    public boolean livingOnly = true;
    @Op(name = "Extra Particles")
    public boolean extraParticles;
    @OpChild(name = "Amount", min = 1, max = 20, inc = 1, parent = "Extra Particles")
    public int amount = 5;

    @EventListener(events = {EventAttackEntity.class})
    private void runMethod(EventAttackEntity eventAttackEntity) {
        if (livingOnly && !(eventAttackEntity.getEntity() instanceof LivingEntity))
            return;
        if (extraParticles) {
            for (int i = 0; i < amount; i++) {
                Wrapper.INSTANCE.getLocalPlayer().addCritParticles(eventAttackEntity.getEntity());
            }
        }
        crit();
    }

    public void crit() {
        if (Wrapper.INSTANCE.getLocalPlayer().isOnGround() && !(Wrapper.INSTANCE.getLocalPlayer().isInLava() || Wrapper.INSTANCE.getLocalPlayer().isTouchingWater())) {
            Wrapper.INSTANCE.getLocalPlayer().networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY() + 0.05F, Wrapper.INSTANCE.getLocalPlayer().getZ(), false));
            Wrapper.INSTANCE.getLocalPlayer().networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), false));
            Wrapper.INSTANCE.getLocalPlayer().networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY() + 0.012511F, Wrapper.INSTANCE.getLocalPlayer().getZ(), false));
            Wrapper.INSTANCE.getLocalPlayer().networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), false));
        }
    }
}
