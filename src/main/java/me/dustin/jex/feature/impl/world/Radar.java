package me.dustin.jex.feature.impl.world;

import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;

@Feat(name = "Radar", category = FeatureCategory.WORLD, description = "Draws a Radar on your HUD telling you where entities are")
public class Radar extends Feature {
    public static Radar INSTANCE;
    @Op(name = "Players")
    public boolean players = true;
    @Op(name = "Hostiles")
    public boolean hostiles = true;
    @Op(name = "Passives")
    public boolean passives = true;
    @Op(name = "Items")
    public boolean items = true;

    public Radar() {
        INSTANCE = this;
    }

    public boolean isValid(Entity entity) {
        if (entity instanceof PlayerEntity && entity != Wrapper.INSTANCE.getLocalPlayer())
            return players;
        if (entity instanceof ItemEntity)
            return items;
        if (EntityHelper.INSTANCE.isHostileMob(entity))
            return hostiles;
        if (EntityHelper.INSTANCE.isPassiveMob(entity))
            return passives;
        return false;
    }
}