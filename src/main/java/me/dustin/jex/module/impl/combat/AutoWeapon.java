package me.dustin.jex.module.impl.combat;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventAttackEntity;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.module.impl.player.AutoEat;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

import java.util.Map;

@ModClass(name = "AutoWeapon", category = ModCategory.COMBAT, description = "Automatically swap to the best weapon when attacking.")
public class AutoWeapon extends Module {

    @Op(name = "Mode", all = {"Sword", "Sword&Axe", "All Tools"})
    public String mode = "Sword";

    @EventListener(events = {EventAttackEntity.class})
    public void run(EventAttackEntity eventAttackEntity) {
        if (AutoEat.isEating)
            return;
            int slot = -1;
            float str = 1;
            ItemStack stack = null;
            for (int i = 0; i < 9; i++) {
                ItemStack stackInSlot = InventoryHelper.INSTANCE.getInventory().getStack(i);
                if (stackInSlot != null) {
                    if (!isGoodItem(stackInSlot.getItem()))
                        continue;
                    float damage = getAdjustedDamage(stackInSlot);

                    if (damage > str) {
                        str = damage;
                        slot = i;
                        stack = stackInSlot;
                    }
                    if (damage == str && str != 1) {
                        if (InventoryHelper.INSTANCE.compareEnchants(stack, stackInSlot, Enchantments.SHARPNESS)) {
                            str = damage;
                            slot = i;
                            stack = stackInSlot;
                        }
                    }
                }

            }
            if (slot != -1 && slot != InventoryHelper.INSTANCE.getInventory().selectedSlot) {
                NetworkHelper.INSTANCE.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
                InventoryHelper.INSTANCE.getInventory().selectedSlot = slot;
            }
    }
    private boolean isGoodItem(Item item) {
        switch (mode.toLowerCase()) {
            case "sword":
                return item instanceof SwordItem;
            case "sword&axe":
                return item instanceof SwordItem || item instanceof AxeItem;
            case "all tools":
                return item instanceof ToolItem;
        }
        return false;
    }

    private float getAdjustedDamage(ItemStack itemStack) {
        float damage = 1;
        if (itemStack.getItem() instanceof SwordItem) {
            SwordItem itemSword = (SwordItem) itemStack.getItem();
            damage = itemSword.getAttackDamage();
        } else if (itemStack.getItem() instanceof MiningToolItem) {
            MiningToolItem miningToolItem = (MiningToolItem) itemStack.getItem();
            damage = miningToolItem.getAttackDamage();
        }
        return damage + getSharpnessModifier(itemStack);
    }

    public float getSharpnessModifier(ItemStack itemStack) {
        if (itemStack.hasEnchantments()) {
            Map<Enchantment, Integer> equippedEnchants = EnchantmentHelper.get(itemStack);
            if (equippedEnchants.containsKey(Enchantments.SHARPNESS)) {
                int level = equippedEnchants.get(Enchantments.SHARPNESS);
                return 0.5f * level + 0.5f;
            }
        }
        return 0;
    }
}