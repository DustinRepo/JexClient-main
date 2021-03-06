package me.dustin.jex.feature.mod.impl.render;

import com.google.common.collect.Maps;
import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.MCAPIHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

@Feature.Manifest(name = "MobOwners", category = Feature.Category.VISUAL, description = "Show the names of the owners of tamed mobs")
public class OwnerTags extends Feature {

    @Op(name = "Draw Faces")
    public boolean drawFaces = true;
    private HashMap<LivingEntity, Vec3d> positions = Maps.newHashMap();

    @EventListener(events = {EventRender3D.class, EventRender2D.class})
    private void runMethod(Event event) {
        if (event instanceof EventRender3D eventRender3D) {
            positions.clear();
            for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
                if (entity instanceof LivingEntity tameableEntity) {
                    if (EntityHelper.INSTANCE.getOwnerUUID(tameableEntity) != null) {
                        positions.put(tameableEntity, Render2DHelper.INSTANCE.getHeadPos(entity, eventRender3D.getPartialTicks(), eventRender3D.getMatrixStack()));
                    }
                }
            }
        } else if (event instanceof EventRender2D eventRender2D) {
            Nametag nametagModule = (Nametag) Feature.get(Nametag.class);
            positions.keySet().forEach(livingEntity -> {
                Vec3d pos = positions.get(livingEntity);
                if (isOnScreen(pos)) {
                    float x = (float) pos.x;
                    float y = (float) pos.y;
                    if (nametagModule.getState() && nametagModule.passives) {
                        y -= 12;
                    }
                    UUID uuid = EntityHelper.INSTANCE.getOwnerUUID(livingEntity);
                    String nameString = PlayerHelper.INSTANCE.getName(uuid);
                    if (nameString == null)
                        nameString = Objects.requireNonNull(uuid).toString();
                    nameString = "\247o" + nameString.trim();
                    float length = FontHelper.INSTANCE.getStringWidth(nameString);
                    Render2DHelper.INSTANCE.fill(eventRender2D.getMatrixStack(), x - (length / 2) - 2, y - 12, x + (length / 2) + 2, y - 1, 0x35000000);
                    FontHelper.INSTANCE.drawCenteredString(eventRender2D.getMatrixStack(), nameString, x, y - 10, -1);

                    if (drawFaces) {
                        Render2DHelper.INSTANCE.drawFace(((EventRender2D) event).getMatrixStack(), x - 8, y - 30, 2, MCAPIHelper.INSTANCE.getPlayerSkin(uuid));
                    }
                }
            });
        }
    }

    public boolean isOnScreen(Vec3d pos) {
        return pos != null && (pos.z > -1 && pos.z < 1);
    }
}
