package me.dustin.jex.module.impl.movement;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

@ModClass(name = "BucketCatch", category = ModCategory.MOVEMENT, description = "Place a water bucket under yourself when you fall to avoid fall damage. 90% of the time, it works every time")
public class BucketCatch extends Module {

    @Op(name = "Fall Distance", min = 3, max = 10)
    public int fallDistance = 5;
    @Op(name = "Rotate")
    public boolean rotate = false;

    private boolean placedBucket;
    private boolean click;

    @EventListener(events = {EventPlayerPackets.class})
    private void runMethod(EventPlayerPackets eventPlayerPackets) {
        if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            if (placedBucket && Wrapper.INSTANCE.getLocalPlayer().fallDistance < fallDistance) {
                int bucket = InventoryHelper.INSTANCE.getFromHotbar(Items.BUCKET);
                if (bucket != -1) {
                    InventoryHelper.INSTANCE.getInventory().selectedSlot = bucket;
                    if (rotate)
                        Wrapper.INSTANCE.getLocalPlayer().pitch = 90;
                    eventPlayerPackets.setPitch(90);
                    if (Wrapper.INSTANCE.getLocalPlayer().isTouchingWater()) {
                        click = true;
                        placedBucket = false;
                        return;
                    }
                } else {
                    placedBucket = false;
                }
            }
            if (!placedBucket && Wrapper.INSTANCE.getLocalPlayer().fallDistance >= fallDistance && EntityHelper.INSTANCE.distanceFromGround(Wrapper.INSTANCE.getLocalPlayer()) <= 3.5f && !placedBucket) {
                int waterBucketSlot = InventoryHelper.INSTANCE.getFromHotbar(Items.WATER_BUCKET);
                if (waterBucketSlot != -1) {
                    InventoryHelper.INSTANCE.getInventory().selectedSlot = waterBucketSlot;
                    if (rotate)
                        Wrapper.INSTANCE.getLocalPlayer().pitch = 90;
                    eventPlayerPackets.setPitch(90);
                    BlockPos pos = Wrapper.INSTANCE.getLocalPlayer().getBlockPos().add(0, -3f, 0);
                    if (WorldHelper.INSTANCE.getBlock(pos) != Blocks.AIR) {
                        click = true;
                        placedBucket = true;
                    }
                }
            }
        } else if (click) {
            NetworkHelper.INSTANCE.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND));
            click = false;
        }
    }
}