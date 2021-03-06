package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventPlayerUpdates;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.baritone.BaritoneHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.FoodComponents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Feature.Manifest(name = "AutoEat", category = Feature.Category.PLAYER, description = "Eat food when hunger is low.")
public class AutoEat extends Feature {

    public static boolean isEating;
    @Op(name = "Mode", all = {"Saturation", "Hunger"})
    public String mode = "Saturation";
    @Op(name = "Press Key")
    public boolean pressKey;
    @Op(name = "Eat Negative Foods")
    public boolean negativeFoods;
    @Op(name = "Eat To Regen")
    public boolean eatToRegen;
    private int savedSlot = 0;
    private int lastFood;

    @EventListener(events = {EventPlayerUpdates.class, EventPacketSent.class})
    public void run(Event event) {
        if (Wrapper.INSTANCE.getLocalPlayer() != null && getBestFood().itemStack != null) {
            setSuffix(getBestFood().itemStack.getName().getString());
        } else {
            setSuffix("None");
        }
        if (Wrapper.INSTANCE.getLocalPlayer() == null || Feature.get(Freecam.class).getState() || Wrapper.INSTANCE.getLocalPlayer().isCreative()) {
            isEating = false;
            return;
        }
        if (event instanceof EventPlayerUpdates) {
            if (((EventPlayerUpdates) event).getMode() == EventPlayerUpdates.Mode.PRE) {
                if (getBestFood().slot != -1 && getBestFood().itemStack != null && needsToEat(getBestFood())) {
                    if (EntityHelper.INSTANCE.isAuraBlocking())
                        PlayerHelper.INSTANCE.unblock();
                    if (!isEating) {
                        savedSlot = InventoryHelper.INSTANCE.getInventory().selectedSlot;
                        if (BaritoneHelper.INSTANCE.baritoneExists())
                            BaritoneHelper.INSTANCE.pause();
                        InventoryHelper.INSTANCE.getInventory().selectedSlot = getBestFood().slot;
                        lastFood = Wrapper.INSTANCE.getLocalPlayer().getHungerManager().getFoodLevel();
                        isEating = true;
                    }
                    if (lastFood != Wrapper.INSTANCE.getLocalPlayer().getHungerManager().getFoodLevel()) {
                        if (lastFood < Wrapper.INSTANCE.getLocalPlayer().getHungerManager().getFoodLevel()) {
                            isEating = false;
                            if (pressKey)
                            Wrapper.INSTANCE.getOptions().keyUse.setPressed(false);
                            NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.UP));
                            InventoryHelper.INSTANCE.getInventory().selectedSlot = savedSlot;
                        }
                        lastFood = Wrapper.INSTANCE.getLocalPlayer().getHungerManager().getFoodLevel();
                        if (BaritoneHelper.INSTANCE.baritoneExists())
                            BaritoneHelper.INSTANCE.resume();
                    }
                    if (isEating) {
                        if (pressKey)
                        Wrapper.INSTANCE.getOptions().keyUse.setPressed(true);
                        Wrapper.INSTANCE.getInteractionManager().interactItem(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND);
                    }
                } else if (isEating) {
                    isEating = false;
                    NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.UP));
                    InventoryHelper.INSTANCE.getInventory().selectedSlot = savedSlot;
                }
            }
        }
        if (event instanceof EventPacketSent eventPacketSent) {
            if (eventPacketSent.getPacket() instanceof UpdateSelectedSlotC2SPacket && isEating) {
                if (((UpdateSelectedSlotC2SPacket) eventPacketSent.getPacket()).getSelectedSlot() != getBestFood().slot)
                    event.cancel();
            }
            if (eventPacketSent.getPacket() instanceof PlayerActionC2SPacket playerActionC2SPacket) {
                if (playerActionC2SPacket.getAction() == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM && isEating)
                    event.cancel();
            }
        }
    }

    private boolean needsToEat(FoodInfo foodInfo) {
        if (!eatToRegen) {
            return 20 - Wrapper.INSTANCE.getLocalPlayer().getHungerManager().getFoodLevel() >= foodInfo.item.getFoodComponent().getHunger();
        } else {
            return 20 - Wrapper.INSTANCE.getLocalPlayer().getHungerManager().getFoodLevel() >= foodInfo.item.getFoodComponent().getHunger() || (Wrapper.INSTANCE.getLocalPlayer().getHealth() < 20 && Wrapper.INSTANCE.getLocalPlayer().getHungerManager().getFoodLevel() < 18);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (isEating) {
            NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.UP));
        }
        isEating = false;
        if (BaritoneHelper.INSTANCE.baritoneExists())
            BaritoneHelper.INSTANCE.resume();
    }

    public FoodInfo getBestFood() {
        float points = 0;
        int slot = -1;
        ItemStack stack = null;
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = InventoryHelper.INSTANCE.getInventory().getStack(i);
            if (itemStack != null && itemStack.getItem().isFood()) {
                Item item = itemStack.getItem();
                switch (mode) {
                    case "Saturation":
                        if (isValidFood(item.getFoodComponent()) && item.getFoodComponent().getSaturationModifier() > points) {
                            points = item.getFoodComponent().getSaturationModifier();
                            slot = i;
                            stack = itemStack;
                        }
                        break;
                    case "Hunger":
                        if (isValidFood(item.getFoodComponent()) && item.getFoodComponent().getHunger() > points) {
                            points = item.getFoodComponent().getHunger();
                            slot = i;
                            stack = itemStack;
                        }
                        break;
                }
            }
        }
        return new FoodInfo(points, slot, stack);
    }

    public boolean isValidFood(FoodComponent foodComponent) {
        if (foodComponent == FoodComponents.PUFFERFISH || foodComponent == FoodComponents.SPIDER_EYE || foodComponent == FoodComponents.ROTTEN_FLESH || foodComponent == FoodComponents.POISONOUS_POTATO)
            return negativeFoods;
        else return true;
    }

    public static class FoodInfo {
        public float points;
        public int slot;
        public ItemStack itemStack;
        public Item item;

        public FoodInfo(float points, int slot, ItemStack itemStack) {
            this.points = points;
            this.slot = slot;
            this.itemStack = itemStack;
            if (itemStack != null)
                item = itemStack.getItem();
        }

    }

}
