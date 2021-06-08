package me.dustin.jex.feature.impl.misc;

import com.google.gson.JsonObject;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.slot.SlotActionType;

@Feat(name = "MendingSaver", category = FeatureCategory.MISC, description = "Save your mending tools from breaking by putting them away automatically.")
public class MendingSaver extends Feature {

    @Op(name = "Notify")
    public boolean notify;

    @EventListener(events = {EventPlayerPackets.class})
    private void runMethod(EventPlayerPackets eventPlayerPackets) {
        if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            ItemStack currentStack = Wrapper.INSTANCE.getLocalPlayer().getMainHandStack();
            if (currentStack != null && currentStack.hasEnchantments()) {
                for (NbtElement tag : currentStack.getEnchantments()) {
                    JsonObject jsonObject = JsonHelper.INSTANCE.gson.fromJson(tag.toString(), JsonObject.class);
                    if (jsonObject.get("id").getAsString().contains("mending")) {
                        if (currentStack.isDamageable() && currentStack.getDamage() > currentStack.getMaxDamage() - 10) {
                            if (notify)
                                ChatHelper.INSTANCE.addClientMessage("MendingSaver just saved your item");

                            if (!InventoryHelper.INSTANCE.isInventoryFull())
                                InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, InventoryHelper.INSTANCE.getInventory().selectedSlot + 36, SlotActionType.QUICK_MOVE);
                            else
                                InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, InventoryHelper.INSTANCE.getInventory().selectedSlot + 36, SlotActionType.SWAP, 8);
                        }
                    }
                }
            }
        }
    }
}
